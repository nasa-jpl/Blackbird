package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.input.RegexUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

public class RedecomposeCommand implements Command {
    private UUID activityID;
    private List<Activity> children;

    public RedecomposeCommand(String commandString) {
        Matcher match = RegexUtilities.COMMAND_ID_PATTERN.matcher(commandString);
        children = new ArrayList<>();

        if (match.find()) {
            activityID = UUID.fromString(match.group("id"));
        }
        else {
            throw new CommandException("Error: Could not find activity ID or parameters in command string for RedecomposeCommand:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    @Override
    public void execute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity actInstance = actList.findActivityByID(activityID);
        // we make a copy of the old children list before deleting it so undo can reset to that
        children.addAll(actInstance.getChildren());

        actInstance.deleteChildren();
        actInstance.decompose();
    }

    @Override
    public void unExecute() throws CommandException {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        Activity actInstance = actList.findActivityByID(activityID);
        actInstance.deleteChildren();
        actInstance.setChildren(children);
        actInstance.addChildrenToInstanceList();
    }
}
