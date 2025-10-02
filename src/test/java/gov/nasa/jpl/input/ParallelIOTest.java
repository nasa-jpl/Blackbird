package gov.nasa.jpl.input;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static gov.nasa.jpl.input.XMLTOLHistoryReaderTest.readInHistoryOfActivitiesAndResource;

public class ParallelIOTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        EpochRelativeTime.addEpoch("a", Time.getDefaultReferenceTime());

        ModelingEngine.getEngine().setTime(Time.getDefaultReferenceTime());
    }

    @Test
    public void readInDirectoryHistory() {
        try {
            readInHistoryOfActivitiesAndResource("history_unit_test.dir", true);
        }
        finally {
            for (Resource res: ResourceList.getResourceList().getListOfAllResources()) {
                res.setFrozen(false);
            }
        }
    }

    @Test
    public void testEmptyDirectories(){
        new File("test.dir/activities").mkdirs();
        new File("test.dir/resources").mkdirs();
        CommandController.issueCommand("OPEN_FILE", "test.dir");
        new File("test.dir/activities").delete();
        new File("test.dir/resources").delete();

        new File("test.dir/activities").mkdirs();
        CommandController.issueCommand("OPEN_FILE", "test.dir");
        new File("test.dir/activities").delete();

        new File("test.dir/resources").mkdirs();
        CommandController.issueCommand("OPEN_FILE", "test.dir");
        new File("test.dir/resources").delete();

        CommandController.issueCommand("OPEN_FILE", "test.dir");
    }
}
