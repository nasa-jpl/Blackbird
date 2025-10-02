package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/*
 * This rule states that “within a certain amount of time after the end of every occurrence of an activity of type
 * mainActivity must be the start of one of type requiredFollower.”
 */
public class RequiredFollowerConstraint extends ActivityConstraint {
    Duration durationFollowerHasToStartBy;

    public RequiredFollowerConstraint(String mainActivity, String requiredFollower, Duration durationFollowerHasToStartBy, String message, ViolationSeverity severity) {
        super(mainActivity, requiredFollower, message, severity);
        this.durationFollowerHasToStartBy = durationFollowerHasToStartBy;
    }

    public RequiredFollowerConstraint(List<String> mainActivity, String requiredFollower, Duration durationFollowerHasToStartBy, String message, ViolationSeverity severity) {
        super(mainActivity, requiredFollower, message, severity);
        this.durationFollowerHasToStartBy = durationFollowerHasToStartBy;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        // we only need to delete activityOneLiveInstances because activityTwoLiveInstances isn't accumulated in this constraint
        pruneOldActivities(currentTime, durationFollowerHasToStartBy, activityOneLiveInstances, true);
        Activity actStarting = (Activity) evt.getNewValue();
        if (activityTypeOne.contains(evt.getPropertyName())) {
            // this list is special in this subclass because we're keeping track only of unfulfilled instances, not just live ones
            activityOneLiveInstances.add(actStarting);
        }
        else {
            List<Activity> finished = new ArrayList<>();
            for (Activity mainActivityInstance : activityOneLiveInstances) {
                if (actStarting.getStart().greaterThanOrEqualTo(mainActivityInstance.getEnd())) {
                    // this means the constraint was satisfied for the specified mainActivityInstance
                    finished.add(mainActivityInstance);
                }
            }
            activityOneLiveInstances.removeAll(finished);
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        //any activities left alive at the end of modeling did not have activities following them, so they are in violation
        for (Activity mainActivity : activityOneLiveInstances) {
            addViolation(mainActivity.getStart(), mainActivity.getEnd());
        }
    }
}
