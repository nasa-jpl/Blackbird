package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.InnerClassActivitySpawner;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.*;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class XMLTOLWriterTest extends BaseTest {

    @Test
    public void writeXMLTOL(){
        // does write succeed with nothing in it? should print a file with just ResourceMetadata in it and not crash
        CommandController.issueCommand("WRITE", "empty.tol.xml RESOURCES EXCLUDE (IntegratesA)");

        // does write succeed with only one node in it? should not crash either
        CommandController.issueCommand("WRITE", "one_entry.tol.xml");

        ActivityOne actOne = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));
        ActivityOne actSecond = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));
        ActivityOne actThird = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));

        // we should have 7 TOLRecords: two for each instance (were not modeled or decomposed) and one for IntegratesA
        String smallFileName = "small_num_entries.tol.xml";
        CommandController.issueCommand("WRITE", smallFileName);
        ActivityInstanceList.getActivityList().clear();
        CommandController.issueCommand("OPEN_FILE", smallFileName);
        assertEquals(3, ActivityInstanceList.getActivityList().length());

        for (Resource res: ResourceList.getResourceList().getListOfAllResources()) {
            res.setFrozen(false);
        }
    }

    @Test
    public void testBatching(){
        List<List<Map.Entry<Integer, Integer>>> indices = XMLTOLWriter.breakLongListIntoStartEndSublistsByBatchAndCore(1000000, 50000, 7);
        assertEquals(20, indices.size());
        assertEquals(7, indices.get(0).size());
        assertEquals(new Integer(7143), indices.get(0).get(0).getValue());
        assertEquals(new Integer(7143), indices.get(0).get(1).getKey());
        assertEquals(new Integer(7143*2), indices.get(0).get(2).getKey());
        assertEquals(new Integer(50000-7142-7143), indices.get(0).get(5).getKey());
        assertEquals(new Integer(50000-7142), indices.get(0).get(5).getValue());
        assertEquals(new Integer(50000-7142), indices.get(0).get(6).getKey());
        assertEquals(new Integer(50000), indices.get(0).get(6).getValue());
        assertEquals(new Integer(50000), indices.get(1).get(0).getKey());
        assertEquals(new Integer(1000000-7142), indices.get(19).get(6).getKey());
        assertEquals(new Integer(1000000), indices.get(19).get(6).getValue());

        indices = XMLTOLWriter.breakLongListIntoStartEndSublistsByBatchAndCore(1000002, 50000, 7);
        assertEquals(21, indices.size());
        assertEquals(7, indices.get(0).size());
        assertEquals(new Integer(7143), indices.get(0).get(0).getValue());
        assertEquals(new Integer(7143), indices.get(0).get(1).getKey());
        assertEquals(new Integer(7143*2), indices.get(0).get(2).getKey());
        assertEquals(new Integer(1000000), indices.get(20).get(0).getKey());
        assertEquals(new Integer(1000001), indices.get(20).get(0).getValue());
        assertEquals(2, indices.get(20).size());
        assertEquals(new Integer(1000000), indices.get(20).get(0).getKey());
        assertEquals(new Integer(1000001), indices.get(20).get(0).getValue());
        assertEquals(new Integer(1000001), indices.get(20).get(1).getKey());
        assertEquals(new Integer(1000002), indices.get(20).get(1).getValue());
    }

    @Test
    public void testResourcesWindowIncludesPastResources(){
        // Create activities that will update IntegratesA resource
        ActivityOne actOne = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));
        ActivityOne actTwo = new ActivityOne(Time.getDefaultReferenceTime().add(new Duration("02:00:00")), new Duration("00:01:00"));

        String fileName = "test_past_resources.tol.xml";
        Time queryStart = Time.getDefaultReferenceTime().add(new Duration("01:00:00"));
        CommandController.issueCommand("WRITE", fileName + " START " + queryStart.toString() + " RESOURCES_WINDOW includeIncon");

        File file = new File(fileName);
        try {
            Scanner scanner = new Scanner(file);
            String fileContent = scanner.useDelimiter("\\Z").next();
            scanner.close();
            // Should contain IntegratesA value at query start time (from before the window)
            assertTrue("File should contain resource value at query start with includeIncon",
                fileContent.contains("IntegratesA") && fileContent.contains(queryStart.toString()) && fileContent.contains("RES_VAL"));
        } catch (FileNotFoundException e) {
            fail("Output file not created");
        }
    }

    @Test
    public void testResourcesWindowExcludesPastResources(){
        // Create activities that will update IntegratesA resource
        ActivityOne actOne = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));
        ActivityOne actTwo = new ActivityOne(Time.getDefaultReferenceTime().add(new Duration("02:00:00")), new Duration("00:01:00"));

        String fileName = "test_no_past_resources.tol.xml";
        Time queryStart = Time.getDefaultReferenceTime().add(new Duration("01:00:00"));
        CommandController.issueCommand("WRITE", fileName + " START " + queryStart.toString() + " RESOURCES_WINDOW onlySetsInWindow");

        File file = new File(fileName);
        try {
            Scanner scanner = new Scanner(file);
            String fileContent = scanner.useDelimiter("\\Z").next();
            scanner.close();
            // Should NOT contain IntegratesA value at query start time with RES_VAL tag
            assertFalse("File should NOT contain resource RES_VAL at query start with onlySetsInWindow",
                fileContent.contains(queryStart.toString()) && fileContent.contains("IntegratesA") && fileContent.contains("RES_VAL"));
        } catch (FileNotFoundException e) {
            fail("Output file not created");
        }
    }

    @Test
    public void testResourcesWindowDefaultBehavior(){
        // Create activities that will update IntegratesA resource
        ActivityOne actOne = new ActivityOne(Time.getDefaultReferenceTime(), new Duration("00:01:00"));
        ActivityOne actTwo = new ActivityOne(Time.getDefaultReferenceTime().add(new Duration("02:00:00")), new Duration("00:01:00"));

        String fileName = "test_default_behavior.tol.xml";
        Time queryStart = Time.getDefaultReferenceTime().add(new Duration("01:00:00"));
        CommandController.issueCommand("WRITE", fileName + " START " + queryStart.toString());

        File file = new File(fileName);
        try {
            Scanner scanner = new Scanner(file);
            String fileContent = scanner.useDelimiter("\\Z").next();
            scanner.close();
            // Default behavior should not include past resources (same as onlySetsInWindow)
            assertFalse("File should contain resource value at query start by default",
                    fileContent.contains(queryStart.toString()) && fileContent.contains("IntegratesA") && fileContent.contains("RES_VAL"));
        } catch (FileNotFoundException e) {
            fail("Output file not created");
        }
    }

    @Test
    public void testResInconWithUseAtStart(){
        String fileName = "test_res_incon_collision.tol.xml";

        Time queryStart = Time.getDefaultReferenceTime().add(new Duration("01:00:00"));

        // this is just to make the modeling start off before the query, even though it doesn't set any resources
        Activity starter = new ActivityNine(Time.getDefaultReferenceTime(), Time.getDefaultReferenceTime().add(Duration.SECOND_DURATION));

        // should add '5' right at queryStart, and we want to make sure there aren't duplicate entries in the output
        Activity act = new ActivityTwo(queryStart, 5.0);
        CommandController.issueCommand("REMODEL", "");

        CommandController.issueCommand("WRITE", fileName + " START " + queryStart.toString() + " RESOURCES_WINDOW includeIncon");

        File file = new File(fileName);
        try {
            Scanner scanner = new Scanner(file);
            String fileContent = scanner.useDelimiter("\\Z").next();
            scanner.close();
            // one for metadata, one for FINAL_VAL, one (and only one) for 2000-001T01:00:00 when it is set AND query time, and one for when it is set again at 2000-001T01:02:00
            assertEquals(4, StringUtils.countMatches(fileContent, "ResourceA"));

        } catch (FileNotFoundException e) {
            fail("Output file not created");
        }
    }

    // utility for other tests, but it involves writing so it's in this class
    public static void createSimulationAndWriteOutFile(String fileName, boolean includeResIncon){
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

        if(includeResIncon) {
            CommandController.issueCommand("WRITE", fileName + " START 2000-002T00:02:20 END 2000-007T00:00:00 RESOURCES_WINDOW includeIncon");
        }
        else{
            CommandController.issueCommand("WRITE", fileName);
        }
    }

}
