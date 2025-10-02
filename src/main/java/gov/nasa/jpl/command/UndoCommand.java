package gov.nasa.jpl.command;

public class UndoCommand implements Command {

    /**
     * Executes the most recent command stored in the undo stack. Adds the
     * command to the redo stack.
     */
    @Override
    public void execute() throws CommandException {
        UndoRedoManager.undo();
    }

    /**
     * Does not do anything. This is because undo will never be undone. This is
     * done by using a redo command.
     */
    @Override
    public void unExecute() throws CommandException {
    }
}