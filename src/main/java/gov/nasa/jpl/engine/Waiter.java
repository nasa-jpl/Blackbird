package gov.nasa.jpl.engine;

import gov.nasa.jpl.time.Time;

import java.util.function.Supplier;

/**
 * A Waiter is an object stored by the engine that tells it to run a certain function (Activity model())
 * at a certain time. Supports 'pausing' and 'resuming' thread by returning new object holding a subset of
 * original model function
 */
public class Waiter implements Comparable<Waiter> {
    // The time at which this waiter should run
    private final Time runTime;

    // used to prioritize edge cases
    private final int priority;
    private final Supplier<Waiter> func;

    // used to know when another worker threads was resumed by this Waiter
    private boolean resumed = false;

    /**
     * Constructs a waiter to execute at the given Time with the given priority.
     * @param t The Time this Waiter should execute.
     * @param priority Used to further prioritize Waiters that execute at the same
     *                 time but under different contextual meanings. By convention
     *                 the modeling engine uses the following values:
     *                 priority = -1 -> signaled activities (immediately)
     *                 priority = 0 -> resumed activities (before activity end)
     *                 priority = 1 -> new activities (at activity start)
     */
    public Waiter(Time t, int priority, Supplier<Waiter> func) {
        this.runTime = t;
        this.priority = priority;
        this.func = func;
    }

    /**
     * @return The Time which this Waiter will execute
     */
    public Time getTime() {
        return runTime;
    }

    /**
     * Sorts waiters by runtime and then priority.
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Waiter o) {
        int ret = this.runTime.compareTo(o.runTime);
        return (ret == 0) ? this.priority - o.priority : ret;
    }

    /**
     * This method is used by the modeling engine to execute the embedded function
     * @return another Waiter or null
     */
    Waiter execute() {
        return func.get();
    }

    /**
     * This method is used by the modeling engine to inform if it should stop
     * the current worker thread if another thread was resumed by the execute()
     * method.
     * @return true if this Waiter successfully resumed another thread
     */
    boolean resumedAThread() {return resumed;}

    /**
     * This method is called by the modeling engine when it creates a waiter
     * which will resume another worker thread.
     * @param willResume set to true if this waiter will resume a worker thread.
     */
    void willResumeThread(boolean willResume) {resumed = willResume;}
}