package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;

import java.util.Arrays;
import java.util.List;

public class SASFGround extends SASFStep {
    private String groundName;
    private List<String> groundArgs;

    /**
     * ground constructor, string args
     * @param absoluteStartTime
     * @param groundName
     * @param groundArgs
     */
    public SASFGround(Time absoluteStartTime, String groundName, String groundArgs) {
        this(absoluteStartTime, groundName, Arrays.asList(groundArgs));
    }

    /**
     * ground constructor, list args
     * @param absoluteStartTime
     * @param groundName
     * @param groundArgs
     */
    public SASFGround(Time absoluteStartTime, String groundName, List<String> groundArgs) {
        super("ground", absoluteStartTime);
        this.groundName = groundName;
        this.groundArgs = groundArgs;
    }

    /**
     * Writes out the body of the spawn step.
     * @return
     */
    public String writeStepBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        if (groundArgs.size() > 0) {
            sb.append(groundName + "(\n" + formatStepArgs(groundArgs) + ")\n");
        }
        else {
            sb.append(groundName + "()\n");
        }
        return sb.toString();
    }
}
