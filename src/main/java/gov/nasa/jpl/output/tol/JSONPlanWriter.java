package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;

public class JSONPlanWriter extends TOLWriter {

    @Override
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        writeJSONHeader();
        writeTOLRecords(actList, resList, conList, startTime, endTime);
        writeJSONFooter();
    }

    /*
     * Loop through interleaved activities only to write JSON blocks - currently does not write resource values or constraint violations
     */
    private void writeTOLRecords(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList constraintList, Time startTime, Time endTime){
        List<Iterator<TOLRecord>> allTOLRecords = new ArrayList<>();
        allTOLRecords.add(new TOLActivityIterator(actList.createListOfActivityBeginTimes()));

        Iterator<TOLRecord> iteratorOverAllRecords = IteratorUtils.collatedIterator(Comparator.naturalOrder(), (Collection) allTOLRecords);
        boolean first = true;

        // now we walk through the whole plan in time order
        while (iteratorOverAllRecords.hasNext()) {
            TOLRecord record = iteratorOverAllRecords.next();
            Time recordTime = record.getTime();
            if ((startTime == null || recordTime.compareTo(startTime) >= 0) && (endTime == null || recordTime.compareTo(endTime) < 0)) {
                if(!first) {
                    // add extra comma and newline for all but the first entry - the footer adds the newline without the last comma
                    writer.print(",\n");
                }
                writer.print(record.toPlanJSON());
                first = false;
            }
        }
    }

    private void writeJSONHeader(){
        // currently we don't need anything else in the header like a schema but that could change
        writer.println("{");
        writer.println("    \"activities\": [");
    }

    private void writeJSONFooter(){
        writer.println("\n    ]");
        writer.println("}");
    }

}
