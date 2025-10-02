package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityThree;
import gov.nasa.jpl.exampleAdaptation.SignalSendingActivity;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ForbiddenOverlapConstraintTest extends BaseTest {

    @Test
    public void propertyChange() {
        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:10"), new Duration("00:00:01"));
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:06"), new Duration("00:00:01"));
        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:00:16"), new Duration("00:00:01"));
        SignalSendingActivity red = new SignalSendingActivity(new Time("2000-001T00:00:05"), new Duration("00:00:10"));

        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));
        SignalSendingActivity orange = new SignalSendingActivity(new Time("2000-001T00:00:23"), new Duration("00:00:10"));
        ActivityThree pink = new ActivityThree(new Time("2000-001T00:00:20"), new Duration("00:00:07"), null);

        Constraint forbiddenOverlap = new ForbiddenOverlapConstraint("ActivityOne", "SignalSendingActivity", "forbiddenOverlap", ViolationSeverity.ERROR);
        Constraint forbiddenMultiOverlap = new ForbiddenOverlapConstraint(Arrays.asList("ActivityOne", "ActivityThree"), "SignalSendingActivity", "forbiddenOverlap", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(new Time("2000-001T00:00:06"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:07"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:00:10"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:00:11"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(1).getValue());
        assertEquals(new Time("2000-001T00:00:23"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(2).getKey());
        assertEquals(new Time("2000-001T00:00:27"), forbiddenOverlap.listOfViolationBeginAndEndTimes.get(2).getValue());

        assertEquals(4, forbiddenMultiOverlap.listOfViolationBeginAndEndTimes.size());
    }
}