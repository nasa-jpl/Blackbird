package gov.nasa.jpl.input;

import com.google.gson.*;
import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.command.CommandException;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivitySix;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static gov.nasa.jpl.input.XMLTOLHistoryReaderTest.readInHistoryOfActivitiesAndResource;
import static gov.nasa.jpl.time.Duration.MINUTE_DURATION;
import static org.junit.Assert.*;

public class PlanJSONHistoryReaderTest extends BaseTest {
    @Before
    public void setUp(){
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        EpochRelativeTime.addEpoch("a", Time.getDefaultReferenceTime());

        ModelingEngine.getEngine().setTime(Time.getDefaultReferenceTime());

        Time.setDefaultOutputPrecision(6);
    }

    @After
    public void tearDown(){
        ModelingEngine.getEngine().setCurrentlyReadingInFile(false);
    }

    @Test
    public void readInPlanJSONHistory() {
        readInHistoryOfActivitiesAndResource("history_unit_test.plan.json", false);
    }

    @Test
    public void writeReadEpochRelativeTimesAndDecompose(){
        String fileName = "epochrelative_test.plan.json";
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        setupWriteReadForTesting(fileName, false, true, false);

        assertEquals(3, actList.length());
        assertEquals("2001-001T00:00:00", actList.get(0).getStart().toUTC(0));
        assertEquals("b+1T00:00:00.000000", actList.get(1).getStart().toString());

        setupWriteReadForTesting(fileName, false, false, false);
        assertEquals(1, actList.length());
    }

    @Test
    public void testNumberChildrenAndRebuildingHierarchy(){
        String fileName = "decompose_test.plan.json";
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();

        setupWriteReadForTesting(fileName, true, false, false);
        assertEquals(3, actList.length());
        List<Activity> twoList = actList.getAllActivitiesOfType(ActivityTwo.class);
        assertTrue(actList.get(2).getParent().getIDString().equals(twoList.get(0).getIDString()));

        setupWriteReadForTesting(fileName, true, true, false);
        twoList = actList.getAllActivitiesOfType(ActivityTwo.class);
        assertEquals(5, actList.length());
        assertEquals(4, twoList.get(0).getChildren().size());
        for(int i = 0; i < actList.length(); i++){
            assertNotNull(actList.get(i).getID());
        }

        setupWriteReadForTesting(fileName, false, true, false);
        twoList = actList.getAllActivitiesOfType(ActivityTwo.class);
        assertEquals(3, actList.length());
        assertEquals(2, twoList.get(0).getChildren().size());
        for(int i = 0; i < actList.length(); i++){
            assertNotNull(actList.get(i).getID());
        }

        setupWriteReadForTesting(fileName, true, false, true);
        assertEquals(2, actList.length());
        assertEquals(null, actList.get(0).getParent());
    }

    @Test
    public void writeReadListOfMixedTimes(){
        ResourceDeclaration.assignNamesToAllResources();
        ResourceList.getResourceList().resetResourceHistories();
        ResourceList.getResourceList().makeAllResourcesUseTheirProfileAtInitialTime();
        ActivityInstanceList.getActivityList().clear();

        String fileName = "mixedtimes_test.plan.json";
        Time time1 = Time.getDefaultReferenceTime();
        Time time2 = new EpochRelativeTime("a+1T00:00:00");
        Time time3 = Time.getDefaultReferenceTime().add(new Duration("5T00:00:00"));
        Time time4 = new EpochRelativeTime("a+10T00:00:00");
        List<Time> inTimes = Arrays.asList(time1, time2, time3, time4);
        ActivitySix act = new ActivitySix(Time.getDefaultReferenceTime(), MINUTE_DURATION, inTimes);
        String actID = act.getIDString();
        CommandController.issueCommand("WRITE", fileName);
        ActivityInstanceList.getActivityList().clear();
        CommandController.issueCommand("OPEN_FILE", fileName);
        ActivitySix newAct = (ActivitySix) ActivityInstanceList.getActivityList().get(0);
        assertEquals(actID, newAct.getIDString());
        assertEquals("2000-001T00:00:00.000000", ((List<Time>)(newAct.getParameterObjects()[1])).get(0).toString());
        assertEquals("a+1T00:00:00.000000", ((List<Time>)(newAct.getParameterObjects()[1])).get(1).toString());
        assertEquals("2000-006T00:00:00.000000", ((List<Time>)(newAct.getParameterObjects()[1])).get(2).toString());
        assertEquals("a+10T00:00:00.000000", ((List<Time>)(newAct.getParameterObjects()[1])).get(3).toString());
    }

    @Test
    public void readJSONWithoutOptionalArguments(){
        String fileName = "optional_args_missing.plan.json";
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.clear();

        JsonArray parameterList = new JsonArray();
        JsonObject parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(fileName, "ActivityOne", Time.getDefaultReferenceTime(), parameterList);

        CommandController.issueCommand("OPEN_FILE", fileName);

        assertEquals(1, actList.length());
        ActivityOne result = (ActivityOne) ActivityInstanceList.getActivityList().get(0);
        assertEquals(null, result.getParent());
        assertEquals(0, result.getChildren().size());
        assertEquals("", result.getNotes());
        assertEquals(MINUTE_DURATION, result.getParameterObjects()[0]);
    }

