package gov.nasa.jpl.input;

import com.google.gson.*;
import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.serialization.ConvertableFromString;
import gov.nasa.jpl.time.EpochRelativeTime;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.nasa.jpl.input.ReflectionUtilities.*;
import static gov.nasa.jpl.input.TypeNameConverters.getWrapperDataType;
import static gov.nasa.jpl.input.XMLTOLHistoryReader.rebuildInstanceHierarchy;

public class PlanJSONHistoryReader implements HistoryReader {
    private String fileName;
    // because we're reading this in chronologically but there may be child activities spawned
    // before their parents, we have to go back after we read everything in and assign parentage
    private HashMap<String, Map.Entry<Activity, String>> mapOfAllIDsToActivitiesAndTheirParentIDs;

    public PlanJSONHistoryReader(String fileName) {
        this.fileName = fileName;
        mapOfAllIDsToActivitiesAndTheirParentIDs = new HashMap<>();
    }

    @Override
    public void readInHistoryOfActivitiesAndResource(boolean areReadInResourcesFrozen, boolean shouldActivitiesDecompose) throws IOException {
        ModelingEngine.getEngine().setCurrentlyReadingInFile(true);

        JsonObject inputJSON;

        try(FileReader fr = new FileReader(this.fileName)){
            inputJSON = JsonParser.parseReader(fr).getAsJsonObject();

        }
        catch(JsonIOException | JsonSyntaxException e) {
            throw new RuntimeException("Could not parse input plan JSON  file. There may be something wrong with the format of the file, " +
                    "such as a missing or extra comma or bracket. See error below:\n\n" + e.toString());
        }

        try {
            JsonArray planContents = inputJSON.getAsJsonArray("activities");
            for (JsonElement act : planContents) {
                addJSONActivityToInstanceList(act.getAsJsonObject());
            }

            // now we've put all the activities into the list, but we haven't re-built the instance hierarchy
            rebuildInstanceHierarchy(mapOfAllIDsToActivitiesAndTheirParentIDs);
        }
        finally {
            ModelingEngine.getEngine().setCurrentlyReadingInFile(false);
        }

        // must do this after setting reading file to false, so child activities get IDs
        if (shouldActivitiesDecompose) {
            decomposeAndScheduleActivities(mapOfAllIDsToActivitiesAndTheirParentIDs.values());
        }

        // hopefully let garbage collection get these references
        mapOfAllIDsToActivitiesAndTheirParentIDs = new HashMap<>();
    }

    private void addJSONActivityToInstanceList(JsonObject act) throws IOException{
        // check to make sure JSON has fields we need
        for(String mandatoryKey : Arrays.asList("type", "start", "parameters")){
            if(act.get(mandatoryKey) == null || act.get(mandatoryKey).isJsonNull()){
                throw new RuntimeException("Activity instance in JSON does not contain fields 'type', 'start', and 'parameters', all of which are required:\n" + act.toString());
            }
        }

        // grab all values from JSON
        String activityTypeName = act.get("type").getAsString();
        String notes = "";
        String actID = "";
        String parentID = "";
        if(act.get("notes") != null && !act.get("notes").isJsonNull()){
            notes = act.get("notes").getAsString();
        }
        if(act.get("id") != null && !act.get("id").isJsonNull()){
            actID = act.get("id").getAsString();
        }
        if(act.get("parent") != null && !act.get("parent").isJsonNull()){
            parentID = act.get("parent").getAsString();
        }

        // find the correct activity class
        ActivityTypeList actList = ActivityTypeList.getActivityList();
        List<Map<String, String>> parameters = actList.getParameters(activityTypeName);

        Object[] args = getParametersFromJSON(act, parameters);

        Class<?> cls = actList.getActivityClass(activityTypeName);
        Activity activityInstance = null;
        try {
            activityInstance = (Activity) ConstructorUtils.invokeConstructor(cls, args);
        }
        catch (NoSuchMethodException e) {
            throw new IOException("Could not find constructor for activity type " + activityTypeName
                    + ": " + e.toString());
        }
        catch (InstantiationException e) {
            throw new IOException("Could not execute constructor for activity type " + activityTypeName);
        }
        catch (IllegalAccessException e) {
            throw new IOException("Did not have permission to execute constructor for activity type " + activityTypeName);
        }
        catch (InvocationTargetException e) {
            throw new IOException("Failed invoking constructor for activity type " + activityTypeName +
                    ". Cause:\n" + e.getTargetException().getMessage());
        }

        if(!actID.isEmpty()) {
            activityInstance.setID(UUID.fromString(actID));
        }
        else{
            activityInstance.setID(UUID.randomUUID());
        }
        activityInstance.setNotes(notes);
        mapOfAllIDsToActivitiesAndTheirParentIDs.put(activityInstance.getIDString(), new AbstractMap.SimpleEntry<>(activityInstance, parentID));
    }

