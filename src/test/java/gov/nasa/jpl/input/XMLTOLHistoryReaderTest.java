package gov.nasa.jpl.input;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.InnerClassActivitySpawner;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.*;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.exampleAdaptation.Res.ExampleBodyState;
import static gov.nasa.jpl.exampleAdaptation.Res.TestState;
import static org.junit.Assert.*;

public class XMLTOLHistoryReaderTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        EpochRelativeTime.addEpoch("a", Time.getDefaultReferenceTime());
    }

    @After
    public void tearDown(){
        ModelingEngine.getEngine().setCurrentlyReadingInFile(false);
    }

    @Test
    public void readInXMLTOLHistory() {
        readInHistoryOfActivitiesAndResource("history_unit_test.tol.xml", true);

        for (Resource res: ResourceList.getResourceList().getListOfAllResources()) {
            res.setFrozen(false);
        }
    }

    public static void readInHistoryOfActivitiesAndResource(String fileName, boolean checkResources){
        ResourceDeclaration.assignNamesToAllResources();
        ResourceList.getResourceList().resetResourceHistories();
        ResourceList.getResourceList().makeAllResourcesUseTheirProfileAtInitialTime();
        ActivityInstanceList.getActivityList().clear();

        Map<String, List<String>> stringListMap = new HashMap<>();
        List firstList = new ArrayList<>();
        List secondList = new ArrayList<>();
        firstList.add("This");
        firstList.add("is");
        firstList.add("the");
        firstList.add("remix");
        stringListMap.put("42", firstList);
        stringListMap.put("kg", secondList);
        ActivityFive act5 = new ActivityFive(new Time("2000-003T00:00:00"), new Duration("00:00:45"), stringListMap);
        act5.decompose();

        String escaped_string = "\"hello there\"";
        List<String> stringList = new ArrayList<>();
        stringList.add(escaped_string);
        ActivityThree three = new ActivityThree(Time.getDefaultReferenceTime(), Duration.MINUTE_DURATION, stringList);
        three.decompose();

        ActivityEight act81 = new ActivityEight(new Time("2000-001T00:00:00"), "Sun", "x");
        act81.decompose();
        ActivityEight act82 = new ActivityEight(new Time("2000-001T00:00:02"), "Sun", "x");
        act82.decompose();

        ActivityTwo act21 = new ActivityTwo(new EpochRelativeTime("a+1T00:00:00"), 5.0);
        act21.decompose();
        ActivityTwo act22 = new ActivityTwo(new Time("2000-002T00:01:00"), 7.0);
        act22.decompose();

        Activity nine = new ActivityNine(new Time("2000-005T00:01:00"), new EpochRelativeTime("test+6T00:00:00"));
        nine.decompose();

        InnerClassActivitySpawner testInner = new InnerClassActivitySpawner(Activity.now());
        testInner.decompose();

        CommandController.issueCommand("REMODEL", "");

        CommandController.issueCommand("WRITE", fileName);

        ResourceList.getResourceList().resetResourceHistories();
        ActivityInstanceList.getActivityList().clear();

        CommandController.issueCommand("OPEN_FILE", fileName);

        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        List<Activity> fiveList = actList.getAllActivitiesOfType(ActivityFive.class);
        List<Activity> threeList = actList.getAllActivitiesOfType(ActivityThree.class);
        List<Activity> twoList = actList.getAllActivitiesOfType(ActivityTwo.class);

        assertEquals(13, actList.length());
        assertEquals(1, fiveList.size());
        assertEquals(2, twoList.size());

        assertEquals("remix", ((Map<String, List<String>>) fiveList.get(0).getParameterObjects()[1]).get("42").get(3));
        assertEquals(escaped_string, ((List<String>) threeList.get(0).getParameterObjects()[1]).get(0));
        assertEquals(new Duration("00:00:45"), fiveList.get(0).getDuration());
        assert(ActivityOne.class.isAssignableFrom(twoList.get(0).getChildren().get(0).getClass()));
        assert(new Duration("00:00:01.000000").equals(twoList.get(0).getChildren().get(0).getDuration()));

        if(checkResources) {
            assertEquals(2.0, ExampleBodyState.get("Sun").get("x").valueAt(new Time("2000-001T00:01:00")), 0.0000001);
            assertEquals("NoSignal", TestState.valueAt(new Time("2000-001T00:01:00")));
            assert (new Time("2000-001T00:00:07").equals(TestState.lastTimeSet()));
        }
    }
}
