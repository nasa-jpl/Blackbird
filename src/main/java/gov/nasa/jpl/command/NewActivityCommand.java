package gov.nasa.jpl.command;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.RegexUtilities;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import static gov.nasa.jpl.input.ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE;
import static gov.nasa.jpl.input.ReflectionUtilities.RELATIVE_TIME_CLASS_PACKAGE;
import static gov.nasa.jpl.time.Time.TIME_PATTERN;

/**
 * This class takes in a class object and object array of parameters
 * for that class and then instantiates the class.
 */
public class NewActivityCommand implements Command {
    private Class<?> cls;
    private Object[] args;
    private UUID activityID;
    private RemoveActivityCommand removeAct;

    /**
     * This function parses an input commandString and stores the variables needed to
     * execute a newActivity command. This includes an activity name followed by
     * parameter type value pairs, separated by commas.
     * Ex: ActivityOne (String "hello", Integer 1)
     *
     * @param commandString - string of activity name followed by parameter type/value pairs
     */
    public NewActivityCommand(String commandString) {
        String activityName = "";
        String parameterString = "";

        // parse command string for activity name and parameters
        Matcher match = RegexUtilities.COMMAND_ACT_PARAM_PATTERN.matcher(commandString);

        // if regex matches then store variables, otherwise give error
        if (match.find()) {
            activityName = match.group("name");
            parameterString = match.group("params");

            // find the correct activity class
            ActivityTypeList actList = ActivityTypeList.getActivityList();
            cls = actList.getActivityClass(activityName);

            // make sure that the input class implements activity, if not then throw error
            if (!Activity.class.isAssignableFrom(cls)) {
                throw new CommandException("Error: Input class " + cls.getName() + " to NewActivityCommand"
                        + " does not extend the Activity class.\n");
            }

            // get the activity parameter types for parsing the parameter string
            List<Map<String, String>> parameters = actList.getParameters(activityName);
            String[] paramTypes = new String[parameters.size() + 1];

            Matcher absoluteTime = TIME_PATTERN.matcher(parameterString);
            if(absoluteTime.find()) {
                paramTypes[0] = ABSOLUTE_TIME_CLASS_PACKAGE;
            }
            else{
                paramTypes[0] = RELATIVE_TIME_CLASS_PACKAGE;
            }
            for (int i = 0; i < parameters.size(); i++) {
                paramTypes[i + 1] = parameters.get(i).get("type");
            }

            // parse the activity parameters into an object array for the new activity command
            args = ReflectionUtilities.parseActivityParameters(parameterString, paramTypes);
        }
        else {
            throw new CommandException("Error: Could not find activity name or parameters in command string for NewActivity:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    /**
     * This method adds the new activity instance to the plan.
     */
    @Override
    public void execute() throws CommandException {
        try {
            // if activityID has already been set then use undo remove activity instead of add activity
            if (removeAct != null) {
                removeAct.unExecute();
            }
            else {
                Object activityInstance = ConstructorUtils.invokeConstructor(cls, args);
                Activity castInstance = (Activity) activityInstance;
                activityID = castInstance.getID();
                castInstance.decompose();
            }

        }
        catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CommandException("Error: Failed to create new activity in NewActivityCommand. Check that the activity "
                    + cls.getName() + " exists and is defined correctly.");
        }
    }

    /**
     * Undoes the NewActivityCommand by removing the activity with the ID of
     * the newly added activity. This is implemented by calling a new
     * RemoveActivityCommand on the ID of the activity.
     */
    @Override
    public void unExecute() throws CommandException {
        removeAct = new RemoveActivityCommand(String.valueOf(activityID));
        removeAct.execute();
    }
}
