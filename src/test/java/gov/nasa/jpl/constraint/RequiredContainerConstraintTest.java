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

public class RequiredContainerConstraintTest extends BaseTest {


    @Test
    public void propertyChange() {
        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:10"), new Duration("00:00:01"));
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:14"), new Duration("00:00:05"));
        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:00:16"), new Duration("00:00:01"));
        SignalSendingActivity red = new SignalSendingActivity(new Time("2000-001T00:00:05"), new Duration("00:00:10"));

        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));
        SignalSendingActivity orange = new SignalSendingActivity(new Time("2000-001T00:00:23"), new Duration("00:00:10"));

        ActivityThree pink = new ActivityThree(new Time("2000-001T00:00:20"), new Duration("00:00:07"), null);

        Constraint requiredContainer      = new RequiredContainerConstraint("ActivityOne", "SignalSendingActivity", "requiredContainer", ViolationSeverity.ERROR);
        Constraint requiredMultiContainer = new RequiredContainerConstraint(Arrays.asList("ActivityOne", "ActivityThree"), "SignalSendingActivity", "requiredContainer", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(3, requiredContainer.listOfViolationBeginAndEndTimes.size());
        assertEquals(new Time("2000-001T00:00:14"), requiredContainer.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:19"), requiredContainer.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:00:16"), requiredContainer.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:00:17"), requiredContainer.listOfViolationBeginAndEndTimes.get(1).getValue());
        assertEquals(new Time("2000-001T00:00:20"), requiredContainer.listOfViolationBeginAndEndTimes.get(2).getKey());
        assertEquals(new Time("2000-001T00:00:27"), requiredContainer.listOfViolationBeginAndEndTimes.get(2).getValue());

        assertEquals(4, requiredMultiContainer.listOfViolationBeginAndEndTimes.size());
    }
}