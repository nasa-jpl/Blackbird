package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
}
