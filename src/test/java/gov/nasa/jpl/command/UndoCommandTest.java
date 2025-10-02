package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivitySix;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class UndoCommandTest extends BaseTest {

    @Test
    public void EditActivityUndo1() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        // test 1 - edit activity undo
        ActivityTwo blue = new ActivityTwo(new Time("2000-001T00:00:10"), 5);
        UUID activityID = blue.getID();
        assertEquals(5.0, blue.getParameterObjects()[0]); // check old value

        Boolean editActTestStatus1 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID + " (amount 10)"
        );
        Activity newBlue1 = actList.findActivityByID(activityID);
        assertEquals(true, editActTestStatus1); // check that command executed correctly
        assertEquals(10.0, newBlue1.getParameterObjects()[0]); // check new value

        Boolean undoActTestStatus1 = CommandController.issueCommand("UNDO", "");
        Activity newBlue2 = actList.findActivityByID(activityID);
        assertEquals(5.0, newBlue2.getParameterObjects()[0]); // check value again

        Boolean redoActTestStatus1 = CommandController.issueCommand("REDO", "");
        Activity newBlue3 = actList.findActivityByID(activityID);
        assertEquals(10.0, newBlue3.getParameterObjects()[0]); // check value again
    }

    @Test
    public void EditActivityUndo2() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        List<Time> timeList = new ArrayList<>();
        timeList.add(new Time("2000-001T00:00:05"));

        ActivitySix blue = new ActivitySix(new Time("2000-001T00:00:05"), new Duration("00:00:05"), timeList);
        UUID activityID = blue.getID();
        assertEquals(new Time("2000-001T00:00:05"), ((List) blue.getParameterObjects()[1]).get(0)); // check old value

        Boolean editActTestStatus1 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID + " (timeList [2018-330T12:00:00.000])"
        );
        Activity newBlue1 = actList.findActivityByID(activityID);
        Time expectedTime = new Time("2018-330T12:00:00.000");
        assertEquals(true, editActTestStatus1); // check that command executed correctly
        assertEquals(expectedTime, ((List) newBlue1.getParameterObjects()[1]).get(0)); // check new value

        Boolean undoActTestStatus1 = CommandController.issueCommand("UNDO", "");
        Activity newBlue2 = actList.findActivityByID(activityID);
        assertEquals(new Time("2000-001T00:00:05"), ((List) newBlue2.getParameterObjects()[1]).get(0)); // check value again

        Boolean redoActTestStatus1 = CommandController.issueCommand("REDO", "");
        Activity newBlue3 = actList.findActivityByID(activityID);
        assertEquals(expectedTime, ((List) newBlue3.getParameterObjects()[1]).get(0)); // check new value

        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        List<Time> relativeTimeList = new ArrayList<>();
        relativeTimeList.add(new EpochRelativeTime("test+00:10:00"));
        relativeTimeList.add(new EpochRelativeTime("test+20:00:00"));
        Boolean editActTestStatus2 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID + " (timeList [" + relativeTimeList.get(0).toString() + "," + relativeTimeList.get(1).toString() + "])"
        );
        Activity editedBlue1 = ActivityInstanceList.getActivityList().findActivityByID(newBlue1.getID());
        assertEquals("test+00:10:00.000000", ((List) editedBlue1.getParameterObjects()[1]).get(0).toString()); // check new value

    }

    @Test
    public void NewActivityUndo() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.clear();

        CommandController.issueCommand("NEW_ACTIVITY", "ActivityTwo (2018-330T13:00:00.000, 5.0)");

        assertEquals(3, actList.length());

        CommandController.issueCommand("UNDO", "");

        assertEquals(0, actList.length());

        CommandController.issueCommand("REDO", "");

        assertEquals(3, actList.length());
    }

    @Test
    public void UndoChainTest() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.clear();
        ActivityTwo blue = new ActivityTwo(new Time("2000-001T00:00:10"), 5);
        blue.decompose();
        Time dummy = new Time("2000-001T00:00:00");
        UUID activityID = blue.getID();
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "MOVE_ACTIVITY",
                activityID + " (2018-330T12:00:00.000)"
        );
        Boolean editActTestStatus2 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID + " (amount 10)"
        );
        Boolean moveActTestStatus3 = CommandController.issueCommand(
                "MOVE_ACTIVITY",
                activityID + " (2018-330T13:00:00.000)"
        );
        Boolean editActTestStatus4 = CommandController.issueCommand(
                "EDIT_ACTIVITY",
                activityID + " (amount 17.0)"
        );
        Boolean moveActTestStatus5 = CommandController.issueCommand(
                "REMOVE_ACTIVITY", activityID.toString());

        assertEquals(0, actList.length());
        CommandController.issueCommand("UNDO", "");
        CommandController.issueCommand("UNDO", "");
        CommandController.issueCommand("REDO", "");
        CommandController.issueCommand("UNDO", "");
        CommandController.issueCommand("UNDO", "");
        dummy.valueOf("2018-330T12:00:00.000");
        Activity newBlue1 = actList.findActivityByID(activityID);
        assertEquals(dummy, newBlue1.getStart());
        CommandController.issueCommand("UNDO", "");
        CommandController.issueCommand("UNDO", "");

        ActivityTwo green = new ActivityTwo(new Time("2000-001T00:00:10"), 5);
        green.decompose();
        UUID actID2 = green.getID();
        assertEquals(6, actList.length());

    }
}
