package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.exampleAdaptation.ActivityFive;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WriteCommandTest extends BaseTest {

    private final String invalidFormatError = "WRITE command string has unexpected format: ";

    @Test
    public void emptyCommandStringTest() {
        try {
            new WriteCommand("");
            fail("WriteCommand accepted empty command string.");
        }
        catch (RuntimeException e) {
            String expectedError = invalidFormatError + "\"\"";
            assertEquals(e.getMessage(), expectedError);
        }
    }

    @Test
    public void basicWriteCommand() {
        new WriteCommand("out-tol.xml");
        new WriteCommand("out.csv");
    }

    @Test
    public void startFiltering() {
        new WriteCommand("out-tol.xml START 2018-330T00:00:00");
    }

    @Test
    public void endFiltering() {
        new WriteCommand("out-tol.xml END 2018-331T00:00:00");
    }

    @Test
    public void activityInclude() {
        ActivityOne act1 = new ActivityOne(new Time("2000-001T00:00:00"), new Duration("05:00:00"));
        ActivityFive act5 = new ActivityFive(new Time("2000-001T00:00:00"), new Duration("05:00:00"), new HashMap<>());

        WriteCommand wc = new WriteCommand("out-tol.xml ACTIVITIES INCLUDE (ActivityOne ActivityTwo ActivityFour)");
        assertEquals(1, wc.actInstList.length());
    }

    @Test
    public void activityExclude() {
        ActivityOne act1 = new ActivityOne(new Time("2000-001T00:00:00"), new Duration("05:00:00"));
        ActivityFive act5 = new ActivityFive(new Time("2000-001T00:00:00"), new Duration("05:00:00"), new HashMap<>());

        WriteCommand wc = new WriteCommand("out-tol.xml ACTIVITIES EXCLUDE (ActivityTwo ActivityFive ActivitySix ActivitySeven)");
        assertEquals(1, wc.actInstList.length());

    }

    @Test
    public void resourceInclude() {
        WriteCommand command1 = new WriteCommand("out-tol.xml RESOURCES INCLUDE (ResourceA ResourceC)");
        WriteCommand command2 = new WriteCommand("out-tol.xml RESOURCES INCLUDE (ResourceA PositionVector[x])");
        WriteCommand command3 = new WriteCommand("out-tol.xml RESOURCES INCLUDE (ResourceA ExampleBodyState)");
        WriteCommand command4 = new WriteCommand("out-tol.xml RESOURCES INCLUDE (ResourceWithSpacesInBin[my bin] ResourceA)");

        assertEquals(2,  command1.resList.length());
        assertEquals(2,  command2.resList.length());
        assertEquals(10, command3.resList.length());
        assertEquals(2,  command4.resList.length());
    }

    @Test
    public void resourceExclude() {
        WriteCommand command1 = new WriteCommand("out-tol.xml RESOURCES EXCLUDE (ResourceB)");
        assertEquals(ResourceList.getResourceList().length()-1, command1.resList.length());

        WriteCommand command2 = new WriteCommand("out-tol.xml RESOURCES EXCLUDE (ExampleBodyState)");
        assertEquals(ResourceList.getResourceList().length()-9, command2.resList.length());
    }

    @Test
    public void constraintInclude() {
        WriteCommand command1 = new WriteCommand("out-tol.xml CONSTRAINTS INCLUDE (TwoBeforeOne forbidden)");
        assertEquals(2, command1.constraintList.length());
    }

    @Test
    public void constraintExclude() {
        WriteCommand command1 = new WriteCommand("out-tol.xml CONSTRAINTS EXCLUDE (TwoBeforeOne forbidden)");
        assertEquals(0, command1.constraintList.length());
    }

    @Test
    public void allAndNone() {
        ActivityOne act1 = new ActivityOne(new Time("2000-001T00:00:00"), new Duration("05:00:00"));
        ActivityFive act5 = new ActivityFive(new Time("2000-001T00:00:00"), new Duration("05:00:00"), new HashMap<>());
        WriteCommand command1 = new WriteCommand("out-tol.xml ACTIVITIES INCLUDE (ALL) RESOURCES EXCLUDE (ALL) CONSTRAINTS INCLUDE (ALL)");
        WriteCommand command2 = new WriteCommand("out-tol.xml ACTIVITIES INCLUDE (NONE) RESOURCES EXCLUDE (NONE) CONSTRAINTS INCLUDE (NONE)");

        assertEquals(0, command1.resList.length());
        assertEquals(2, command1.actInstList.length());

        assertEquals(ResourceList.getResourceList().length(), command2.resList.length());
        assertEquals(0, command2.actInstList.length());
    }

    @Test
    public void fullFiltering() {
        new WriteCommand("out-tol.xml START 2018-330T00:00:00 END 2018-331T00:00:00 ACTIVITIES INCLUDE (ActivityOne ActivityThree) RESOURCES EXCLUDE (ResourceB) CONSTRAINTS EXCLUDE (ALL)");
    }

    @Test
    public void commandTypo() {
        String command = "out-tol.xml START 2018-330T00:00:00 ACTIVITY INCLUDE (ALL)";
        try {
            new WriteCommand(command);
            fail("WriteCommand accepted command with typo.");
        }
        catch (RuntimeException e) {
            String expectedError = invalidFormatError + "\"" + command + "\"";
            assertEquals(e.getMessage(), expectedError);
        }
    }
}
