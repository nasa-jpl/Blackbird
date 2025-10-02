package gov.nasa.jpl.command;

import java.util.*;
import java.util.regex.Matcher;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.input.StringParsingUtilities;

// this command edits an activity
public class EditActivityCommand implements Command {
    private UUID activityID;
    private Object[] activityParams;

    /**
     * Takes in an id and set of parameter name and parameter value pairs. Searches
     * for the activity instance with the given id and then edits the input parameters
     * to be equal to the new values.
     *
     * @param commandString - id of activity followed by parameter name/value pairs
     */
    public EditActivityCommand(String commandString) {
        Matcher match = RegexUtilities.COMMAND_ID_PARAM_PATTERN.matcher(commandString);

        if (match.find()) {
            activityID = UUID.fromString(match.group("id"));
            setActivityParams(match.group("params"));
        }
        else {
            throw new CommandException("Error: Could not find activity ID or parameters in command string for EditActivity:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    /**
     * This method edits an activity by its ID.
     */
    @Override
    public void execute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity oldActInstance = actList.findActivityByID(activityID);
        Class<?> newActivityClass = oldActInstance.getClass();

        // get array of input args required for new activity
        List<Object> argsList = new ArrayList<>();
        argsList.add(oldActInstance.getStart());
        argsList.addAll(Arrays.asList(activityParams));
        Object[] args = argsList.toArray();

        // instantiate new activity and remove old one
        oldActInstance.deleteChildren();
        actList.remove(oldActInstance);
        ReflectionUtilities.instantiateActivity(newActivityClass, args, activityID);
        activityParams = oldActInstance.getParameterObjects();
    }

    /**
     * Undoes the editing of an activity.
     */
    @Override
    public void unExecute() throws CommandException {
        execute();
    }

    /**
     * Updates activityParams with the parameters for the stored activity, with
     * any parameters specified being updated to the new value.
     *
     * @param parameterString
     * @return
     */
    private void setActivityParams(String parameterString) {
        ActivityInstanceList actInstanceList = ActivityInstanceList.getActivityList();
        ActivityTypeList actTypeList = ActivityTypeList.getActivityList();
        Activity act = actInstanceList.findActivityByID(activityID);

        List<Map<String, String>> parameters = actTypeList.getParameters(
                ReflectionUtilities.getClassNameWithoutPackage(act.getClass().getName()));
        Object[] parameterObjects = act.getParameterObjects();

        // this should not ever be the case, but throw error if it is
        if (parameters.size() != parameterObjects.length) {
            throw new CommandException("Error: Arrays for parameter names and objects for activity "
                    + act.getClass().getName() + " with ID " + activityID + " in EditActivity did not have"
                    + "the same length. Could not parse correctly.");
        }
        combineUpdatedActivityParameters(parameterString, parameters, parameterObjects);
    }

    /**
     * Instantiates activityParams, an array of argument objects needed to instantiate a new activity,
     * with any input parameters replaced in the array.
     *
     * @param parameterString
     * @param parameters
     * @param parameterObjects
     * @return
     */
    private void combineUpdatedActivityParameters(String parameterString, List<Map<String, String>> parameters, Object[] parameterObjects) {
        String[] splitParamString = StringParsingUtilities.splitParamStringByChar(parameterString, ',');
        activityParams = parameterObjects.clone();

        for (String subStr : splitParamString) {
            Matcher match = RegexUtilities.PARAMETER_NAME_VALUE_PATTERN.matcher(subStr);

            if (match.find()) {
                String paramName = match.group("name");
                int paramIndex = -1;
                for (int i = 0; i < parameters.size(); i++) {
                    if (parameters.get(i).get("name").equals(paramName)) {
                        paramIndex = i;
                        break;
                    }
                }
                if (paramIndex == -1) {
                    throw new CommandException("Error: Could not find parameter " + paramName + " in available"
                            + " parameters in EditActivity for activity with ID " + activityID + ".");
                }
                Object paramValue = ReflectionUtilities.returnValueOf(parameters.get(paramIndex).get("type"), match.group("value"), true);

                activityParams[paramIndex] = paramValue;
            }
            else {
                throw new CommandException("Error: Could not find parameter name/value in parameter substring "
                        + subStr + ". Cannot parse correctly in EditActivity for activity  with ID "
                        + activityID + ".");
            }
        }
    }
}