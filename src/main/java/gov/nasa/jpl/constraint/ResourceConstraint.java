package gov.nasa.jpl.constraint;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

// the constraint will listen to resources change and throw flags if they violate the passive constraint
public abstract class ResourceConstraint extends Constraint {
    // in addition to fields provided by Constraint, ResourceConstraint holds one or two Conditions
    protected Condition primaryCondition;
    protected Condition secondaryCondition;

    protected ResourceConstraint(Condition primaryCondition, Condition secondaryCondition, String message, ViolationSeverity severity) {
        super(message, severity);
        this.primaryCondition = primaryCondition;
        this.secondaryCondition = secondaryCondition;
        hookToListeners();
    }

    @Override
    public void clearViolationHistory() {
        mostRecentTimeViolationBegan = null;
        if (primaryCondition != null) primaryCondition.setEvaluatedTo(new Time());
        if (secondaryCondition != null) secondaryCondition.setEvaluatedTo(new Time());
        listOfViolationBeginAndEndTimes = new ArrayList<>();
    }

    @Override
    void hookToListeners(){
        if (primaryCondition != null) {
            for (Resource r : primaryCondition.getAllResourcesRecursively()) {
                r.addChangeListener(this);
            }
        }
        if (secondaryCondition != null) {
            for (Resource r : secondaryCondition.getAllResourcesRecursively()) {
                r.addChangeListener(this);
            }
        }
    }

    @Override
    void unhookFromListeners(){
        if (primaryCondition != null) {
            for (Resource r : primaryCondition.getAllResourcesRecursively()) {
                r.removeChangeListener(this);
            }
        }
        if (secondaryCondition != null) {
            for (Resource r : secondaryCondition.getAllResourcesRecursively()) {
                r.removeChangeListener(this);
            }
        }
    }
}
