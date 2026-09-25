package gov.nasa.jpl.output.csv;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.output.parallel.ParallelDirectoryWriter;
import gov.nasa.jpl.output.tol.JSONPlanWriter;
import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

public class CSVWriter extends TOLWriter {

    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime, String resourcesWindow, String activitiesAtStart) {
        for (int i = 0; i < actList.length(); i++) {
            Activity act = actList.get(i);
            if(ActivityInstanceList.shouldIncludeActivity(act, startTime, endTime, activitiesAtStart)){
                writer.println(act.getType() + "," + act.getStart());
            }
        }

        List<Resource> listOfRelevantResources = resList.getListOfAllResources();

        for (int i = 0; i < listOfRelevantResources.size(); i++) {
            Resource currentResource = listOfRelevantResources.get(i);
            Iterator<Map.Entry<Time, Comparable>> thisResourceHistory = currentResource.historyIterator(startTime, endTime, false);
            if(resourcesWindow.equals(RegexUtilities.PAST_SET_STRING) && startTime!=null && !startTime.equals(currentResource.nextTimeSet(startTime, true))){
                if(DoubleResource.class.isAssignableFrom(currentResource.getClass()) && currentResource.getInterpolation().equalsIgnoreCase("linear")) {
                    writer.println(currentResource.getName() + "," + startTime + "," + ((DoubleResource) currentResource).interpval(startTime));
                }
                else{
                    writer.println(currentResource.getName() + "," + startTime + "," + currentResource.valueAt(startTime));
                }
            }
            while (thisResourceHistory.hasNext()) {
                Time currentTime = thisResourceHistory.next().getKey();
                writer.println(currentResource.getName() + "," + currentTime + "," + currentResource.valueAt(currentTime));
            }
        }
    }

}
