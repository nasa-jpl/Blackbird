package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/*
 * This rule specifies that every occurrence of an activity of type outerType must contain one of type mustContain, i. e.,
 * for each occurrence of outerType, there must be a mustContain that starts later and ends sooner than outerType
 */
public class RequiredContainmentConstraint extends ActivityConstraint {
    public RequiredContainmentConstraint(String outerType, String mustContain, String message, ViolationSeverity severity) {
        super(outerType, mustContain, message, severity);
    }

    public RequiredContainmentConstraint(List<String> outerType, String mustContain, String message, ViolationSeverity severity) {
        super(outerType, mustContain, message, severity);
    }

    // we're listening to activity instances start by type
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        // we only need to delete activityOneLiveInstances because activityTwoLiveInstances isn't accumulated in this constraint
        pruneOldActivities(currentTime, Duration.ZERO_DURATION, activityOneLiveInstances, true);
        Activity actStarting = (Activity) evt.getNewValue();
        if (activityTypeOne.contains(evt.getPropertyName())) {
            // this list is special in this subclass because we're keeping track only of unfulfilled instances, not just live ones
            activityOneLiveInstances.add(actStarting);
        }
        else {
            // if an outerType instance gets deleted here, that's because we found a mustContain and don't need to keep track of that outerType anymore
            List<Activity> finished = new ArrayList<>();
            for (Activity outerTypeInstance : activityOneLiveInstances) {
                if (actStarting.getStart().greaterThan(outerTypeInstance.getStart()) && actStarting.getEnd().lessThan(outerTypeInstance.getEnd())) {
                    // this means the constraint was satisfied for the specified outerTypeInstance
                    finished.add(outerTypeInstance);
                }
            }
            activityOneLiveInstances.removeAll(finished);
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        //any activities left alive at the end of modeling did not have activities contained in them, so they are in violation
        for (Activity outerTypeInstance : activityOneLiveInstances) {
            addViolation(outerTypeInstance.getStart(), outerTypeInstance.getEnd());
        }
    }
}
