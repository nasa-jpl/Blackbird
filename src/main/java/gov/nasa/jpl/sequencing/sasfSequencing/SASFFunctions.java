package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;

public class SASFFunctions {

    /**
     * Formats the arguments for blocks and activities.
     * @param args
     * @return
     */
    public static String formatArguments(String args) {
        StringBuilder sb = new StringBuilder();

        if(args.trim().equals("")) {
            sb.append(")\n");
        }
        else {
            sb.append(",\n");
            sb.append(args);
            sb.append(")\n");
        }

        return sb.toString();
    }
}
