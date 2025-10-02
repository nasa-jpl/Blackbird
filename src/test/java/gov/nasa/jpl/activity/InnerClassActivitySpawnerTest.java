package gov.nasa.jpl.activity;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.command.CommandException;
import gov.nasa.jpl.command.NewActivityCommand;
import gov.nasa.jpl.common.BaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InnerClassActivitySpawnerTest extends BaseTest {

    @Test
    public void testInnerActivity()
    {
        CommandController.issueCommand("NEW_ACTIVITY", "InnerClassActivitySpawner (1970-001T00:00:00.000)");
        CommandController.issueCommand("REMODEL", "");
    }
}
