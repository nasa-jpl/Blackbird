package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;

public class SeqAct2 extends Activity {
    private String seqid;

    public SeqAct2(Time t, Duration d, String seqid) {
        super(t,d,seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    public void sequence() {
        new SATFAbsoluteStep(seqid, getStart(), "call", "testBlock", "", new ArrayList<>());
    }

    public void model() {
    }
}
