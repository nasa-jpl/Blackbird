package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static gov.nasa.jpl.exampleAdaptation.Res.*;
import static gov.nasa.jpl.input.RegexUtilities.UUID_REGEX_STRING;
import static org.junit.Assert.*;

public class TOLResourceValueTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
    }

    @Test
    public void toFlatTOL() {
        Assert.assertEquals("2000-001T00:00:00.000000,RES,ResourceB=35;\n", new TOLResourceValue(new Time("2000-001T00:00:00"), 35, ResourceB).toFlatTOL());
        assertEquals("2000-001T00:00:00.000000,RES,ResourceC=00:05:00.000000;\n", new TOLResourceValue(new Time("2000-001T00:00:00"), new Duration("00:05:00"), ResourceC).toFlatTOL());
        assertEquals("2000-001T00:00:00.000000,RES,ExampleBodyState[\"Sun\"][\"x\"]=35.0;\n", new TOLResourceValue(new Time("2000-001T00:00:00"), 35.0, ExampleBodyState.get("Sun").get("x")).toFlatTOL());
        assertEquals("2000-001T00:00:00.000000,RES,ResourceC=00:05:00.000000;\n", new TOLResourceValue(new EpochRelativeTime("test+0T00:00:00"), new Duration("00:05:00"), ResourceC).toFlatTOL());
    }

    @Test
    public void toXML() {
        String expectedB = "    <TOLrecord type=\"RES_VAL\">\n" +
                "        <TimeStamp>2000-001T00:00:00.000000</TimeStamp>\n" +
                "        <Resource>\n" +
                "            <Name>ResourceB</Name>\n" +
                "            <IntegerValue>35</IntegerValue>\n" +
                "        </Resource>\n" +
                "    </TOLrecord>\n";

        String expectedC = "    <TOLrecord type=\"RES_VAL\">\n" +
                "        <TimeStamp>2000-001T00:00:00.000000</TimeStamp>\n" +
                "        <Resource>\n" +
                "            <Name>ResourceC</Name>\n" +
                "            <DurationValue milliseconds=\"300000\">00:05:00.000000</DurationValue>\n" +
                "        </Resource>\n" +
                "    </TOLrecord>\n";

        String expectedArrayed = "    <TOLrecord type=\"RES_VAL\">\n" +
                "        <TimeStamp>2000-001T00:00:00.000000</TimeStamp>\n" +
                "        <Resource>\n" +
                "            <Name>ExampleBodyState</Name>\n" +
                "            <Index level=\"0\">Sun</Index>\n" +
                "            <Index level=\"1\">x</Index>\n" +
                "            <DoubleValue>35.0</DoubleValue>\n" +
                "        </Resource>\n" +
                "    </TOLrecord>\n";

        String expectedRelative = "    <TOLrecord type=\"RES_VAL\">\n" +
                "        <TimeStamp>2000-001T00:00:00.000000</TimeStamp>\n" +
                "        <Resource>\n" +
                "            <Name>ResourceC</Name>\n" +
                "            <DurationValue milliseconds=\"300000\">00:05:00.000000</DurationValue>\n" +
                "        </Resource>\n" +
                "    </TOLrecord>\n";

        assertEquals(expectedB, new TOLResourceValue(new Time("2000-001T00:00:00"), 35, ResourceB).toXML());
        assertEquals(expectedC, new TOLResourceValue(new Time("2000-001T00:00:00"), new Duration("00:05:00"), ResourceC).toXML());
        assertEquals(expectedArrayed, new TOLResourceValue(new Time("2000-001T00:00:00"), 35.0, ExampleBodyState.get("Sun").get("x")).toXML());
        assertEquals(expectedRelative, new TOLResourceValue(new EpochRelativeTime("test+00:00:00"), new Duration("00:05:00"), ResourceC).toXML());
    }

    @Test
    public void toJSON() {
        String expectedB = "{\n" +
                "    \"recordType\": \"resource\",\n" +
                "    \"id\": \"\",\n" +
                "    \"displayName\": \"ResourceB\",\n" +
                "    \"name\": \"ResourceB\",\n" +
                "    \"component\": \"generic\",\n" +
                "    \"dataTimestamp\": \"2000-001T00:00:00.000000\",\n" +
                "    \"dataValue\": 35\n" +
                "}";

        String expectedC = "{\n" +
                "    \"recordType\": \"resource\",\n" +
                "    \"id\": \"\",\n" +
                "    \"displayName\": \"ResourceC\",\n" +
                "    \"name\": \"ResourceC\",\n" +
                "    \"component\": \"subsystem2\",\n" +
                "    \"dataTimestamp\": \"2000-001T00:00:00.000000\",\n" +
                "    \"dataValue\": \"00:05:00.000000\"\n" +
                "}";

        String expectedRelative = "{\n" +
                "    \"recordType\": \"resource\",\n" +
                "    \"id\": \"\",\n" +
                "    \"displayName\": \"ResourceC\",\n" +
                "    \"name\": \"ResourceC\",\n" +
                "    \"component\": \"subsystem2\",\n" +
                "    \"dataTimestamp\": \"2000-001T00:00:00.000000\",\n" +
                "    \"dataValue\": \"00:05:00.000000\"\n" +
                "}";

        String expectedArrayed = "{\n" +
                "    \"recordType\": \"resource\",\n" +
                "    \"id\": \"\",\n" +
                "    \"displayName\": \"ExampleBodyState[Sun][x]\",\n" +
                "    \"name\": \"ExampleBodyState_Sun_x\",\n" +
                "    \"component\": \"generic\",\n" +
                "    \"dataTimestamp\": \"2000-001T00:00:00.000000\",\n" +
                "    \"dataValue\": 35.0\n" +
                "}";

        // we need to not diff random UUIDs to get a reasonable comparison
        assertEquals(expectedB, new TOLResourceValue(new Time("2000-001T00:00:00"), 35, ResourceB).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedC, new TOLResourceValue(new Time("2000-001T00:00:00"), new Duration("00:05:00"), ResourceC).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedArrayed, new TOLResourceValue(new Time("2000-001T00:00:00"), 35.0, ExampleBodyState.get("Sun").get("x")).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedRelative, new TOLResourceValue(new EpochRelativeTime("test+00:00:00"), new Duration("00:05:00"), ResourceC).toESJSON().replaceAll(UUID_REGEX_STRING,""));
    }
}