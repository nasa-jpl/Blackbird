package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class FSW_DIAG extends Activity {
    String seqid;

    public FSW_DIAG(Time t, Duration d, String seqid) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    public void sequence() {
        new SATFAbsoluteStep(seqid, getStart(), "spawn", "fsw_diag_surface", "",
                MasterActivityFunctions.createLmstComment(getStart()), 6);
    }
}