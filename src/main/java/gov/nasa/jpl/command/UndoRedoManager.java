package gov.nasa.jpl.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoRedoManager {
    private static Deque<Object> undoStack = new ArrayDeque<>();
    private static Deque<Object> redoStack = new ArrayDeque<>();

    /**
     * Executes the most recent command stored in the undo stack. Adds the
     * command to the redo stack.
     */
    public static void undo() throws CommandException {
        if (!undoStack.isEmpty()) {
            Command command = (Command) undoStack.pop();
            addToRedoStack(command);
            command.unExecute();
        }
    }

    /**
     * Executes the most recent command stored in the redo stack. Adds the
     * command to the undo stack.
     */
    public static void redo() throws CommandException {
        if (!redoStack.isEmpty()) {
            Command command = (Command) redoStack.pop();
            addToUndoStack(command);
            command.execute();
        }
    }

    /**
     * Add a command to the undo stack.
     *
     * @param command
     */
    public static void addToUndoStack(Command command) {
        undoStack.push(command);
    }

    /**
     * Add a command to the redo stack.
     *
     * @param command
     */
    public static void addToRedoStack(Command command) {
        redoStack.push(command);
    }

    /**
     * Clears the redo stack. Should be done whenever a command is executed that
     * is not undo/redo.
     */
    public static void clearRedoStack() {
        redoStack = new ArrayDeque<>();
    }
}
