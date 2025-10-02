package gov.nasa.jpl.command;

import gov.nasa.jpl.command.CommandException;

// this command removes an activity
public class QuitCommand implements Command {

    public QuitCommand() {
    }

    /**
     * Exits the program with exit status 0.
     */
    @Override
    public void execute() throws CommandException {
        System.exit(0);
    }

    /**
     * Does not currently do anything.
     */
    @Override
    public void unExecute() throws CommandException {
    }
}