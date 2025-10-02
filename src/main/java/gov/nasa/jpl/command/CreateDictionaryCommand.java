package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.input.StringParsingUtilities;
import gov.nasa.jpl.output.adaptation.AdaptationWriter;
import gov.nasa.jpl.output.adaptation.JSONDictionaryWriter;
import gov.nasa.jpl.resource.ResourceList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static gov.nasa.jpl.command.WriteCommand.*;

public class CreateDictionaryCommand implements Command{
    String outfile;

    List<String> activityTypeNames;
    ResourceList resList;
    ConstraintInstanceList constraintList;

    public CreateDictionaryCommand(String commandString){
        Matcher commandMatch = RegexUtilities.WRITE_COMMAND_PATTERN.matcher(commandString);
        if (!commandMatch.find()) {
            throw new CommandException("CREATE_DICTIONARY command string has unexpected format: \"" + commandString + "\"");
        }

        outfile = commandString.split(" ")[0];

        activityTypeNames = getActivityTypesFromString(commandString);
        resList = getResourceListFromString(commandString);
        constraintList = getConstraintListFromString(commandString);
    }

    @Override
    public void execute() throws CommandException {
        try {
            AdaptationWriter writer = chooseWriterBasedOnFileType(outfile);
            writer.writeAdaptationDictionary(outfile, activityTypeNames, resList, constraintList);
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

    private AdaptationWriter chooseWriterBasedOnFileType(String fileName) throws IOException {
        if (fileName.endsWith(".dict.json")) {
            return new JSONDictionaryWriter();
        }
        // to add a new kind of writer, add it here
        else {
            throw new IOException("No AdaptationWriter currently available for specified file suffix");
        }
    }

    private static List<String> getActivityTypesFromString(String commandString){
        Matcher activitiesMatch = RegexUtilities.WRITE_ACTIVITIES_PATTERN.matcher(commandString);

        if (activitiesMatch.find()) {
            String action = activitiesMatch.group("action");
            return filterActivityTypeList(StringParsingUtilities.parseActivityTypeStringByType(activitiesMatch.group("activities")), action);
        }
        else {
            return ActivityTypeList.getActivityList().getNamesOfAllDefinedTypes();
        }
    }

    private static List<String> filterActivityTypeList(List<String> activityTypes, String action){
        validateActivityTypes(activityTypes);

        List<String> fullActTypeList = ActivityTypeList.getActivityList().getNamesOfAllDefinedTypes();
        List<String> filteredActTypeList = new ArrayList<>();

        for (int i = 0; i < fullActTypeList.size(); i++) {
            String act = fullActTypeList.get(i);

            if (action.equals(RegexUtilities.INCLUDE_STRING)) {
                if (activityTypes.contains(act)) {
                    filteredActTypeList.add(act);
                }
            }
            else if (action.equals(RegexUtilities.EXCLUDE_STRING)) {
                if (!activityTypes.contains(act)) {
                    filteredActTypeList.add(act);
                }
            }
            else {
                throw new CommandException("action \"" + action + "\" not valid. Valid actions are INCLUDE or EXCLUDE .");
            }
        }

        return filteredActTypeList;
    }
}
