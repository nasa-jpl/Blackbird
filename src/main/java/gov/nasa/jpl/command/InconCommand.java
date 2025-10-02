package gov.nasa.jpl.command;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.InitialConditionActivity;
import gov.nasa.jpl.engine.InitialConditionList;
import gov.nasa.jpl.input.InconReader;
import gov.nasa.jpl.input.XMLInconReader;
import gov.nasa.jpl.input.JSONInconReader;

import java.io.IOException;

public class InconCommand implements Command {
    String inFileName;
    InitialConditionList incon;
    InitialConditionActivity inconActivity;

    public InconCommand(String inFileName) {
        this.inFileName = inFileName;
    }

    @Override
    public void execute() throws CommandException {
        // we only want to do this once, if we haven't been initialized yet - this would go in constructor but right now only execute() in CommandController is catching CommandException
        if (incon == null) {
            InconReader read = null;
            try {
                read = chooseInconReaderBasedOnFileType(inFileName);
                incon = read.getInitialConditions();
            }
            catch (IOException e) {
                throw new CommandException(e.getMessage());
            }
            inconActivity = new InitialConditionActivity(incon.getInconTime(), incon.getEntireInconMap());
        }
        else {
            ActivityInstanceList.getActivityList().add(inconActivity);
        }
    }

    @Override
    public void unExecute() throws CommandException {
        ActivityInstanceList.getActivityList().remove(inconActivity);
    }

    private InconReader chooseInconReaderBasedOnFileType(String inFileName) throws IOException {
        // XMLTOL
        if (inFileName.endsWith(".xml")) {
            return new XMLInconReader(inFileName);
        }
        // JSON
        if (inFileName.endsWith(".fincon.json") || inFileName.endsWith(".incon.json")) {
            return new JSONInconReader(inFileName);
        }
        // if we want to add a CSV or any other kind of incon, it'd go here
        else {
            throw new IOException("No InconReader currently available for specified file suffix");
        }
    }
}
