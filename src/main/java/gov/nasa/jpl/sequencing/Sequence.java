package gov.nasa.jpl.sequencing;

import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public abstract class Sequence {
    protected String seqid;
    protected Time sequenceStartTime;
    protected List<SequenceFragment> fragments;

    public Sequence(String seqid, Time sequenceStartTime){
        this.seqid = seqid;
        this.sequenceStartTime = sequenceStartTime;
        fragments = new ArrayList<>();
        SequenceMap.getSequenceMap().addSequence(this);
    }

    public String getSeqid() {
        return seqid;
    }

    /*
    Returns the name of the sequence with the extension (.sasf, .satf, etc.)
     */
    public abstract String getSequenceName();


    public Time getStart() {
        return sequenceStartTime;
    }

    public abstract Time getEnd();

    protected abstract String writeSequenceHeader();

    protected abstract String writeSequenceFooter();

    /*
    Writes sequence by writing out the header, the steps and then the footer.
     */
    public String writeSequenceBody() {
        StringBuilder sb = new StringBuilder();

        int stepNum = 1;
        for (SequenceFragment step : fragments) {
            sb.append(step.toSequenceString(stepNum));
            stepNum++;
        }
        return sb.toString();
    }

    public void addSequenceFragment(SequenceFragment seqFrag) {
        fragments.add(seqFrag);
    }
}
