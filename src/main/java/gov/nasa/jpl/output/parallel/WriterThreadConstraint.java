package gov.nasa.jpl.output.parallel;

import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import static gov.nasa.jpl.input.RegexUtilities.CON;

public class WriterThreadConstraint implements Runnable {
    private Constraint con;
    private String dir;
    private Time begin;
    private Time end;
    private PrintWriter writer;

    public WriterThreadConstraint(Constraint con, String dir, Time begin, Time end) {
        this.con = con;
        this.dir = dir;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public void run() {
        String filename = dir + File.separator + CON + File.separator + con.getName() + ".csv";

        try {
            writer = new PrintWriter(filename, "UTF-8");

            // serially for each violation for the type, write out the time then the violation text. since constraints are currently not re-read back in, no special formatting is needed
            Iterator<Map.Entry<Time, Time>> iter = con.historyIterator();
            while (iter.hasNext()) {
                Map.Entry<Time, Time> entry = iter.next();
                writer.println(entry.getKey() + "," + entry.getValue());
            }

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find or create file " + filename + " when trying to write constraints to directory. Error: " + e.getMessage());
        } finally {
            writer.close();
        }

    }
}