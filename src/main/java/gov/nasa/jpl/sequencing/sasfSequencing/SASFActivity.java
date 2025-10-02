package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;

import java.util.List;

public class SASFActivity extends SASFStep {
    private String activityType;
    private String activityName;
    private String activityArgs;

    /**
     * activity constructor, string args
     * @param absoluteStartTime
     * @param activityType
     * @param activityName
     * @param activityArgs
     */
    public SASFActivity(Time absoluteStartTime, String activityType, String activityName, String activityArgs) {
        super("activity", absoluteStartTime);
        this.activityType = activityType;
        this.activityName = activityName;
        this.activityArgs = SequenceFormattingFunctions.multipleIndents(4) + activityArgs.trim();
    }

    /**
     * activity constructor, list args
     * @param absoluteStartTime
     * @param activityType
     * @param activityName
     * @param activityArgs
     */
    public SASFActivity(Time absoluteStartTime, String activityType, String activityName, List<String> activityArgs) {
        this(absoluteStartTime, activityType, activityName, formatStepArgs(activityArgs));
    }

    /**
     * Writes out the body of the spawn step.
     * @return
     */
    public String writeStepBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append(activityType + "(" + activityName);
        sb.append(SASFFunctions.formatArguments(activityArgs));

        return sb.toString();
    }
}
