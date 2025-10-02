package gov.nasa.jpl.output;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

public abstract class TOLWriter {
    protected PrintWriter writer;

    public void createFile(String name) {
        try {
            writer = new PrintWriter(name, "UTF-8");
        }
        catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public abstract void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime);

    public void closeFile() {
        writer.close();
    }

    public void dumpTimelinesToFile(String filename, ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        createFile(filename);
        writeFileContents(actList, resList, conList, startTime, endTime);
        closeFile();
    }
}
