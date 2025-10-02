package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.List;

/*
 * This rule specifies that every occurrence of an activity of type innerType must be contained by one of type requiredContainer, i. e.,
 * for each occurrence of innerType, there must be a requiredContainer that starts earlier and ends sooner than innerType
 */
public class RequiredContainerConstraint extends ActivityConstraint {
    public RequiredContainerConstraint(String innerType, String requiredContainer, String message, ViolationSeverity severity) {
        super(innerType, requiredContainer, message, severity);
    }

    public RequiredContainerConstraint(List<String> innerType, String requiredContainer, String message, ViolationSeverity severity) {
        super(innerType, requiredContainer, message, severity);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        seeIfAnyLiveActivitiesHaveEnded(currentTime, Duration.ZERO_DURATION);
        Activity actStarting = (Activity) evt.getNewValue();
        boolean wasThereAContainerAct = false;
        if (activityTypeOne.contains(evt.getPropertyName())) {
            for (Activity otherAct : activityTwoLiveInstances) {
                if (actStarting.getStart().greaterThan(otherAct.getStart()) && actStarting.getEnd().lessThan(otherAct.getEnd())) {
                    wasThereAContainerAct = true;
                    break;
                }
            }
            if (!wasThereAContainerAct) {
                addViolation(currentTime, actStarting.getEnd());
            }
        }
        else {
            activityTwoLiveInstances.add(actStarting);
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // no violations are possible after all activities have started
    }
}
