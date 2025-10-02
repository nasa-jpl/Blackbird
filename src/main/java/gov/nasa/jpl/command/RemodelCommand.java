package gov.nasa.jpl.command;

import gov.nasa.jpl.engine.ModelingEngine;

// this command removes an activity
public class RemodelCommand implements Command {

    public RemodelCommand() {
    }

    /**
     * Starts another modeling loop.
     */
    @Override
    public void execute() throws CommandException {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        myEngine.model();
    }

    /**
     * Does not currently do anything.
     */
    @Override
    public void unExecute() throws CommandException {
    }
}