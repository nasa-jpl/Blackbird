package gov.nasa.jpl.engine;

import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.resource.ResourceList;

public abstract class ModelingEngine {

    private Time modelingTime;

    // we use this to support the method isModeling(), which determines whether signals continue processing and whether activities immediately get threads or wait to acquire them
    private boolean modeling;

    // we only turn this on while reading in a file where decomposition has already happened
    private boolean currentlyReadingInFile = false;

    // we're implementing the singleton design pattern for now, since we only need one engine and this is a clean way of having a global instance
    private static ModelingEngine instance = null;

    protected ModelingEngine() {
        // Exists only to defeat instantiation.
        modelingTime = new Time();
        modeling = false;
    }

    /**
     * Gets singleton modeling engine. In the future if there are multiple engines this may be removed.
     * @return
     */
    public static ModelingEngine getEngine() {
        if (instance == null) {
            setEngine(new FunctionalWaitModelingEngine());
        }
        return instance;
    }

    protected static void setEngine(ModelingEngine engine) {
        if (instance != null)
            throw new RuntimeException("Modeling engine already set!");
        instance = engine;
    }

    /**
     * The main method that REMODEL eventually calls. This loops through all activity instances registered
     * to the engine and invokes in time-order their ActivityThread s, which calls the model() method
     * defined in each activity.
     */
    public void model() {
        // tell all activities that we're modeling
        modeling = true;

        // prepare for modeling by resetting all constraint violation histories - must be done before activities or
        // resources are added or changed in order to avoid bug where they update using old information
        ConstraintInstanceList.getConstraintList().resetAllConstraints();

        // insert all activity instances into threads, which are indexed by start time for engine to begin processing
        ActivityInstanceList.getActivityList().prepareActivitiesForModeling();

        runModeling();

        // any activities still waiting on signals will never receive them, so we clear references to them
        Signal.clearAllSignals();

        // any constraints that still have unfinished violations are told to add them to their violation list
        ConstraintInstanceList.getConstraintList().finalizeAllConstraints();

        // now we unschedule any activities that were turned on so next time we run remodel they don't duplicate
        ActivityInstanceList.getActivityList().stopAllSchedulers();

        // tell all activities that we're done modeling
        modeling = false;
    }

    // called to setup the initial time
    protected void initTime(Time currentModelingTime) {
        setTime(currentModelingTime);
        ResourceList.getResourceList().resetResourceHistories();
        ResourceList.getResourceList().makeAllResourcesUseTheirProfileAtInitialTime();
    }

    /**
     * Getter for current modeling time
     * @return
     */
    public Time getCurrentTime() {
        return modelingTime;
    }

    /**
     * Setter for current modeling time. Should only be used by engine internally and publicly for testing.
     * @param t
     */
    public void setTime(Time t) {
        modelingTime = t;
    }

    /**
     * Returns whether or not the engine is currently inside the modeling loop. Inserting activities
     * behaves differently if this is true or not, so this check is useful for that.
     * @return
     */
    public boolean isModeling() {
        return modeling;
    }

    /**
     * TEST ONLY METHOD for resetting engine if a previous test left it mid-model
     */
    public void resetEngine(){
        modeling = false;
    }

    /**
     * Returns whether or not the engine is currently reading in a file. Activity decomposition
     * behaves differently if the instances come from TOLs or not, so this is needed.
     * @return
     */
    public boolean isCurrentlyReadingInFile() {
        return currentlyReadingInFile;
    }

    /**
     * Only to be set by input readers, not adapters. Changes how activity decomposition occurs.
     * @param currentlyReadingInFile
     */
    public void setCurrentlyReadingInFile(boolean currentlyReadingInFile) {
        this.currentlyReadingInFile = currentlyReadingInFile;
    }

    /**
     * Run the modeling engine after queueing up all Activity instances.
     */
    protected abstract void runModeling();

    /**
     * Inserts an Activity into the engine to run.
     * @param toInsert
     */
    public abstract void insertActivityIntoEngine(Activity toInsert);

    /**
     * Inserts a Waiter into the engine to run.
     * @param toInsert
     */
    abstract void insertWaiter(Waiter toInsert);
}
