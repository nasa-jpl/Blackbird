package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class SHUTDOWN extends Activity {
    String seqid;
    String shutdownBoolString;

    public SHUTDOWN(Time t, Duration d, String seqid, Boolean diagnostic) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
        this.shutdownBoolString = Boolean.toString(diagnostic).toUpperCase();
    }

    public void sequence() {
        new SATFAbsoluteStep(seqid, getStart(), "call", "shutdown", shutdownBoolString,
                MasterActivityFunctions.createLmstComment(getStart()));
    }
}