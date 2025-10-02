package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityFive;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.exampleAdaptation.GetWindowsActivity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.input.RegexUtilities.UUID_REGEX_STRING;
import static org.junit.Assert.*;

public class TOLActivityEndTest extends BaseTest {

    @Test
    public void toFlatTOL() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / , \" \"");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);

        Assert.assertEquals("2000-001T00:00:01.000000,ACT_END,;\n", new TOLActivityEnd(getWindows).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("2020-050T00:05:00.000000,ACT_END,;\n", new TOLActivityEnd(one).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("1975-364T00:00:01.000000,ACT_END,;\n", new TOLActivityEnd(two).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("1995-012T09:59:50.000000,ACT_END,;\n", new TOLActivityEnd(five).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
    }

    @Test
    public void toXML() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / , \" \"");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);

        String expectedGW = "    <TOLrecord type=\"ACT_END\">\n" +
                "        <TimeStamp>2000-001T00:00:01.000000</TimeStamp>\n" +
                "        <ActivityID></ActivityID>\n" +
                "    </TOLrecord>\n";

        String expectedOne = "    <TOLrecord type=\"ACT_END\">\n" +
                "        <TimeStamp>2020-050T00:05:00.000000</TimeStamp>\n" +
                "        <ActivityID></ActivityID>\n" +
                "    </TOLrecord>\n";

        String expectedTwo = "    <TOLrecord type=\"ACT_END\">\n" +
                "        <TimeStamp>1975-364T00:00:01.000000</TimeStamp>\n" +
                "        <ActivityID></ActivityID>\n" +
                "    </TOLrecord>\n";

        String expectedFive = "    <TOLrecord type=\"ACT_END\">\n" +
                "        <TimeStamp>1995-012T09:59:50.000000</TimeStamp>\n" +
                "        <ActivityID></ActivityID>\n" +
                "    </TOLrecord>\n";

        assertEquals(expectedGW, new TOLActivityEnd(getWindows).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedOne, new TOLActivityEnd(one).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwo, new TOLActivityEnd(two).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedFive, new TOLActivityEnd(five).toXML().replaceAll(UUID_REGEX_STRING,""));
    }

    @Test
    public void toJSON() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / , \" \"");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);

        assertEquals("", new TOLActivityEnd(getWindows).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("", new TOLActivityEnd(one).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("", new TOLActivityEnd(two).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals("", new TOLActivityEnd(five).toESJSON().replaceAll(UUID_REGEX_STRING,""));
    }
}