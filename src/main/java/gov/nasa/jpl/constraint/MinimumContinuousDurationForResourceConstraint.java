package gov.nasa.jpl.constraint;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.Map;

public class MinimumContinuousDurationForResourceConstraint extends ResourceConstraint {
    private Duration minimumDuration;

    public MinimumContinuousDurationForResourceConstraint(Condition condition, Duration duration, String message, ViolationSeverity severity) {
        super(condition, null, message, severity);
        this.minimumDuration = duration;
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
            if (mostRecentTimeViolationBegan != null) {
                Time endTime = ModelingEngine.getEngine().getCurrentTime();
                Duration observedDuration = endTime.subtract(mostRecentTimeViolationBegan);
                if (observedDuration.lessThan(minimumDuration)) {
                    addViolation(mostRecentTimeViolationBegan, endTime);
                }
                // set slate clean for next violation
                mostRecentTimeViolationBegan = null;
            }
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // we don't do anything here because we might always get false positives here from cutting it off "too early"
    }
}
