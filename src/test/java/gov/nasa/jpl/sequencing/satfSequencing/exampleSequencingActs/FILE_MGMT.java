package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class FILE_MGMT extends Activity {
    String seqid;
    Time nextWakeStart;

    public FILE_MGMT(Time t, Duration d, String seqid, Time nextWakeStart) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
        this.nextWakeStart = nextWakeStart;
    }

    public void sequence() {
        Time currentStart = getStart();
        new SATFAbsoluteStep(seqid, currentStart, "command", "FILE_DELETE",
                "\"d:/seq/" + seqid + ".mod\"", MasterActivityFunctions.createLmstComment(currentStart));
        currentStart = currentStart.add(new Duration("00:00:01"));

        new SATFAbsoluteStep(seqid, currentStart, "command", "FILE_DELETE",
                "\"f2:/seq/fpe_wakeup.mod\"", MasterActivityFunctions.createLmstComment(currentStart));
        currentStart = currentStart.add(new Duration("00:00:01"));

        List<String> vmGvSetUintArgs = new ArrayList<>();
        vmGvSetUintArgs.add("\"GV_WAKEUP_TIME\"");
        vmGvSetUintArgs.add(MasterActivityFunctions.scet2sclk(currentStart).toString());
        new SATFAbsoluteStep(seqid, currentStart, "command", "VM_GV_SET_UINT",
                vmGvSetUintArgs, MasterActivityFunctions.createLmstComment(currentStart));
    }
}