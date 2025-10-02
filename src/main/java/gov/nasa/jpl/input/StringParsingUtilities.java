package gov.nasa.jpl.input;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;

import java.util.*;

public class StringParsingUtilities {

    // because this is a utilities class with just static methods, we never want anyone to instantiate
    // an object of this, so we hide the public constructor by making a private one
    private StringParsingUtilities() {
    }

    /**
     * Splits a string by commas, but only when outside of double quote,
     * [], {} or &lt;&gt; pairs.
     */
    public static String[] splitParamStringByChar(String inputString, char splitChar) {
        ArrayList<String> splitStr = new ArrayList<>();

        // loop over input string, splitting at each comma when we are at an even number
        // of double quotes and equal numbers of each type of bracket
        Map<String, Integer> numChars = new HashMap<>();
        numChars.put("\"", 0);
        numChars.put("[", 0);
        numChars.put("]", 0);
        numChars.put("{", 0);
        numChars.put("}", 0);
        numChars.put("<", 0);
        numChars.put(">", 0);
        boolean escapeNextChar = false;    // set true when we get to a \ character

        // this string will get reset at each comma being split on
        StringBuilder currentSubString = new StringBuilder();

        for (int index = 0; index < inputString.length(); index++) {
            char currentChar = inputString.charAt(index);
            String currentCharString = Character.toString(currentChar);

            // if the current char is escaped then continue to the next iteration
            // after adding the current char to the currentSubString and resetting
            // escapeNextChar to false
            // the only exception to this is if there is an escaped " character which
            // is escaped with a single \
            if (escapeNextChar) {
                currentSubString.append(currentChar);
                escapeNextChar = false;
                continue;
            }

            // if we get to a \ character then set escapeNextChar to true
            if (currentChar == '\\') {
                escapeNextChar = true;
            }

            // if we get to a comma and we are not within double quotes or brackets then
            // add currentSubString to the splitStr and reset currentSubString
            if ((currentChar == splitChar)
                    && ((numChars.get("\"") % 2) == 0)
                    && (numChars.get("[").equals(numChars.get("]")))
                    && (numChars.get("{").equals(numChars.get("}")))
                    && (numChars.get("<").equals(numChars.get(">")))) {
                splitStr.add(currentSubString.toString());
                currentSubString.setLength(0);
            }
            else {
                currentSubString.append(currentChar);
            }

            // increment the various counters depending on the character
            if (numChars.containsKey(currentCharString)) {
                numChars.put(currentCharString, numChars.get(currentCharString) + 1);
            }
        }
        // At the end of the loop, add the currentSubString to the splitStr
        splitStr.add(currentSubString.toString());

        // check that the string had correct numbers of un-escaped quotes, brackets, and </>
        if (((numChars.get("\"") % 2) != 0)) {
            throw new RuntimeException("Error: Input string " + inputString + " did not have"
                    + " an even number of double quotes. Could not parse correctly.");
        }
        if (!numChars.get("[").equals(numChars.get("]")) || !numChars.get("{").equals(numChars.get("}")) || !numChars.get("<").equals(numChars.get(">"))) {
            throw new RuntimeException("Error: Input string " + inputString + " had unequal numbers"
                    + " of either [], {}, or <>. Could not parse correctly.");
        }

        // return the splitStr list cast to a String[]
        return splitStr.toArray(new String[splitStr.size()]);
    }


    /**
     * parses an activity type list string into a list of activity types
     * an input string of "NONE" or "ALL" may be provided to get a list of
     * none or all of the activity instances of those types respectively
     */
    public static List<String> parseActivityTypeStringByInstance(String inputString) {

        if (inputString.equals("NONE")) {
            return new ArrayList<>();

        }
        else if (inputString.equals("ALL")) {
            ActivityInstanceList activityList = ActivityInstanceList.getActivityList();
            ArrayList<String> activities = new ArrayList<>();
            for (int i = 0; i < activityList.length(); i++) {
                String actType = activityList.get(i).getType();
                if (!activities.contains(actType)) {
                    activities.add(actType);
                }
            }
            return activities;

        }
        else {
            return StringParsingUtilities.splitStringByWhitespace(inputString);
        }
    }

    /**
     * parses an activity type list string into a list of activity types
     * an input string of "NONE" or "ALL" may be provided to get a list of
     * none or all of the activity type names respectively
     */
    public static List<String> parseActivityTypeStringByType(String inputString) {

        if (inputString.equals("NONE")) {
            return new ArrayList<>();
        }
        else if (inputString.equals("ALL")) {
            return ActivityTypeList.getActivityList().getNamesOfAllDefinedTypes();
        }
        else {
            return StringParsingUtilities.splitStringByWhitespace(inputString);
        }
    }

    /**
     * parses a resource name list string into a list of resource names
     * an input string of "NONE" or "ALL" may be provided to get a list of
     * none or all of the resources respectively
     */
    public static List<String> parseResourceNameString(String inputString) {

        if (inputString.equals("NONE")) {
            return new ArrayList<>();

        }
        else if (inputString.equals("ALL")) {
            List<Resource> resourceList = ResourceList.getResourceList().getListOfAllResources();
            ArrayList<String> resources = new ArrayList<>();
            for (Resource res : resourceList) {
                resources.add(res.getUniqueName());
            }
            return resources;

        }
        else {
            return Arrays.asList(StringParsingUtilities.splitParamStringByChar(inputString, ' '));
        }
    }

    public static List<String> parseConstraintTypeString(String inputString) {

        if (inputString.equals("NONE")) {
            return new ArrayList<>();
        }
        else if (inputString.equals("ALL")) {
            ConstraintInstanceList constraintList = ConstraintInstanceList.getConstraintList();
            ArrayList<String> constraints = new ArrayList<>();
            for (int i = 0; i < constraintList.length(); i++) {
                String constraintType = constraintList.get(i).getName();
                if (constraintType != null && !constraints.contains(constraintType)) {
                    constraints.add(constraintType);
                }
            }
            return constraints;
        }
        else {
            return StringParsingUtilities.splitStringByWhitespace(inputString);
        }
    }

    /**
     * Splits a string of activity types by spaces
     */
    public static List<String> splitStringByWhitespace(String inputString) {
        return Arrays.asList(inputString.split("\\s+"));
    }
}
