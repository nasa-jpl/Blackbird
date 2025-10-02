package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;

import java.util.Arrays;
import java.util.List;

public class SASFCommand extends SASFStep {
    private String commandName;
    private List<String> commandArgs;

    /**
     * command constructor, string args
     * @param absoluteStartTime
     * @param commandName
     * @param commandArgs
     */
    public SASFCommand(Time absoluteStartTime, String commandName, String commandArgs) {
        this(absoluteStartTime, commandName, Arrays.asList(commandArgs));
    }

    /**
     * command constructor, list args
     * @param absoluteStartTime
     * @param commandName
     * @param commandArgs
     */
    public SASFCommand(Time absoluteStartTime, String commandName, List<String> commandArgs) {
        super("command", absoluteStartTime);
        this.commandName = commandName;
        this.commandArgs = commandArgs;
    }

    /**
     * Writes out the body of the spawn step.
     * @return
     */
    public String writeStepBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        if (commandArgs.size() > 0) {
            sb.append(commandName + "(\n" + formatStepArgs(commandArgs) + ")\n");
        }
        else {
            sb.append(commandName + "()\n");
        }

        return sb.toString();
    }
}
