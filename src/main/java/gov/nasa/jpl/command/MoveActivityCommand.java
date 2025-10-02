package gov.nasa.jpl.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.time.Time;

// this command edits an activity
public class MoveActivityCommand implements Command {
    private UUID activityID;
    private Time newStartTime = new Time();

    /**
     * Takes in an id and a time, which is surrounded by parenthesis.
     * Instantiates a new version of the activity at the given time,
     * storing the old activity instance for undo.
     *
     * @param commandString - id of activity followed by a new start time
     */
    public MoveActivityCommand(String commandString) {
        Matcher match = RegexUtilities.COMMAND_ID_PARAM_PATTERN.matcher(commandString);

        if (match.find()) {
            activityID = UUID.fromString(match.group("id"));
            newStartTime.valueOf(match.group("params"));
        }
        else {
            throw new CommandException("Error: Could not find activity ID in command string for MoveActivity:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    /**
     * Change the start time of an activity and store the current start time.
     */
    @Override
    public void execute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity oldActInstance = actList.findActivityByID(activityID);
        Class<?> newActivityClass = oldActInstance.getClass();

        // get args to instantiate new activity, only update start time
        List<Object> argsList = new ArrayList<>();
        argsList.add(newStartTime);
        argsList.addAll(Arrays.asList(oldActInstance.getParameterObjects()));
        Object[] args = argsList.toArray();

        // instantiate new activity and delete old one
        oldActInstance.deleteChildren();
        actList.remove(oldActInstance);
        ReflectionUtilities.instantiateActivity(newActivityClass, args, activityID);
        newStartTime = oldActInstance.getStart();
    }

    /**
     * Changes back the start time to the old value.
     */
    @Override
    public void unExecute() throws CommandException {
        execute();
    }
}
