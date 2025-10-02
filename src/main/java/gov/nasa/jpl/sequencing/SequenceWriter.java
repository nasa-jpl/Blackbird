package gov.nasa.jpl.sequencing;

import gov.nasa.jpl.time.Time;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SequenceWriter {
    protected static PrintWriter writer;

    public static void createFile(String name) {
        try {
            writer = new PrintWriter(name, "UTF-8");
        }
        catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void closeFile() {
        writer.close();
    }

    /**
     * Loops through the sequence list and writes each sequence which starts
     * within the start time and end time.
     * @param startTime - first allowable sequence time
     * @param endTime - last allowable sequence time
     */
    public static void writeSequences(Time startTime, Time endTime) {
        // write each sequence which is between the start and end time
        for (Sequence sequence : SequenceMap.getSequenceMap().getAllSequences()) {
            if (sequence.getStart().greaterThanOrEqualTo(startTime) && sequence.getEnd().lessThanOrEqualTo(endTime)) {
                createFile(sequence.getSequenceName());
                writer.print(sequence.writeSequenceHeader());
                writer.print(sequence.writeSequenceBody());
                writer.print(sequence.writeSequenceFooter());
                closeFile();
            }
        }
    }
}
