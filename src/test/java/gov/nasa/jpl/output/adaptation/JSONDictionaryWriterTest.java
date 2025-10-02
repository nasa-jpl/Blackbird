package gov.nasa.jpl.output.adaptation;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.Setup;
import org.junit.Before;
import org.junit.Test;
import org.sonarsource.scanner.api.internal.shaded.minimaljson.Json;
import org.sonarsource.scanner.api.internal.shaded.minimaljson.JsonValue;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.Assert.*;

public class JSONDictionaryWriterTest extends BaseTest {

    @Test
    public void activityTypeToJson(){
        JSONDictionaryWriter writer = new JSONDictionaryWriter();
        String jsonActual = writer.activityTypeToJson("ActivityThree");

        String jsonExpected = "        \"ActivityThree\":{\n" +
                "            \"description\":\"This is an activity that showcases a List parameter as well as class and parameter annotations\",\n" +
                "            \"subsystem\":\"generic\",\n" +
                "            \"parameters\":[\n" +
                "                {\n" +
                "                    \"name\":\"d\",\n" +
                "                    \"type\":\"duration\",\n" +
                "                    \"default\":\"00:01:00\",\n" +
                "                    \"units\":\"hh:mm:ss\",\n" +
                "                    \"description\":\"\",\n" +
                "                    \"range\":\"[00:00:30,00:10:00]\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\":\"stringList\",\n" +
                "                    \"type\":\"list<string>\",\n" +
                "                    \"default\":\"\",\n" +
                "                    \"units\":\"\",\n" +
                "                    \"description\":\"This is a list\",\n" +
                "                    \"range\":\"[]\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }";

        assertEquals(jsonExpected, jsonActual);

        jsonActual = writer.activityTypeToJson("ActivityTwo");

        jsonExpected = "        \"ActivityTwo\":{\n" +
                "            \"description\":\"\",\n" +
                "            \"subsystem\":\"testSubsystem2\",\n" +
                "            \"parameters\":[\n" +
                "                {\n" +
                "                    \"name\":\"amount\",\n" +
                "                    \"type\":\"float\",\n" +
                "                    \"default\":\"\",\n" +
                "                    \"units\":\"\",\n" +
                "                    \"description\":\"\",\n" +
                "                    \"range\":\"\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }";

        assertEquals(jsonExpected, jsonActual);

        String testFileName = "adaptation.dict.json";
        CommandController.issueCommand("CREATE_DICTIONARY", testFileName);

        // check to make sure what we're writing out is actually valid JSON
        try {
            BufferedReader br = new BufferedReader(new FileReader(testFileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                line = br.readLine();
            }
            JsonValue hello = Json.parse(sb.toString());

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }


    }
}