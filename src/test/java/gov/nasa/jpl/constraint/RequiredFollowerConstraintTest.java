package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.SignalSendingActivity;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequiredFollowerConstraintTest extends BaseTest {

    @Test
    public void propertyChange() {
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:05"), new Duration("00:00:05"));
        SignalSendingActivity red = new SignalSendingActivity(new Time("2000-001T00:00:15"), new Duration("00:00:10"));

        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));
        SignalSendingActivity orange = new SignalSendingActivity(new Time("2000-001T00:00:27"), new Duration("00:00:10"));

        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:40"), new Duration("00:00:07"));

        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:01:00"), new Duration("00:00:07"));
        SignalSendingActivity cyan = new SignalSendingActivity(new Time("2000-001T00:00:58"), new Duration("00:00:05"));

        Constraint requiredFollower = new RequiredFollowerConstraint("ActivityOne", "SignalSendingActivity", new Duration("00:00:10"), "requiredFollower", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(2, requiredFollower.listOfViolationBeginAndEndTimes.size());
        assertEquals(new Time("2000-001T00:00:40"), requiredFollower.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:47"), requiredFollower.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:01:00"), requiredFollower.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:01:07"), requiredFollower.listOfViolationBeginAndEndTimes.get(1).getValue());
    }
}