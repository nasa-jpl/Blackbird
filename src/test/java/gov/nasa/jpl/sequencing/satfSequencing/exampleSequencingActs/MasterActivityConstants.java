package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class MasterActivityConstants {
    // durations for master activities
    public static final Duration BootInitDuration = new Duration("00:02:30");
    public static final Duration LoadBlockLibDuration = new Duration("00:00:43");
    public static final Duration WakeupDiagDuration = new Duration("00:04:10");
    public static final Duration WakeupNonDiagDuration = new Duration("00:07:30");
    public static final Duration FileMgmtDuration = new Duration("00:00:04");
    public static final Duration FileCopyDuration = new Duration("00:00:01");
    public static final Duration FswDiagDuration = new Duration("00:01:00");
    public static final Duration ShutdownDiagDuration = new Duration("00:05:00");
    public static final Duration ShutdownNonDiagDuration = new Duration("00:08:00");
    public static final Duration SubmasterDiagDuration = new Duration("00:01:00");
    public static final Duration GvWakeupTimeOffset = new Duration("00:00:30");
    public static final Duration RunoutShutdownMargin = new Duration("00:10:00");
    public static final Duration MinProcDur = new Duration("00:10:00");

    // constants needed for LMST conversion
    public static final Time     MarsTimeOrigin = new Time("2018-330T05:10:50.336");
    public static final Double   MarsTimeScale = 1.02749125;
    public static final Integer  msPerDay = 86400000;
    public static final Integer  msPerHour = 3600000;
    public static final Integer  msPerMinute = 60000;
    public static final Integer  msPerSecond = 60000;

    // spacecraft info
    public static final Integer InSightSCID = 189;
}
