package gov.nasa.jpl.activity;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.exampleAdaptation.ActivityEight;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ActivityTest extends BaseTest {

    @Test
    public void getParameterValues() {
        ArrayList<String> paramList = new ArrayList();
        paramList.add("one");
        paramList.add("five");
        Activity red = new Activity(new Time("2000-001T00:00:06"), 5.0, 7.3, "hello world", paramList);
        Object[] expectedOutput = new Object[]{5.0, 7.3, "hello world", paramList};
        assertArrayEquals(expectedOutput, red.getParameterObjects());
    }

    @Test
    public void getStart() {
        Activity red = new Activity(new Time("2000-001T00:00:15"));
        assertTrue(red.getStart().equals(new Time("2000-001T00:00:15")));
    }

    @Test
    public void getWindow(){
        ActivityOne act = new ActivityOne(new Time("2000-001T00:00:15"), new Duration("1T00:00:00"));
        assertEquals(new Window(new Time("2000-001T00:00:15"), new Time("2000-002T00:00:15")), act.getWindow());
    }

    @Test
    public void compareTo() {
        Activity red = new Activity(new Time("2000-001T00:00:15"));
        Activity blue = new Activity(new Time("2000-001T00:00:10"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        Activity orange = new Activity(new EpochRelativeTime("test+00:00:07"));

        assertTrue(blue.compareTo(blue) == 0);
        assertTrue(blue.compareTo(red) < 0);
        assertTrue(blue.compareTo(green) > 0);
        assertTrue(orange.compareTo(green) > 0);
        assertTrue(orange.compareTo(blue) < 0);
    }

    @Test
    public void getID() {
        Activity red = new Activity(new Time("2000-001T00:00:15"));
        Activity blue = new Activity(new Time("2000-001T00:00:10"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        assertNotEquals(red.getID(), blue.getID());
        assertNotEquals(red.getID(), green.getID());
        assertNotEquals(blue.getID(), green.getID());
    }

    @Test
    public void getIDString() {
        Activity red = new Activity(new Time("2000-001T00:00:15"));
        Activity blue = new Activity(new Time("2000-001T00:00:10"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        assertEquals(red.getIDString(), red.getID().toString());
        assertEquals(blue.getIDString(), blue.getID().toString());
        assertEquals(green.getIDString(), green.getID().toString());
    }

    @Test
    public void checkNullStartTime(){
        try {
            ActivityEight act = new ActivityEight(null, "my_name", "another_string");
            fail();
        }
        catch(AdaptationException e){
            if(!e.getMessage().startsWith("Cannot pass null start time into activity instance")){
                fail();
            }
        }
    }

    @Test
    public void testSpawn(){
        Activity act1 = new Activity(new Time("2000-001T00:00:15"));
        Activity act2 = new Activity(new Time("2000-001T00:00:20"));
        Activity act3 = new Activity(new Time("2000-001T00:00:25"));

        act1.spawn(act2);
        // should check if decompose ran but not an easy way to do that, so we just check parentage here
        assertEquals(act1, act2.getParent());
        assertEquals(act2, act1.getChildren().get(0));

        act1.spawnWithNotes(act3, "My test notes");
        assertEquals(act1, act3.getParent());
        assertEquals(act3, act1.getChildren().get(1));
        assertEquals("My test notes", act3.getNotes());
    }

}