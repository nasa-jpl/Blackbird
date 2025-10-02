package gov.nasa.jpl.sequencing;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Time;

/**
 * The sequence engine is MUCH simpler than the modeling engine
 * because we only need to execute each sequence section in order
 * by start time of each activity.
 */
public class SequenceEngine {
    // implementing the singleton design pattern for now,
    // for the same reason as ModelingEngine
    private static SequenceEngine instance = null;

    protected SequenceEngine() {
        // Exists only to defeat instantiation.
    }

    public static SequenceEngine getEngine() {
        if (instance == null) {
            instance = new SequenceEngine();
        }
        return instance;
    }

    /**
     * Clears all stored sequences and executes each sequence section
     * of all activities.
     */
    public void sequence() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.prepareActivitiesForSequencing();
        SequenceMap.getSequenceMap().resetSequences();

        // loop through actList and execute each sequence section
        for(int i=0; i < actList.length(); i++) {
            actList.get(i).sequence();
        }
    }
}
