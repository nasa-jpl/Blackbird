package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class MasterActivityFunctions {
    public static Duration calculateSubmasterDuration(Duration masterDuration) {
        Duration submasterDuration = masterDuration;
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.BootInitDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.LoadBlockLibDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.WakeupNonDiagDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.FileMgmtDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.FswDiagDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.FileCopyDuration);
        submasterDuration = submasterDuration.subtract(MasterActivityConstants.ShutdownNonDiagDuration);

        if(submasterDuration.lessThanOrEqualTo(new Duration("00:00:00"))) {
            throw new RuntimeException("Error: calculateSubmasterDuration returned negative duration.");
        }

        return submasterDuration;
    }

    public static String utcToLmst(Time utcTime) {
        Long msSinceEpoch = utcTime.subtract(MasterActivityConstants.MarsTimeOrigin).getMicroseconds();
        Double marsSecondsSinceEpoch = msSinceEpoch/MasterActivityConstants.MarsTimeScale;
        Integer sols = (int) Math.floor(marsSecondsSinceEpoch/MasterActivityConstants.msPerDay);
        Double remainder = marsSecondsSinceEpoch % MasterActivityConstants.msPerDay;
        Integer marsHours = (int) Math.floor(remainder/MasterActivityConstants.msPerHour);
        remainder = remainder % MasterActivityConstants.msPerHour;
        Integer marsMinutes = (int) Math.floor(remainder/MasterActivityConstants.msPerMinute);
        remainder = remainder % MasterActivityConstants.msPerMinute;
        Double marsSeconds = remainder/MasterActivityConstants.msPerSecond;

        String lmstString = String.format("Sol-%04dM%02d:%02d:%06.3f", sols, marsHours, marsMinutes, marsSeconds);
        return lmstString;
    }

    public static List<String> createLmstComment(Time startTime) {
        List <String> lmstCommentList = new ArrayList<>();
        lmstCommentList.add(utcToLmst(startTime));
        return lmstCommentList;
    }

    public static String masterToSubmasterSeqid(String masterSeqid) {
        return "ns" + masterSeqid.substring(2,9);
    }

    public static String createEventSeqid(String masterOrSubmasterSeqid) {
        return "evnt_" + masterOrSubmasterSeqid.substring(2,9) + "a";
    }

    public static Integer scet2sclk(Time inTime) {
        // for now, just going to return 0 for test case
        // will need to set up kernel for this to work
        return 0;
//        try {
//            return (int) CSPICE.sce2c(MasterActivityConstants.InSightSCID, inTime.toET());
//        }
//        catch (SpiceErrorException e) {
//            throw new RuntimeException("Error in scet2sclk function: " + e.toString());
//        }
    }
}
