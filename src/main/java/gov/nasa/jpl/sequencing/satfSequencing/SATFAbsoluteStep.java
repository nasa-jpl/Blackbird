package gov.nasa.jpl.sequencing.satfSequencing;

import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Time;

import java.util.List;

public class SATFAbsoluteStep implements SequenceFragment {
    private String seqid;
    private Time absoluteStartTime;
    private String stepType;
    private String stepName;
    private String stepArgs;
    private List<String> comments;
    private Integer engine;

    // with engine, arguments as string (spawn only)
    public SATFAbsoluteStep(String seqid, Time absoluteStartTime, String stepType, String stepName, String stepArgs, List<String> comments, Integer engine) {
        this.seqid = seqid;
        this.absoluteStartTime = absoluteStartTime;
        this.stepType = stepType;
        this.stepName = stepName;
        this.stepArgs = stepArgs;
        this.comments = comments;
        this.engine = engine;
        SequenceMap.getSequenceMap().addSequenceFragment(this, seqid);
    }

    // with engine, arguments as list of strings (spawn only)
    public SATFAbsoluteStep(String seqid, Time absoluteStartTime, String stepName, String stepType, List<String> stepArgs, List<String> comments, Integer engine) {
        this(seqid, absoluteStartTime, stepType, stepName, String.join(", ", stepArgs), comments, engine);
    }

    // without engine, arguments as string (everything but spawn)
    public SATFAbsoluteStep(String seqid, Time absoluteStartTime, String stepType, String stepName, String stepArgs, List<String> comments) {
        this(seqid, absoluteStartTime, stepType, stepName, stepArgs, comments, null);
    }

    // without engine, arguments as list of strings (everything but spawn)
    public SATFAbsoluteStep(String seqid, Time absoluteStartTime, String stepType, String stepName, List<String> stepArgs, List<String> comments) {
        this(seqid, absoluteStartTime, stepType, stepName, String.join(", ", stepArgs), comments);
    }

    public String toSequenceString(Integer stepNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append(SATFStepWriter.writeAbsoluteStepHeader(stepType, stepNumber, absoluteStartTime));
        sb.append(SATFStepWriter.writeStep(stepType, stepName, stepArgs, engine, comments));

        return sb.toString();
    }

    public Time getAbsoluteStartTime(Time latestStartTime) {
        return absoluteStartTime;
    }
}
