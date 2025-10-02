package gov.nasa.jpl.command;

import gov.nasa.jpl.input.HistoryReader;
import gov.nasa.jpl.input.PlanJSONHistoryReader;
import gov.nasa.jpl.input.XMLTOLHistoryReader;
import gov.nasa.jpl.input.parallel.ParallelDirectoryReader;

import java.io.File;
import java.io.IOException;

public class OpenFileCommand implements Command {
    String filename;
    // most times you read resources in you want them frozen since they are a previous final calculation
    boolean areResourcesFrozen = true;
    // most times you read activities in you want to not decompose since they'd contain parents and children, but sometimes you do want that
    boolean shouldActivitiesDecompose = false;

    public OpenFileCommand(String commandString) {
        String[] tokens = commandString.split(" ");
        filename = tokens[0];

        if (tokens.length > 1 && tokens[1].equalsIgnoreCase("unfrozen")) {
            areResourcesFrozen = false;
        }
        else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("decompose")) {
            shouldActivitiesDecompose = true;
        }

        if (tokens.length > 2 && tokens[2].equalsIgnoreCase("unfrozen")) {
            areResourcesFrozen = false;
        }
        else if (tokens.length > 2 && tokens[2].equalsIgnoreCase("decompose")) {
            shouldActivitiesDecompose = true;
        }
    }

    @Override
    public void execute() throws CommandException {
        try {
            File inputFile = new File(filename);
            if (!(inputFile.exists())) {
                throw new CommandException("Could not find or read file " + filename);
            }

            HistoryReader reader = chooseHistoryReaderBasedOnFileType(filename);
            reader.readInHistoryOfActivitiesAndResource(areResourcesFrozen, shouldActivitiesDecompose);
        }
        catch (IOException | RuntimeException e) {
            throw new CommandException("HistoryReader encountered an error while reading in " + filename + " :\n" + e.getMessage());
        }
    }

    @Override
    public void unExecute() throws CommandException {
        // does not do anything. unclear if you should be able to undo reading a history in
    }

    private HistoryReader chooseHistoryReaderBasedOnFileType(String inFileName) throws IOException {
        // XMLTOL
        if (inFileName.endsWith(".xml")) {
            return new XMLTOLHistoryReader(inFileName);
        }
        else if(inFileName.endsWith(".dir")){
            return new ParallelDirectoryReader(inFileName);
        }
        else if(inFileName.endsWith(".plan.json")){
            return new PlanJSONHistoryReader(inFileName);
        }
        // if we want to add a CSV or any other kind of history file, it'd go here
        else {
            throw new IOException("No HistoryReader currently available for specified file suffix");
        }
    }
}
