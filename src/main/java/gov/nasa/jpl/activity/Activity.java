package gov.nasa.jpl.activity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.function.*;

import gov.nasa.jpl.engine.*;
import gov.nasa.jpl.output.tol.TOLActivityBegin;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.scheduler.Scheduler;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.time.Duration;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * The base Activity class that Blackbird is based on. This holds on to a thread so that when an instance
 * of a subclass gets its model() method called, the instance can manipulate simulated time via that thread.
 * Activities support arbitrarily many and deep child activity instances. When activities are created they
 * automatically register themselves with the engine's ActivityInstanceList.
 */
public class Activity implements Comparable<Activity>, PropertyChangeListener {
    private Time start;
    private Duration duration;
    private String name;
    private String group = "generic";
    private String notes = "";
    private Activity parent;
    private List<Activity> children;
    private WaitProvider threadRunningMe;
    private UUID id;

    private Condition condition;

    // these are only used for printing out to reports or displays and should never be used for any modeling
    // to use other args besides time for modeling, set them in the subclass Activity
    private Object[] parameterObjects;

    /**
     * Superconstructor for all Activity types - must be passed all arguments that child class is
     * @param t The time the activity instance should be started
     * @param varargs all other parameters to the child class's constructor
     */
    public Activity(Time t, Object... varargs) {
        // Make sure we can reflect and create this activity.
        // This will throw an exception for anonymous classes.
        String simpleName = ReflectionUtilities.getClassNameWithoutPackage(this.getClass().getName());
        if (!simpleName.equals("Activity")) {
            ActivityTypeList.getActivityList().getActivityClass(simpleName);
        }
        // check for null start time
        if(t == null){
            StringBuilder parameterString = new StringBuilder();
            new TOLActivityBegin(this).writeParametersToJSON(parameterString, varargs, true);
            throw new AdaptationException("Cannot pass null start time into activity instance. Type: " + getType() + "\n" + "Other parameters for reference:\n" + parameterString.toString());
        }

        ActivityInstanceList.getActivityList().add(this);
        start = t;
        duration = new Duration("00:00:01");

        parent = null;
        children = new ArrayList<>();

        // because condition is attached to the activity type, not instance, we can build it here
        condition = setCondition();

        // the default is that an Activity's name gets set to its type, but this can be overridden in subclass constructor
        name = getType();

        // we store this so activities can be edited later, then work when those edits are undone
        storeParameterValues(varargs);

        // if we're reading in information from a file, we get both our IDs and children from the file. otherwise we generate them here
        if (!ModelingEngine.getEngine().isCurrentlyReadingInFile()) {
            assignID();
            // if we're a scheduler, we should automatically set up to be scheduled to eliminate SCHEDULE call
            schedule();
        }
    }

    /**
     * Method called after Activity constructor where one can create child activities according to whatever
     * algorithm and combination of parameters is desired. Blank in the base class but expected to be extended
     * in the subclass an adapter writes if they want to create child activities in that manner.
     */
    public void decompose() {
        // adaptation fills this in
    }

    /**
     * Method called by simulation engine when it reaches the Activity instance's stated start time, which is usually triggered
     * by a REMODEL command being issued. Blank in the base class but expected to be filled out by an adapter if they want to
     * have this instance change resource timelines.
     * @return null or a Waiter object with a function to be executed in the future.
     * @throws InterruptedException because we can call waitForSignal and have to be interrupted, modeling can
     * (but doesn't have to) throw this exception
     */
    public Waiter modelFunc() throws InterruptedException {
        model();
        return null;
    }
    /**
     * Provided for backwards compatibility, this function is called by modelFunc() if modelFunc() has not been
     * specialized.
     * @throws InterruptedException because we can call waitForSignal and have to be interrupted, modeling can
     * (but doesn't have to) throw this exception
     */
    public void model() throws InterruptedException {
        // adaptation fills this in
    }

    /**
     * Generate sequence fragments that will be added to Sequence objects and eventually written out to files.
     * This is the interface by which command expansion occurs in Blackbird. Blank in the base class but all types
     * that want to write to sequences should fill this out.
     */
    public void sequence() {
        // adaptation fills this in
    }

    /**
     *  Main method that adapters can override in order to set condition that will trigger dispatchOnCondition() to run
     *  Needs to be overwritten by things that want to be scheduled by get_windows or dispatchOnCondition - template method design pattern
     */
    public Condition setCondition() {
        return null;
    }

