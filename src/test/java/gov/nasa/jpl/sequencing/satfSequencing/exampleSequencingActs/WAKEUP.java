package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class WAKEUP extends Activity {
    String seqid;
    String wakeType;

    public WAKEUP(Time t, Duration d, String seqid, Boolean diagnostic) {
        super(t,d, seqid, diagnostic);
        setDuration(d);
        this.seqid = seqid;
        if(diagnostic) {
            this.wakeType = "\"DIAGNOSTIC\"";
        }
        else {
            this.wakeType = "\"FULL\"";
        }
    }

    public void sequence() {
        new SATFAbsoluteStep(seqid, getStart(), "spawn", "wakeup", wakeType,
                MasterActivityFunctions.createLmstComment(getStart()), 2);
    }
}