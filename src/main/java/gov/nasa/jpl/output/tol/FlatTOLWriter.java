package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.resource.Resource;
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
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime, String resourcesWindow, String activitiesAtStart) {
        writeTOLRecords(actList, resList, conList, startTime, endTime, resourcesWindow, activitiesAtStart);
    }

    private void writeTOLRecords(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList constraintList, Time startTime, Time endTime, String resourcesWindow, String activitiesAtStart){
        List<Iterator<TOLRecord>> allTOLRecords = new ArrayList<>();
        if(resourcesWindow.equals(RegexUtilities.PAST_SET_STRING) && startTime!=null){
            allTOLRecords.add(getInconTOLRecordIterator(resList, startTime).listIterator());
        }
        allTOLRecords.add(new TOLActivityIterator(actList.createListOfActivityBeginAndEndTimes(startTime, endTime, activitiesAtStart)));
        allTOLRecords.add(new TOLResourceIterator(resList.getResourcesIterator(startTime, endTime)));
        allTOLRecords.add(new TOLConstraintIterator(constraintList.createListOfConstraintBeginAndEndTimes(startTime, endTime)));

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

    // package protected so JSONTOLWriter can also use it
    static List<TOLRecord> getInconTOLRecordIterator(ResourceList resList, Time startTime){
        List<TOLRecord> toReturn = new ArrayList<>();
        for(Resource res: resList.getListOfAllResources()) {
            if (DoubleResource.class.isAssignableFrom(res.getClass()) && res.getInterpolation().equalsIgnoreCase("linear")) {
                toReturn.add(new TOLResourceValue(startTime, ((DoubleResource) res).interpval(startTime), res));
            } else {
                toReturn.add(new TOLResourceValue(startTime, res.valueAt(startTime), res));
            }
        }
        return toReturn;
    }
}
