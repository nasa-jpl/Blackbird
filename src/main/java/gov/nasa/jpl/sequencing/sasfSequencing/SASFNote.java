package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;

public class SASFNote extends SASFStep {
    private String note;

    /**
     * note constructor
     * @param absoluteStartTime
     */
    public SASFNote(Time absoluteStartTime, String note) {
        super("note", absoluteStartTime);
        this.note = note;
    }

    /**
     * Writes out the body of the spawn step.
     * @return
     */
    public String writeStepBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("TEXT,\"" + note.replace("\"", "\\\"") + "\"\n");

        return sb.toString();
    }
}
