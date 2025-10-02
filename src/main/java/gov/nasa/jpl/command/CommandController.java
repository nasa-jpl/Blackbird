package gov.nasa.jpl.command;

import gov.nasa.jpl.input.RegexUtilities;

/**
 * This class takes in a command string, parses it, and issues the
 * appropriate command.
 */
public class CommandController {

    /**
     * Takes in a command as a string and issues the appropriate command.
     *
     * @param command       - name of command to be executed
     * @param commandString - string containing activity name, id, etc. depending on command
     * @return - boolean, true when execution finishes correctly or throws CommandException
     */
    public static boolean issueCommand(String command, String commandString) {
        Command newCommand = null;
        // switch statements for all commands
        if (command.equals(RegexUtilities.UNDO)) {
            newCommand = new UndoCommand();
        }
        else if (command.equals(RegexUtilities.REDO)) {
            newCommand = new RedoCommand();
        }
        else {
            // for anything other than undo/redo we need to add the command to
            // the undo stack and clear redo
            if(command.equals(RegexUtilities.CREATE_DICTIONARY)){
                newCommand = new CreateDictionaryCommand(commandString);
            }
            else if (command.equals(RegexUtilities.EDIT_ACTIVITY)) {
                newCommand = new EditActivityCommand(commandString);
            }
            else if (command.equals(RegexUtilities.INCON)) {
                newCommand = new InconCommand(commandString);
            }
            else if (command.equals(RegexUtilities.LOAD_KERNELS)) {
                newCommand = new LoadKernelsCommand(commandString);
            }
            else if (command.equals(RegexUtilities.MOVE_ACTIVITY)) {
                newCommand = new MoveActivityCommand(commandString);
            }
            else if (command.equals(RegexUtilities.NEW_ACTIVITY)) {
                newCommand = new NewActivityCommand(commandString);
            }
            else if (command.equals(RegexUtilities.OPEN_FILE)) {
                newCommand = new OpenFileCommand(commandString);
            }
            else if (command.equals(RegexUtilities.QUIT)) {
                newCommand = new QuitCommand();
            }
            else if (command.equals(RegexUtilities.REDECOMPOSE)) {
                newCommand = new RedecomposeCommand(commandString);
            }
            else if (command.equals(RegexUtilities.REMODEL)) {
                newCommand = new RemodelCommand();
            }
            else if (command.equals(RegexUtilities.REMOVE_ACTIVITY)) {
                newCommand = new RemoveActivityCommand(commandString);
            }
            else if (command.equals(RegexUtilities.SCHEDULE)) {
                newCommand = new ScheduleCommand(commandString);
            }
            else if (command.equals(RegexUtilities.SET_PARAMETER)) {
                newCommand = new SetParameterCommand(commandString);
            }
            else if (command.equals(RegexUtilities.SEVER_ACTIVITY)) {
                newCommand = new SeverParentCommand(commandString);
            }
            else if (command.equals(RegexUtilities.WRITE)) {
                newCommand = new WriteCommand(commandString);
            }
            else if (command.equals(RegexUtilities.SEQUENCE)) {
                newCommand = new SequenceCommand(commandString);
            }

            else {
                throw new CommandException("Error: Could not find available command in command string:\n" + command
                        + "\nCheck that the name of the command is spelled correctly.");
            }
            UndoRedoManager.clearRedoStack();
            UndoRedoManager.addToUndoStack(newCommand);
        }
        // execute the command
        newCommand.execute();
        return true;
    }
}
