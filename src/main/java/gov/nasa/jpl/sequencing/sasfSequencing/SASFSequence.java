package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.Sequence;
import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;

public class SASFSequence extends Sequence {
    private String header;

    /**
     * This is the full constructor for an SASF sequence.
     * @param seqid
     * @param sequenceStartTime
     * @param header
     */
    public SASFSequence(String seqid, Time sequenceStartTime, String header) {
        super(seqid, sequenceStartTime);
        this.header = header;
    }

    @Override
    /*
    Loops through steps in the sequence and returns the start time of the last step.
     */
    public Time getEnd() {
        Time latestStartTime = getStart();
        for (SequenceFragment step : fragments) {
            latestStartTime = step.getAbsoluteStartTime(latestStartTime);
        }
        return latestStartTime;
    }

    @Override
    protected String writeSequenceHeader() {
        return header;
    }

    @Override
    protected String writeSequenceFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append("$$EOF");

        return sb.toString();
    }

    @Override
    public String getSequenceName() {
        return getSeqid() + ".sasf";
    }
}
