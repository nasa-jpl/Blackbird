package gov.nasa.jpl.input;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gov.nasa.jpl.input.XMLTOLHistoryReaderTest.readInHistoryOfActivitiesAndResource;
import static gov.nasa.jpl.output.tol.XMLTOLWriterTest.createSimulationAndWriteOutFile;
import static gov.nasa.jpl.output.tol.XMLTOLWriterTest.createTinySimWithActivityAtStartChanges;
import static gov.nasa.jpl.time.Duration.MINUTE_DURATION;
import static org.junit.Assert.*;

public class ParallelIOTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("a", Time.getDefaultReferenceTime());
        ModelingEngine.getEngine().setTime(Time.getDefaultReferenceTime());
    }

    @Test
    public void readInDirectoryHistory() {
        try {
            readInHistoryOfActivitiesAndResource(REGRESSION_TEST_OUTPUT_DIR + "history_unit_test.dir", true);
        }
        finally {
            for (Resource res: ResourceList.getResourceList().getListOfAllResources()) {
                res.setFrozen(false);
            }
        }
    }

    @Test
    public void testEmptyDirectories(){
        String testDir = REGRESSION_TEST_OUTPUT_DIR + "test.dir";

        new File(testDir + "/activities").mkdirs();
        new File(testDir + "/resources").mkdirs();
        CommandController.issueCommand("OPEN_FILE", testDir);
        new File(testDir + "/activities").delete();
        new File(testDir + "/resources").delete();

        new File(testDir + "/activities").mkdirs();
        CommandController.issueCommand("OPEN_FILE", testDir);
        new File(testDir + "/activities").delete();

        new File(testDir + "/resources").mkdirs();
        CommandController.issueCommand("OPEN_FILE", testDir);
        new File(testDir + "/resources").delete();

        CommandController.issueCommand("OPEN_FILE", testDir);
    }

    @Test
    public void testIncludingInconResources(){
        createSimulationAndWriteOutFile(REGRESSION_TEST_OUTPUT_DIR + "history_unit_test_resource_incon.dir", true);

        BufferedReader br = null;
        List<List<String>> contents = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(REGRESSION_TEST_OUTPUT_DIR + "history_unit_test_resource_incon.dir" + File.separator + "resources" + File.separator + "PositionVector[y].csv"));
            String line = br.readLine();
            while (line != null) {
                contents.add(Arrays.asList(line.split(",")));
                line = br.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // check that the first entry corresponds to the desired incon time
        Time priorTime = null;
        for(int i = 0; i< contents.size(); i++){
            // check that the first entry is the incon value we want
            if(i == 0){
                assertEquals("2000-002T00:02:20.000000", contents.get(i).get(0));
                assertEquals("0.02", contents.get(i).get(2));
                priorTime = new Time(contents.get(i).get(0));
            }
            // check that all the entries following in the output are in time order
            else{
                Time currentTime = new Time(contents.get(i).get(0));
                assertTrue(currentTime.greaterThan(priorTime));
                priorTime = currentTime;
            }
        }
    }

    @Test
    public void testActivityAtStartFlag(){
        ActivityInstanceList.getActivityList().clear();
        String fileName = REGRESSION_TEST_OUTPUT_DIR + "test_include_ongoing_act.dir";
        createTinySimWithActivityAtStartChanges(fileName, true);
        ActivityInstanceList.getActivityList().clear();
        CommandController.issueCommand("OPEN_FILE", fileName);
        assertEquals(3, ActivityInstanceList.getActivityList().length());
        assertEquals(Time.getDefaultReferenceTime().subtract(MINUTE_DURATION), ActivityInstanceList.getActivityList().get(0).getStart());
        ActivityInstanceList.getActivityList().clear();

        fileName = REGRESSION_TEST_OUTPUT_DIR + "test_exclude_ongoing_act.dir";
        createTinySimWithActivityAtStartChanges(fileName, false);
        ActivityInstanceList.getActivityList().clear();
        CommandController.issueCommand("OPEN_FILE", fileName);
        assertEquals(1, ActivityInstanceList.getActivityList().length());
        assertEquals(Time.getDefaultReferenceTime(), ActivityInstanceList.getActivityList().get(0).getStart());
    }
}
