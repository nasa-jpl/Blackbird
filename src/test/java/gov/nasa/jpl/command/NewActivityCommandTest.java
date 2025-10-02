package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewActivityCommandTest extends BaseTest {

    @Test
    public void NewActivityCommand() {
        // NewActivity test 1 - custom data types
        Boolean newActTestStatus1 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityOne (2018-330T13:00:00.000, 00:01:00)"
        );
        assertEquals(true, newActTestStatus1);

        // NewActivity test 2 - list<String> data type
        Boolean newActTestStatus2 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityThree (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " [\"This\", \"is\", \"a\", \"test\"])"
        );
        assertEquals(true, newActTestStatus2);

        // NewActivity test 3 - Map<String, String> data type
        Boolean newActTestStatus3 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityFour (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " {\"This\" = \"is\", \"a\" = \"test\"})"
        );
        assertEquals(true, newActTestStatus3);

        // NewActivity test 4 - nested Map/list data type
        Boolean newActTestStatus4 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityFive (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " {\"This\" = [\"is\"], \"a\" = [\"t\", \"e\", \"s\", \"t\"]})"
        );
        assertEquals(true, newActTestStatus4);

        // NewActivity test 5 - empty map parameter
        Boolean newActTestStatus5 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityFour (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " {})"
        );
        assertEquals(true, newActTestStatus5);

        // NewActivity test 6 - empty list parameter
        Boolean newActTestStatus6 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityThree (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " [])"
        );
        assertEquals(true, newActTestStatus6);

        // NewActivity test 7 - empty list and empty string
        Boolean newActTestStatus7 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityFive (2018-330T13:00:00.000,"
                        + " 00:01:00,"
                        + " {\"\" = [], \"a\" = [\"\", \"\", \" \", \"\"]})"
        );
        assertEquals(true, newActTestStatus7);

        // NewActivity test 8 - epoch relative time
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        Boolean newActTestStatus8 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityOne (test+00:05:00, 00:01:00)"
        );
        assertEquals(true, newActTestStatus8);

        // NewActivity test 9 - epoch relative time parameter
        Boolean newActTestStatus9 = CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityNine (test+00:05:00,test+00:10:00)"
        );
        assertEquals(true, newActTestStatus9);
    }
}
