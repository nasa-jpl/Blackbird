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

import static org.junit.Assert.*;

public class RequiredContainmentConstraintTest extends BaseTest {

    @Test
    public void propertyChange() {
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:14"), new Duration("00:00:05"));
        SignalSendingActivity red = new SignalSendingActivity(new Time("2000-001T00:00:05"), new Duration("00:00:10"));

        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));
        SignalSendingActivity orange = new SignalSendingActivity(new Time("2000-001T00:00:23"), new Duration("00:00:10"));

        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:40"), new Duration("00:00:07"));

        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:01:00"), new Duration("00:00:07"));
        SignalSendingActivity cyan = new SignalSendingActivity(new Time("2000-001T00:01:02"), new Duration("00:00:01"));

        ActivityThree pink = new ActivityThree(new Time("2000-001T01:00:00"), Duration.MINUTE_DURATION, null);

        Constraint requiredContainment = new RequiredContainmentConstraint("ActivityOne", "SignalSendingActivity", "requiredContainment", ViolationSeverity.ERROR);
        Constraint requiredMultiContainment = new RequiredContainmentConstraint(Arrays.asList("ActivityOne", "ActivityThree"), "SignalSendingActivity", "requiredContainment", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(3, requiredContainment.listOfViolationBeginAndEndTimes.size());
        assertEquals(new Time("2000-001T00:00:14"), requiredContainment.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:19"), requiredContainment.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:00:20"), requiredContainment.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:00:27"), requiredContainment.listOfViolationBeginAndEndTimes.get(1).getValue());
        assertEquals(new Time("2000-001T00:00:40"), requiredContainment.listOfViolationBeginAndEndTimes.get(2).getKey());
        assertEquals(new Time("2000-001T00:00:47"), requiredContainment.listOfViolationBeginAndEndTimes.get(2).getValue());

        assertEquals(4, requiredMultiContainment.listOfViolationBeginAndEndTimes.size());
    }
}