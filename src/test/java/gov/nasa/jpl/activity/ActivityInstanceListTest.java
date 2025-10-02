package gov.nasa.jpl.activity;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ActivityInstanceListTest extends BaseTest {

    @Test
    public void prepareActivitiesForModeling() {
    }

    @Test
    public void add() {
        assertEquals(0, ActivityInstanceList.getActivityList().length());
        Activity red = new Activity(new Time("2000-001T00:00:00"));
        assertEquals(1, ActivityInstanceList.getActivityList().length());
        ActivityInstanceList.getActivityList().add(red);
        assertEquals(2, ActivityInstanceList.getActivityList().length());

    }

    @Test
    public void remove0() {
        Activity red = new Activity(new Time("2000-001T00:00:00"));
        ActivityInstanceList.getActivityList().add(red);
        assertEquals(2, ActivityInstanceList.getActivityList().length());
        ActivityInstanceList.getActivityList().remove(red);
        assertEquals(1, ActivityInstanceList.getActivityList().length());
        ActivityInstanceList.getActivityList().remove(red);
        assertEquals(0, ActivityInstanceList.getActivityList().length());
    }

    @Test
    public void length() {
        Activity red = new Activity(new Time("2000-001T00:00:00"));
        ActivityInstanceList.getActivityList().add(red);
        ActivityInstanceList.getActivityList().add(red);
        ActivityInstanceList.getActivityList().add(red);
        ActivityInstanceList.getActivityList().add(red);
        assertEquals(5, ActivityInstanceList.getActivityList().length());
        ActivityInstanceList.getActivityList().clear();
        Activity blue = new Activity(new Time("2000-001T00:00:00"));
        Activity green = new Activity(new Time("2000-001T00:00:00"));
        Activity cyan = new Activity(new Time("2000-001T00:00:00"));
        assertEquals(3, ActivityInstanceList.getActivityList().length());
    }


    @Test
    public void getFirstActivityStartTime() {
        Activity blue = new Activity(new Time("2000-001T00:00:02"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        Activity cyan = new Activity(new Time("2000-001T00:00:10"));
        assertEquals(new Time("2000-001T00:00:02"), ActivityInstanceList.getActivityList().getFirstActivityStartTime());
    }

    @Test
    public void getLastActivityEndTime() {
        Activity blue = new Activity(new Time("2000-001T00:00:02"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        Activity cyan = new Activity(new Time("2000-001T00:00:10"));
        cyan.setDuration(new Duration("00:00:05"));
        assertEquals(new Time("2000-001T00:00:15"), ActivityInstanceList.getActivityList().getLastActivityEndTime());
    }

    @Test
    public void createListOfActivityBeginAndEndTimes() {
        Activity blue = new Activity(new Time("2000-001T00:00:02"));
        blue.setDuration(new Duration("00:00:05"));
        Activity green = new Activity(new Time("2000-001T00:00:05"));
        green.setDuration(new Duration("00:01:57"));
        Activity cyan = new Activity(new Time("2000-001T00:00:10"));
        cyan.setDuration(new Duration("00:00:10"));
        List<Map.Entry<Time, Map.Entry<Boolean, Activity>>> listOfAllBeginAndEndTimes = ActivityInstanceList.getActivityList().createListOfActivityBeginAndEndTimes();
        List<Map.Entry<Time, Map.Entry<Boolean, Activity>>> expectedValues = new ArrayList<>();
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:00:02"), new AbstractMap.SimpleImmutableEntry(true, null)));
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:00:05"), new AbstractMap.SimpleImmutableEntry(true, null)));
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:00:07"), new AbstractMap.SimpleImmutableEntry(false, null)));
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:00:10"), new AbstractMap.SimpleImmutableEntry(true, null)));
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:00:20"), new AbstractMap.SimpleImmutableEntry(false, null)));
        expectedValues.add(new AbstractMap.SimpleImmutableEntry(new Time("2000-001T00:02:02"), new AbstractMap.SimpleImmutableEntry(false, null)));

        for (int i = 0; i < listOfAllBeginAndEndTimes.size(); i++) {
            if ((!listOfAllBeginAndEndTimes.get(i).getKey().equals(expectedValues.get(i).getKey())) || (!listOfAllBeginAndEndTimes.get(i).getValue().getKey()) == (expectedValues.get(i).getValue().getKey())) {
                fail("Begin and end times not as expected");
            }
        }
    }

    @Test
    public void getListsOfActivities(){
        Time t = Time.getDefaultReferenceTime();
        ActivityOne one1 = new ActivityOne(t, Duration.MINUTE_DURATION);
        ActivityOne one2 = new ActivityOne(t.add(Duration.MINUTE_DURATION), Duration.MINUTE_DURATION);
        ActivityOne one3 = new ActivityOne(t.add(Duration.MINUTE_DURATION), Duration.MINUTE_DURATION.multiply(4));
        ActivityOne one4 = new ActivityOne(t.add(Duration.MINUTE_DURATION.multiply(3)), Duration.MINUTE_DURATION);
        ActivityTwo decoy = new ActivityTwo(t, 5.0);

        assertEquals(5, ActivityInstanceList.getActivityList().length());
        assertEquals(4, ActivityInstanceList.getActivityList().getAllActivitiesOfType(ActivityOne.class).size());
        assertEquals(3, ActivityInstanceList.getActivityList().getActivitiesOfTypeBetween(t, t.add(Duration.MINUTE_DURATION.multiply(2)), false, ActivityOne.class).size());
        assertEquals(2, ActivityInstanceList.getActivityList().getActivitiesOfTypeBetween(t, t.add(Duration.MINUTE_DURATION.multiply(2)), true, ActivityOne.class).size());
        assertEquals(t.add(Duration.MINUTE_DURATION), ActivityInstanceList.getActivityList().getActivitiesOfTypeBetween(t, t.add(Duration.MINUTE_DURATION.multiply(2)), true, ActivityOne.class).get(1).getStart());

        assertEquals(5, ActivityInstanceList.getActivityList().getAllActivitiesOfType(ActivityOne.class, ActivityTwo.class).size());
        assertEquals(4, ActivityInstanceList.getActivityList().getActivitiesOfTypeBetween(t, t.add(Duration.MINUTE_DURATION.multiply(2)), false, ActivityOne.class, ActivityTwo.class).size());
    }

    @Test
    public void getInstanceCountForEachType(){
        Time t = Time.getDefaultReferenceTime();
        ActivityOne one1 = new ActivityOne(t, Duration.MINUTE_DURATION);
        ActivityOne one2 = new ActivityOne(t.add(Duration.MINUTE_DURATION), Duration.MINUTE_DURATION);
        ActivityOne one3 = new ActivityOne(t.add(Duration.MINUTE_DURATION), Duration.MINUTE_DURATION.multiply(4));
        ActivityOne one4 = new ActivityOne(t.add(Duration.MINUTE_DURATION.multiply(3)), Duration.MINUTE_DURATION);
        ActivityTwo decoy = new ActivityTwo(t, 5.0);

        List<Map.Entry<String, Integer>> counts = ActivityInstanceList.getActivityList().getInstanceCountForEachType();

        int countsOne = counts.get(0).getValue();
        assertEquals(4, countsOne);
        assertEquals("ActivityOne", counts.get(0).getKey());

        int countsTwo = counts.get(1).getValue();
        assertEquals(1, countsTwo);
        assertEquals("ActivityTwo", counts.get(1).getKey());

        int countsThree = counts.get(2).getValue();
        assertEquals(0, countsThree);
    }

    @Test
    public void getBySubsystem(){
        Time t = Time.getDefaultReferenceTime();
        ActivityOne one1 = new ActivityOne(t, Duration.MINUTE_DURATION);
        ActivityTwo two1 = new ActivityTwo(t, 5.0);
        ActivityTwo two2 = new ActivityTwo(t.add(Duration.MINUTE_DURATION), 5.0);

        List<Activity> result = ActivityInstanceList.getActivityList().getActivityInstancesWithSubsystem("testSubsystem2", t, t.add(Duration.HOUR_DURATION), false);
        assertEquals(2, result.size());
        assertEquals(t.add(Duration.MINUTE_DURATION), result.get(1).getStart());
        List<Activity> result2 = ActivityInstanceList.getActivityList().getActivityInstancesWithSubsystem("generic", t, t.add(Duration.HOUR_DURATION), false);
        assertTrue(!result2.isEmpty());

        List<String> types = ActivityTypeList.getActivityList().getNamesOfAllTypesWithSubsystem("testSubsystem2");
        assertEquals(1, types.size());
    }
}