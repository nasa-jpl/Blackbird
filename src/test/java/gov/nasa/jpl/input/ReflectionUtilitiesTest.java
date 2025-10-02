package gov.nasa.jpl.input;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReflectionUtilitiesTest extends BaseTest {
    @Test
    public void getClassNameWithoutPackage() {
        // test 1 - Basic data types
        String[] combinedBasicTypes = new String[]{
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.String"),
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.Boolean"),
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.Integer"),
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.Long"),
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.Float"),
                ReflectionUtilities.getClassNameWithoutPackage("java.lang.Double")
        };
        String[] expectedOutput1 = new String[]{
                "String", "Boolean", "Integer",
                "Long", "Float", "Double"
        };
        assertArrayEquals(expectedOutput1, combinedBasicTypes);


        // test 2 - Custom data types
        String[] combinedCustomTypes = new String[]{
                ReflectionUtilities.getClassNameWithoutPackage("gov.nasa.jpl.time.Time"),
                ReflectionUtilities.getClassNameWithoutPackage("gov.nasa.jpl.time.Duration")
        };
        String[] expectedOutput2 = new String[]{
                "Time", "Duration"
        };
        assertArrayEquals(expectedOutput2, combinedCustomTypes);
    }

    @Test
    public void returnValueOf() {
        // test 1 - String
        Object stringTest1 = ReflectionUtilities.returnValueOf("String", "\"Hello World!\"", true);
        Object stringTest2 = ReflectionUtilities.returnValueOf("String", "Hello World!", true);
        Object stringTest3 = ReflectionUtilities.returnValueOf("String", "\"Hello World!\"", false);
        String expectedStringOutput = "Hello World!";
        assertEquals(expectedStringOutput, stringTest1);
        assertEquals(expectedStringOutput, stringTest2);
        assertEquals("\"Hello World!\"", stringTest3);

        // test 2 - Boolean
        Object booleanTest = ReflectionUtilities.returnValueOf("Boolean", "false", true);
        Boolean expectedBooleanOutput = false;
        assertEquals(expectedBooleanOutput, booleanTest);

        // test 3 - Integer
        Object integerTest = ReflectionUtilities.returnValueOf("Integer", "42", true);
        Integer expectedIntegerOutput = 42;
        assertEquals(expectedIntegerOutput, integerTest);

        // test 4 - Long
        Object longTest = ReflectionUtilities.returnValueOf("Long", "42", true);
        Long expectedLongOutput = 42L;
        assertEquals(expectedLongOutput, longTest);

        // test 5 - Float
        Object floatTest = ReflectionUtilities.returnValueOf("Float", "42.0f", true);
        Float expectedFloatOutput = 42.0f;
        assertEquals(expectedFloatOutput, floatTest);

        // test 6 - Double
        Object doubleTest = ReflectionUtilities.returnValueOf("Double", "42.0f", true);
        Double expectedDoubleOutput = 42.0;
        assertEquals(expectedDoubleOutput, doubleTest);
        CommandController.issueCommand("NEW_ACTIVITY", "ActivityOne (2018-330T13:00:00.000, 00:01:00)");

        // test 7 - Time
        Object timeTest = ReflectionUtilities.returnValueOf(ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE,
            "2018-330T12:00:00.000", true);
        Time expectedTimeOutput = new Time();
        expectedTimeOutput.valueOf("2018-330T12:00:00.000");
        assertEquals(expectedTimeOutput, timeTest);

        // test 8 - Duration
        Object durationTest = ReflectionUtilities.returnValueOf("Duration", "12:00:00", true);
        Duration expectedDurationOutput = new Duration();
        expectedDurationOutput.valueOf("12:00:00");
        assertEquals(expectedDurationOutput, durationTest);
    }

    @Test
    public void getCustomDataType() {
    }

    @Test
    public void parseActivityParameters() {
    }

    @Test
    public void parseParamTypeValuePair() {
    }

    @Test
    public void initList() {
    }

    @Test
    public void parseListString() {
    }

    @Test
    public void initMap() {
    }

    @Test
    public void parseMapString() {
    }
}
