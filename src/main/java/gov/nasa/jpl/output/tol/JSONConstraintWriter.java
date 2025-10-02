package gov.nasa.jpl.output.tol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Time;

import java.util.*;

public class JSONConstraintWriter extends TOLWriter {
    private boolean writeDefinitions;

    public JSONConstraintWriter(boolean writeDefinitions){
        this.writeDefinitions = writeDefinitions;
    }

    // default is to write violations
    public JSONConstraintWriter(){
        this(true);
    }

    @Override
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        Map<String, Map<String, String>> constraintsChecked = null;
        if(writeDefinitions) {
            constraintsChecked = getNonDeactivatedConstraintsForWriting(conList);
        }

        Map<String, List<String>> constraintViolations = getConstraintViolationsForWriting(conList, startTime, endTime);

        // actually open and write files
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject constraints = new JsonObject();
        if(constraintsChecked != null) {
            constraints.add("constraintsChecked", gson.toJsonTree(constraintsChecked));
        }
        constraints.add("violations", gson.toJsonTree(constraintViolations));
        writer.append(gson.toJson(constraints));
    }

    public static Map<String, Map<String, String>> getNonDeactivatedConstraintsForWriting(ConstraintInstanceList conList) {
        Map<String, Map<String, String>> allConDefs = new TreeMap<>();
        for (int i = 0; i < conList.length(); i++) {
            // 'get' only returns constraints which haven't been turned off
            Constraint cur = conList.get(i);
            Map<String, String> conOut = new TreeMap<>();
            conOut.put("description", cur.getMessage());
            conOut.put("severity", cur.getSeverity().toString());
            allConDefs.put(cur.getName(), conOut);
        }
        return allConDefs;
    }

    public static Map<String, List<String>> getConstraintViolationsForWriting(ConstraintInstanceList conList, Time startTime, Time endTime){
        Map<String, List<String>> relevantConViolations = new TreeMap<>();
        for (int i = 0; i < conList.length(); i++) {
            Constraint cur = conList.get(i);

            if(cur.getName() == null){
                throw new AdaptationException("Constraint declared without name. To fix this, make sure it is declared public static with a variable name in a class that extends ConstraintDeclaration. The particular instance has class " + cur.getClass() + " and message " + cur.getMessage());
            }

            Iterator<Map.Entry<Time, Time>> violations = cur.historyIterator();
            List<String> relevantViolations = new ArrayList<>();
            while (violations.hasNext()) {
                Map.Entry<Time, Time> violation = violations.next();
                Window violationWindow = new Window(violation.getKey(), violation.getValue());

                if(!violationWindow.hasLengthZero() &&
                        (startTime == null || violationWindow.getEnd().greaterThan(startTime)) &&
                        (endTime == null   || violationWindow.getStart().lessThan(endTime))) {
                    relevantViolations.add(violationWindow.toString());
                }
            }

            if(!relevantViolations.isEmpty()){
                relevantConViolations.put(cur.getName(), relevantViolations);
            }
        }
        return relevantConViolations;
    }
}
