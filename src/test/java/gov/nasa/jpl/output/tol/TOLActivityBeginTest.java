package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.InnerClassActivitySpawner.*;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.exampleAdaptation.*;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.input.RegexUtilities.UUID_REGEX_STRING;
import static org.junit.Assert.*;

public class TOLActivityBeginTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        Time.setDefaultOutputPrecision(6);
    }

    @Test
    public void toFlatTOL() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / ,");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);
        Activity twoRelative = new ActivityTwo(new EpochRelativeTime("test+05:00:00"), -5.0);
        Activity inner = new SecondInnerClassActivity(new Time("2020-050T00:00:00"), Duration.MINUTE_DURATION);

        // we will replaceAll the UUIDs out of the string to get a real comparison
        String expectedGW = "2000-001T00:00:00.000000,ACT_START,GetWindowsActivity,d,00:00:01.000000,\"VISIBLE\",type=GetWindowsActivity,node_id=,description=\"\",attributes=(\"Subsystem\"=\"generic\"),parameters=();\n";
        String expectedOne = "2020-050T00:00:00.000000,ACT_START,ActivityOne,d,00:05:00.000000,\"VISIBLE\",type=ActivityOne,node_id=,description=\"\",attributes=(\"Subsystem\"=\"generic\"),parameters=(d=00:05:00.000000);\n";
        String expectedTwo = "1975-364T00:00:00.000000,ACT_START,ActivityTwo,d,00:00:01.000000,\"VISIBLE\",type=ActivityTwo,node_id=,description=\"\",attributes=(\"Subsystem\"=\"testSubsystem2\"),parameters=(amount=5.0);\n";
        String expectedFive = "1995-010T23:59:50.000000,ACT_START,ActivityFive,d,1T10:00:00.000000,\"VISIBLE\",type=ActivityFive,node_id=,description=\"\",attributes=(\"Subsystem\"=\"generic\"),parameters=(d=1T10:00:00.000000,stringListMap=[\"first\"=[\"one\",\"this is a string with spaces\",\"string with special chars \\ / ,\"],\"second\"=[]]);\n";
        String expectedTwoRelative = "test+05:00:00.000000,ACT_START,ActivityTwo,d,00:00:01.000000,\"VISIBLE\",type=ActivityTwo,node_id=,description=\"\",attributes=(\"Subsystem\"=\"testSubsystem2\"),parameters=(amount=-5.0);\n";
        String expectedInner = "2020-050T00:00:00.000000,ACT_START,SecondInnerClassActivity,d,00:01:00.000000,\"VISIBLE\",type=SecondInnerClassActivity,node_id=,description=\"\",attributes=(\"Subsystem\"=\"generic\"),parameters=(d=00:01:00.000000);\n";

        assertEquals(expectedGW, new TOLActivityBegin(getWindows).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedOne, new TOLActivityBegin(one).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwo, new TOLActivityBegin(two).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedFive, new TOLActivityBegin(five).toFlatTOL().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwoRelative, new TOLActivityBegin(twoRelative).toFlatTOL().replaceAll(UUID_REGEX_STRING, ""));
        assertEquals(expectedInner, new TOLActivityBegin(inner).toFlatTOL().replaceAll(UUID_REGEX_STRING, ""));
    }

    @Test
    public void toXML() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / ,");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);
        Activity twoRelative = new ActivityTwo(new EpochRelativeTime("test+05:00:00"), -5.0);
        Activity nine = new ActivityNine(new EpochRelativeTime("test+05:00:00"), new EpochRelativeTime("test+10:00:00"));
        Activity inner = new InnerClassActivity(new Time("2020-050T00:00:00"), Duration.MINUTE_DURATION);

        String expectedGW = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>2000-001T00:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>GetWindowsActivity</Name>\n" +
                "            <Type>GetWindowsActivity</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"946684800000\">2000-001T00:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"1000\">00:00:01.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedOne = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>2020-050T00:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>ActivityOne</Name>\n" +
                "            <Type>ActivityOne</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"1582070400000\">2020-050T00:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"300000\">00:05:00.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>d</Name>\n" +
                "                    <DurationValue>00:05:00.000000</DurationValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedTwo = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>1975-364T00:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>ActivityTwo</Name>\n" +
                "            <Type>ActivityTwo</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"189129600000\">1975-364T00:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"1000\">00:00:01.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>testSubsystem2</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>testLegend</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>amount</Name>\n" +
                "                    <DoubleValue>5.0</DoubleValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedTwoRelative = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>2000-001T05:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>ActivityTwo</Name>\n" +
                "            <Type>ActivityTwo</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"946702800000\">2000-001T05:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"1000\">00:00:01.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>testSubsystem2</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>testLegend</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>amount</Name>\n" +
                "                    <DoubleValue>-5.0</DoubleValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedFive = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>1995-010T23:59:50.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>ActivityFive</Name>\n" +
                "            <Type>ActivityFive</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"789782390000\">1995-010T23:59:50.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"122400000\">1T10:00:00.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>d</Name>\n" +
                "                    <DurationValue>1T10:00:00.000000</DurationValue>\n" +
                "                </Parameter>\n" +
                "                <Parameter>\n" +
                "                    <Name>stringListMap</Name>\n" +
                "                    <StructValue>\n" +
                "                        <Element index=\"first\">\n" +
                "                            <ListValue>\n" +
                "                                <Element index=\"0\">\n" +
                "                                    <StringValue>one</StringValue>\n" +
                "                                </Element>\n" +
                "                                <Element index=\"1\">\n" +
                "                                    <StringValue>this is a string with spaces</StringValue>\n" +
                "                                </Element>\n" +
                "                                <Element index=\"2\">\n" +
                "                                    <StringValue>string with special chars \\ / ,</StringValue>\n" +
                "                                </Element>\n" +
                "                            </ListValue>\n" +
                "                        </Element>\n" +
                "                        <Element index=\"second\">\n" +
                "                            <ListValue>\n" +
                "                            </ListValue>\n" +
                "                        </Element>\n" +
                "                    </StructValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedNine = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>2000-001T05:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>ActivityNine</Name>\n" +
                "            <Type>ActivityNine</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"946702800000\">2000-001T05:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"18000000\">05:00:00.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>endTime</Name>\n" +
                "                    <TimeValue>2000-001T10:00:00.000000</TimeValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        String expectedInner = "    <TOLrecord type=\"ACT_START\">\n" +
                "        <TimeStamp>2020-050T00:00:00.000000</TimeStamp>\n" +
                "        <Instance>\n" +
                "            <ID></ID>\n" +
                "            <Name>InnerClassActivity</Name>\n" +
                "            <Type>InnerClassActivity</Type>\n" +
                "            <Parent></Parent>\n" +
                "            <Visibility>visible</Visibility>\n" +
                "            <Attributes>\n" +
                "                <Attribute>\n" +
                "                    <Name>start</Name>\n" +
                "                    <TimeValue milliseconds=\"1582070400000\">2020-050T00:00:00.000000</TimeValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>span</Name>\n" +
                "                    <DurationValue milliseconds=\"60000\">00:01:00.000000</DurationValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>subsystem</Name>\n" +
                "                    <StringValue>innerClassTesting</StringValue>\n" +
                "                </Attribute>\n" +
                "                <Attribute>\n" +
                "                    <Name>legend</Name>\n" +
                "                    <StringValue>generic</StringValue>\n" +
                "                </Attribute>\n" +
                "            </Attributes>\n" +
                "            <Parameters>\n" +
                "                <Parameter>\n" +
                "                    <Name>d</Name>\n" +
                "                    <DurationValue>00:01:00.000000</DurationValue>\n" +
                "                </Parameter>\n" +
                "            </Parameters>\n" +
                "        </Instance>\n" +
                "    </TOLrecord>\n";

        assertEquals(expectedGW, new TOLActivityBegin(getWindows).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedOne, new TOLActivityBegin(one).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwo, new TOLActivityBegin(two).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedFive, new TOLActivityBegin(five).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwoRelative, new TOLActivityBegin(twoRelative).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedNine, new TOLActivityBegin(nine).toXML().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedInner, new TOLActivityBegin(inner).toXML().replaceAll(UUID_REGEX_STRING, ""));
    }

    @Test
    public void toESJSON() {
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / ,");
        param.put("first", first);
        param.put("second", second);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);
        Activity twoRelative = new ActivityTwo(new EpochRelativeTime("test+05:00:00"), -5.0);
        Activity nine = new ActivityNine(new EpochRelativeTime("test+05:00:00"), new EpochRelativeTime("test+10:00:00"));
        Activity inner = new InnerClassActivity(new Time("2020-050T00:00:00"), Duration.MINUTE_DURATION);

        String expectedGW = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"2000-001T00:00:00.000000\",\n" +
                "    \"endTime\": \"2000-001T00:00:01.000000\",\n" +
                "    \"activityName\": \"GetWindowsActivity\",\n" +
                "    \"activityType\": \"GetWindowsActivity\",\n" +
                "    \"parameters\": [\n" +
                "        ],\n" +
                "    \"legend\": \"generic\"\n" +
                "}";

        String expectedOne = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"2020-050T00:00:00.000000\",\n" +
                "    \"endTime\": \"2020-050T00:05:00.000000\",\n" +
                "    \"activityName\": \"ActivityOne\",\n" +
                "    \"activityType\": \"ActivityOne\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"d\",\n" +
                "        \"type\": \"duration\",\n" +
                "        \"value\": \"00:05:00.000000\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"generic\"\n" +
                "}";

        String expectedTwo = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"1975-364T00:00:00.000000\",\n" +
                "    \"endTime\": \"1975-364T00:00:01.000000\",\n" +
                "    \"activityName\": \"ActivityTwo\",\n" +
                "    \"activityType\": \"ActivityTwo\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"amount\",\n" +
                "        \"type\": \"float\",\n" +
                "        \"value\": 5.0\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"testLegend\"\n" +
                "}";

        String expectedTwoRelative = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"2000-001T05:00:00.000000\",\n" +
                "    \"endTime\": \"2000-001T05:00:01.000000\",\n" +
                "    \"activityName\": \"ActivityTwo\",\n" +
                "    \"activityType\": \"ActivityTwo\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"amount\",\n" +
                "        \"type\": \"float\",\n" +
                "        \"value\": -5.0\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"testLegend\"\n" +
                "}";

        String expectedFive = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"1995-010T23:59:50.000000\",\n" +
                "    \"endTime\": \"1995-012T09:59:50.000000\",\n" +
                "    \"activityName\": \"ActivityFive\",\n" +
                "    \"activityType\": \"ActivityFive\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"d\",\n" +
                "        \"type\": \"duration\",\n" +
                "        \"value\": \"1T10:00:00.000000\"\n" +
                "        },\n" +
                "        {\n" +
                "        \"name\": \"stringListMap\",\n" +
                "        \"type\": \"map\",\n" +
                "        \"value\": \"{first=[one,this is a string with spaces,string with special chars \\ / ,],second=[]}\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"generic\"\n" +
                "}";

        String expectedNine = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"2000-001T05:00:00.000000\",\n" +
                "    \"endTime\": \"2000-001T10:00:00.000000\",\n" +
                "    \"activityName\": \"ActivityNine\",\n" +
                "    \"activityType\": \"ActivityNine\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"endTime\",\n" +
                "        \"type\": \"time\",\n" +
                "        \"value\": \"2000-001T10:00:00.000000\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"generic\"\n" +
                "}";

        String expectedInner = "{\n" +
                "    \"recordType\": \"activity\",\n" +
                "    \"id\": \"\",\n" +
                "    \"startTime\": \"2020-050T00:00:00.000000\",\n" +
                "    \"endTime\": \"2020-050T00:01:00.000000\",\n" +
                "    \"activityName\": \"InnerClassActivity\",\n" +
                "    \"activityType\": \"InnerClassActivity\",\n" +
                "    \"parameters\": [\n" +
                "        {\n" +
                "        \"name\": \"d\",\n" +
                "        \"type\": \"duration\",\n" +
                "        \"value\": \"00:01:00.000000\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"legend\": \"generic\"\n" +
                "}";

        assertEquals(expectedGW, new TOLActivityBegin(getWindows).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedOne, new TOLActivityBegin(one).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwo, new TOLActivityBegin(two).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedFive, new TOLActivityBegin(five).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwoRelative, new TOLActivityBegin(twoRelative).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedNine, new TOLActivityBegin(nine).toESJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedInner, new TOLActivityBegin(inner).toESJSON().replaceAll(UUID_REGEX_STRING, ""));
    }

    @Test
    public void toPlanJSON(){
        Map param = new HashMap<>();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        Map fourth = new HashMap();
        fourth.put("nestedOne", "");
        first.add("one");
        first.add("this is a string with spaces");
        first.add("string with special chars \\ / ,");
        param.put("first", first);
        param.put("second", second);
        param.put("third", false);
        param.put("fourth", fourth);

        Activity getWindows = new GetWindowsActivity(new Time("2000-001T00:00:00"));
        Activity one = new ActivityOne(new Time("2020-050T00:00:00"), new Duration("00:05:00"));
        Activity two = new ActivityTwo(new Time("1975-364T00:00:00"), 5.0);
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), param);
        Activity twoRelative = new ActivityTwo(new EpochRelativeTime("test+05:00:00"), -5.0);
        Activity nine = new ActivityNine(new EpochRelativeTime("test+05:00:00"), new EpochRelativeTime("test+10:00:00"));
        Activity inner = new InnerClassActivity(new Time("2020-050T00:00:00"), Duration.MINUTE_DURATION);

        String expectedGW =
                "        {\n" +
                "            \"type\": \"GetWindowsActivity\",\n" +
                "            \"start\": \"2000-001T00:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedOne =
                "        {\n" +
                "            \"type\": \"ActivityOne\",\n" +
                "            \"start\": \"2020-050T00:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"d\",\n" +
                "                    \"type\": \"duration\",\n" +
                "                    \"value\": \"00:05:00.000000\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedTwo =
                "        {\n" +
                "            \"type\": \"ActivityTwo\",\n" +
                "            \"start\": \"1975-364T00:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"amount\",\n" +
                "                    \"type\": \"float\",\n" +
                "                    \"value\": 5.0\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedFive =
                "        {\n" +
                "            \"type\": \"ActivityFive\",\n" +
                "            \"start\": \"1995-010T23:59:50.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"d\",\n" +
                "                    \"type\": \"duration\",\n" +
                "                    \"value\": \"1T10:00:00.000000\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"stringListMap\",\n" +
                "                    \"type\": \"map<string, list<string>>\",\n" +
                "                    \"value\": {\n" +
                "                        \"third\": false,\n" +
                "                        \"fourth\": {\n" +
                "                            \"nestedOne\": \"\"\n" +
                "                        },\n" +
                "                        \"first\": [\n" +
                "                            \"one\",\n" +
                "                            \"this is a string with spaces\",\n" +
                "                            \"string with special chars \\\\ / ,\"\n" +
                "                        ],\n" +
                "                        \"second\": [\n" +
                "                        ]\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedTwoRelative =
                "        {\n" +
                "            \"type\": \"ActivityTwo\",\n" +
                "            \"start\": \"test+05:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"amount\",\n" +
                "                    \"type\": \"float\",\n" +
                "                    \"value\": -5.0\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedNine =
                "        {\n" +
                "            \"type\": \"ActivityNine\",\n" +
                "            \"start\": \"test+05:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"endTime\",\n" +
                "                    \"type\": \"time\",\n" +
                "                    \"value\": \"test+10:00:00.000000\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        String expectedInner =
                "        {\n" +
                "            \"type\": \"InnerClassActivity\",\n" +
                "            \"start\": \"2020-050T00:00:00.000000\",\n" +
                "            \"parameters\": [\n" +
                "                {\n" +
                "                    \"name\": \"d\",\n" +
                "                    \"type\": \"duration\",\n" +
                "                    \"value\": \"00:01:00.000000\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"notes\": \"\",\n" +
                "            \"id\": \"\",\n" +
                "            \"parent\": null\n" +
                "        }";

        assertEquals(expectedGW, new TOLActivityBegin(getWindows).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedOne, new TOLActivityBegin(one).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwo, new TOLActivityBegin(two).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedFive, new TOLActivityBegin(five).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedTwoRelative, new TOLActivityBegin(twoRelative).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedNine, new TOLActivityBegin(nine).toPlanJSON().replaceAll(UUID_REGEX_STRING,""));
        assertEquals(expectedInner, new TOLActivityBegin(inner).toPlanJSON().replaceAll(UUID_REGEX_STRING, ""));
    }

    @Test
    public void nullValuesInOutputs(){
        Activity five = new ActivityFive(new Time("1995-010T23:59:50"), new Duration("1T10:00:00"), null);

        try{
            new TOLActivityBegin(five).toFlatTOL();
        }
        catch(NullPointerException e){
            fail();
        }
        catch (AdaptationException e){
            assertTrue(e.getMessage().startsWith("Parameter"));
        }

        try{
            new TOLActivityBegin(five).toXML();
        }
        catch(NullPointerException e){
            fail();
        }
        catch (AdaptationException e){
            assertTrue(e.getMessage().startsWith("Parameter"));
        }

        try{
            new TOLActivityBegin(five).toESJSON();
        }
        catch(NullPointerException e){
            fail();
        }
        catch (AdaptationException e){
            assertTrue(e.getMessage().startsWith("Parameter"));
        }

        try{
            new TOLActivityBegin(five).toPlanJSON();
        }
        catch(NullPointerException e){
            fail();
        }
        catch (AdaptationException e){
            assertTrue(e.getMessage().startsWith("Parameter"));
        }
    }

}