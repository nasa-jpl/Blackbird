package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.constraint.ConstraintDeclaration;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemodelCommandTest extends BaseTest {

    @Test
    public void RemodelCommand() {
        // Remodel test 1 - no activities in plan
        Boolean remodelTestStatus1 = CommandController.issueCommand("REMODEL", "");
        assertEquals(true, remodelTestStatus1);

        // Remodel test 2 - activities added to plan with resource usage
        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:10"), new Duration("00:00:01"));
        ActivityOne red = new ActivityOne(new Time("2000-001T00:00:05"), new Duration("00:00:02"));
        ActivityOne teal = new ActivityOne(new Time("2000-005T00:00:00"), new Duration("00:00:02"));
        ActivityOne cyan = new ActivityOne(new Time("2000-005T00:00:01"), new Duration("00:00:03"));

        Boolean remodelTestStatus2 = CommandController.issueCommand("REMODEL", "");
        assertEquals(true, remodelTestStatus2);
    }
}
