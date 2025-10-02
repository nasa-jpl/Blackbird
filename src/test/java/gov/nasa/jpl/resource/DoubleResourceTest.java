package gov.nasa.jpl.resource;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleResourceTest extends BaseTest {

    @Test
    public void interpval() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        myEngine.setTime(new Time("2000-001T00:01:00"));
        ResourceA.set(10.0);
        ResourceB.set(-20.0);
        myEngine.setTime(new Time("2000-001T00:01:40"));
        ResourceA.set(20.0);
        ResourceB.set(20.0);
        myEngine.setTime(new Time("2000-001T00:02:00"));
        ResourceA.set(5.0);

        assertEquals(0.0, ResourceA.interpval(new Time("2000-001T00:00:01")), 0.000001);
        assertEquals(15.0, ResourceA.interpval(new Time("2000-001T00:01:20")), 0.000001);
        assertEquals(20.0, ResourceA.interpval(new Time("2000-001T00:01:40")), 0.000001);
        assertEquals(10.25, ResourceA.interpval(new Time("2000-001T00:01:01")), 0.000001);
        assertEquals(8.75, ResourceA.interpval(new Time("2000-001T00:01:55")), 0.000001);
        assertEquals(5.0, ResourceA.interpval(new Time("2000-001T00:02:01")), 0.000001);

        assertEquals(0.0, ResourceB.interpval(new Time("2000-001T00:01:20")), 0.000001);
        assertEquals(-19.0, ResourceB.interpval(new Time("2000-001T00:01:01")), 0.000001);

    }
}