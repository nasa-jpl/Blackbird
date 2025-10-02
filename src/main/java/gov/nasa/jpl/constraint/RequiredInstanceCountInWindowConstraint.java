package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.constraint.ActivityConstraint;
import gov.nasa.jpl.constraint.ViolationSeverity;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.scheduler.CompareToValues;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;

public class RequiredInstanceCountInWindowConstraint extends ActivityConstraint {
    private Window window;
    private CompareToValues comparison;
    private int threshold;

    public RequiredInstanceCountInWindowConstraint(String activityType, Window window, CompareToValues comparison, int threshold, String message, ViolationSeverity severity) {
        this(Arrays.asList(activityType), window, comparison, threshold, message, severity);
    }

    public RequiredInstanceCountInWindowConstraint(List<String> activityType, Window window, CompareToValues comparison, int threshold, String message, ViolationSeverity severity){
        super(activityType, null, message, severity);
        this.window = window;
        this.comparison = comparison;
        this.threshold = threshold;
        if(window.getStart() == null || window.getEnd() == null){
            throw new AdaptationException("Cannot pass a RequiredInstanceCountInWindowConstraint a window with null entries. Constraint has message: " + message);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();

        if(activityTypeOne.contains(evt.getPropertyName()) && window.contains(currentTime)){
            Activity actStarting = (Activity) evt.getNewValue();
            activityOneLiveInstances.add(actStarting);
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        if(Math.signum(Integer.compare(activityOneLiveInstances.size(), threshold)) != comparison.toInt()){
            addViolation(window.getStart(), window.getEnd());
        }
    }
}
