package gov.nasa.jpl.output.adaptation;

import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public abstract class AdaptationWriter {
    protected PrintWriter writer;

    public void createFile(String name) {
        try {
            writer = new PrintWriter(name, "UTF-8");
        }
        catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public abstract void writeFileContents(List<String> actTypeList, ResourceList resList, ConstraintInstanceList conList);

    public void closeFile() {
        writer.close();
    }

    public void writeAdaptationDictionary(String filename, List<String> actTypeList, ResourceList resList, ConstraintInstanceList conList){
        createFile(filename);
        writeFileContents(actTypeList, resList, conList);
        closeFile();
    }
}
