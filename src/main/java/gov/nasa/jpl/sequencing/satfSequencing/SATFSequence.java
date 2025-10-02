package gov.nasa.jpl.sequencing.satfSequencing;

import gov.nasa.jpl.sequencing.Sequence;
import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SATFSequence extends Sequence {
    private String sequenceTimeType;
    private List<String> flags;
    private String header;

    /**
     * This is the full constructor for an SATF sequence.
     * @param seqid
     * @param sequenceStartTime
     * @param sequenceTimeType
     * @param flags
     * @param header
     */
    public SATFSequence(String seqid, Time sequenceStartTime, String sequenceTimeType, List<String> flags, String header) {
        super(seqid, sequenceStartTime);
        this.sequenceTimeType = sequenceTimeType;
        this.flags = flags;
        this.header = header;
    }

    /**
     * This is the SATF sequence constructor without flags.
     * @param seqid
     * @param sequenceStartTime
     * @param sequenceTimeType
     * @param header
     */
    public SATFSequence(String seqid, Time sequenceStartTime, String sequenceTimeType, String header) {
        this(seqid, sequenceStartTime, sequenceTimeType, new ArrayList<String>(), header);
    }

    @Override
    /*
    Loops through steps in the sequence and returns the start time of the last step.
    Note that relative step start times are calculated by adding the offset to the
    previous step start time, but this will not be accurate if the step was meant to
    be from the end of the previous step.
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
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        sb.append(SequenceFormattingFunctions.multipleIndents(1));
        sb.append("ABSOLUTE_SEQUENCE(" + seqid + "," + seqid + "\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(2) + "FLAGS,");

        // add each flag with a | in between each, except for the last flag
        Iterator<String> flagIterator = flags.iterator();
        // special-case first item.  in this case, no prior |
        if (flagIterator.hasNext()) {
            sb.append(flagIterator.next());

            // process the rest
            while (flagIterator.hasNext()) {
                sb.append("|" + flagIterator.next());
            }
        }
        sb.append("\n" + SequenceFormattingFunctions.multipleIndents(2) + "STEPS,\n");

        return sb.toString();
    }

    @Override
    protected String writeSequenceFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append(SequenceFormattingFunctions.multipleIndents(2) + "end\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(1) + ")\n");
        sb.append("$$EOF");

        return sb.toString();
    }

    @Override
    public String getSequenceName() {
        return getSeqid() + ".satf";
    }
}
