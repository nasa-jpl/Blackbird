package gov.nasa.jpl.command;

import gov.nasa.jpl.command.CommandException;

public class RedoCommand implements Command {

    public RedoCommand() {
    }

    /**
     * Executes the latest command in the redo stack.
     * Implemented by performing the unExecute method of an
     * UndoCommand.
     */
    @Override
    public void execute() throws CommandException {
        UndoRedoManager.redo();
    }

    /**
     * Does not do anything. This is because redo will never be undone. This is
     * done by using an undo command.
     */
    @Override
    public void unExecute() throws CommandException {
    }
}