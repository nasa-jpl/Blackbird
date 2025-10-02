package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MoveActivityCommandTest extends BaseTest {

    @Test
    public void MoveActivityCommand() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        // test 1 - change start time of activity
        ActivityTwo blue = new ActivityTwo(new Time("2000-001T00:00:10"), 5.0);
        UUID activityID = blue.getID();
        assertEquals(new Time("2000-001T00:00:10"), blue.getStart()); // check old value
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "MOVE_ACTIVITY",
                activityID + " (2018-330T12:00:00.000)"
        );
        Time newStart = new Time("2000-001T00:00:10");
        newStart.valueOf("2018-330T12:00:00.000");
        Activity newBlue = actList.findActivityByID(activityID);
        assertEquals(newStart, newBlue.getStart());
    }

    @Test
    /**
     * This tests the capability for the MoveActivityCommand to move
     * the children of the activity after the start time is changed.
     */
    public void MoveActivityWithChildren() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        Time actStart = new Time("2000-001T00:00:10");
        ActivityTwo blue = new ActivityTwo(actStart, 5.0);
        blue.decompose();
        UUID activityID = blue.getID();
        assertEquals(actStart, blue.getStart());

        // check old start time of first child
        List<Activity> childrenList1 = blue.getChildren();
        ActivityOne childOne1 = (ActivityOne) childrenList1.get(0);
        assertEquals(actStart, childOne1.getStart());

        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "MOVE_ACTIVITY",
                activityID + " (2018-330T12:00:00.000)"
        );
        Time newStart = new Time("2000-001T00:00:10");
        newStart.valueOf("2018-330T12:00:00.000");
        Activity newBlue = actList.findActivityByID(activityID);
        assertEquals(newStart, newBlue.getStart());

        // check new start time of first child
        List<Activity> childrenList2 = newBlue.getChildren();
        ActivityOne childOne2 = (ActivityOne) childrenList2.get(0);
        assertEquals(newStart, childOne2.getStart());
    }
}
