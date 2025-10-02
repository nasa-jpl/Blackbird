package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.util.AbstractMap;
import java.util.List;

/*
 * This rule says that "before the start of every occurrence of an activity of type mainActivity must be the end of one
 * of type requiredPreceder within a certain amount of time".
 */
public class RequiredPrecederConstraint extends ActivityConstraint {
    Duration durationToPrecedeBy;

    public RequiredPrecederConstraint(String mainActivity, String requiredPreceder, Duration durationToPrecedeBy, String message, ViolationSeverity severity) {
        super(mainActivity, requiredPreceder, message, severity);
        this.durationToPrecedeBy = durationToPrecedeBy;
    }

    public RequiredPrecederConstraint(List<String> mainActivity, String requiredPreceder, Duration durationToPrecedeBy, String message, ViolationSeverity severity) {
        super(mainActivity, requiredPreceder, message, severity);
        this.durationToPrecedeBy = durationToPrecedeBy;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        Activity actStarting = (Activity) evt.getNewValue();
        // we need to save requiredPreceders past their end time for durationToPrecedeBy to fulfill the constraint
        pruneOldActivities(currentTime, durationToPrecedeBy, activityTwoLiveInstances, false);
        boolean weFoundAPreceder = false;
        if (activityTypeOne.contains(evt.getPropertyName())) {
            for (Activity precederInstances : activityTwoLiveInstances) {
                // we need to check that it actually precedes it
                if (precederInstances.getEnd().lessThanOrEqualTo(actStarting.getStart())) {
                    weFoundAPreceder = true;
                    break;
                }
            }
            if (!weFoundAPreceder) {
                listOfViolationBeginAndEndTimes.add(new AbstractMap.SimpleImmutableEntry<>(actStarting.getStart(), actStarting.getEnd()));
            }
        }
        else {
            this.activityTwoLiveInstances.add(actStarting);
        }
    }

    @Override
    public void finalizeConstraintAfterModeling() {
        // we process mainActivities as we get them, so there's nothing to do here
    }
}
