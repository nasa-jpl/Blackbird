package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.Setup;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateDictionaryCommandTest extends BaseTest {
    @Test
    public void testFiltering(){
        CreateDictionaryCommand commandAll = new CreateDictionaryCommand("test.dict.json");

        int totalNumActs = commandAll.activityTypeNames.size();
        int totalGenerics = ActivityTypeList.getActivityList().getNamesOfAllTypesWithSubsystem("generic").size();

        CreateDictionaryCommand commandInclude = new CreateDictionaryCommand("test.dict.json ACTIVITIES INCLUDE (" + String.join(" ", ActivityTypeList.getActivityList().getNamesOfAllTypesWithSubsystem("generic")) + ")");
        assertEquals(totalGenerics, commandInclude.activityTypeNames.size());

        CreateDictionaryCommand commandExclude = new CreateDictionaryCommand("test.dict.json ACTIVITIES EXCLUDE (" + String.join(" ", ActivityTypeList.getActivityList().getNamesOfAllTypesWithSubsystem("generic")) + ")");
        assertEquals(totalNumActs - totalGenerics, commandExclude.activityTypeNames.size());
    }
}