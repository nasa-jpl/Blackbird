package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeverParentCommandTest extends BaseTest {
    @Test
    public void severParentCommandTest(){
        // right now, just checking that this doesn't crash
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityTwo (2018-330T13:00:00.000, 5.0)"
        );
        String id = actList.get(0).getIDString();
        CommandController.issueCommand("SEVER_ACTIVITY", id);
        assertEquals(0, actList.get(0).getChildren().size());
    }
}