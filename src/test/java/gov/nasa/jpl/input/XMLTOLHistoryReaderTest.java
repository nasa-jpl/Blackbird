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
import static gov.nasa.jpl.output.tol.XMLTOLWriterTest.createSimulationAndWriteOutFile;
import static org.junit.Assert.*;

public class XMLTOLHistoryReaderTest extends BaseTest {
    @Before
    public void setUp(){
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
        String escaped_string = "\"hello there\"";
        createSimulationAndWriteOutFile(fileName, false);

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
