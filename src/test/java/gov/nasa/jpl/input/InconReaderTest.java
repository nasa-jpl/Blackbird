package gov.nasa.jpl.input;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityEight;
import gov.nasa.jpl.exampleAdaptation.ActivityFive;
import gov.nasa.jpl.exampleAdaptation.ActivityNine;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.exampleAdaptation.Res.*;
import static gov.nasa.jpl.time.Duration.ZERO_DURATION;
import static org.junit.Assert.*;

public class InconReaderTest extends BaseTest {
    @Test
    public void xmlReader() {
        getInitialConditions("incon_unit_test.tol.xml");
    }

    @Test
    public void jsonReader() {
        getInitialConditions("incon_unit_test.fincon.json");
    }

    public void getInitialConditions(String fileName) {
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

        ActivityEight act81 = new ActivityEight(new Time("2000-001T00:00:00"), "Sun", "x");
        act81.decompose();
        ActivityEight act82 = new ActivityEight(new Time("2000-001T00:00:02"), "Sun", "x");
        act82.decompose();

        ActivityTwo act21 = new ActivityTwo(new Time("2000-002T00:00:00"), 5.0);
        act21.decompose();
        ActivityTwo act22 = new ActivityTwo(new Time("2000-002T00:01:00"), 7.0);
        act22.decompose();

        Activity nine = new ActivityNine(new Time("2000-005T00:01:00"), new Time("2000-005T00:02:00"));
        nine.decompose();

        CommandController.issueCommand("REMODEL", "");

        CommandController.issueCommand("WRITE", fileName);

        resetForTest();

        CommandController.issueCommand("INCON", fileName);
        CommandController.issueCommand("REMODEL", "");

        // need to put out XMLTOL to make sure initial condition activities get written out OK
        CommandController.issueCommand("WRITE", fileName.split("\\.")[0] + "_cycled.tol.xml");

        // there should only be one activity added - the 'incon' activity
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        assert(actList.length() == 1);

        assertEquals((Integer) 0, ResourceB.valueAt(new Time("2000-001T00:00:00.000")));
        // based on the activities added, the last change to the plan is at the time the incon activity is placed:
        assertEquals("NoSignal", TestState.valueAt(new Time("2000-002T00:03:40.000000")));
        assertEquals((Integer) 35, ResourceB.valueAt(new Time("2000-002T00:03:40.000000")));
        assertEquals(42.49, ResourceA.valueAt(new Time("2000-002T00:03:40.000000")), 0.1);
        assertEquals(4727.96, IntegratesA.valueAt(new Time("2000-002T00:03:40.000000")), 0.01);
        assertEquals(12.0, PositionVector.get("x").valueAt(new Time("2000-002T00:03:40.000000")), 0.0000001);
        assertEquals(0.04, PositionVector.get("y").valueAt(new Time("2000-002T00:03:40.000000")), 0.0000001);
        assertEquals(2.0, ExampleBodyState.get("Sun").get("x").valueAt(new Time("2000-002T00:03:40.000000")), 0.0000001);
        assertEquals(ZERO_DURATION, ResourceC.valueAt(new Time("2000-002T00:03:40.000000")));
    }
}