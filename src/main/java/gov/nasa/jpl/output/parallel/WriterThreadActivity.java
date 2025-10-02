package gov.nasa.jpl.output.parallel;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.output.tol.TOLActivityBegin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static gov.nasa.jpl.input.RegexUtilities.ACT;

public class WriterThreadActivity implements Runnable {
    private String typeName;
    private List<Activity> instances;
    private String dirName;
    private PrintWriter writer;

    public WriterThreadActivity(String typeName, List<Activity> instances, String dirName){
        this.typeName = typeName;
        this.instances = instances;
        this.dirName = dirName;
    }

    @Override
    public void run() {
        String filename = dirName + File.separator + ACT + File.separator + typeName + ".csv";

        try {
            writer = new PrintWriter(filename, "UTF-8");

            // serially for each instance in the type, call toCSV to write one line in the file
            for(Activity act : instances){
                writer.println(new TOLActivityBegin(act).toCSV());
            }

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find or create file " + filename + " when trying to write activities to directory. Error: " + e.getMessage());
        } finally {
            writer.close();
        }

    }
}
