package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.sequencing.sasfSequencing.exampleSequencingActs.ScheduleMRORequests;
import gov.nasa.jpl.spice.Spice;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SASFSequenceTest extends BaseTest {

    @Before
    public void setUp(){
        ModelingEngine.getEngine().setTime(Time.getDefaultReferenceTime());

        Time.setDefaultOutputPrecision(6);
    }

    @Test
    public void sasfSequenceTest1() {
        ScheduleMRORequests scheduler1 = new ScheduleMRORequests(new Time("2019-001T00:00:00"),
                new Duration("24:00:00"), "rm352");
        scheduler1.decompose();
        Boolean editActTestStatus1 = CommandController.issueCommand(
                "SEQUENCE", "START 2019-001T00:00:00 END 2019-003T00:00:00"
        );
    }

    @Test
    public void testEpochRelativeTime(){
        EpochRelativeTime.addEpoch("PSB_DLTERM_001", new Time("2026-080T12:00:00"));
        EpochRelativeTime reqStart = new EpochRelativeTime("PSB_DLTERM_001+00:10:00");

        SASFCommand LVLHcommand = new SASFCommand(reqStart, "GNC_ATC_LVLH_DELTA_ASSIGN", Arrays.asList("2", "3"));

        SASFRequest relativeRequest = new SASFRequest("", reqStart, "TURN_OFF_NADIR_001", "\"VC-1\"", "\"NO_KEY\"", LVLHcommand);

        String expectedOut =
                "request(TURN_OFF_NADIR_001, REQUESTOR, \"\",\n" +
                "            START_TIME, PSB_DLTERM_001+00:10:00.000000,\n" +
                "            PROCESSOR, \"VC-1\",\n" +
                "            KEY, \"NO_KEY\")\n" +
                "        command(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            GNC_ATC_LVLH_DELTA_ASSIGN(\n" +
                "                2,\n" +
                "                3)\n" +
                "             ),\n"+
                "    end;\n";

        assertEquals(expectedOut, relativeRequest.toSequenceString(0));
    }

    @Test
    public void SASFActivityTest(){
        SASFActivity testAct = new SASFActivity(new Time("2020-245T06:58:14.000"), "DSN", "TXR_SUP", Arrays.asList("\"ALL\"", "2020-245T08:00:00.000000"));

        String expectedOut =
                "            DSN(TXR_SUP,\n" +
                "                \"ALL\",\n" +
                "                2020-245T08:00:00.000000)\n";

        assertEquals(expectedOut, testAct.writeStepBody());
    }

    @Test
    public void SASFCommandTest(){
        List<SASFStep> sasfSteps = new ArrayList<>();
        List<String> argList = new ArrayList<>();

        argList.add("\"/eng/seq/ep_conf_thrust_start\"");
        argList.add("-1");
        argList.add("1");
        argList.add("P1TA-P2TD");
        argList.add("A");
        argList.add("180");
        argList.add("true");
        argList.add("2400");

        SASFCommand loadCommand = new SASFCommand(new Time("2024-188T06:00:18.184000") , "USER_SEQ_VAR_SEQ_LOAD", argList);
        SASFCommand execCommand = new SASFCommand(new Time("2024-188T06:01:18.184000") , "USER_SEQ_EXECUTE", "ep_conf_thrust_start");

        String desiredLoadString =
                "            USER_SEQ_VAR_SEQ_LOAD(\n" +
                "                \"/eng/seq/ep_conf_thrust_start\",\n" +
                "                -1,\n" +
                "                1,\n" +
                "                P1TA-P2TD,\n" +
                "                A,\n" +
                "                180,\n" +
                "                true,\n" +
                "                2400)\n";

        String desiredExecString =
                "            USER_SEQ_EXECUTE(\n" +
                "                ep_conf_thrust_start)\n";

        assertEquals(desiredLoadString, loadCommand.writeStepBody());
        assertEquals(desiredExecString, execCommand.writeStepBody());

        // make sure these produce the right thing inside and outside of a request
        sasfSteps.add(loadCommand);
        sasfSteps.add(execCommand);

        SASFRequest thrustStart = new SASFRequest("test", new Time("2024-188T05:59:18.184000"), "thrust_start_0", "\"VC2A\"", "\"NO_KEY\"", sasfSteps);

        String desiredRequestString =
                "request(thrust_start_0, REQUESTOR, \"\",\n" +
                        "            START_TIME, 2024-188T05:59:18.184000,\n" +
                        "            PROCESSOR, \"VC2A\",\n" +
                        "            KEY, \"NO_KEY\")\n" +
                        "        command(1, SCHEDULED_TIME, 00:01:00.000000, FROM_REQUEST_START,\n" +
                        "            USER_SEQ_VAR_SEQ_LOAD(\n" +
                        "                \"/eng/seq/ep_conf_thrust_start\",\n" +
                        "                -1,\n" +
                        "                1,\n" +
                        "                P1TA-P2TD,\n" +
                        "                A,\n" +
                        "                180,\n" +
                        "                true,\n" +
                        "                2400)\n" +
                        "             ),\n" +
                        "        command(2, SCHEDULED_TIME, 00:02:00.000000, FROM_REQUEST_START,\n" +
                        "            USER_SEQ_EXECUTE(\n" +
                        "                ep_conf_thrust_start)\n" +
                        "             ),\n" +
                        "    end;\n";

        assertEquals(desiredRequestString, thrustStart.toSequenceString(0));

        SASFRequest thrustStartWithRequestor = new SASFRequest("test", new Time("2024-188T05:59:18.184000"), "thrust_start_0", "\"VC2A\"", "\"NO_KEY\"", "", "\"SEQ\"", sasfSteps);
        String desiredRequestStringWithRequestor =
                "request(thrust_start_0, REQUESTOR, \"SEQ\",\n" +
                        "            START_TIME, 2024-188T05:59:18.184000,\n" +
                        "            PROCESSOR, \"VC2A\",\n" +
                        "            KEY, \"NO_KEY\")\n" +
                        "        command(1, SCHEDULED_TIME, 00:01:00.000000, FROM_REQUEST_START,\n" +
                        "            USER_SEQ_VAR_SEQ_LOAD(\n" +
                        "                \"/eng/seq/ep_conf_thrust_start\",\n" +
                        "                -1,\n" +
                        "                1,\n" +
                        "                P1TA-P2TD,\n" +
                        "                A,\n" +
                        "                180,\n" +
                        "                true,\n" +
                        "                2400)\n" +
                        "             ),\n" +
                        "        command(2, SCHEDULED_TIME, 00:02:00.000000, FROM_REQUEST_START,\n" +
                        "            USER_SEQ_EXECUTE(\n" +
                        "                ep_conf_thrust_start)\n" +
                        "             ),\n" +
                        "    end;\n";

        assertEquals(desiredRequestStringWithRequestor, thrustStartWithRequestor.toSequenceString(0));

        SASFCommand throttleCommand = new SASFCommand(new Time("2024-188T18:00:28.184000"), "EP_THRUSTER_THROTTLE", Arrays.asList("180", "A"));
        SASFCommand assignCommand   = new SASFCommand(new Time("2024-188T18:05:28.184000"), "GNC_THRUST_LEVEL_ASSIGN", "180");

        String desiredThrottleString =
                "            EP_THRUSTER_THROTTLE(\n" +
                "                180,\n" +
                "                A)\n";

        String desiredAssignString =
                "            GNC_THRUST_LEVEL_ASSIGN(\n" +
                "                180)\n";

        assertEquals(desiredThrottleString, throttleCommand.writeStepBody());
        assertEquals(desiredAssignString, assignCommand.writeStepBody());

        List<SASFStep> sasfCommands = new ArrayList<>();
        sasfCommands.add(throttleCommand);
        sasfCommands.add(assignCommand);

        SASFRequest throttleChange = new SASFRequest("test2", new Time("2024-188T17:59:28.184000"), "set_throttle_level2", "\"VC2A\"", "\"NO_KEY\"", sasfCommands);

        String desiredRequestString2 =
                "request(set_throttle_level2, REQUESTOR, \"\",\n" +
                "            START_TIME, 2024-188T17:59:28.184000,\n" +
                "            PROCESSOR, \"VC2A\",\n" +
                "            KEY, \"NO_KEY\")\n" +
                "        command(1, SCHEDULED_TIME, 00:01:00.000000, FROM_REQUEST_START,\n" +
                "            EP_THRUSTER_THROTTLE(\n" +
                "                180,\n" +
                "                A)\n" +
                "             ),\n" +
                "        command(2, SCHEDULED_TIME, 00:06:00.000000, FROM_REQUEST_START,\n" +
                "            GNC_THRUST_LEVEL_ASSIGN(\n" +
                "                180)\n" +
                "             ),\n" +
                "    end;\n";

        assertEquals(desiredRequestString2, throttleChange.toSequenceString(0));

        SASFCommand noOpCommaand = new SASFCommand(new Time("2024-188T06:00:18.184000") , "CMD_NO_OP", Collections.emptyList());
        SASFRequest noOpRequest = new SASFRequest("test3", new Time("2024-188T05:59:18.184000"), "cmd_no_op", "\"VC2A\"", "\"NO_KEY\"", Collections.singletonList(noOpCommaand));

        String desiredRequestString3 =
                "request(cmd_no_op, REQUESTOR, \"\",\n" +
                        "            START_TIME, 2024-188T05:59:18.184000,\n" +
                        "            PROCESSOR, \"VC2A\",\n" +
                        "            KEY, \"NO_KEY\")\n" +
                        "        command(1, SCHEDULED_TIME, 00:01:00.000000, FROM_REQUEST_START,\n" +
                        "            CMD_NO_OP()\n" +
                        "             ),\n" +
                        "    end;\n";

        assertEquals(desiredRequestString3, noOpRequest.toSequenceString(0));

    }

    @Test
    public void SASFGroundTest(){
        SASFGround test = new SASFGround(Time.getDefaultReferenceTime(), "SOCCB", Arrays.asList("OCCDUR","ORBIT_NO"));

        String expectedOut =
                "            SOCCB(\n" +
                "                OCCDUR,\n" +
                "                ORBIT_NO)\n";

        assertEquals(expectedOut, test.writeStepBody());

        SASFRequest testReq = new SASFRequest("my_seqid", Time.getDefaultReferenceTime(), "test", "\"VC2A\"", "\"NO_KEY\"", test);

        String expectedRequest =
                "request(test, REQUESTOR, \"\",\n" +
                "            START_TIME, 2000-001T00:00:00.000000,\n" +
                "            PROCESSOR, \"VC2A\",\n" +
                "            KEY, \"NO_KEY\")\n" +
                "        ground(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            SOCCB(\n" +
                "                OCCDUR,\n" +
                "                ORBIT_NO)\n"+
                "             ),\n"+
                "    end;\n";

        assertEquals(expectedRequest, testReq.toSequenceString(0));
    }

    @Test
    public void SASFNoteTest(){
        SASFNote test = new SASFNote(Time.getDefaultReferenceTime().add(new Duration("00:40:00")), "NAV_VSA_OpNav_002: Turn \"complete\"");

        String expectedOut = "            TEXT,\"NAV_VSA_OpNav_002: Turn \\\"complete\\\"\"\n";

        assertEquals(expectedOut, test.writeStepBody());

        SASFRequest testReq = new SASFRequest("my_seqid", Time.getDefaultReferenceTime(), "test", "\"VC2A\"", "\"NO_KEY\"", test);

        String expectedRequest =
                "request(test, REQUESTOR, \"\",\n" +
                "            START_TIME, 2000-001T00:00:00.000000,\n" +
                "            PROCESSOR, \"VC2A\",\n" +
                "            KEY, \"NO_KEY\")\n" +
                "        note(1, SCHEDULED_TIME, 00:40:00.000000, FROM_REQUEST_START,\n" +
                "            TEXT,\"NAV_VSA_OpNav_002: Turn \\\"complete\\\"\"\n" +
                "             ),\n" +
                "    end;\n";

        assertEquals(expectedRequest, testReq.toSequenceString(0));
    }

    @Test
    public void SASFSpawnTest(){
        SASFSpawn test = new SASFSpawn(new Time("2019-001T00:00:00.000000"), "hga_mgr", Arrays.asList("\"track\"", "0", "0", "0", "0", "TRUE", "00:00:00", "00:11:00.000"));

        String expectedOut =
                "            REQ_ENGINE_ID, -1,\n" +
                "            RT_on_board_block(hga_mgr,\n" +
                "                \"track\",\n" +
                "                0,\n" +
                "                0,\n" +
                "                0,\n" +
                "                0,\n" +
                "                TRUE,\n" +
                "                00:00:00,\n" +
                "                00:11:00.000)\n";

        assertEquals(expectedOut, test.writeStepBody());
    }

    @Test
    public void SASFStepHeaderTest(){
        SASFGround commentTest = new SASFGround(Time.getDefaultReferenceTime(), "SOCCB", Arrays.asList("OCCDUR","ORBIT_NO"));
        commentTest.addComment("The first ground event shall be at the SCHEDULE_TIME");

        String expectedCommentOut =
                "        ground(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            COMMENT,\"The first ground event shall be at the SCHEDULE_TIME\",\n";

        assertEquals(expectedCommentOut, commentTest.writeStepHeader(1, Time.getDefaultReferenceTime()));


        SASFCommand nTextTest = new SASFCommand(Time.getDefaultReferenceTime(), "GNC_ATC_LVLH_DELTA_ASSIGN", Arrays.asList("2", "3"));
        nTextTest.addNText("TURN_NADIR_001: Select Attitude and Turn Spec");

        String expectedNTextOut =
                "        command(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            NTEXT,\"TURN_NADIR_001: Select Attitude and Turn Spec\",\n";

        assertEquals(expectedNTextOut, nTextTest.writeStepHeader(1, Time.getDefaultReferenceTime()));

        nTextTest.addComment("We have a comment here too \"\"");

        String expectedBothOut =
                "        command(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            NTEXT,\"TURN_NADIR_001: Select Attitude and Turn Spec\",\n" +
                "            COMMENT,\"We have a comment here too \\\"\\\"\",\n";

        assertEquals(expectedBothOut, nTextTest.writeStepHeader(1, Time.getDefaultReferenceTime()));

        SASFRequest fullRequest = new SASFRequest("", Time.getDefaultReferenceTime(), "TURN_OFF_NADIR_001", "\"VC-1\"", "\"NO_KEY\"", nTextTest);

        String requestOut =
                "request(TURN_OFF_NADIR_001, REQUESTOR, \"\",\n" +
                "            START_TIME, 2000-001T00:00:00.000000,\n" +
                "            PROCESSOR, \"VC-1\",\n" +
                "            KEY, \"NO_KEY\")\n" +
                "        command(1, SCHEDULED_TIME, 00:00:00.000000, FROM_REQUEST_START,\n" +
                "            NTEXT,\"TURN_NADIR_001: Select Attitude and Turn Spec\",\n" +
                "            COMMENT,\"We have a comment here too \\\"\\\"\",\n" +
                "            GNC_ATC_LVLH_DELTA_ASSIGN(\n" +
                "                2,\n" +
                "                3)\n" +
                "             ),\n"+
                "    end;\n";

        assertEquals(requestOut, fullRequest.toSequenceString(0));
    }
}
