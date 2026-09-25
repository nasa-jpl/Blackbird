package gov.nasa.jpl.output.tol;

import com.google.gson.*;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

import static gov.nasa.jpl.output.tol.XMLTOLWriterTest.createSimulationAndWriteOutFile;
import static org.junit.Assert.*;

public class JSONTOLWriterTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("a", Time.getDefaultReferenceTime());
    }

    @Test
    public void writeJSONTOL(){
        String testFileName = "history_unit_test.tol.json";
        createSimulationAndWriteOutFile(testFileName, true);

        JsonArray inputJSON = null;
        try(FileReader fr = new FileReader(testFileName)){
            inputJSON = JsonParser.parseReader(fr).getAsJsonArray();
        }
        catch(IOException | JsonIOException | JsonSyntaxException e) {
            fail(e.getMessage());
        }

        boolean foundRecord = false;
        for(JsonElement elem : inputJSON){
            JsonObject obj = (JsonObject) elem;
            if(obj.get("recordType").getAsString().equals("resource") && obj.get("name").getAsString().equals("PositionVector_y")){
                assertEquals("2000-002T00:02:20.000000", obj.get("dataTimestamp").getAsString());
                assertEquals(0.02, obj.get("dataValue").getAsDouble(), 1E-8);
                foundRecord = true;
                break;
            }
        }
        assertTrue(foundRecord);
    }

}
