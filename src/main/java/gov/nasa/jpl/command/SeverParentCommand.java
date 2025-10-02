package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.input.RegexUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

public class SeverParentCommand implements Command {
    Activity activityToSever;
    List<Activity> childrenBecomingTopLevelActivities;

    public SeverParentCommand(String commandString) {
        Matcher match = RegexUtilities.COMMAND_ID_PATTERN.matcher(commandString);

        if (match.find()) {
            UUID activityID = UUID.fromString(match.group("id"));
            activityToSever = ActivityInstanceList.getActivityList().findActivityByID(activityID);
            childrenBecomingTopLevelActivities = new ArrayList();
            for (Activity child : activityToSever.getChildren()) {
                childrenBecomingTopLevelActivities.add(child);
            }
        }
        else {
            throw new CommandException("Error: Could not find activity ID in command string for SeverParentCommand:\n"
                    + commandString + "\nCheck format of command string.");
        }
    }

    @Override
    public void execute() throws CommandException {
        for (Activity child : childrenBecomingTopLevelActivities) {
            // this makes the child a top-level activity
            child.setParent(null);
        }
        activityToSever.clearChildrenArrayWithoutDeletingThem();
        ActivityInstanceList.getActivityList().remove(activityToSever);
    }

    @Override
    public void unExecute() throws CommandException {
        // we add back in the activity we removed from the modeling queue
        ActivityInstanceList.getActivityList().add(activityToSever);
        for (Activity child : childrenBecomingTopLevelActivities) {
            // this makes all formerly child activities children again and sets the children array correctly
            // this is usually paired with new Activity() which adds to the instance list, but since we don't
            // want to do that again, we use the already existing child
            activityToSever.spawn(child);
        }
    }
}
