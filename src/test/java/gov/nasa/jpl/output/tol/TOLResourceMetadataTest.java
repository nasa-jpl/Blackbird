package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.resource.ResourceDeclaration;
import org.junit.Assert;
import org.junit.Test;

import static gov.nasa.jpl.exampleAdaptation.Res.*;
import static org.junit.Assert.*;

public class TOLResourceMetadataTest extends BaseTest {

    @Test
    public void toFlatTOL() {
        Assert.assertEquals("", new TOLResourceMetadata(TestState).toFlatTOL());
        assertEquals("", new TOLResourceMetadata(ResourceC).toFlatTOL());
        assertEquals("", new TOLResourceMetadata(ExampleBodyState.get("Sun").get("x")).toFlatTOL());
    }

    @Test
    public void toXML() {
        String expectedState = "        <ResourceSpec>\n" +
                "            <Name>TestState</Name>\n" +
                "            <DataType>string</DataType>\n" +
                "            <PossibleStates>\n" +
                "                <StringValue>NoSignal</StringValue>\n" +
                "                <StringValue>SignalSent</StringValue>\n" +
                "            </PossibleStates>\n" +
                "            <Units></Units>\n" +
                "            <Interpolation>constant</Interpolation>\n" +
                "            <Maximum></Maximum>\n" +
                "            <Minimum></Minimum>\n" +
                "            <Subsystem>generic</Subsystem>\n" +
                "        </ResourceSpec>\n";

        String expectedDur = "        <ResourceSpec>\n" +
                "            <Name>ResourceC</Name>\n" +
                "            <DataType>duration</DataType>\n" +
                "            <Units></Units>\n" +
                "            <Interpolation>constant</Interpolation>\n" +
                "            <Maximum></Maximum>\n" +
                "            <Minimum></Minimum>\n" +
                "            <Subsystem>subsystem2</Subsystem>\n" +
                "        </ResourceSpec>\n";

        String expectedArray = "        <ResourceSpec>\n" +
                "            <Name>ExampleBodyState</Name>\n" +
                "            <Index level=\"0\">Sun</Index>\n" +
                "            <Index level=\"1\">x</Index>\n" +
                "            <DataType>float</DataType>\n" +
                "            <Units></Units>\n" +
                "            <Interpolation>constant</Interpolation>\n" +
                "            <Maximum></Maximum>\n" +
                "            <Minimum></Minimum>\n" +
                "            <Subsystem>generic</Subsystem>\n" +
                "        </ResourceSpec>\n";

        assertEquals(expectedState, new TOLResourceMetadata(TestState).toXML());
        assertEquals(expectedDur, new TOLResourceMetadata(ResourceC).toXML());
        assertEquals(expectedArray, new TOLResourceMetadata(ExampleBodyState.get("Sun").get("x")).toXML());
    }

    @Test
    public void toJSON() {
        String expectedState = "{\n" +
                "    \"recordType\": \"resource_metadata\",\n" +
                "    \"displayName\": \"TestState\",\n" +
                "    \"name\": \"TestState\",\n" +
                "    \"component\": \"generic\",\n" +
                "    \"resourceType\": \"string\",\n" +
                "    \"interpolation\": \"constant\",\n" +
                "    \"possibleStates\": [\"NoSignal\",\"SignalSent\"],\n" +
                "    \"unit\": \"\"\n" +
                "},\n";

        String expectedDur = "{\n" +
                "    \"recordType\": \"resource_metadata\",\n" +
                "    \"displayName\": \"ResourceC\",\n" +
                "    \"name\": \"ResourceC\",\n" +
                "    \"component\": \"subsystem2\",\n" +
                "    \"resourceType\": \"duration\",\n" +
                "    \"interpolation\": \"constant\",\n" +
                "    \"unit\": \"\"\n" +
                "},\n";

        String expectedArray = "{\n" +
                "    \"recordType\": \"resource_metadata\",\n" +
                "    \"displayName\": \"ExampleBodyState[Sun][x]\",\n" +
                "    \"name\": \"ExampleBodyState_Sun_x\",\n" +
                "    \"component\": \"generic\",\n" +
                "    \"resourceType\": \"float\",\n" +
                "    \"interpolation\": \"constant\",\n" +
                "    \"unit\": \"\"\n" +
                "},\n";

        assertEquals(expectedState, new TOLResourceMetadata(TestState).toESJSON());
        assertEquals(expectedDur, new TOLResourceMetadata(ResourceC).toESJSON());
        assertEquals(expectedArray, new TOLResourceMetadata(ExampleBodyState.get("Sun").get("x")).toESJSON());
    }
}