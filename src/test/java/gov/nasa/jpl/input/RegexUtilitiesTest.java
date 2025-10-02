package gov.nasa.jpl.input;

import gov.nasa.jpl.common.BaseTest;
import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertArrayEquals;

public class RegexUtilitiesTest extends BaseTest {
    @Test
    public void ListMapTypeValueParsing() {
        // test 1 - activity parameters with maps
        String testInput1 = "Map<String, String> {\"This\" = \"is\", \"a\" = \"test\"}";
        String[] expectedOutput1 = new String[]{
                "Map<String, String>",
                "String, String",
                "\"This\" = \"is\", \"a\" = \"test\""
        };
        Matcher match = RegexUtilities.MAP_TYPE_VALUE_PATTERN.matcher(testInput1);
        match.matches();
        String[] testOutput1 = new String[]{
                match.group("type"), match.group("genericTypes"), match.group("contents")
        };
        assertArrayEquals(expectedOutput1, testOutput1);


        // test 2 - nested maps
        String testInput2 = "Map<String, Map<String, List<Integer>>> {\"This\" = {\"is\" = [\"a test\"]}}";
        String[] expectedOutput2 = new String[]{
                "Map<String, Map<String, List<Integer>>>",
                "String, Map<String, List<Integer>>",
                "\"This\" = {\"is\" = [\"a test\"]}"
        };
        match = RegexUtilities.MAP_TYPE_VALUE_PATTERN.matcher(testInput2);
        match.matches();
        String[] testOutput2 = new String[]{
                match.group("type"), match.group("genericTypes"), match.group("contents")
        };
        assertArrayEquals(expectedOutput2, testOutput2);

        // test 3 - list
        String testInput3 = "List<List<String>> [[\"This\"], [\"is a test\"]]";
        String[] expectedOutput3 = new String[]{
                "List<List<String>>",
                "List<String>",
                "[\"This\"], [\"is a test\"]"
        };
        match = RegexUtilities.LIST_TYPE_VALUE_PATTERN.matcher(testInput3);
        match.matches();
        String[] testOutput3 = new String[]{
                match.group("type"), match.group("genericType"), match.group("contents")
        };
        assertArrayEquals(expectedOutput3, testOutput3);
    }
}
