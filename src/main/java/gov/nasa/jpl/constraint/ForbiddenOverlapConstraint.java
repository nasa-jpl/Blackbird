package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.List;

public class ForbiddenOverlapConstraint extends ActivityConstraint {
    public ForbiddenOverlapConstraint(String activityTypeOne, String activityTypeTwo, String message, ViolationSeverity severity) {
        super(activityTypeOne, activityTypeTwo, message, severity);
    }

    public ForbiddenOverlapConstraint(List<String> activityTypeOne, String activityTypeTwo, String message, ViolationSeverity severity) {
        super(activityTypeOne, activityTypeTwo, message, severity);
    }

    // we're listening to activity instances start by type
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        seeIfAnyLiveActivitiesHaveEnded(currentTime, Duration.ZERO_DURATION);
        Activity actStarting = (Activity) evt.getNewValue();
        if (activityTypeOne.contains(evt.getPropertyName())) {
            activityOneLiveInstances.add(actStarting);
            for (Activity otherAct : activityTwoLiveInstances) {
                addViolation(currentTime, Time.min(actStarting.getEnd(), otherAct.getEnd()));
            }
        }
        else {
            activityTwoLiveInstances.add(actStarting);
            for (Activity otherAct : activityOneLiveInstances) {
                addViolation(currentTime, Time.min(actStarting.getEnd(), otherAct.getEnd()));
            }
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // nothing to do at the end of modeling, all overlaps will have been noted
    }
}
