package gov.nasa.jpl.constraint;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.Map;

/*
 * This type of rule says that “if condition 1 occurs, condition 2 must have been true continuously for a certain
 * amount of time before that and also for the entire period of time during which condition 1 is true”
 */
public class PrerequisiteConditionConstraint extends ResourceConstraint {
    Duration prerequisiteDuration;
    // this is null if the prerequisite condition is not true
    Time mostRecentTimePrerequisiteConditionStartedBeingTrue;

    public PrerequisiteConditionConstraint(Condition conditionActual, Condition prerequisiteCondition, Duration duration, String message, ViolationSeverity severity) {
        super(conditionActual, prerequisiteCondition, message, severity);
        this.prerequisiteDuration = duration;
        this.mostRecentTimePrerequisiteConditionStartedBeingTrue = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // we can't tell from evt, whose source is a resource, which Condition was triggered
        if (primaryCondition.getAllResourcesRecursively().contains(evt.getSource())) {
            primaryCondition.update((Resource) evt.getSource(), ((Comparable) ((Map.Entry) evt.getNewValue()).getValue()));
        }
        if (secondaryCondition.getAllResourcesRecursively().contains(evt.getSource())) {
            secondaryCondition.update((Resource) evt.getSource(), ((Comparable) ((Map.Entry) evt.getNewValue()).getValue()));
        }

        if (secondaryCondition.isTrue()) {
            if (mostRecentTimePrerequisiteConditionStartedBeingTrue == null) {
                mostRecentTimePrerequisiteConditionStartedBeingTrue = ModelingEngine.getEngine().getCurrentTime();
            }
        }
        else {
            mostRecentTimePrerequisiteConditionStartedBeingTrue = null;
            // if the prerequisite stops being true during the main condition, the violation is thrown
            if (primaryCondition.isTrue() && mostRecentTimeViolationBegan == null) {
                mostRecentTimeViolationBegan = ModelingEngine.getEngine().getCurrentTime();
            }
        }

        if (primaryCondition.isTrue()) {
            if (mostRecentTimePrerequisiteConditionStartedBeingTrue == null || ModelingEngine.getEngine().getCurrentTime().subtract(mostRecentTimePrerequisiteConditionStartedBeingTrue).lessThan(prerequisiteDuration)) {
                mostRecentTimeViolationBegan = ModelingEngine.getEngine().getCurrentTime();
            }
        }
        else {
            if (mostRecentTimeViolationBegan != null) {
                Time endTime = ModelingEngine.getEngine().getCurrentTime();
                addViolation(mostRecentTimeViolationBegan, endTime);
                // set slate clean for next violation
                mostRecentTimeViolationBegan = null;
            }
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // we could have a violation that started but wasn't added as ending to the list
        if (mostRecentTimeViolationBegan != null) {
            addViolation(mostRecentTimeViolationBegan, ModelingEngine.getEngine().getCurrentTime());
        }
    }
}
