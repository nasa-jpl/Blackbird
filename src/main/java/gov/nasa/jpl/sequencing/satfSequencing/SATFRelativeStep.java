package gov.nasa.jpl.sequencing.satfSequencing;

import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.List;

public class SATFRelativeStep implements SequenceFragment {

    private String seqid;
    private String relativeTimeType;
    private Duration offset;
    private String stepType;
    private String stepName;
    private String stepArgs;
    private List<String> comments;
    private Integer engine;

    // with engine, arguments as string (spawn only)
    public SATFRelativeStep(String seqid, String relativeTimeType, Duration offset, String stepType, String stepName, String stepArgs, List<String> comments, Integer engine) {
        this.seqid = seqid;
        this.relativeTimeType = relativeTimeType;
        this.offset = offset;
        this.stepType = stepType;
        this.stepName = stepName;
        this.stepArgs = stepArgs;
        this.comments = comments;
        this.engine = engine;
        SequenceMap.getSequenceMap().addSequenceFragment(this, seqid);
    }

    // with engine, arguments as list of strings (spawn only)
    public SATFRelativeStep(String seqid, String relativeTimeType, Duration offset, String stepType, String stepName, List<String> stepArgs, List<String> comments, Integer engine) {
        this(seqid, relativeTimeType, offset, stepType, stepName, String.join(", ", stepArgs), comments, engine);
    }

    // without engine, arguments as string (everything but spawn)
    public SATFRelativeStep(String seqid, String relativeTimeType, Duration offset, String stepType, String stepName, String stepArgs, List<String> comments) {
        this(seqid, relativeTimeType, offset, stepType, stepName, stepArgs, comments, null);
    }

    // without engine, arguments as list of strings (everything but spawn)
    public SATFRelativeStep(String seqid, String relativeTimeType, Duration offset, String stepType, String stepName, List<String> stepArgs, List<String> comments) {
        this(seqid, relativeTimeType, offset, stepType, stepName, String.join(", ", stepArgs), comments);
    }

    @Override
    public String toSequenceString(Integer stepNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append(SATFStepWriter.writeRelativeStepHeader(stepType, stepNumber, offset, relativeTimeType));
        sb.append(SATFStepWriter.writeStep(stepType, stepName, stepArgs, engine, comments));

        return sb.toString();
    }

    @Override
    public Time getAbsoluteStartTime(Time latestStartTime) {
        return latestStartTime.add(offset);
    }
}
