package gov.nasa.jpl.output.parallel;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import static gov.nasa.jpl.input.ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE;
import static gov.nasa.jpl.input.RegexUtilities.RES;

public class WriterThreadResource implements Runnable {
    private Resource<Comparable> res;
    private String dir;
    private Time begin;
    private Time end;
    private PrintWriter writer;

    public WriterThreadResource(Resource<Comparable> res, String dir, Time begin, Time end){
        this.res = res;
        this.dir = dir;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public void run() {
        String filename = dir + File.separator + RES + File.separator + res.getUniqueName() + ".csv";

        try {
            writer = new PrintWriter(filename, "UTF-8");

            // serially for each value node in the resource history, write out the human-readable time, then the time in its backing format for fast read-in, then the resource value
            Iterator<Map.Entry<Time, Comparable>> iter = res.historyIterator(begin, end);
            if(res.getDataType().equals(ABSOLUTE_TIME_CLASS_PACKAGE)){
                while(iter.hasNext()){
                    Map.Entry<Time, Comparable> entry = iter.next();
                    Time t = entry.getKey();
                    // writing all Time values to absolute
                    writer.println(t.toUTC() + "," + t.getTics() + "," + ((Time) entry.getValue()).toUTC());
                }
            }
            else {
                while(iter.hasNext()){
                    Map.Entry<Time, Comparable> entry = iter.next();
                    Time t = entry.getKey();
                    writer.println(t.toUTC() + "," + t.getTics() + "," + entry.getValue());
                }
            }

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find or create file " + filename + " when trying to write resources to directory. Error: " + e.getMessage());
        } finally {
            writer.close();

        }
    }
}