    /**
     * called at the beginning of a modeling run for schedulers by the engine
     */
    public void setInitialCondition() {
        if (this instanceof Scheduler && condition != null) {
            try{
                condition.setEvaluatedTo(new Time());
            }
            catch(ClassCastException e){
                throw new RuntimeException("Probable type mismatch between resource and threshold value for condition attached to activity " +
                        "instance of type " + getType() + " starting at " + getStart().toString() + ". Full error message:\n" + e.getMessage());
            }
        }
    }

    /**
     * Call when instantiating a new activity during decomposition.
     * This method adds the new activity to the children array of the parent activity.
     *
     * @param act
     */
    public void spawn(Activity act) {
        act.setParent(this);
        children.add(act);
        if (ModelingEngine.getEngine().isModeling()) {
            // in addition to adding it to the static list, if we're decomposing while modeling (scheduling), we need to add to the thread list
            ModelingEngine.getEngine().insertActivityIntoEngine(act);
        }
        if (!ModelingEngine.getEngine().isCurrentlyReadingInFile()) {
            act.decompose();
        }
    }

    /**
     * Calls spawn() then sets the notes field of the supplied activity to the supplied string
     * @param act
     * @param notes
     */
    public void spawnWithNotes(Activity act, String notes){
        spawn(act);
        act.setNotes(notes);
    }

    /**
     * Returns the parent Activity instance of the calling object. All instances have at most one parent.
     * @return Activity object, can be null
     */
    public Activity getParent() {
        return parent;
    }

    /**
     * Sets the calling object's parent to the parameter object. Should only be called by Blackbird core, not by adapters
     * @param act Activity object
     */
    public void setParent(Activity act) {
        parent = act;
    }

    /**
     * Encapsulating assigning the ID so if we want to change the schema, it is easy
     */
    private synchronized void assignID() {
        id = UUID.randomUUID();
    }