    @Test
    public void checkErrorReporting(){
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.clear();

        String filename;
        JsonArray parameterList;
        JsonObject parameter1;

        filename = "negative_duration.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("-00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", Time.getDefaultReferenceTime(), parameterList);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("Activity durations are not allowed to have negative values.")){
                fail();
            }
        }

        filename = "type_does_not_exist.plan.json";
        writeFileWithOneActivity(filename, "ShouldNotExist", Time.getDefaultReferenceTime(), parameterList);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("Could not find activity type")){
                fail();
            }
        }

        filename = "wrong_type_parameter.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive(5));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", Time.getDefaultReferenceTime(), parameterList);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("HistoryReader encountered an error while reading in")){
                fail();
            }
        }

        filename = "null_parameter.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", null);
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", Time.getDefaultReferenceTime(), parameterList);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("Error while creating activity instance of type")){
                fail();
            }
        }

        filename = "bad_start_time.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", "2020-200T00:00", parameterList, true, true, true);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("Error while creating activity instance of type")){
                fail();
            }
        }

        filename = "no_start_time.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", "2020-200T00:00", parameterList, false, true, true);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("does not contain fields 'type', 'start'")){
                fail();
            }
        }

        filename = "no_type.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", "2020-200T00:00", parameterList, true, false, true);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("does not contain fields 'type', 'start'")){
                fail();
            }
        }

        filename = "no_params_instance.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", "2020-200T00:00", parameterList, true, true, false);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("does not contain fields 'type', 'start'")){
                fail();
            }
        }

        filename = "only_start_time.plan.json";
        parameterList = new JsonArray();
        parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("00:01:00"));
        parameterList.add(parameter1);
        writeFileWithOneActivity(filename, "ActivityOne", "2020-200T00:00", parameterList, true, false, false);

        try {
            CommandController.issueCommand("OPEN_FILE", filename);
        }
        catch(CommandException e){
            if(!e.getMessage().contains("does not contain fields 'type', 'start'")){
                fail();
            }
        }

    }

    @Test
    public void testNonDOYFormat(){
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.clear();

        String filename = "non_DOY_format.plan.json";
        JsonArray parameterList = new JsonArray();
        JsonObject parameter1 = new JsonObject();
        parameter1.add("value", new JsonPrimitive("2020 MAY 27 02:00:00"));
        parameterList.add(parameter1);

        JsonObject file = new JsonObject();
        JsonArray activities = new JsonArray();
        JsonObject actJSON = new JsonObject();

        actJSON.add("type", new JsonPrimitive("ActivityNine"));
        actJSON.add("start", new JsonPrimitive("2020-05-27T00:00:00"));
        actJSON.add("parameters", parameterList);

        activities.add(actJSON);
        file.add("activities", activities);

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        try (FileWriter outFile = new FileWriter(filename)) {
            outFile.write(gson.toJson(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        CommandController.issueCommand("OPEN_FILE", filename);

        assertEquals(1, actList.length());
    }

    private void writeFileWithOneActivity(String filename, String type, Time startTime, JsonArray parameterList){
        writeFileWithOneActivity(filename, type, startTime.toString(), parameterList, true, true, true);
    }

    private void writeFileWithOneActivity(String filename, String type, String startTime, JsonArray parameterList, boolean writeStart, boolean writeType, boolean writeParams){
        JsonObject file = new JsonObject();
        JsonArray activities = new JsonArray();
        JsonObject actJSON = new JsonObject();

        if(writeStart) {
            actJSON.add("start", new JsonPrimitive(startTime));
        }
        if(writeType) {
            actJSON.add("type", new JsonPrimitive(type));
        }
        if(writeParams) {
            actJSON.add("parameters", parameterList);
        }

        activities.add(actJSON);
        file.add("activities", activities);

        try (FileWriter outFile = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

            outFile.write(gson.toJson(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupWriteReadForTesting(String fileName, boolean shouldDecomposeBeforeWriting, boolean shouldDecomposeAfterWriting, boolean removeParent){
        ResourceDeclaration.assignNamesToAllResources();
        ResourceList.getResourceList().resetResourceHistories();
        ResourceList.getResourceList().makeAllResourcesUseTheirProfileAtInitialTime();
        ActivityInstanceList.getActivityList().clear();

        EpochRelativeTime.addEpoch("b", Time.getDefaultReferenceTime());
        ActivityTwo act21 = new ActivityTwo(new EpochRelativeTime("b+1T00:00:00"), 5.0);
        if(shouldDecomposeBeforeWriting){
            act21.decompose();
        }
        assertEquals("2000-002T00:00:00", act21.getStart().toUTC(0));

        if(removeParent) {
            CommandController.issueCommand("WRITE", fileName + " ACTIVITIES EXCLUDE (ActivityTwo)");
        }
        else{
            CommandController.issueCommand("WRITE", fileName);
        }
        EpochRelativeTime.removeEpoch("b");
        EpochRelativeTime.addEpoch("b", Time.getDefaultReferenceTime().add(new Duration("365T00:00:00")));
        ActivityInstanceList.getActivityList().clear();
        if(shouldDecomposeAfterWriting) {
            CommandController.issueCommand("OPEN_FILE", fileName + " decompose");
        }
        else{
            CommandController.issueCommand("OPEN_FILE", fileName);
        }
    }
}