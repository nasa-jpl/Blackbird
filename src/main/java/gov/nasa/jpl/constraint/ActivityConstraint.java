package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.*;

public abstract class ActivityConstraint extends Constraint {
    // in addition to fields provided by Constraint, ActivityConstraint holds one or two activity types
    HashSet<String> activityTypeOne;
    String activityTypeTwo;

    // we keep track of these here because all constraints use basically the same paradigm
    ArrayList<Activity> activityOneLiveInstances;
    ArrayList<Activity> activityTwoLiveInstances;

    public ActivityConstraint(String activityTypeOne, String activityTypeTwo, String message, ViolationSeverity severity) {
        this(Arrays.asList(activityTypeOne), activityTypeTwo, message, severity);
    }

    public ActivityConstraint(List<String> activityTypeOne, String activityTypeTwo, String message, ViolationSeverity severity){
        super(message, severity);
        this.activityTypeOne = new HashSet<>(activityTypeOne);
        this.activityTypeTwo = activityTypeTwo;
        this.activityOneLiveInstances = new ArrayList<>();
        this.activityTwoLiveInstances = new ArrayList<>();
        hookToListeners();
    }

    protected void seeIfAnyLiveActivitiesHaveEnded(Time currentTime, Duration durationToWaitBeforeDeleting) {
        pruneOldActivities(currentTime, durationToWaitBeforeDeleting, activityOneLiveInstances, false);
        pruneOldActivities(currentTime, durationToWaitBeforeDeleting, activityTwoLiveInstances, false);
    }

    protected void pruneOldActivities(Time currentTime, Duration durationToWaitBeforeDeleting, List<Activity> actList, boolean writeViolationIfDeleted) {
        List<Activity> finished = new ArrayList();
        for (Activity activityInstance : actList) {
            if (activityInstance.getEnd().add(durationToWaitBeforeDeleting).lessThan(currentTime)) {
                if (writeViolationIfDeleted) {
                    listOfViolationBeginAndEndTimes.add(new AbstractMap.SimpleImmutableEntry(activityInstance.getStart(), activityInstance.getEnd()));
                }
                finished.add(activityInstance);
            }
        }
        actList.removeAll(finished);
    }

    @Override
    public void clearViolationHistory() {
        mostRecentTimeViolationBegan = null;
        activityOneLiveInstances = new ArrayList<>();
        activityTwoLiveInstances = new ArrayList<>();
        listOfViolationBeginAndEndTimes = new ArrayList<>();
    }

    @Override
    void hookToListeners(){
        for(String type : activityTypeOne) {
            ActivityTypeList.getActivityList().addToObserverList(type, this);
        }
        if (activityTypeTwo != null) {
            ActivityTypeList.getActivityList().addToObserverList(activityTypeTwo, this);
        }
    }

    @Override
    void unhookFromListeners(){
        for(String type : activityTypeOne) {
            ActivityTypeList.getActivityList().removeFromObserverList(type, this);
        }
        if (activityTypeTwo != null) {
            ActivityTypeList.getActivityList().removeFromObserverList(activityTypeTwo, this);
        }
    }
}
