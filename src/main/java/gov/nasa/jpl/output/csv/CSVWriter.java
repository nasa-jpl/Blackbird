package gov.nasa.jpl.output.csv;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

public class CSVWriter extends TOLWriter {

    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        for (int i = 0; i < actList.length(); i++) {
            Activity act = actList.get(i);
            Time actStart = act.getStart();
            if ((startTime == null || actStart.compareTo(startTime) >= 0) && (endTime == null || actStart.compareTo(endTime) < 0)) {
                writer.println(act.getType() + "," + actStart);
            }
        }

        List<Resource> listOfRelevantResources = resList.getListOfAllResources();

        for (int i = 0; i < listOfRelevantResources.size(); i++) {
            Resource currentResource = listOfRelevantResources.get(i);
            Iterator<Map.Entry<Time, Comparable>> thisResourceHistory = currentResource.historyIterator(startTime, endTime);
            while (thisResourceHistory.hasNext()) {
                Time currentTime = thisResourceHistory.next().getKey();
                if ((startTime == null || currentTime.compareTo(startTime) >= 0) && (endTime == null || currentTime.compareTo(endTime) < 0)) {
                    writer.println(currentResource.getName() + "," + currentTime + "," + currentResource.valueAt(currentTime));
                }
            }
        }
    }

}
