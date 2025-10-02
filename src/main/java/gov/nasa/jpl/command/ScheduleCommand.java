package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.input.RegexUtilities;

import java.util.UUID;
import java.util.regex.Matcher;

public class ScheduleCommand implements Command {
    private UUID activityID;

    public ScheduleCommand(String commandString) {
        Matcher match = RegexUtilities.COMMAND_ID_PATTERN.matcher(commandString);

        if (match.find()) {
            activityID = UUID.fromString(match.group("id"));
        }
        else {
            throw new CommandException("Error: Could not find activity ID or parameters in command string for ScheduleCommand:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }


    @Override
    public void execute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity actInstance = actList.findActivityByID(activityID);
        actInstance.schedule();
    }

    @Override
    public void unExecute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity actInstance = actList.findActivityByID(activityID);
        actInstance.stopScheduling();
    }
}
