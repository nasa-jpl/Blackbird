package gov.nasa.jpl.sequencing.satfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.time.Duration;

import java.util.List;

public class SATFStepWriter {
    /**
     * This function writes out the comment lines for an SATF
     * step.
     * @param comments
     * @return
     */
    protected static String writeComments(List<String> comments) {
        StringBuilder sb = new StringBuilder();
        for(String comment: comments) {
            sb.append(SequenceFormattingFunctions.multipleIndents(4));
            sb.append("COMMENT,\"" + comment + "\",\n");
        }
        return sb.toString();
    }

    /**
     * Writes out the header line for an absolute step.
     * @param stepType
     * @param stepNumber
     * @param start
     * @return
     */
    protected static String writeAbsoluteStepHeader(String stepType, Integer stepNumber, Time start) {
        StringBuilder sb = new StringBuilder();
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append(stepType + "(" + stepNumber.toString() + ", SCHEDULED_TIME, " + start.toUTC() + ", ABSOLUTE");
        return sb.toString();
    }

    /**
     * Writes out the header line for a relative step.
     * @param stepType
     * @param stepNumber
     * @param offset
     * @param relativeTimeType
     * @return
     */
    protected static String writeRelativeStepHeader(String stepType, Integer stepNumber, Duration offset, String relativeTimeType) {
        StringBuilder sb = new StringBuilder();
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append(stepType + "(" + stepNumber.toString() + ", SCHEDULED_TIME, " + offset.toString() + ", " + relativeTimeType);
        return sb.toString();
    }

    /**
     * Writes out all of the additional lines for a given step.
     * @param stepType
     * @param stepName
     * @param stepArgs
     * @param engine
     * @param comments
     * @return
     */
    protected static String writeStep(String stepType, String stepName, String stepArgs, Integer engine, List<String> comments) {
        StringBuilder sb = new StringBuilder();

        // if this is anything but an end_if then we need to add a comma after
        // the header line for the command
        if(!stepType.equals("end_if")) {
            sb.append(",");
        }
        sb.append("\n");

        // add comments before any other lines
        sb.append(writeComments(comments));

        // add indent for step for any case except end_if
        if(!stepType.equals("end_if")) {
            sb.append(SequenceFormattingFunctions.multipleIndents(4));
        }

        // add line for step
        if(stepType.equals("command")) {
            sb.append(stepName + "(" + stepArgs + ")\n");
        }
        else if(stepType.equals("note")) {
            sb.append("TEXT,\\\"" + stepArgs + "\"\\\n");
        }

        else if(stepType.equals("call")) {
            if(stepArgs.length() > 0) {
                sb.append("RT_on_board_block(" + stepName + ", " + stepArgs + ")\n");
            }
            else {
                sb.append("RT_on_board_block(" + stepName + ")\n");
            }
        }
        else if(stepType.equals("spawn")) {
            // make sure that engine is not null
            if(engine == null) {
                throw new RuntimeException("Error in writing step for " + stepType + " " + stepName
                    + ". Engine was not provided for a spawn.");
            }

            sb.append("REQ_ENGINE_ID, " + engine.toString() + ",\n");
            sb.append(SequenceFormattingFunctions.multipleIndents(4));
            if(stepArgs.length() > 0) {
                sb.append("RT_on_board_block(" + stepName + ", " + stepArgs + ")\n");
            }
            else {
                sb.append("RT_on_board_block(" + stepName + ")\n");
            }
        }
        else if(stepType.equals("delay_by")) {
            sb.append("DURATION,\\\"" + stepArgs + "\"\\\n");
    }
        else if(stepType.equals("return")) {
            sb.append("RETURN,\\\"" + stepArgs + "\"\\\n");
        }
        else if(stepType.equals("eval")) {
            sb.append("EVAL,\\\"" + stepArgs + "\"\\\n");
        }
        else if(stepType.equals("command_dynamic")) {
            sb.append("\"" + stepName + "\"(" + stepArgs + ")\n");
        }
        else if(stepType.equals("if_cond")) {
            sb.append("INCLUSION_CONDITION,\\\"" + stepArgs + "\"\\\n");
        }
        else if(stepType.equals("end_if")) {
            // do nothing
        }
        else {
            throw new RuntimeException("Error in writing step of type " + stepType +
                " with name " + stepName + " and arguments " + stepArgs + ".\n" +
                " Step type " + stepType + " is not in the list of allowed types.");
        }

        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("),\n\n");

        return sb.toString();
    }
}
