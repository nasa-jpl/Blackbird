package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;

import java.util.List;

public class SASFSpawn extends SASFStep {
    private String blockName;
    private String blockArgs;
    private String engine;

    /**
     * spawn constructor, string args, with engine
     * @param absoluteStartTime
     * @param blockName
     * @param blockArgs
     * @param engine
     */
    public SASFSpawn(Time absoluteStartTime, String blockName, String blockArgs, String engine) {
        super("spawn", absoluteStartTime);
        this.blockName = blockName;
        this.blockArgs = SequenceFormattingFunctions.multipleIndents(4) + blockArgs.trim();
        this.engine = engine;
    }

    /**
     * spawn constructor, list args, with engine
     * @param absoluteStartTime
     * @param blockName
     * @param blockArgs
     * @param engine
     */
    public SASFSpawn(Time absoluteStartTime, String blockName, List<String> blockArgs, String engine) {
        this(absoluteStartTime, blockName, formatStepArgs(blockArgs), engine);
    }

    /**
     * spawn constructor, string args, with engine
     * @param absoluteStartTime
     * @param blockName
     * @param blockArgs
     */
    public SASFSpawn(Time absoluteStartTime, String blockName, String blockArgs) {
        this(absoluteStartTime, blockName, blockArgs, "-1");
    }

    /**
     * spawn constructor, list args, without engine
     * @param absoluteStartTime
     * @param blockName
     * @param blockArgs
     */
    public SASFSpawn(Time absoluteStartTime, String blockName, List<String> blockArgs) {
        this(absoluteStartTime, blockName, blockArgs, "-1");
    }

    /**
     * Writes out the body of the spawn step.
     * @return
     */
    public String writeStepBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("REQ_ENGINE_ID, " + engine + ",\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("RT_on_board_block(" + blockName);
        sb.append(SASFFunctions.formatArguments(blockArgs));

        return sb.toString();
    }
}
