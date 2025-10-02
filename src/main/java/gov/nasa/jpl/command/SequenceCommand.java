package gov.nasa.jpl.command;

import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.sequencing.SequenceEngine;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.sequencing.SequenceWriter;
import gov.nasa.jpl.time.Time;

import java.util.regex.Matcher;

public class SequenceCommand implements Command {
    Time startTime;
    Time endTime;

    /**
     * Extracts and stores the start/end times for writing sequences.
     * @param commandString
     */
    public SequenceCommand(String commandString) {
        Matcher startMatch = RegexUtilities.WRITE_START_PATTERN.matcher(commandString);
        Matcher endMatch = RegexUtilities.WRITE_END_PATTERN.matcher(commandString);

        if (startMatch.find()) {
            startTime = (Time) ReflectionUtilities.returnValueOf(ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE, startMatch.group("time"), true);
        }
        if (endMatch.find()) {
            endTime = (Time) ReflectionUtilities.returnValueOf(ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE, endMatch.group("time"), true);
        }
    }

    /**
     * Writes out all of the sequences which have steps within the start and end times.
     * @throws CommandException
     */
    @Override
    public void execute() throws CommandException {
        SequenceEngine.getEngine().sequence();
        // SequenceWriter has static reference to SequenceMap.getSequenceMap()
        SequenceWriter.writeSequences(startTime, endTime);
    }

    /**
     * Does not do anything. Only way I could think to implement
     * an undo of a sequence command would be to delete all of the
     * sequence files which were created, but I don't think we want
     * to do that.
     */
    @Override
    public void unExecute() throws CommandException {

    }
}