    private void decomposeAndScheduleActivities(Collection<Map.Entry<Activity, String>> activitiesFromThisFile){
        for(Map.Entry<Activity, String> entry : activitiesFromThisFile){
            try {
                entry.getKey().decompose();
            }
            catch(RuntimeException e){
                throw new RuntimeException("Error trying decompose activity of type " + entry.getKey().getType() + " with start time " + entry.getKey().getStart().toString() + ". Root cause:\n" + e.getMessage() + "\nat\n" + e.getStackTrace()[0].toString());
            }
            entry.getKey().schedule();
        }
    }

    private Object[] getParametersFromJSON(JsonObject act, List<Map<String, String>> parameters){
        Object[] args = new Object[parameters.size()+1];
        String timeString = act.get("start").getAsString();
        try{
            args[0] =  EpochRelativeTime.getAbsoluteOrRelativeTime(timeString);
        }
        catch (RuntimeException e){
            throw new RuntimeException("Error while creating activity instance of type " + act.get("type") + " at time " + timeString + ". Root cause:\n" + e.getMessage());
        }

        JsonArray parameterNamesAndValues = act.get("parameters").getAsJsonArray();
        if(parameterNamesAndValues.size() != parameters.size()){
            throw new AdaptationException("Error reading in plan JSON. Activity type " + act.get("type") + " has " + parameters.size() + " parameters:\n"
            + parameters.stream().map(e -> "  " + e.get("name") + " (" + e.get("type") + ")").collect(Collectors.joining("\n")) + "\n" +
                    "but the instance at time " + timeString + " has " + parameterNamesAndValues.size() + " parameter values:\n" +
                    StreamSupport.stream(parameterNamesAndValues.spliterator(), true).map(p -> "  " + p.getAsJsonObject().get("value")).collect(Collectors.joining("\n")));
        }
        for(int i = 1; i<=parameters.size(); i++){
            JsonObject parameterObject = parameterNamesAndValues.get(i-1).getAsJsonObject();
            try {
                args[i] = convertJSONToObject(parameterObject.get("value"), parameters.get(i - 1).get("type"));
            }
            catch(RuntimeException e){
                throw new RuntimeException("Error while creating activity instance of type " + act.get("type") + " at time " + timeString + ". Root cause:\n" + e.getMessage());
            }
        }

        return args;
    }

    // package protected by choice
    static Object convertJSONToObject(JsonElement obj, String typeString){
        try {
            if(obj == null || obj.isJsonNull()){
                throw new RuntimeException("Cannot convert null to type " + typeString);
            }
            else if (matchesString(typeString)) {
                return obj.getAsString();
            } else if (getWrapperDataType(typeString) != null) {
                Method m = getValueOfMethod(getWrapperDataType(typeString));
                return m.invoke(null, obj.getAsString());

            } else if (getListType(typeString) != null) {
                List<Object> newList = new ArrayList<>();
                String innerType = getListType(typeString);
                for(JsonElement innerObj : obj.getAsJsonArray()){
                    newList.add(convertJSONToObject(innerObj, innerType));
                }
                return newList;

            } else if (getMapType(typeString) != null) {
                Map<Object, Object> newMap = new HashMap<>();
                String[] innerTypes =  StringParsingUtilities.splitParamStringByChar(getMapType(typeString), ',');
                String valueType = innerTypes[1].trim();
                for(Map.Entry<String, JsonElement> entry : obj.getAsJsonObject().entrySet()){
                    newMap.put(entry.getKey(), convertJSONToObject(entry.getValue(), valueType));
                }
                return newMap;

            } else if (typeString.equals(ABSOLUTE_TIME_CLASS_PACKAGE)) {
                return EpochRelativeTime.getAbsoluteOrRelativeTime(obj.getAsString());
            } else if (typeString.equals(DURATION_CLASS_PACKAGE)) {
                return returnValueOf(typeString, obj.getAsString(), true);
            } else {
                Class<?> classType = getCustomDataType(typeString);
                ConvertableFromString classInstance = (ConvertableFromString) classType.newInstance();
                classInstance.valueOf(obj.getAsString());
                return classInstance;
            }
        }
        catch (InstantiationException  | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Error: Cannot cast value " + obj.toString()
                    + " from JSON to type: " + typeString + "\n");
        }
    }
}
