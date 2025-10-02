package gov.nasa.jpl.output.constraints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.output.tol.JSONConstraintWriter.getConstraintViolationsForWriting;
import static gov.nasa.jpl.output.tol.JSONConstraintWriter.getNonDeactivatedConstraintsForWriting;
import static org.junit.Assert.assertEquals;

public class JSONConstraintWriterTest extends BaseTest {
    @Test
    public void testConstraintJSONOutput(){
        Time t = Time.getDefaultReferenceTime();
        Time.setDefaultOutputPrecision(3);

        // first: test that time filtering is working ok
        ConstraintInstanceList conList = ConstraintInstanceList.getConstraintList();
        ConstraintInstanceList cleanConList = new ConstraintInstanceList();
        for(int i = 0; i<conList.length(); i++){
            if(conList.get(i).getName() != null){
                cleanConList.registerConstraint(conList.get(i));
            }
        }

        // due to weird behavior of ActivityTwo and integrating resources, this creates windows [t+2min, t+2hr] and [t+3hr, t+3hr2min]
        new ActivityTwo(t.add(Duration.HOUR_DURATION.multiply(0)), 10000);
        new ActivityTwo(t.add(Duration.HOUR_DURATION.multiply(1)), -50000);
        new ActivityTwo(t.add(Duration.HOUR_DURATION.multiply(2)), 200000);
        new ActivityTwo(t.add(Duration.HOUR_DURATION.multiply(3)), 0);

        ModelingEngine.getEngine().model();

        // without filtering, expect 2 violations
        Map<String, List<String>> violations = getConstraintViolationsForWriting(cleanConList, null, null);
        assertEquals(2, violations.get("forbidden").size());

        // also check correctness here
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject constraints = new JsonObject();
        JsonElement defs = gson.toJsonTree(getNonDeactivatedConstraintsForWriting(cleanConList));
        JsonElement viols = gson.toJsonTree(violations);
        constraints.add("constraintsChecked", defs);
        constraints.add("violations", viols);
        String result = gson.toJson(constraints);
        String expectedOut = "{\n" +
                "  \"constraintsChecked\": {\n" +
                "    \"TwoBeforeOne\": {\n" +
                "      \"description\": \"ActivityOne must be preceded by an instance of ActivityTwo\",\n" +
                "      \"severity\": \"ERROR\"\n" +
                "    },\n" +
                "    \"forbidden\": {\n" +
                "      \"description\": \"\",\n" +
                "      \"severity\": \"WARNING\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"violations\": {\n" +
                "    \"forbidden\": [\n" +
                "      \"[2000-001T00:02:00.000, 2000-001T02:00:00.000]\",\n" +
                "      \"[2000-001T03:00:00.000, 2000-001T03:02:00.000]\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        assertEquals(expectedOut, result);

        // filter one out
        violations = getConstraintViolationsForWriting(cleanConList, null, t.add(Duration.HOUR_DURATION.multiply(2)));
        assertEquals(1, violations.get("forbidden").size());

        // filter both out means there won't be a key at all
        violations = getConstraintViolationsForWriting(cleanConList, t.add(Duration.HOUR_DURATION.multiply(5)), t.add(Duration.HOUR_DURATION.multiply(6)));
        assertEquals(false, violations.containsKey("forbidden"));

        Time.setDefaultOutputPrecision(6);
    }
}
