package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class LOAD_BLOCK_LIB extends Activity {
    String seqid;

    public LOAD_BLOCK_LIB(Time t, Duration d, String seqid) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    public void sequence() {
        Time currentStart = getStart();
        new SATFAbsoluteStep(seqid, currentStart, "command", "VM_ABORT_MODE",
                "4, \"ENABLE\"", MasterActivityFunctions.createLmstComment(currentStart));
        currentStart = currentStart.add(new Duration("00:00:01"));

        new SATFAbsoluteStep(seqid, currentStart, "command", "VM_UNLOAD_FILE",
                "\"d:/seq/fpe_cfg_landed.mod\"", MasterActivityFunctions.createLmstComment(currentStart));
        currentStart = currentStart.add(new Duration("00:00:01"));

        new SATFAbsoluteStep(seqid, currentStart, "command", "VM_LOAD",
                "\"d:/seq/load_surface_block_libs.mod\"", MasterActivityFunctions.createLmstComment(currentStart));
    }
}