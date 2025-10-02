package gov.nasa.jpl.input.parallel;

import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.io.*;

public class ReaderThreadResource implements Runnable{
    private File toRead;
    private Resource<Comparable> res;
    private boolean isFrozen;

    public ReaderThreadResource(File toRead, boolean isFrozen){
        this.toRead = toRead;
        String resName = toRead.toPath().getFileName().toString().replaceAll(".csv","");
        res = (Resource<Comparable>) ResourceList.getResourceList().get(resName);
        this.isFrozen = isFrozen;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(toRead))) {
            String resType = res.getDataType();
            int lineNo = 0;
            String line;
            while ((line = br.readLine()) != null) {
                int c1 = line.indexOf(',');
                int c2 = line.indexOf(',', c1 + 1);
                lineNo++;

                if (c1 == -1 || c2 == -1) {
                    throw new RuntimeException("Invalid format at line " + lineNo + " in file " + toRead);
                }

                // line format has a human-readable date string (we ignore), date in tics, and then value
                // String humanTime = line.substring(0, c1);
                String tics = line.substring(c1 + 1, c2);
                String valueStr = line.substring(c2 + 1);

                Time t = Time.fromTics(Long.parseLong(tics));
                Comparable value = (Comparable) ReflectionUtilities.returnValueOf(resType, valueStr, true);

                // actually insert record
                // even though TreeMap isn't thread-safe, the beauty of this approach is that each Thread gets one resource history/TreeMap to populate
                res.insertRecord(this, t, value);
            }

            res.setFrozen(isFrozen);

        } catch (IOException e) {
            throw new RuntimeException("Could not open file " + toRead.toString() + " when trying to read resources from directory. Error: " + e.getMessage());
        }
    }
}
