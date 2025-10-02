package gov.nasa.jpl.resource;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.*;
import java.util.stream.StreamSupport;

import static gov.nasa.jpl.exampleAdaptation.Res.vectorComponents;
import static gov.nasa.jpl.time.Duration.*;
import static org.junit.Assert.*;

public class ResourceTest extends BaseTest {

    @Test
    public void set() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        ResourceA.set(98.7);
        assertEquals(new Double(98.7), ResourceA.valueAt(new Time("2000-001T00:00:00")));
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        assertEquals(new Double(98.7), ResourceA.valueAt(new Time("2000-001T00:00:00")));
        assertEquals(new Double(-115.0), ResourceA.valueAt(new Time("2000-001T00:01:39")));
        assertEquals(new Double(-115.0), ResourceA.valueAt(new Time("2000-001T00:01:40")));
    }

    @Test
    public void currentval() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        ResourceA.set(98.7);
        assertEquals(ResourceA.valueAt(new Time("2000-001T00:00:00")), ResourceA.currentval());
    }

    @Test
    public void valueAt() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        assertEquals(ResourceA.valueAt(new Time("2000-001T00:00:00")), ResourceA.profile(new Time("2000-001T00:00:00")));
        ResourceA.set(98.7);
        assertEquals(new Double(98.7), ResourceA.valueAt(new Time("2000-001T00:00:00")));
    }

    @Test
    public void firstTimeSet() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        ResourceA.set(98.7);
        assertEquals(new Time("2000-001T00:00:00"), ResourceA.firstTimeSet());
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        assertEquals(new Time("2000-001T00:00:00"), ResourceA.firstTimeSet());
    }

    @Test
    public void lastTimeSet() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        ResourceA.set(98.7);
        assertEquals(new Time("2000-001T00:00:00"), ResourceA.lastTimeSet());
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        assertEquals(new Time("2000-001T00:01:39"), ResourceA.lastTimeSet());
    }

    @Test
    public void nextPriorTimeSet(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(t);
        IntegerResource ResourceA = new IntegerResource(0, "subsystem1", "");

        ResourceA.set(98);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(300)));
        ResourceA.set(99);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(777)));
        ResourceA.set(1337);

        assertEquals(null, ResourceA.priorTimeSet(t, false));
        assertEquals(null, ResourceA.priorTimeSet(t.subtract(SECOND_DURATION), true));
        assertEquals(null, ResourceA.nextTimeSet(t.add(SECOND_DURATION.multiply(777)), false));
        assertEquals(null, ResourceA.nextTimeSet(t.add(SECOND_DURATION.multiply(778)), true));

        assertEquals(t, ResourceA.priorTimeSet(t, true));
        assertEquals(t.add(SECOND_DURATION.multiply(777)), ResourceA.nextTimeSet(t.add(SECOND_DURATION.multiply(777)), true));

        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.priorTimeSet(t.add(SECOND_DURATION.multiply(220)), true));
        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.priorTimeSet(t.add(SECOND_DURATION.multiply(220)), false));

        assertEquals(t.add(SECOND_DURATION.multiply(240)), ResourceA.nextTimeSet(t.add(SECOND_DURATION.multiply(220)), true));
        assertEquals(t.add(SECOND_DURATION.multiply(240)), ResourceA.nextTimeSet(t.add(SECOND_DURATION.multiply(220)), false));
    }

    @Test
    public void lastValue() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(new Time("2000-001T00:00:00"));
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        ResourceA.set(98.7);
        assertEquals((Double) 98.7, ResourceA.lastValue());
        myEngine.setTime(new Time("2000-001T00:01:39"));
        ResourceA.set(-115.0);
        assertEquals(Double.valueOf(-115.0), ResourceA.lastValue());
    }

    @Test
    public void testMinMax(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(t);
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");

        Map.Entry<Time, Double> profile = new AbstractMap.SimpleImmutableEntry<>(t, ResourceA.profile(t));
        assertEquals(profile, ResourceA.max(null, null));
        assertEquals(profile, ResourceA.max(t, t.add(MINUTE_DURATION)));
        assertEquals(profile, ResourceA.min(null, null));
        assertEquals(profile, ResourceA.min(t, t.add(MINUTE_DURATION)));

        ResourceA.set(98.7);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300.0);

        assertEquals((Double) 300.0, ResourceA.max(null, null).getValue());
        assertEquals(t.add(SECOND_DURATION.multiply(240)), ResourceA.max(null, null).getKey());
        assertEquals((Double) 98.7, ResourceA.max(t, t.add(MINUTE_DURATION)).getValue());
        assertEquals((Double) 200.0, ResourceA.max(t, t.add(SECOND_DURATION.multiply(220))).getValue());
        assertEquals((Double) 300.0, ResourceA.max(t, t.add(SECOND_DURATION.multiply(240))).getValue());
        assertEquals((Double) 98.7, ResourceA.max(t.add(SECOND_DURATION), t.add(MINUTE_DURATION)).getValue());
        assertEquals((Double) 200.0, ResourceA.max(t.add(SECOND_DURATION.multiply(220)), t.add(SECOND_DURATION.multiply(230))).getValue());
        assertEquals(t.add(SECOND_DURATION.multiply(220)), ResourceA.max(t.add(SECOND_DURATION.multiply(220)), t.add(SECOND_DURATION.multiply(230))).getKey());
        assertEquals(Double.valueOf(98.7), ResourceA.max(t.subtract(MINUTE_DURATION), t.add(SECOND_DURATION)).getValue());
        assertEquals(t, ResourceA.max(t.subtract(MINUTE_DURATION), t.add(SECOND_DURATION)).getKey());

        assertEquals(Double.valueOf(-115.0), ResourceA.min(null, null).getValue());
        assertEquals(Double.valueOf(98.7), ResourceA.min(t, t.add(MINUTE_DURATION)).getValue());
        assertEquals(Double.valueOf(-115.0), ResourceA.min(t.add(SECOND_DURATION.multiply(50)), t.add(SECOND_DURATION.multiply(300))).getValue());
        assertEquals(Double.valueOf(200.0), ResourceA.min(t.add(SECOND_DURATION.multiply(200)), t.add(SECOND_DURATION.multiply(300))).getValue());
        assertEquals(Double.valueOf(-115.0), ResourceA.min(t.add(SECOND_DURATION.multiply(100)), t.add(SECOND_DURATION.multiply(240))).getValue());
        assertEquals(Double.valueOf(-115.0), ResourceA.min(t.subtract(MINUTE_DURATION), t.add(HOUR_DURATION)).getValue());
        assertEquals(Double.valueOf(0.0), ResourceA.min(t.subtract(MINUTE_DURATION), t.subtract(SECOND_DURATION)).getValue());
        assertEquals(t.subtract(MINUTE_DURATION), ResourceA.min(t.subtract(MINUTE_DURATION), t.subtract(SECOND_DURATION)).getKey());

        StringResource lexicographic = new StringResource();
        myEngine.setTime(t);
        lexicographic.set("a");
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        lexicographic.set("A");
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        lexicographic.set("hello_there");
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        lexicographic.set("?");
        myEngine.setTime(t);

        assertEquals("hello_there", lexicographic.max(null, null).getValue());
        assertEquals("hello_there", lexicographic.max(null, lexicographic.lastTimeSet()).getValue());
        assertEquals("", lexicographic.min(null, null).getValue());
        assertEquals(t, lexicographic.min(null, null).getKey());
        assertEquals("?", lexicographic.min(t, null).getValue());
        assertEquals(t.add(SECOND_DURATION.multiply(240)), lexicographic.min(t, null).getKey());
        assertEquals("A", lexicographic.min(t.add(SECOND_DURATION.multiply(100)), t.add(SECOND_DURATION.multiply(101))).getValue());
    }

    @Test
    public void testGetSize(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        assertEquals(0, ResourceA.getSize());

        ResourceA.set(98.7);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300.0);

        assertEquals(4, ResourceA.getSize());
        ResourceA.clearHistory();
        assertEquals(0, ResourceA.getSize());
    }

    @Test
    public void testIterator(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(t);
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");

        ResourceA.set(98.7);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300.0);

        Iterator<Map.Entry<Time, Double>> iter0 = ResourceA.historyIterator(null, null);
        assertEquals(4, StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter0, Spliterator.ORDERED), false).count());
        // this one is '1' because we want it to find the entry at the end of the history as the value that will persist
        Iterator<Map.Entry<Time, Double>> iter1 = ResourceA.historyIterator(t.add(HOUR_DURATION), t.add(DAY_DURATION));
        assertEquals(1, StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter1, Spliterator.ORDERED), false).count());

        Iterator<Map.Entry<Time, Double>> iter2 = ResourceA.historyIterator(t.add(SECOND_DURATION.multiply(150)), t.add(SECOND_DURATION.multiply(199)));
        assertEquals(2, StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter2, Spliterator.ORDERED), false).count());
    }

    @Test
    public void testTimeResourceSetToValue(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(t);
        IntegerResource ResourceA = new IntegerResource(0, "subsystem1", "");

        ResourceA.set(98);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300);

        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.nextTimeResourceSetToValue(200, t));
        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.nextTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(100))));
        assertEquals(null, ResourceA.nextTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(240))));
        assertEquals(null, ResourceA.nextTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(300))));
        assertEquals(t.add(SECOND_DURATION.multiply(99)), ResourceA.nextTimeResourceSetToValue(-115, t));
        assertEquals(null, ResourceA.nextTimeResourceSetToValue(215, t));

        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.priorTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(240))));
        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.priorTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(300))));
        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceA.priorTimeResourceSetToValue(200, t.add(SECOND_DURATION.multiply(250))));
        assertEquals(null, ResourceA.priorTimeResourceSetToValue(200, t));
        assertEquals(null, ResourceA.priorTimeResourceSetToValue(200, t.subtract(SECOND_DURATION)));
        assertEquals(t.add(SECOND_DURATION.multiply(99)), ResourceA.priorTimeResourceSetToValue(-115, t.add(SECOND_DURATION.multiply(150))));
        assertEquals(null, ResourceA.nextTimeResourceSetToValue(215, t.add(SECOND_DURATION.multiply(300))));

        myEngine.setTime(t);
        StringResource ResourceB = new StringResource();
        assertEquals(null, ResourceB.nextTimeResourceSetToValue("testme", t.subtract(SECOND_DURATION)));
        assertEquals(null, ResourceB.priorTimeResourceSetToValue("testme", t.add(SECOND_DURATION)));
        assertEquals(null, ResourceB.priorTimeResourceSetToValue("testme", t));

        ResourceB.set("a");
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceB.set("b");
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceB.set("c");

        assertEquals(t.add(SECOND_DURATION.multiply(99)), ResourceB.nextTimeResourceSetToValue("b", t));
        assertEquals(null, ResourceB.nextTimeResourceSetToValue("b", t.add(SECOND_DURATION.multiply(200))));
        assertEquals(t.add(SECOND_DURATION.multiply(200)), ResourceB.priorTimeResourceSetToValue("c", t.add(SECOND_DURATION.multiply(240))));
    }

    @Test
    public void testChangeList(){
        Time t = Time.getDefaultReferenceTime();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.setTime(t);
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");

        ResourceA.set(98.7);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(99)));
        ResourceA.set(-115.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(200)));
        ResourceA.set(200.0);
        myEngine.setTime(t.add(SECOND_DURATION.multiply(240)));
        ResourceA.set(300.0);

        List<Map.Entry<Time, Double>> changes = ResourceA.getChangesDuringWindow(Time.getDefaultReferenceTime(), Time.getDefaultReferenceTime().add(DAY_DURATION));
        assertEquals(4, changes.size());
        assertEquals(98.7, changes.get(0).getValue(), 0.0000001);
        assertEquals(t.add(SECOND_DURATION.multiply(240)), changes.get(3).getKey());
    }

    @Test
    public void testConstructors(){
        BooleanResource booleanResource1 = new BooleanResource(false, "generic", "", "constant");
        BooleanResource booleanResource2 = new BooleanResource(false, "generic", "");
        BooleanResource booleanResource3 = new BooleanResource(false, "generic");
        BooleanResource booleanResource4 = new BooleanResource();

        assert(booleanResource1.profile(Time.getDefaultReferenceTime()) == booleanResource2.profile(Time.getDefaultReferenceTime()) && booleanResource3.profile(Time.getDefaultReferenceTime()) == booleanResource4.profile(Time.getDefaultReferenceTime()));
        assert(booleanResource1.subsystem.equals(booleanResource2.subsystem) && booleanResource2.subsystem.equals(booleanResource3.subsystem) && booleanResource3.subsystem.equals(booleanResource4.subsystem));

        DoubleResource doubleResource1 = new DoubleResource(0.0, "generic", "", "constant");
        DoubleResource doubleResource2 = new DoubleResource(0.0, "generic", "");
        DoubleResource doubleResource3 = new DoubleResource(0.0, "generic");
        DoubleResource doubleResource4 = new DoubleResource();

        assert(doubleResource1.profile(Time.getDefaultReferenceTime()).equals(doubleResource2.profile(Time.getDefaultReferenceTime())) && doubleResource3.profile(Time.getDefaultReferenceTime()).equals(doubleResource4.profile(Time.getDefaultReferenceTime())));
        assert(doubleResource1.subsystem.equals(doubleResource2.subsystem) && doubleResource2.subsystem.equals(doubleResource3.subsystem) && doubleResource3.subsystem.equals(doubleResource4.subsystem));

        DurationResource durationResource1 = new DurationResource(new Duration(), "generic", "constant", "", null, null);
        DurationResource durationResource2 = new DurationResource(new Duration(), "generic", "constant", "");
        DurationResource durationResource3 = new DurationResource(new Duration(), "generic", "constant");
        DurationResource durationResource4 = new DurationResource(new Duration(), "generic");
        DurationResource durationResource5 = new DurationResource();

        assert(durationResource1.profile(Time.getDefaultReferenceTime()).equals(durationResource2.profile(Time.getDefaultReferenceTime())) && durationResource3.profile(Time.getDefaultReferenceTime()).equals(durationResource4.profile(Time.getDefaultReferenceTime())));
        assertEquals(durationResource1.interpolation, durationResource5.interpolation);

        IntegerResource intResource1 = new IntegerResource(0, "generic", "", "constant");
        IntegerResource intResource2 = new IntegerResource("generic", "", "constant");
        IntegerResource intResource3 = new IntegerResource(0, "generic", "");
        IntegerResource intResource4 = new IntegerResource("generic");
        IntegerResource intResource5 = new IntegerResource();

        assert(intResource1.profile(Time.getDefaultReferenceTime()).equals(intResource2.profile(Time.getDefaultReferenceTime())) && intResource3.profile(Time.getDefaultReferenceTime()).equals(intResource4.profile(Time.getDefaultReferenceTime())) && intResource1.profile(Time.getDefaultReferenceTime()).equals(intResource5.profile(Time.getDefaultReferenceTime())));
        assert(intResource1.subsystem.equals(intResource2.subsystem) && intResource3.subsystem.equals(booleanResource3.subsystem) && booleanResource3.subsystem.equals(booleanResource4.subsystem));

        StringResource stringResource1 = new StringResource("", "generic", "", "constant", Arrays.asList(""));
        StringResource stringResource2 = new StringResource("generic", "", "constant", Arrays.asList(""));
        StringResource stringResource3 = new StringResource("", "generic", "", "constant");
        StringResource stringResource4 = new StringResource("", "generic", "");
        StringResource stringResource5 = new StringResource("", "generic");
        StringResource stringResource6 = new StringResource("", Arrays.asList(""));
        StringResource stringResource7 = new StringResource(Arrays.asList(""));
        StringResource stringResource8 = new StringResource();

        SumIntegerResource sumint1 = new SumIntegerResource(intResource1, intResource2, "generic", "", "constant", null, null);
        SumIntegerResource sumint2 = new SumIntegerResource(intResource1, intResource2, "generic", "", "constant");
        SumIntegerResource sumint3 = new SumIntegerResource(intResource1, intResource2, "generic", "");
        SumIntegerResource sumint4 = new SumIntegerResource(intResource1, intResource2, "generic");
        SumIntegerResource sumint5 = new SumIntegerResource(intResource1, intResource2);

        SumDoubleResource sumdouble1 = new SumDoubleResource(doubleResource1, doubleResource2, "generic", "", "constant", null, null);
        SumDoubleResource sumdouble2 = new SumDoubleResource(doubleResource1, doubleResource2, "generic", "", "constant");
        SumDoubleResource sumdouble3 = new SumDoubleResource(doubleResource1, doubleResource2, "generic", "");
        SumDoubleResource sumdouble4 = new SumDoubleResource(doubleResource1, doubleResource2, "generic");
        SumDoubleResource sumdouble5 = new SumDoubleResource(doubleResource1, doubleResource2);

        ArrayedResource<DoubleResource> PositionVector = new ArrayedResource<DoubleResource>(vectorComponents){};
        //String subsystem, String units, String interpolation, ArrayedResource toIntegrate, long dtInMilliseconds
        ArrayedResource<IntegratingResource> IntegratesPosition = new ArrayedResource<IntegratingResource>("generic", "", "constant", PositionVector, ONE_SECOND) {};
    }

    @Test
    public void getBySubsystem(){
        assertEquals(2, ResourceList.getResourceList().getNamesOfAllResourcesWithSubsystem("subsystem1").size());
        assertEquals(1, ResourceList.getResourceList().getNamesOfAllResourcesWithSubsystem("subsystem2").size());
        assertEquals("ResourceC", ResourceList.getResourceList().getNamesOfAllResourcesWithSubsystem("subsystem2").get(0));
    }

    @Test
    public void testArrayedResourceKeyQuery() {
        ArrayedResource<DoubleResource> somePositionVector = new ArrayedResource<DoubleResource>(vectorComponents){};
        assert(somePositionVector.containsIndex("x"));
        assert(somePositionVector.containsIndex("y"));
        assert(somePositionVector.containsIndex("z"));
        assert(!somePositionVector.containsIndex("w"));
    }
}