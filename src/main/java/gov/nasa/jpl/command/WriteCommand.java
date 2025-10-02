package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.input.StringParsingUtilities;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.output.csv.CSVWriter;
import gov.nasa.jpl.output.parallel.ParallelDirectoryWriter;
import gov.nasa.jpl.output.tol.*;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;


/**
 * This command writes out one of several file types depending on the input name. It can filter by activity or resource
 * type and time range.
 */
public class WriteCommand implements Command {
    String outfile;
    ActivityInstanceList actInstList;
    ResourceList resList;
    ConstraintInstanceList constraintList;
    Time startTime;
    Time endTime;

    public WriteCommand(String commandString) {

        Matcher commandMatch = RegexUtilities.WRITE_COMMAND_PATTERN.matcher(commandString);
        if (!commandMatch.find()) {
            throw new CommandException("WRITE command string has unexpected format: \"" + commandString + "\"");
        }

        outfile = commandString.split(" ")[0];
        Matcher startMatch = RegexUtilities.WRITE_START_PATTERN.matcher(commandString);
        Matcher endMatch = RegexUtilities.WRITE_END_PATTERN.matcher(commandString);

        if (startMatch.find()) {
            startTime = (Time) ReflectionUtilities.returnValueOf(ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE, startMatch.group("time"), true);
        }

        if (endMatch.find()) {
            endTime = (Time) ReflectionUtilities.returnValueOf(ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE, endMatch.group("time"), true);
        }

        actInstList = getActInstListFromString(commandString);
        resList = getResourceListFromString(commandString);
        constraintList = getConstraintListFromString(commandString);
    }

    @Override
    public void execute() throws CommandException {
        try {
            TOLWriter writer = chooseWriterBasedOnFileType(outfile);
            writer.dumpTimelinesToFile(outfile, actInstList, resList, constraintList, startTime, endTime);
        }
        catch (IOException e) {
            throw new CommandException("Could not find writer for specified filename: " + outfile);
        }
    }

    /**
     * Does not currently do anything.
     */
    @Override
    public void unExecute() throws CommandException {
    }

    /**
     * See class comments in each class that implements TOLWriter interface for the details of what each are
     * @param fileName
     * @return A class that implements the TOLWriter interface
     * @throws IOException
     */
    private TOLWriter chooseWriterBasedOnFileType(String fileName) throws IOException {
        if (fileName.endsWith(".xml")) {
            return new XMLTOLWriter();
        }
        else if (fileName.endsWith(".tol.json")) {
            return new JSONTOLWriter();
        }
        else if (fileName.endsWith(".fincon.json") || fileName.endsWith(".incon.json")) {
            return new JSONInconWriter();
        }
        else if (fileName.endsWith(".plan.json")) {
            return new JSONPlanWriter();
        }
        else if (fileName.endsWith(".constraints.json")) {
            return new JSONConstraintWriter();
        }
        else if (fileName.endsWith(".tol")) {
            return new FlatTOLWriter();
        }
        else if (fileName.endsWith(".csv")) {
            return new CSVWriter();
        }
        else if (fileName.endsWith(".dir")) {
            return new ParallelDirectoryWriter();
        }
        // to add a new kind of writer, add it here
        else {
            throw new IOException("No TOLWriter currently available for specified file suffix");
        }
    }

    private static ActivityInstanceList getActInstListFromString(String commandString){
        Matcher activitiesMatch = RegexUtilities.WRITE_ACTIVITIES_PATTERN.matcher(commandString);

        if (activitiesMatch.find()) {
            String action = activitiesMatch.group("action");
            List<String> activities = StringParsingUtilities.parseActivityTypeStringByInstance(activitiesMatch.group("activities"));

            return filterActInstList(activities, action);
        }
        else {
            return ActivityInstanceList.getActivityList();
        }
    }

    /**
     * Returns an ActivityInstanceList copy of the global ActivityInstanceList instance
     * filtered to include/exclude provided activity types
     *
     * @param activityTypes List of activity types to include/exclude
     * @param action        String to include/exclude the activities provided.  values determined by RegexUtilities.
     * @return ActivityInstanceList containing specified activities
     */
    private static ActivityInstanceList filterActInstList(List<String> activityTypes, String action) {

        // make sure all provided activity types are defined
        validateActivityTypes(activityTypes);

        // Create a new activity instance list from the main one, by filtering out specified activities
        ActivityInstanceList fullActInstList = ActivityInstanceList.getActivityList();
        ActivityInstanceList filteredActInstList = new ActivityInstanceList();

        for (int i = 0; i < fullActInstList.length(); i++) {
            Activity act = fullActInstList.get(i);

            if (action.equals(RegexUtilities.INCLUDE_STRING)) {
                if (activityTypes.contains(act.getType())) {
                    filteredActInstList.add(act);
                }
            }
            else if (action.equals(RegexUtilities.EXCLUDE_STRING)) {
                if (!activityTypes.contains(act.getType())) {
                    filteredActInstList.add(act);
                }
            }
            else {
                throw new CommandException("action \"" + action + "\" not valid. Valid actions are INCLUDE or EXCLUDE .");
            }
        }

        return filteredActInstList;
    }

