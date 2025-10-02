package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class RemoveActivityCommandTest extends BaseTest {

    @Test
    public void RemoveActivityCommand() {
        // get the list of all activities to be used by the tests
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        // RemoveActivity test 1
        // add and then remove the activity
        ActivityOne teal = new ActivityOne(new Time("2000-001T00:00:01"), new Duration("00:00:02"));
        UUID tealID = teal.getID();
        String tealIdString = tealID.toString();

        // check that the activity is in the instance list
        assertTrue(actList.containsID(tealID));

        // remove the activity and check the return status
        boolean removeActTestStatus1 = CommandController.issueCommand("REMOVE_ACTIVITY", tealIdString);
        assertTrue(removeActTestStatus1);

        // check that the activity is no longer in the list
        assertFalse(actList.containsID(tealID));
    }

    @Test
    /**
     * This tests the capability for the RemoveActivityCommand to remove
     * the children of the activity and then add them back in when undo
     * is used.
     */
    public void RemoveActivityWithChildren() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        ActivityTwo blue = new ActivityTwo(new Time("2000-001T00:00:10"), 5.0);
        blue.decompose();
        UUID activityID = blue.getID();
        List<Activity> childrenList1 = blue.getChildren();
        ActivityOne childOne1 = (ActivityOne) childrenList1.get(0);
        UUID childActivityID = childOne1.getID();

        assertTrue(actList.containsID(activityID));
        assertTrue(actList.containsID(childActivityID));

        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "REMOVE_ACTIVITY", activityID.toString());

        assertFalse(actList.containsID(activityID));
        assertFalse(actList.containsID(childActivityID));

        CommandController.issueCommand("UNDO", "");

        assertTrue(actList.containsID(activityID));
        assertTrue(actList.containsID(childActivityID));

        CommandController.issueCommand("REDO", "");

        assertFalse(actList.containsID(activityID));
        assertFalse(actList.containsID(childActivityID));
    }
}
