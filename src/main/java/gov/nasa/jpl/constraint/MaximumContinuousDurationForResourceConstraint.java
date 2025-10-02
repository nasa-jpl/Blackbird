package gov.nasa.jpl.constraint;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.Map;

public class MaximumContinuousDurationForResourceConstraint extends ResourceConstraint {
    private Duration maximumDuration;

    public MaximumContinuousDurationForResourceConstraint(Condition condition, Duration duration, String message, ViolationSeverity severity) {
        super(condition, null, message, severity);
        this.maximumDuration = duration;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        primaryCondition.update((Resource) evt.getSource(), ((Comparable) ((Map.Entry) evt.getNewValue()).getValue()));
        if (primaryCondition.isTrue()) {
            if (mostRecentTimeViolationBegan == null) {
                mostRecentTimeViolationBegan = ModelingEngine.getEngine().getCurrentTime();
            }
        }
        else {
            endViolation();
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // we could have a violation that started but wasn't added as ending to the list
        endViolation();
    }

    private void endViolation() {
        if (mostRecentTimeViolationBegan != null) {
            Time endTime = ModelingEngine.getEngine().getCurrentTime();
            Duration observedDuration = endTime.subtract(mostRecentTimeViolationBegan);
            if (observedDuration.greaterThan(maximumDuration)) {
                addViolation(mostRecentTimeViolationBegan, endTime);
            }
            // set slate clean for next violation
            mostRecentTimeViolationBegan = null;
        }
    }
}
