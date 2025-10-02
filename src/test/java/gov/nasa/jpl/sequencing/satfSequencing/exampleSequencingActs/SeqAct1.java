package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFSequence;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class SeqAct1 extends Activity {
    private String seqid;

    public SeqAct1(Time t, Duration d, String seqid) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    public void sequence() {
        new SATFSequence(seqid, getStart(), SequenceInfo.masterSequenceType, SequenceInfo.flags, SequenceInfo.createSequenceHeader(seqid));
    }

    public void model() {
    }
}
