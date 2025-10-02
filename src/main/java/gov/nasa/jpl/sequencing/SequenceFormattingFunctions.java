package gov.nasa.jpl.sequencing;

public class SequenceFormattingFunctions {
    /**
     * Returns a string with the input number of indents (4 spaces).
     * @param indentCount
     * @return
     */
    public static String multipleIndents(Integer indentCount) {
        return new String(new char[indentCount]).replace("\0", "    ");
    }

    /**
     * Returns an input string with quotes around it.
     * @param inString
     * @return
     */
    public static String addQuotes(String inString) {
        return "\"" + inString + "\"";
    }
}
