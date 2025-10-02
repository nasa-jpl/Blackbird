package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.input.RegexUtilities;

import java.util.UUID;
import java.util.regex.Matcher;

// this command removes an activity
public class RemoveActivityCommand implements Command {
    private UUID activityID;
    private Activity myActivity;

    public RemoveActivityCommand(String commandString) {
        String activityIDString = "";

        // parse command string for activity ID
        Matcher match = RegexUtilities.COMMAND_ID_PATTERN.matcher(commandString);

        // if regex matches then attempt to remove the activity instance, otherwise give error
        if (match.find()) {
            activityIDString = match.group("id");
            activityID = UUID.fromString(activityIDString);
        }
        else {
            throw new CommandException("Error: Could not find activity ID in command string for RemoveActivity:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    /**
     * This method removes an activity by its ID.
     */
    @Override
    public void execute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        myActivity = actList.findActivityByID(activityID);
        actList.remove(activityID);
        myActivity.removeChildrenFromInstanceList();
    }

    /**
     * Undoes the removal of an activity.
     */
    @Override
    public void unExecute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        actList.add(myActivity);
        myActivity.addChildrenToInstanceList();
    }
}