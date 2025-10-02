package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityThree;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class EditActivityCommandTest extends BaseTest {

    @Test
    public void EditActivityCommand() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        // test 1 - edit individual parameter
        ActivityTwo blue = new ActivityTwo(new Time("2000-001T00:00:10"), 5);
        UUID activityID = blue.getID();
        assertEquals(5.0, blue.getParameterObjects()[0]); // check old value

        Boolean editActTestStatus1 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID.toString() + " (amount 10)"
        );
        Activity newBlue1 = actList.findActivityByID(activityID);
        assertEquals(true, editActTestStatus1); // check that command executed correctly
        assertEquals(10.0, newBlue1.getParameterObjects()[0]); // check new value

        // test 2 - multiple parameters
        Time timeParam = new Time("2000-001T00:01:40");
        Duration durParam = new Duration("00:00:10");
        List<String> listParam = Arrays.asList("test1", "test2");

        ActivityThree green = new ActivityThree(timeParam, durParam, listParam);
        UUID activityID2 = green.getID();
        Boolean editActTestStatus2 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID2 + " (d 00:01:00,"
                        + " stringList [\"This\", \"is\", \"a\", \"test\"])"
        );
        Activity newGreen = (Activity) actList.findActivityByID(activityID2);
        assertEquals(true, editActTestStatus2);

        Duration expectedDurOutput2 = new Duration("00:00:01");
        expectedDurOutput2.valueOf("00:01:00");
        List<String> expectedListOutput2 = Arrays.asList("This", "is", "a", "test");
        assertEquals(expectedDurOutput2, newGreen.getParameterObjects()[0]);
        assertEquals(expectedListOutput2, newGreen.getParameterObjects()[1]);

        // test 3 - multiple params, only edit one
        ActivityThree red = new ActivityThree(timeParam, durParam, listParam);
        UUID activityID3 = red.getID();
        Boolean editActTestStatus3 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID3 + " (stringList [\"This\", \"is\", \"a\", \"test\"])"
        );
        Activity newRed = actList.findActivityByID(activityID3);
        assertEquals(durParam, newRed.getParameterObjects()[0]);
        assertEquals(expectedListOutput2, newRed.getParameterObjects()[1]);

        // test 4 - multiple params, different order
        ActivityThree orange = new ActivityThree(timeParam, durParam, listParam);
        UUID activityID4 = green.getID();
        Boolean editActTestStatus4 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID4 + " ("
                        + "stringList [\"This\", \"is\", \"a\", \"test\"],"
                        + " d 00:01:00)"
        );
        Activity newOrange = (Activity) actList.findActivityByID(activityID4);
        assertEquals(true, editActTestStatus4);
        assertEquals(expectedDurOutput2, newOrange.getParameterObjects()[0]);
        assertEquals(expectedListOutput2, newOrange.getParameterObjects()[1]);
    }
}
