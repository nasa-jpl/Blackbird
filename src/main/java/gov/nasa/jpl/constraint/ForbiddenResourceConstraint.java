package gov.nasa.jpl.constraint;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;

import java.beans.PropertyChangeEvent;
import java.util.Map;

public class ForbiddenResourceConstraint extends ResourceConstraint {
    public ForbiddenResourceConstraint(Condition condition, String message, ViolationSeverity severity) {
        super(condition, null, message, severity);
    }

    // we're listening to Resource values change here
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
                addViolation(mostRecentTimeViolationBegan, ModelingEngine.getEngine().getCurrentTime());
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
