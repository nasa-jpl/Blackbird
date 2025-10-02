package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;

/**
 * This class manages writing to a 'flat' TOL CSV-like file, a legacy format that certain missions might request due to
 * its smaller size than XMLTOL. There are python scripts floating around the planning community that parse such files.
 * However, due to how it stringifys complex data types, it is also difficult to read back in to Blackbird to reconstruct
 * the history, so it is not recommended to try to set that up.
 */
public class FlatTOLWriter extends TOLWriter {
    @Override
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        writeTOLRecords(actList, resList, conList, startTime, endTime);
    }

    private void writeTOLRecords(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList constraintList, Time startTime, Time endTime){
        List<Iterator<TOLRecord>> allTOLRecords = new ArrayList<>();
        allTOLRecords.add(new TOLActivityIterator(actList.createListOfActivityBeginAndEndTimes()));
        allTOLRecords.add(new TOLResourceIterator(resList.getResourcesIterator(startTime, endTime)));
        allTOLRecords.add(new TOLConstraintIterator(constraintList.createListOfConstraintBeginAndEndTimes()));

        Iterator<TOLRecord> iteratorOverAllRecords = IteratorUtils.collatedIterator(Comparator.naturalOrder(), (Collection) allTOLRecords);

        // now we walk through the whole plan in time order
        while (iteratorOverAllRecords.hasNext()) {
            TOLRecord record = iteratorOverAllRecords.next();
            Time recordTime = record.getTime();
            if ((startTime == null || recordTime.compareTo(startTime) >= 0) && (endTime == null || recordTime.compareTo(endTime) < 0)) {
                writer.print(record.toFlatTOL());
            }
        }
    }
}
