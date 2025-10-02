package gov.nasa.jpl.command;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ParameterDeclaration;
import gov.nasa.jpl.exampleAdaptation.AdaptationGlobals;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.*;

public class SetParameterCommandTest extends BaseTest {

    @Test
    public void execute() {
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.NumStarTrackers 3");
        assertEquals(3, AdaptationGlobals.NumStarTrackers);
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.NumStarTrackers 7");
        assertEquals(7, AdaptationGlobals.NumStarTrackers);

        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.compressionRatio 0.5");
        assertEquals(0.5, AdaptationGlobals.compressionRatio, 0.00000000001);
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.compressionRatio 16777217.0");
        assertEquals(16777217.0, AdaptationGlobals.compressionRatio, 0.0000000000000000001);

        assertEquals(new Time("2020-001T00:00:00"), AdaptationGlobals.LANDING_EPOCH);
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.LANDING_EPOCH 2022-100T00:00:00");
        assertEquals(new Time("2022-100T00:00:00"), AdaptationGlobals.LANDING_EPOCH);
    }

    @Test
    public void unExecute() {
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.NumStarTrackers 3");
        CommandController.issueCommand("SET_PARAMETER", "AdaptationGlobals.NumStarTrackers 7");
        assertEquals(7, AdaptationGlobals.NumStarTrackers);
        CommandController.issueCommand("UNDO", "");
        assertEquals(3, AdaptationGlobals.NumStarTrackers);
    }
}