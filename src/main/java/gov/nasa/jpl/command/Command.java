package gov.nasa.jpl.command;

public interface Command {
    public void execute() throws CommandException;

    public void unExecute() throws CommandException;
}