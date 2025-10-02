package gov.nasa.jpl.constraint;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.StringResource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrerequisiteConditionConstraintTest extends BaseTest {
    @Test
    public void propertyChange() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        StringResource ResourceA = new StringResource("", "subsystem1");
        StringResource ResourceB = new StringResource("", "subsystem1");

        Condition cond1 = ResourceA.whenEqualTo("ready");
        Condition cond2 = ResourceB.whenEqualTo("ready");

        Constraint constraint = new PrerequisiteConditionConstraint(cond1, cond2, new Duration("00:00:10"), "violatedPrerequisite", ViolationSeverity.WARNING);

        ResourceA.set("notready");
        ResourceB.set("notready");
        myEngine.setTime(new Time("2000-001T00:00:50"));
        ResourceA.set("ready");
        myEngine.setTime(new Time("2000-001T00:01:00"));
        ResourceA.set("notready");
        myEngine.setTime(new Time("2000-001T00:01:10"));
        ResourceB.set("ready");
        myEngine.setTime(new Time("2000-001T00:01:25"));
        ResourceA.set("ready");
        myEngine.setTime(new Time("2000-001T00:01:30"));
        ResourceA.set("notready");
        ResourceB.set("notready");
        myEngine.setTime(new Time("2000-001T00:01:35"));
        ResourceA.set("ready");
        ResourceB.set("ready");
        myEngine.setTime(new Time("2000-001T00:02:10"));
        ResourceA.set("notready");
        ResourceB.set("notready");
        myEngine.setTime(new Time("2000-001T00:02:30"));
        ResourceB.set("ready");
        myEngine.setTime(new Time("2000-001T00:02:50"));
        ResourceA.set("ready");
        myEngine.setTime(new Time("2000-001T00:03:10"));
        ResourceB.set("notready");
        myEngine.setTime(new Time("2000-001T00:03:20"));
        ResourceA.set("notready");

        // usually model() would do this for us, but since we don't have acts in this test we call it manually
        constraint.finalizeConstraintAfterModeling();

        assertEquals(new Time("2000-001T00:00:50"), constraint.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:01:00"), constraint.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:01:35"), constraint.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:02:10"), constraint.listOfViolationBeginAndEndTimes.get(1).getValue());
        assertEquals(new Time("2000-001T00:03:10"), constraint.listOfViolationBeginAndEndTimes.get(2).getKey());
        assertEquals(new Time("2000-001T00:03:20"), constraint.listOfViolationBeginAndEndTimes.get(2).getValue());
    }
}