    /**
     * This method recursively deletes all children of the activity.
     */
    public void deleteChildren() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).deleteChildren();
            ActivityInstanceList.getActivityList().remove(children.get(i));
        }
        children = new ArrayList<>();
    }

    /**
     * Recursively remove all children from the instance list, but do not clear
     * the children arrays.
     */
    public void removeChildrenFromInstanceList() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).removeChildrenFromInstanceList();
            ActivityInstanceList.getActivityList().remove(children.get(i));
        }
    }

    /**
     * Recursively add all children to the instance list.
     */
    public void addChildrenToInstanceList() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).addChildrenToInstanceList();
            ActivityInstanceList.getActivityList().add(children.get(i));
        }
    }

    /**
     * Not recursive - just child list of this activity, and doesn't remove anything from the instance list
     */
    public void clearChildrenArrayWithoutDeletingThem() {
        children = new ArrayList();
    }

    /**
     * Returns a list of children activities.
     */
    public List<Activity> getChildren() {
        return children;
    }

    /**
     * Assigns the children list to the given parameter - only used for redecompose command
     */
    public void setChildren(List<Activity> list) {
        children = list;
    }

    /**
     * @return boolean capturing whether the activity is the top of the decomposition tree
     */
    public boolean isTopLevelActivity() {
        return parent == null;
    }

    /**
     * Main method adapters have of changing resource timelines at different times during the execution of an activity
     * Without this, all resource changes will happen at the modeled start time of an activity instance
     * @param t Time that the engine should wait until in simulated time before re-waking up the activity and resuming its model() section
     */
    public void waitUntil(Time t) {
        if(threadRunningMe != null) {
            threadRunningMe.waitUntil(t);
        }
        else{
            throw new AdaptationException("Error in waitUntil for activity of type " + getType() + ".\n" +
                    "waitUntil() was called on thread that no longer exists. Most likely, a dispatchOnCondition() method " +
                    "tried to call waitUntil(), which is not allowed. To create events in the future in dispatchOnCondition()," +
                    " spawn new activities at a later start date.");
        }
    }

    /**
     *  waitFor is just a wrapper for waitUntil
     * @param d Duration that the engine should wait for in simulated time before re-waking up the activity and resuming its model() section
     */
    public void waitFor(Duration d) {
        if(threadRunningMe == null){
            throw new AdaptationException("Error in waitFor for activity of type " + getType() + ".\n" +
                    "waitFor() was called on thread that no longer exists. Most likely, a dispatchOnCondition() method " +
                    "tried to call waitFor(), which is not allowed. To create events in the future in dispatchOnCondition()," +
                    " spawn new activities at a later start date.");
        }
        else if (d.greaterThanOrEqualTo(Duration.ZERO_DURATION)) {
            threadRunningMe.waitUntil(threadRunningMe.getCurrentTime().add(d));
        }
        else {
            throw new AdaptationException("Error in waitFor for activity of type " + getType() + ".\nInput " +
                    "duration of " + d.toString() + " is negative, which is not allowed.");
        }
    }

    /**
     * A call to this method blocks until another activity throws a Signal of the same String signalName that this
     * activity instance is waiting on
     * @param signalName
     * @return Map that contains the contents of the signal
     * @throws InterruptedException
     */
    public Map waitForSignal(String signalName) throws InterruptedException {
        if(threadRunningMe != null) {
            return threadRunningMe.waitForSignal(signalName);
        }
        else{
            throw new AdaptationException("Error in waitForSignal for activity of type " + getType() + ".\n" +
                    "waitForSignal() was called on thread that no longer exists. Most likely, a dispatchOnCondition() method " +
                    "tried to call waitForSignal(), which is not allowed. To create events in the future in dispatchOnCondition()," +
                    " spawn new activities at a later start date.");
        }
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute at a time in the future.
     * @param t time in future to execute the given function
     * @param func the given function
     * @return Waiter object
     */
    public Waiter waitUntil(Time t, Supplier<Waiter> func) {
        if(threadRunningMe != null) {
            return threadRunningMe.waitUntil(t, func);
        }
        else{
            throw new AdaptationException("Error in waitUntil for activity of type " + getType() + ".\n" +
                    "waitUntil() was called on thread that no longer exists. Most likely, a dispatchOnCondition() method " +
                    "tried to call waitUntil(), which is not allowed. To create events in the future in dispatchOnCondition()," +
                    " spawn new activities at a later start date.");
        }
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute at a time in the future.
     * @param t time in future to execute the given function
     * @param func the given function
     * @return Waiter object
     */
    public Waiter waitUntil(Time t, Runnable func) {
        return waitUntil(t, () -> {func.run(); return null;});
    }

    /**
     * Executes the given function starting at time startTime up to but not including time endTime
     * with each successive time incremented by timeStep. At time endTime the function endFunc is executed.
     * @param startTime the start time of the loop.
     * @param endTime the end time of the loop.
     * @param timeStep step duration between times.
     * @param func function executed at each time step in [startTime,endTime)
     * @param endFunc function executed at endTime which can return a Waiter to be executed in the future.
     * @return a Waiter which implements the loop.
     */
    public Waiter waitLoop(Time startTime, Time endTime, Duration timeStep, BiConsumer<Time,Time> func, Function<Time,Waiter> endFunc) {
        if (startTime.greaterThan(endTime)) {
            throw new AdaptationException(String.format("Start time (%s) cannot be greater than end time (%s)",
                    startTime, endTime));
        }
        if (timeStep.lessThanOrEqualTo(Duration.ZERO_DURATION)) {
            throw new AdaptationException(String.format("Time step duration (%s) cannot be less than or equal to zero.",
                    timeStep));
        }
        return waitLoopImpl(startTime, endTime, timeStep, func, endFunc);
    }

    // Private method that implements the loop without time checks. Times have already been checked.
    private Waiter waitLoopImpl(Time startTime, Time endTime, Duration timeStep, BiConsumer<Time,Time> func, Function<Time,Waiter> endFunc) {
        return startTime.lessThan(endTime)
            ? waitUntil(startTime, () -> {
                func.accept(startTime, endTime);
                return waitLoopImpl(startTime.add(timeStep), endTime, timeStep, func, endFunc);})
            : waitUntil(endTime, () -> endFunc.apply(endTime));
    }

    /**
     * Executes the given function starting at time startTime up to but not including time endTime
     * with each successive time incremented by timeStep. At time endTime the function endFunc is executed.
     * @param startTime the start time of the loop.
     * @param endTime the end time of the loop.
     * @param timeStep step duration between times.
     * @param func function executed at each time step in [startTime,endTime)
     * @param endFunc function executed at endTime.
     * @return a Waiter which implements the loop.
     */
    public Waiter waitLoop(Time startTime, Time endTime, Duration timeStep, BiConsumer<Time,Time> func, Consumer<Time> endFunc) {
        return waitLoop(startTime, endTime, timeStep, func, (et) -> {endFunc.accept(et); return null;});
    }

    /**
     * Executes the given function starting at time startTime up to but not including time endTime
     * with each successive time incremented by timeStep. At time endTime the function endFunc is executed.
     * @param startTime the start time of the loop.
     * @param endTime the end time of the loop.
     * @param timeStep step duration between times.
     * @param func function executed at each time step in [startTime,endTime)
     * @param endFunc function executed at endTime.
     * @return a Waiter which implements the loop.
     */
    public Waiter waitLoop(Time startTime, Time endTime, Duration timeStep, Consumer<Time> func, Consumer<Time> endFunc) {
        return waitLoop(startTime, endTime, timeStep, (t,et) -> func.accept(t), (et) -> {endFunc.accept(et); return null;});
    }

    /**
     * Executes the given function starting at time startTime up to and including time endTime
     * with each successive time incremented by timeStep.
     * @param startTime the start time of the loop.
     * @param endTime the end time of the loop.
     * @param timeStep step duration between times.
     * @param func function executed at each time step in [startTime,endTime]
     * @return a Waiter which implements the loop.
     */
    public Waiter waitLoop(Time startTime, Time endTime, Duration timeStep, Consumer<Time> func) {
        return waitLoop(startTime, endTime, timeStep, (t,et) -> func.accept(t), (et) -> {func.accept(et); return null;});
    }

    /**
     * Executes the given function starting at time startTime up to and including time endTime
     * with each successive time incremented by timeStep.
     * @param startTime the start time of the loop.
     * @param endTime the end time of the loop.
     * @param timeStep step duration between times.
     * @param func function executed at each time step in [startTime,endTime]
     * @return a Waiter which implements the loop.
     */
    public Waiter waitLoop(Time startTime, Time endTime, Duration timeStep, BiConsumer<Time,Time> func) {
        return waitLoop(startTime, endTime, timeStep, func, (et) -> {func.accept(et,et); return null;});
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute
     * after waiting for the given amount of time.
     * @param d duration to wait before executing the given function.
     * @param func the given function
     * @return Waiter object
     */
    public Waiter waitFor(Duration d, Supplier<Waiter> func) {
        return waitUntil(now().add(d), func);
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute
     * after waiting for the given amount of time.
     * @param d duration to wait before executing the given function.
     * @param func the given function
     * @return Waiter object
     */
    public Waiter waitFor(Duration d, Runnable func) {
        return waitUntil(now().add(d), func);
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute
     * after the given signal is raised.
     * @param signalName signal name to wait to be raised.
     * @param func the given function to run when the signal is raised.
     * @return Waiter object
     */
    public Waiter waitForSignal(String signalName, Consumer<Map> func) {
        return waitForSignal(signalName, (m) -> {func.accept(m); return null;});
    }

    /**
     * Returns a Waiter object that can be returned to the modeling engine to execute
     * after the given signal is raised.
     * @param signalName signal name to wait to be raised.
     * @param func the given function to run when the signal is raised.
     * @return Waiter object
     */
    public Waiter waitForSignal(String signalName, Function<Map,Waiter> func) {
        if(threadRunningMe != null) {
            return threadRunningMe.waitForSignal(signalName, func);
        }
        else{
            throw new AdaptationException("Error in waitForSignal for activity of type " + getType() + ".\n" +
                    "waitForSignal() was called on thread that no longer exists. Most likely, a dispatchOnCondition() method " +
                    "tried to call waitForSignal(), which is not allowed. To create events in the future in dispatchOnCondition()," +
                    " spawn new activities at a later start date.");
        }
    }

    /**
     *  Should not be called by adapters, only through Command interface
     *  Doesn't actually begin on-line scheduling, just sets it up so it will begin when the next remodel occurs
     */
    public void schedule() {
        if (this instanceof Scheduler && condition != null) {
            if(ModelingEngine.getEngine().isModeling()){
                throw new AdaptationException("Cannot spawn scheduler from another scheduler - place all schedulers in the timeline before starting a REMODEL. Since schedulers only key off conditions becoming true, there is no need to only create one during a model pass");
            }
            // now we need the condition to notify us of when things change
            for (Resource r : condition.getAllResourcesRecursively()) {
                r.addChangeListener(this);
            }
            // we need to delete our children so the scheduler doesn't duplicate them - this could be done right before modeling but that would add complexity
            deleteChildren();
        }
    }

    /**
     * Should not be called by adapters, only by engine.
     * Once a scheduler has run in one remodel, we typically don't want it to run a second time and this achieves that.
     */
    public void stopScheduling() {
        if (this instanceof Scheduler && condition != null) {
            // the condition should stop notifying us when things change
            for (Resource r : condition.getAllResourcesRecursively()) {
                r.removeChangeListener(this);
            }
        }
    }


    // setters and getters for private variables

    /**
     * Updates the duration of the activity, which also updates the end time.
     * Cannot be negative.
     * @param newDuration
     */
    public void setDuration(Duration newDuration) {
        if (newDuration.lessThan(Duration.ZERO_DURATION)) {
            throw new RuntimeException("Error instantiating activity " + getType() + " at time " + getStart().toString()
                    + ". Input duration value " + newDuration.toString() + " is negative. Activity " +
                    "durations are not allowed to have negative values.");
        }
        duration = newDuration;
    }

    /**
     * This method updates the duration of the activity to give the desired end time.
     * The start time of the activity is kept constant. This method is safe to use during
     * modeling.
     * @param newEnd
     */
    public void setEndByChangingDuration(Time newEnd) {
        setDuration(newEnd.subtract(start));
    }

    /**
     * Sets the name parameter of the activity. This determines how the activity
     * name is displayed in the output products or GUIs.
     * @param newName
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Returns the start time of this activity instance
     * @return
     */
    public Time getStart() {
        return start;
    }

    /**
     * Returns the end time of this activity instance. This is computed by taking the start time
     * from getStart() and adding the duration returned from getDuration(). It does not have anything
     * to do with when the last modeled time that the model() section of the instance sees.
     * @return
     */
    public Time getEnd() {
        return start.add(duration);
    }

    /**
     * Returns a Window with (getStart(), getEnd())
     * @return
     */
    public Window getWindow(){
        return new Window(getStart(), getEnd());
    }

    /**
     * Returns the duration field of the activity. This is set by setDuration() and does not necessarily have
     * anything to do with how long the model() section of the instance takes to execute in simulated time.
     * @return
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns the name attribute of the activity. Can be set with setName()
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return The 'group' of the activity, which can be used to assign to GUI legends, waterfall groups, or filters
     */
    public String getGroup(){
        return group;
    }

    /**
     * Sets the 'group' of the activity, which can be used to assign to GUI legends, waterfall groups, or filters
     * @param group
     */
    public void setGroup(String group){
        this.group = group;
    }

    /**
     * @return The 'Notes' attached to the activity, which can be used to explain why a human or scheduler placed an activity where it did
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the 'Notes' attached to the activity, which can be used to explain why a human or scheduler placed an activity where it did
     * @param notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Returns the current modeling time. Convenience method for adapters.
     * @return
     */
    public static Time now() {
        return ModelingEngine.getEngine().getCurrentTime();
    }

    /**
     * Returns the name of the activity type, which is based on the class name.
     * @return
     */
    public String getType() {
        // the indexOf call is to strip the package from the beginning of it
        return ReflectionUtilities.getClassNameWithoutPackage(this.getClass().getName());
    }

    /**
     * Natural comparator of Activity instances is by start time
     * @param act2
     * @return
     */
    public int compareTo(Activity act2) {
        return start.compareTo(act2.start);
    }

    /**
     * Only should be used by engine, not adapters. Attaches WaitProvider to Activity instance for later processing.
     * @param t
     */
    public void setThread(WaitProvider t) {
        threadRunningMe = t;
    }

    /**
     * We do this so there will be no live references to a thread after it runs, halving memory usage
     */
    public void detachThread() {
        threadRunningMe = null;
    }

    /**
     * This method is used to return the parameters to an activity
     * as objects, so that they can be used to instantiate a new activity or write to an output file.
     *
     * @return
     */
    public Object[] getParameterObjects() {
        return parameterObjects;
    }

    /**
     * Updates the ID of an activity.
     *
     * @param newID
     */
    public void setID(UUID newID) {
        id = newID;
    }

    /**
     * Returns ID associated with instance
     * @return
     */
    public UUID getID() {
        return id;
    }

    /**
     * Returns ID of activity as string, not UUID object
     * @return
     */
    public String getIDString() {
        return id.toString();
    }

    /**
     * Returns Condition associated with this Activity type
     * @param subclass
     * @return
     */
    public Condition getCondition(Class subclass) {
        //we need a truly empty instance without making adapters make a nullary constructor, so we'll use objenesis
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator thingyInstantiator = objenesis.getInstantiatorOf(subclass);
        Activity instanceOfSubClass = (Activity) thingyInstantiator.newInstance();
        return instanceOfSubClass.setCondition();
    }

    private void storeParameterValues(Object[] varargs) {
        parameterObjects = new Object[varargs.length];
        for (int i = 0; i < varargs.length; i++) {
            parameterObjects[i] = varargs[i];
        }
    }

    /**
     *  This method will be called by resources changing - if the condition becomes true we want to call dispatchOnCondition()
     *  Should not be called manually by adapters.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        condition.update((Resource) evt.getSource(), ((Comparable) ((Map.Entry) evt.getNewValue()).getValue()));
        if (condition.isTrue()) {
            ((Scheduler) this).dispatchOnCondition();
        }
    }
}
