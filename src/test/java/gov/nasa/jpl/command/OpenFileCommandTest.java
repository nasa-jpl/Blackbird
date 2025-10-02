package gov.nasa.jpl.command;

import gov.nasa.jpl.common.BaseTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class OpenFileCommandTest extends BaseTest {

    @Test
    public void testOpenFileStates() {
        OpenFileCommand command0 = new OpenFileCommand("my_file");
        assertTrue(command0.areResourcesFrozen);
        assertFalse(command0.shouldActivitiesDecompose);

        OpenFileCommand command1 = new OpenFileCommand("my_file unfrozen");
        assertFalse(command1.areResourcesFrozen);
        assertFalse(command1.shouldActivitiesDecompose);

        OpenFileCommand command2 = new OpenFileCommand("my_file decompose");
        assertTrue(command2.areResourcesFrozen);
        assertTrue(command2.shouldActivitiesDecompose);

        OpenFileCommand command3 = new OpenFileCommand("my_file unfrozen decompose");
        assertFalse(command3.areResourcesFrozen);
        assertTrue(command3.shouldActivitiesDecompose);

        OpenFileCommand command4 = new OpenFileCommand("my_file decompose unfrozen");
        assertFalse(command4.areResourcesFrozen);
        assertTrue(command4.shouldActivitiesDecompose);
    }
}