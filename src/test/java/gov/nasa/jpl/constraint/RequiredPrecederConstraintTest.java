package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.*;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RequiredPrecederConstraintTest extends BaseTest {

    @Test
    public void propertyChange() {
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:05"), new Duration("00:00:05"));
        SignalSendingActivity red = new SignalSendingActivity(new Time("2000-001T00:00:00"), new Duration("00:00:03"));
        SignalSendingActivity wayBefore = new SignalSendingActivity(new Time("1990-001T00:00:00"), new Duration("00:00:03"));

        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));
        SignalSendingActivity orange = new SignalSendingActivity(new Time("2000-001T00:00:15"), new Duration("00:00:10"));

        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:40"), new Duration("00:00:07"));

        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:01:00"), new Duration("00:00:07"));
        SignalSendingActivity cyan = new SignalSendingActivity(new Time("2000-001T00:01:10"), new Duration("00:00:02"));

        ActivityOne rainbow = new ActivityOne(new Time("2000-001T00:02:10"), new Duration("00:00:07"));
        SignalSendingActivity greyscale = new SignalSendingActivity(new Time("2000-001T00:02:00"), new Duration("00:00:10"));

        Constraint requiredPreceder = new RequiredPrecederConstraint("ActivityOne", "SignalSendingActivity", new Duration("00:00:10"), "requiredPreceder", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(3, requiredPreceder.listOfViolationBeginAndEndTimes.size());
        assertEquals(new Time("2000-001T00:00:20"), requiredPreceder.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:27"), requiredPreceder.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:00:40"), requiredPreceder.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:00:47"), requiredPreceder.listOfViolationBeginAndEndTimes.get(1).getValue());
        assertEquals(new Time("2000-001T00:01:00"), requiredPreceder.listOfViolationBeginAndEndTimes.get(2).getKey());
        assertEquals(new Time("2000-001T00:01:07"), requiredPreceder.listOfViolationBeginAndEndTimes.get(2).getValue());
    }

    @Test
    public void dispatchOnCondition() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));

        ConstraintScheduleTestAct green = new ConstraintScheduleTestAct(new Time("2000-001T00:00:05"));

        List<String> actParams = Collections.emptyList();
        ActivityThree blue = new ActivityThree(new Time("2000-001T00:01:15"), new Duration("00:00:10"), actParams);

        // change the ConstraintScheduleTestRes resource so that ActivityOne is placed
        // whenever ConstraintScheduleTestRes is set to ready
        Res.ConstraintScheduleTestRes.set("notready");
        myEngine.setTime(new Time("2000-001T00:00:50"));
        Res.ConstraintScheduleTestRes.set("ready");
        myEngine.setTime(new Time("2000-001T00:01:00"));
        Res.ConstraintScheduleTestRes.set("notready");
        myEngine.setTime(new Time("2000-001T00:01:35"));
        Res.ConstraintScheduleTestRes.set("ready");

        Constraint requiredPreceder = new RequiredPrecederConstraint("ActivityOne", "ActivityThree", new Duration("00:00:10"), "requiredPreceder", ViolationSeverity.ERROR);

        ModelingEngine.getEngine().model();

        assertEquals(new Time("2000-001T00:00:50"), requiredPreceder.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:00:55"), requiredPreceder.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(1, requiredPreceder.listOfViolationBeginAndEndTimes.size());
    }
}