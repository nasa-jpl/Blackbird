package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.Sequence;
import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;

public class SASFSequence extends Sequence {
    private String header;
    private String filenameWithoutExtension;

    /**
     * This is the full constructor for an SASF sequence.
     */
    public SASFSequence(String seqid, Time sequenceStartTime, String header, String filenameWithoutExtension) {
        super(seqid, sequenceStartTime);
        this.header = header;
        this.filenameWithoutExtension = filenameWithoutExtension;
    }

    // without specifying an explicit filename, the seqid is used in the current working directory
    public SASFSequence(String seqid, Time sequenceStartTime, String header) {
        this(seqid, sequenceStartTime, header, seqid);
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
        return filenameWithoutExtension + ".sasf";
    }
}
