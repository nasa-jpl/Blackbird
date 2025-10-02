package gov.nasa.jpl.constraint;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.*;

public class ForbiddenResourceConstraintTest extends BaseTest {

    @Test
    public void propertyChange() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        Condition cond = Condition.and(ResourceA.whenGreaterThan(0.0), ResourceB.whenGreaterThan(0.0));
        ForbiddenResourceConstraint forbiddenResourceConstraint = new ForbiddenResourceConstraint(cond, "forbiddenCond", ViolationSeverity.ERROR);
        ResourceA.set(98.7);
        myEngine.setTime(new Time("2000-001T00:00:50"));
        ResourceB.set(1100.0);
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        myEngine.setTime(new Time("2000-001T00:01:40"));
        ResourceB.set(-1.0);
        myEngine.setTime(new Time("2000-001T00:01:45"));
        ResourceA.set(1.5);
        ResourceB.set(0.7);
        myEngine.setTime(new Time("2000-001T00:03:20"));

        // usually model() would do this for us, but since we don't have acts in this test we call it manually
        forbiddenResourceConstraint.finalizeConstraintAfterModeling();

        assertEquals(new Time("2000-001T00:00:50"), forbiddenResourceConstraint.listOfViolationBeginAndEndTimes.get(0).getKey());
        assertEquals(new Time("2000-001T00:01:39"), forbiddenResourceConstraint.listOfViolationBeginAndEndTimes.get(0).getValue());
        assertEquals(new Time("2000-001T00:01:45"), forbiddenResourceConstraint.listOfViolationBeginAndEndTimes.get(1).getKey());
        assertEquals(new Time("2000-001T00:03:20"), forbiddenResourceConstraint.listOfViolationBeginAndEndTimes.get(1).getValue());
    }
}