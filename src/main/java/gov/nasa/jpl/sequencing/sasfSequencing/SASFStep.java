package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.Iterator;
import java.util.List;

public abstract class SASFStep {
    protected String stepType;
    protected Time stepStart;
    protected String comment = "";
    protected String ntext = "";

    protected SASFStep(String stepType, Time stepStart) {
        this.stepType = stepType;
        this.stepStart = stepStart;
    }

    /**
     * Returns the offset from the start of the request.
     * @param requestStart
     * @return
     */
    private Duration getRequestOffset(Time requestStart) {
        return stepStart.subtract(requestStart);
    }

    /**
     * Takes in a list of stepArgs and formats them by adding new lines
     * in between arguments and commas after all but the last argument.
     * @param stepArgs
     * @return
     */
    protected static String formatStepArgs(List<String> stepArgs) {
        StringBuilder sb = new StringBuilder();

        Iterator<String> argIterator = stepArgs.iterator();
        // special-case first item.  in this case, no leading comma
        if (argIterator.hasNext()) {
            sb.append(SequenceFormattingFunctions.multipleIndents(4));
            sb.append(argIterator.next());

            // process the rest
            while (argIterator.hasNext()) {
                sb.append(",\n");
                sb.append(SequenceFormattingFunctions.multipleIndents(4));
                sb.append(argIterator.next());
            }
        }

        return sb.toString();
    }

    /**
     * Writes out the header for a given step inside of a request.
     * @param stepNumber
     * @param requestStart
     * @return
     */
    public String writeStepHeader(Integer stepNumber, Time requestStart) {
        StringBuilder sb = new StringBuilder();
        sb.append(SequenceFormattingFunctions.multipleIndents(2));
        sb.append(stepType + "(" + stepNumber.toString() + ", SCHEDULED_TIME, ");
        sb.append(getRequestOffset(requestStart).toString() + ", ");
        sb.append("FROM_REQUEST_START,\n");
        if(ntext != null && !ntext.equals("")){
            sb.append(SequenceFormattingFunctions.multipleIndents(3));
            sb.append("NTEXT,\"" + ntext.replace("\"", "\\\"") + "\"");
            sb.append(",\n");
        }
        if(comment != null && !comment.equals("")){
            sb.append(SequenceFormattingFunctions.multipleIndents(3));
            sb.append("COMMENT,\"" + comment.replace("\"", "\\\"") + "\"");
            sb.append(",\n");
        }

        return sb.toString();
    }

    /**
     * Writes out the name of the step along with any arguments.
     * @return
     */
    public abstract String writeStepBody();

    /**
     * Writes out the footer for a given step inside of a request.
     * @return
     */
    public String writeStepFooter() {
        return SequenceFormattingFunctions.multipleIndents(3) + " ),\n";
    }

    public void addComment(String comment){
        this.comment = comment;
    }

    public void addNText(String ntext){
        this.ntext = ntext;
    }
}
