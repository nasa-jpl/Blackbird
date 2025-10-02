package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.constraint.ForbiddenResourceConstraint;
import gov.nasa.jpl.constraint.ViolationSeverity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Time;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TOLConstraintViolationEndTest extends BaseTest {

    @Test
    public void toFlatTOL() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        Condition cond = Condition.and(ResourceA.whenGreaterThan(0.0), ResourceB.whenGreaterThan(0.0));
        ForbiddenResourceConstraint forbiddenResourceConstraint = new ForbiddenResourceConstraint(cond, "forbiddenCond message", ViolationSeverity.ERROR);
        forbiddenResourceConstraint.setName("forbiddenCond");
        ResourceA.set(98.7);
        myEngine.setTime(new Time("2000-001T00:00:50"));
        ResourceB.set(1100.0);
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        // usually model() would do this for us, but since we don't have acts in this test we call it manually
        forbiddenResourceConstraint.finalizeConstraintAfterModeling();

        Assert.assertEquals("2000-001T00:01:39.000000,RELEASE,forbiddenCond,\"END OF VIOLATION forbiddenCond message\";\n", new TOLConstraintViolationEnd(new Time("2000-001T00:01:39"), forbiddenResourceConstraint).toFlatTOL());

    }

    @Test
    public void toXML() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        Condition cond = Condition.and(ResourceA.whenGreaterThan(0.0), ResourceB.whenGreaterThan(0.0));
        ForbiddenResourceConstraint forbiddenResourceConstraint = new ForbiddenResourceConstraint(cond, "forbiddenCond message", ViolationSeverity.ERROR);
        forbiddenResourceConstraint.setName("forbiddenCond");
        ResourceA.set(98.7);
        myEngine.setTime(new Time("2000-001T00:00:50"));
        ResourceB.set(1100.0);
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        // usually model() would do this for us, but since we don't have acts in this test we call it manually
        forbiddenResourceConstraint.finalizeConstraintAfterModeling();

        String expectedOutput = "    <TOLrecord type=\"RELEASE\">\n" +
                "        <TimeStamp>2000-001T00:01:39.000000</TimeStamp>\n" +
                "        <Rule>\n" +
                "            <Name>forbiddenCond</Name>\n" +
                "        </Rule>\n" +
                "    </TOLrecord>\n";

        assertEquals(expectedOutput, new TOLConstraintViolationEnd(new Time("2000-001T00:01:39"), forbiddenResourceConstraint).toXML());
    }

    @Test
    public void toJSON() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        Condition cond = Condition.and(ResourceA.whenGreaterThan(0.0), ResourceB.whenGreaterThan(0.0));
        ForbiddenResourceConstraint forbiddenResourceConstraint = new ForbiddenResourceConstraint(cond, "forbiddenCond message", ViolationSeverity.ERROR);
        forbiddenResourceConstraint.setName("forbiddenCond");
        ResourceA.set(98.7);
        myEngine.setTime(new Time("2000-001T00:00:50"));
        ResourceB.set(1100.0);
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        // usually model() would do this for us, but since we don't have acts in this test we call it manually
        forbiddenResourceConstraint.finalizeConstraintAfterModeling();

        assertEquals("", new TOLConstraintViolationEnd(new Time("2000-001T00:01:39"), forbiddenResourceConstraint).toESJSON());

    }
}