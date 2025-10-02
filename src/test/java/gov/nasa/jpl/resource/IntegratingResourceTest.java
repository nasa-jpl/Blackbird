package gov.nasa.jpl.resource;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static gov.nasa.jpl.exampleAdaptation.Res.IntegratesPosition;
import static org.junit.Assert.*;

public class IntegratingResourceTest extends BaseTest {

    @Test
    public void propertyChange() {
        DoubleResource resourceA = new DoubleResource(0.0, "subsystem1");
        IntegratingResource integratesA = new IntegratingResource(resourceA, Duration.ONE_HOUR);
        runPropertyChangeTest(resourceA, integratesA);
    }

    @Test
    public void propertyChangeDuration() {
        DoubleResource resourceA = new DoubleResource(0.0, "subsystem1");
        IntegratingResource integratesA = new IntegratingResource(resourceA, Duration.HOUR_DURATION);
        runPropertyChangeTest(resourceA, integratesA);
    }

    @Test
    public void currentvalWithUpdate() {
        DoubleResource resourceA = new DoubleResource(0.0, "subsystem1");
        IntegratingResource integratesA = new IntegratingResource(resourceA, Duration.HOUR_DURATION);

        testSetup(resourceA, integratesA);
        ModelingEngine myEngine = ModelingEngine.getEngine();

        // reverse order of sets and asserts to make sure that the update method works
        assertEquals(0.0, integratesA.currentval(), 0.0000001);
        resourceA.set(30.0);

        myEngine.setTime(new Time("2000-001T00:20:00"));
        assertEquals(10.0, integratesA.currentval(), 0.0000001);
        resourceA.set(450.0);

        myEngine.setTime(new Time("2000-001T00:40:00"));
        assertEquals(160.0, integratesA.currentval(), 0.0000001);
        resourceA.set(-15.0);

        myEngine.setTime(new Time("2000-001T02:00:00"));
        assertEquals(140.0, integratesA.currentval(), 0.0000001);
    }

    @Test
    public void interpval() {
        DoubleResource resourceA = new DoubleResource(0.0, "subsystem1");
        IntegratingResource integratesA = new IntegratingResource(resourceA, Duration.HOUR_DURATION);

        testSetup(resourceA, integratesA);
        ModelingEngine myEngine = ModelingEngine.getEngine();

        // reverse order of sets and asserts to make sure that the update method works
        resourceA.set(30.0);
        myEngine.setTime(new Time("2000-001T00:20:00"));
        resourceA.set(450.0);
        myEngine.setTime(new Time("2000-001T00:40:00"));
        resourceA.set(-15.0);
        myEngine.setTime(new Time("2000-001T02:00:00"));

        assertEquals(5.0, integratesA.interpval(new Time("2000-001T00:10:00")), 0.0000001);
        assertEquals(10.0, integratesA.interpval(new Time("2000-001T00:20:00")), 0.0000001);
        assertEquals(160.0, integratesA.interpval(new Time("2000-001T00:40:00")), 0.0000001);
        assertEquals(140.0, integratesA.interpval(new Time("2000-001T02:00:00")), 0.0000001);

        // valueAt should perform the same as long as the integrating resource is not set
        assertEquals(5.0, integratesA.valueAt(new Time("2000-001T00:10:00")), 0.0000001);
        assertEquals(10.0, integratesA.valueAt(new Time("2000-001T00:20:00")), 0.0000001);
        assertEquals(160.0, integratesA.valueAt(new Time("2000-001T00:40:00")), 0.0000001);
        assertEquals(140.0, integratesA.valueAt(new Time("2000-001T02:00:00")), 0.0000001);
    }

    @Test
    public void set() {
        DoubleResource resourceA = new DoubleResource(0.0, "subsystem1");
        IntegratingResource integratesA = new IntegratingResource(resourceA, Duration.HOUR_DURATION);

        testSetup(resourceA, integratesA);
        ModelingEngine myEngine = ModelingEngine.getEngine();

        // reverse order of sets and asserts to make sure that the update method works
        resourceA.set(30.0);
        myEngine.setTime(new Time("2000-001T00:10:00"));
        integratesA.set(100.0);
        myEngine.setTime(new Time("2000-001T00:20:00"));

        assertEquals(2.5, integratesA.valueAt(new Time("2000-001T00:05:00")), 0.0000001);
        assertEquals(100.0, integratesA.valueAt(new Time("2000-001T00:10:00")), 0.0000001);
        assertEquals(105.0, integratesA.valueAt(new Time("2000-001T00:20:00")), 0.0000001);
    }

    @Test
    public void arrayedIntegratingResource(){
        Time t = Time.getDefaultReferenceTime();
        ActivityOne actOne = new ActivityOne(t, Duration.MINUTE_DURATION);
        ActivityOne actTwo = new ActivityOne(t.add(Duration.MINUTE_DURATION), Duration.MINUTE_DURATION);
        ActivityOne actThree = new ActivityOne(t.add(Duration.MINUTE_DURATION.multiply(2)), Duration.MINUTE_DURATION);

        ModelingEngine.getEngine().model();

        assertEquals(0.0, IntegratesPosition.get("y").valueAt(t), 1E-9);
        assertEquals((0.01*1)+(0.02*1)+(0.03*1), IntegratesPosition.get("y").valueAt(t.add(new Duration("00:04:00"))), 1E-9);
    }

    private void testSetup(DoubleResource resourceA, IntegratingResource integratesA) {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
    }

    private void runPropertyChangeTest(DoubleResource resourceA, IntegratingResource integratesA) {
        testSetup(resourceA, integratesA);
        ModelingEngine myEngine = ModelingEngine.getEngine();

        resourceA.set(30.0);
        assertEquals(0.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T00:20:00"));
        resourceA.set(450.0);
        assertEquals(10.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T00:40:00"));
        resourceA.set(-15.0);
        assertEquals(160.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T02:00:00"));
        resourceA.set(0.0);
        assertEquals(140.0, integratesA.currentval(), 0.0000001);

        myEngine.setTime(new Time("2000-001T00:00:00"));
        // mimic beginning of model loop where resources are reset to profile and histories deleted
        resourceA.clearHistory();
        integratesA.clearHistory();
        resourceA.set(resourceA.profile(myEngine.getCurrentTime()));

        resourceA.set(30.0);
        assertEquals(0.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T00:20:00"));
        resourceA.set(90.0);
        assertEquals(10.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T00:40:00"));
        resourceA.set(-15.0);
        assertEquals(40.0, integratesA.currentval(), 0.0000001);
        myEngine.setTime(new Time("2000-001T02:00:00"));
        resourceA.set(0.0);
        assertEquals(20.0, integratesA.currentval(), 0.0000001);
    }
}