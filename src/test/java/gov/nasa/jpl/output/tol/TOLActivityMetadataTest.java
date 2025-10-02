package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.common.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TOLActivityMetadataTest extends BaseTest {

    @Test
    public void toFlatTOL() {
        ActivityTypeList allActivityTypes = ActivityTypeList.getActivityList();

        Assert.assertEquals("", new TOLActivityMetadata("ActivityOne").toFlatTOL());
    }

    @Test
    public void toXML() {
        ActivityTypeList allActivityTypes = ActivityTypeList.getActivityList();

        assertEquals("", new TOLActivityMetadata("ActivityOne").toXML());

    }

    @Test
    public void toJSON() {
        ActivityTypeList allActivityTypes = ActivityTypeList.getActivityList();

        String expected = "{\n" +
                "    \"recordType\": \"activity_metadata\",\n" +
                "    \"name\": \"ActivityOne\",\n" +
                "    \"displayName\": \"ActivityOne\"\n" +
                "},\n";

        assertEquals(expected, new TOLActivityMetadata("ActivityOne").toESJSON());

    }
}