    /**
     * checks that every activity type in the provided array is a valid activity type
     * throws a CommandException if an invalid resource is found
     */
    public static void validateActivityTypes(List<String> activityTypes) {
        for (String actType : activityTypes) {
            try {
                ActivityTypeList.getActivityList().getActivityClass(actType);
            }
            catch (CommandException e) {
                throw new CommandException("Activity type \"" + actType + "\" not found.");
            }
        }
    }

    public static ResourceList getResourceListFromString(String commandString){
        Matcher resourcesMatch = RegexUtilities.WRITE_RESOURCES_PATTERN.matcher(commandString);

        if (resourcesMatch.find()) {
            String action = resourcesMatch.group("action");
            List<String> resources = StringParsingUtilities.parseResourceNameString(resourcesMatch.group("resources"));

            return filterResourceList(resources, action);
        }
        else {
            return ResourceList.getResourceList();
        }
    }

    /**
     * Returns a ResourceList containing all resources indicated by the parameters
     *
     * @param resTypes List of resource names to include/exclude
     * @param action   String to include/exclude the resources provided. values determined by RegexUtilities.
     * @return ResourceList containing specified resources
     */
    private static ResourceList filterResourceList(List<String> resTypes, String action) {

        // make sure all provided resources are defined
        // an error will be thrown if not
        validateResources(resTypes);

        // Create a new resource list from the main one, by filtering out specified resources
        List<Resource> fullResourceList = ResourceList.getResourceList().getListOfAllResources();
        ResourceList filteredResourceList = new ResourceList();

        for (int i = 0; i < fullResourceList.size(); i++) {
            Resource res = fullResourceList.get(i);

            if (action.equals(RegexUtilities.INCLUDE_STRING)) {
                if (resTypes.contains(res.getUniqueName()) || resTypes.contains(res.getName())) {
                    filteredResourceList.registerResource(res);
                }
            }
            else if (action.equals(RegexUtilities.EXCLUDE_STRING)) {
                if (! (resTypes.contains(res.getUniqueName()) || resTypes.contains(res.getName()))) {
                    filteredResourceList.registerResource(res);
                }
            }
            else {
                throw new CommandException("action \"" + action + "\" not valid.");
            }
        }

        return filteredResourceList;
    }

    /**
     * checks that every resource name in the provided array is a valid resource
     * throws a CommandException if an invalid resource is found
     */
    private static void validateResources(List<String> resTypes) {
        for (String res : resTypes) {
            boolean exists = false;
            List<Resource> resList = ResourceList.getResourceList().getListOfAllResources();
            for (Resource resInst : resList) {
                if (resInst.getName().equals(res) || resInst.getUniqueName().equals(res)) {
                    exists = true;
                }
            }
            if (!exists) {
                throw new CommandException("Resource with name \"" + res + "\" not found in adaptation.");
            }
        }
    }

    public static ConstraintInstanceList getConstraintListFromString(String commandString){
        Matcher constraintsMatch = RegexUtilities.WRITE_CONSTRAINTS_PATTERN.matcher(commandString);

        if (constraintsMatch.find()) {
            String action = constraintsMatch.group("action");
            List<String> constraints = StringParsingUtilities.parseConstraintTypeString(constraintsMatch.group("constraints"));

            return filterConstraintList(constraints, action);
        }
        else {
            return ConstraintInstanceList.getConstraintList();
        }
    }

    /**
     * Returns a ConstraintInstanceList containing all constraints indicated by the parameters
     *
     * @param constraintTypes List of constraint types to include/exclude
     * @param action          String to include/exclude the constraints provided. values determined by RegexUtilities.
     * @return ConstraintInstanceList containing specified resources
     */
    private static ConstraintInstanceList filterConstraintList(List<String> constraintTypes, String action) {

        // make sure all provided constraints are defined
        validateConstraints(constraintTypes);

        // Create a new constraint list from the main one, by filtering out specified constraints
        ConstraintInstanceList fullConstraintList = ConstraintInstanceList.getConstraintList();
        ConstraintInstanceList filteredConstraintList = new ConstraintInstanceList();

        for (int i = 0; i < fullConstraintList.length(); i++) {
            Constraint constraint = fullConstraintList.get(i);

            if (action.equals(RegexUtilities.INCLUDE_STRING)) {
                if (constraintTypes.contains(constraint.getName())) {
                    filteredConstraintList.registerConstraint(constraint);
                }
            }
            else if (action.equals(RegexUtilities.EXCLUDE_STRING)) {
                if (!constraintTypes.contains(constraint.getName())) {
                    filteredConstraintList.registerConstraint(constraint);
                }
            }
            else {
                throw new RuntimeException("action \"" + action + "\" not valid. Valid actions are INCLUDE or EXCLUDE .");
            }
        }

        return filteredConstraintList;
    }

    /**
     * checks that every constraint type in the provided array is a valid constraint
     * throws a CommandException if an invalid constraint is found
     */
    private static void validateConstraints(List<String> constraintTypes) {
        ConstraintInstanceList constraintList = ConstraintInstanceList.getConstraintList();
        for (String constraint : constraintTypes) {
            boolean exists = false;
            for (int i = 0; i < constraintList.length(); i++) {
                if (constraint.equals(constraintList.get(i).getName())) {
                    exists = true;
                }
            }
            if (!exists) {
                throw new CommandException("Constraint with name \"" + constraint + "\" not found.");
            }
        }
    }
}