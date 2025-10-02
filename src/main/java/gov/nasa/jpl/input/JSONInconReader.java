package gov.nasa.jpl.input;

import com.google.gson.*;
import gov.nasa.jpl.engine.InitialConditionList;

import java.io.FileReader;
import java.io.IOException;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.input.PlanJSONHistoryReader.convertJSONToObject;
import static gov.nasa.jpl.input.ReflectionUtilities.*;

public class JSONInconReader implements InconReader {
    private String inconName;

    public JSONInconReader(String inconName) {
        this.inconName = inconName;
    }

    @Override
    public InitialConditionList getInitialConditions() throws IOException {
        InitialConditionList incon = new InitialConditionList();
        JsonObject inputJSON;

        try(FileReader fr = new FileReader(inconName)){
            inputJSON = JsonParser.parseReader(fr).getAsJsonObject();

        }
        catch(JsonIOException | JsonSyntaxException e) {
            throw new RuntimeException("Could not parse input JSON incon file. There may be something wrong with the format of the file, " +
                    "such as a missing or extra comma or bracket. See error below:\n\n" + e.toString());
        }

        Time inconTime = new Time(inputJSON.get("inconTime").getAsString());
        incon.setInconTime(inconTime);

        JsonArray values = inputJSON.get("finalValues").getAsJsonArray();
        for (int i = 0; i < values.size(); i++) {
            JsonObject o = values.get(i).getAsJsonObject();
            String resourceName = o.get("resourceName").getAsString();
            Resource toSet = ResourceList.getResourceList().get(resourceName);
            Comparable finalValue = (Comparable) convertJSONToObject(o.get("finalValue"), toSet.getDataType());
            incon.addToInconList(resourceName, finalValue);
        }

        return incon;
    }
}
