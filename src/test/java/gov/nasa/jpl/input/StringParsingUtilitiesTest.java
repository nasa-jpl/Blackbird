package gov.nasa.jpl.input;

import gov.nasa.jpl.common.BaseTest;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class StringParsingUtilitiesTest extends BaseTest {
    @Test
    public void splitParamStringByChar() {
        // test 1 - comma + escaped double quotes test
        String inputString1 = "\"Key1\" = \"double quote test\", \"Key2\" = \" \\\"escape test\\\" \"";
        String[] expectedOutput1 = new String[]{
                "\"Key1\" = \"double quote test\"",
                " \"Key2\" = \" \\\"escape test\\\" \""
        };
        String[] testOutput1 = StringParsingUtilities.splitParamStringByChar(inputString1, ',');
        assertArrayEquals(expectedOutput1, testOutput1);

        // test 2 - equals + double quotes test
        String inputString2 = "\"Key1\" = \"double quote = test\"";
        String[] expectedOutput2 = new String[]{
                "\"Key1\" ",
                " \"double quote = test\""
        };
        String[] testOutput2 = StringParsingUtilities.splitParamStringByChar(inputString2, '=');
        assertArrayEquals(expectedOutput2, testOutput2);

        // test 3 - comma + brackets + double quotes test
        String inputString3 = "[1,2, \\[3, 4], [5, 6], {7, 8, [9, 10]}, 11, \"12, 13\"";
        String[] expectedOutput3 = new String[]{
                "[1,2, \\[3, 4]",
                " [5, 6]",
                " {7, 8, [9, 10]}",
                " 11",
                " \"12, 13\""
        };
        String[] testOutput3 = StringParsingUtilities.splitParamStringByChar(inputString3, ',');
        assertArrayEquals(expectedOutput3, testOutput3);

        // test 4 - comma + brackets + map parameter
        String inputString4 = "Time 2018-330T13:00:00.000, Duration 00:01:00,"
                + " Map<String, String> {\"This\" = \"is\", \"a\" = \"test\"}";
        String[] expectedOutput4 = new String[]{
                "Time 2018-330T13:00:00.000",
                " Duration 00:01:00",
                " Map<String, String> {\"This\" = \"is\", \"a\" = \"test\"}"
        };
        String[] testOutput4 = StringParsingUtilities.splitParamStringByChar(inputString4, ',');
        assertArrayEquals(expectedOutput4, testOutput4);
    }
}
