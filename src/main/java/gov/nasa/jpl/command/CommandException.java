package gov.nasa.jpl.command;

/**
 * Custom exception for commands. Will be expanded in the future.
 */
public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
