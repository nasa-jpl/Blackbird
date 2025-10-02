package gov.nasa.jpl.engine;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.activity.WaitProvider;
import gov.nasa.jpl.time.Time;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of ModelingEngine that uses ArrayBlockingQueue objects for
 * synchronization between threads. Schedules Waiter objects to run at a given
 * time. Waiter objects basically contain information to run a function at a
 * given time. These are used to schedule Activity modeling, resuming threads,
 * or Activity functions which need to be run upon a signal or time.
 */
public class FunctionalWaitModelingEngine extends ModelingEngine implements WaitProvider {

    // Result signals sent from worker thread to the main modeling thread
    private enum Result {
        Waiting, // signal to the engine that worker thread is going into a wait state
        Error,   // signal to the engine that an error has occurred and modeling needs to stop
        Done     // signal to the engine the modelling has completed successfully
    }

    private final PriorityQueue<Waiter> waiters = new PriorityQueue<>();
    private final BlockingQueue<Result> workerToMain = new ArrayBlockingQueue<>(1);
    private Exception thrownException = null;
    private boolean inmodel = false;

    FunctionalWaitModelingEngine()
    {
    }

    /**
     * Method which runs the modeling. All Activity objects are scheduled to run.
     */
    @Override
    protected void runModeling() {
        waiters.clear();
        ActivityInstanceList activities = ActivityInstanceList.getActivityList();
        for (int i = 0; i < activities.length(); i++) {
            insertActivityIntoEngine(activities.get(i));
        }

        // Initial time is set to the first activity (waiter) in queue
        Time currentModelingTime = waiters.isEmpty() ? null : waiters.peek().getTime();
        if (currentModelingTime == null) {
            currentModelingTime = new Time();
        }

        // This call clears and then sets initial profile for all resources
        initTime(currentModelingTime);

        thrownException = null;

        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            inmodel = true;
            threadPool.submit(this::runModelingThread);

            Result res = Result.Error; // anything but Done so we can enter the loop below
            while (res != Result.Done) {

                // wait until we get a result from the worker thread
                res = workerToMain.take();

                // the worker thread caught a thrown error, so we need to re-throw
                // from this main thread
                if (res == Result.Error) {
                    throw new RuntimeException(thrownException);
                }

                // the worker thread is going into wait state, so we need to start
                // another worker thread
                if (res == Result.Waiting) {
                    threadPool.submit(this::runModelingThread);
                }
            }
        }
        catch (InterruptedException ex) {
            thrownException = ex;
        }
        finally {
            threadPool.shutdown();
            inmodel = false;
        }
    }

    /**
     * Function run by each worker thread. The worker thread continues to run until one of the following things happen:
     * 1) Modeling is done
     * 2) An Error happens.
     * 3) It enters a wait state by the adaptation calling one of the explicit wait functions.
     * (waitUntil, waitFor, waitForSignal)
     * 4) Or until it resumes another worker thread that was in a wait state.
     */
    private void runModelingThread() {
        try {
            try {
                while (!waiters.isEmpty()) {
                    Waiter next = waiters.remove();
                    setTime(next.getTime());

                    Waiter future = next.execute();
                    if (next.resumedAThread()) {
                        // If the waiter we just executed resumed
                        // another worker thread, then we need to
                        // end processing immediately.
                        return;
                    }

                    if (future != null) {
                        insertWaiter(future);
                    }
                }
                workerToMain.put(Result.Done);
            }
            catch (RuntimeException ex) {
                thrownException = ex;
                workerToMain.put(Result.Error);
            }
        }
        catch (InterruptedException ex) {
            // we got interrupted. ignore and exit
        }
    }

    /**
     * Inserts a Waiter into the modeling queue.
     * @param toInsert the Waiter to be inserted.
     */
    @Override
    void insertWaiter(Waiter toInsert) {
        if (inmodel && toInsert.getTime().lessThan(getCurrentTime())) {
            throw new RuntimeException(String.format(
                    "Current time is [%s]. Cannot schedule an event at [%s] because it is in the past.",
                    getCurrentTime().toString(), toInsert.getTime().toString()));
        }
        waiters.add(toInsert);
    }

    /**
     * Inserts an Activity into the modeling queue.
     * @param activity the activity to be inserted.
     */
    @Override
    public void insertActivityIntoEngine(Activity activity) {
        // set the WaitProvider for the Activity
        activity.setThread(this);
        // queue a waiter to run at Activity start time
        insertWaiter(new Waiter(activity.getStart(), 1, () -> {
            ActivityTypeList.getActivityList().propertyChangeForActivityType(activity.getType(), activity);
            try {
                return activity.modelFunc();
            }
            catch (InterruptedException ex) {
                return null;
            }
        }));
    }

    /**
     * Schedules a Waiter to resume this thread at time t.
     * @param t the time to resume execution.
     */
    @Override
    public void waitUntil(Time t) {
        try {
            BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
            // queue a waiter to resume this thread at a future time
            Waiter waiter = new Waiter(t, 0, () -> {
                try {
                    // we can put() any non-null object
                    queue.put(this);
                    return null;
                }
                catch (InterruptedException ex) {
                    return null;
                }
            });
            waiter.willResumeThread(true);
            insertWaiter(waiter);
            // tell the main thread that this thread is going into wait state
            workerToMain.put(Result.Waiting);
            // wait on the queue until an object is put into it
            // this is how the thread waits
            queue.take();
        }
        catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Schedules a waiter to resume this thread upon the given signal
     * @param signalName name of signal to wait
     * @return data sent when the signal was raised
     * @throws InterruptedException
     */
    @Override
    public Map waitForSignal(String signalName) throws InterruptedException {
        BlockingQueue<Map> queue = new ArrayBlockingQueue<>(1);
        // give the signal a handler to queue up when the signal is raised
        Signal.getSignal(signalName).addSignalHandler(true, (m) -> {
            try {
                // we can't put() a null object
                queue.put(m == null ? new HashMap() : m);
                return null;
            } catch (InterruptedException ex) {
                return null;
        }});
        // tell the main thread that this thread is going into wait state
        workerToMain.put(Result.Waiting);
        // now we wait until the signal is raised and the data map is
        // put into the queue
        return queue.take();
    }
    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute at a time in the future.
     * @param t time in future to execute the given function
     * @param func the given function
     * @return Waiter object
     */
    @Override
    public Waiter waitUntil(Time t, Supplier<Waiter> func) {
        return new Waiter(t, 0, func);
    }

    @Override
    public Waiter waitForSignal(String signalName, Function<Map, Waiter> func) {
        Signal.getSignal(signalName).addSignalHandler(false, func);
        return null;
    }
}
