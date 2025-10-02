package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RedecomposeCommandTest extends BaseTest {

    @Test
    public void redecomposeTest(){
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityTwo (2018-330T13:00:00.000, 5.0)"
        );
        String id = actList.get(0).getIDString();
        actList.get(0).deleteChildren();
        assertEquals(1, actList.length());
        CommandController.issueCommand("REDECOMPOSE", id);
        assertEquals(3, actList.length());
    }
}