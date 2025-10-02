package gov.nasa.jpl.output.tol;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.input.RegexUtilities;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import org.apache.commons.collections4.IteratorUtils;

/**
 * This class produces an XMLTOL, which contains all resource, activity, and constraint information available in the planning
 * engine at the time it was written in a very hierarchical, non-flattened format. Because of this, it is the baseline format for reading
 * back into Blackbird either as a plan history or initial conditions file.
 */
public class XMLTOLWriter extends TOLWriter {
    private ExecutorService exec;
    private int numAvailableCores;
    private final int MAX_BATCH_SIZE = 50000; // should be in the low megabytes per batch

    public XMLTOLWriter(){
        numAvailableCores = Runtime.getRuntime().availableProcessors() - 1;
        exec = Executors.newFixedThreadPool(numAvailableCores);
    }

    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        writeXMLHeader();
        writeResourceMetadata(resList);
        writeTOLRecords(actList, resList, conList, startTime, endTime);
        writeResFinalVal(resList, endTime);
        writeXMLFooter();
    }

    private void writeXMLHeader() {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<XML_TOL>");
    }

    private void writeResourceMetadata(ResourceList resList) {
        writer.println("    <ResourceMetadata>");

        List<Resource> listOfRelevantResources = resList.getListOfAllResources();

        for (int i = 0; i < listOfRelevantResources.size(); i++) {
            Resource<?> currentRes = listOfRelevantResources.get(i);

            Matcher disallowedMatch = RegexUtilities.XML_DISALLOWED_CHARACTERS.matcher(currentRes.getUniqueName());
            if (disallowedMatch.find()) {
                throw new AdaptationException("Arrayed resource with an index string that includes &, <, or > attempting to write out to XMLTOL, which will result in illegal XML.\n" +
                        "Resource name that will break XML: " + currentRes.getUniqueName());
            }
            else{
                writer.print(new TOLResourceMetadata(currentRes).toXML());
            }
        }
        writer.println("    </ResourceMetadata>");
    }

    /*
     * Loop through interleaved activities to write <TOLrecord> entries
     */
    private void writeTOLRecords(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList constraintList, Time startTime, Time endTime) {
        List<Iterator<TOLRecord>> allTOLRecords = new ArrayList<>();
        allTOLRecords.add(new TOLActivityIterator(actList.createListOfActivityBeginAndEndTimes()));
        allTOLRecords.add(new TOLResourceIterator(resList.getResourcesIterator(startTime, endTime)));
        allTOLRecords.add(new TOLConstraintIterator(constraintList.createListOfConstraintBeginAndEndTimes()));

        Iterator<TOLRecord> iteratorOverAllRecords = IteratorUtils.collatedIterator(Comparator.naturalOrder(), (Collection) allTOLRecords);
        List<TOLRecord> inBoundsTOLRecords = new ArrayList<>();

        // now we walk through the whole plan in time order and farm parts out to threads
        while (iteratorOverAllRecords.hasNext()) {
            TOLRecord record = iteratorOverAllRecords.next();
            Time recordTime = record.getTime();
            if ((startTime == null || recordTime.greaterThanOrEqualTo(startTime)) && (endTime == null || recordTime.lessThan(endTime))) {
                inBoundsTOLRecords.add(record);
            }
        }

        List<List<Map.Entry<Integer, Integer>>> subListIndices = breakLongListIntoStartEndSublistsByBatchAndCore(inBoundsTOLRecords.size(), MAX_BATCH_SIZE, numAvailableCores);
        // iterate over number of batches
        for(int j = 0; j < subListIndices.size(); j++) {
            List<Future<StringBuilder>> threads = new ArrayList<>();
            // iterate over number of cores
            for (int i = 0; i < subListIndices.get(j).size(); i++) {
                threads.add(exec.submit(new XMLTOLSnippetThread(inBoundsTOLRecords.subList(subListIndices.get(j).get(i).getKey(), subListIndices.get(j).get(i).getValue()))));
            }
            for (int i = 0; i < subListIndices.get(j).size(); i++) {
                try {
                    StringBuilder sb = threads.get(i).get();
                    writer.print(sb.toString());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        exec.shutdown();
    }

    public static List<List<Map.Entry<Integer, Integer>>> breakLongListIntoStartEndSublistsByBatchAndCore(int listSize, int batchSize, int numCores){
        List<List<Map.Entry<Integer, Integer>>> toReturn = new ArrayList<>();
        int numCoresToUse = Integer.min(numCores, listSize);
        int numBatches = listSize % batchSize == 0 ? listSize / batchSize : (listSize/batchSize)+1;

        for(int j = 0; j<numBatches; j++){
            toReturn.add(new ArrayList<>());
            int numRecordsInBatch;
            if(j<numBatches-1 || (listSize % batchSize) == 0){
                numRecordsInBatch = batchSize;
            }
            else{
                numRecordsInBatch = listSize % batchSize;
            }
            int minNumRecordsPerThread = numRecordsInBatch / numCoresToUse;
            int howManyCoresNeedOneMore = numRecordsInBatch % numCoresToUse;
            int pastEndIndex = batchSize*j;
            for(int i = 0; i<numCoresToUse; i++){
                int endIndex = pastEndIndex + (i<howManyCoresNeedOneMore ? minNumRecordsPerThread+1 : minNumRecordsPerThread);
                if(pastEndIndex >= endIndex){
                    break;
                }
                toReturn.get(toReturn.size()-1).add(new AbstractMap.SimpleImmutableEntry<>(pastEndIndex, endIndex));
                pastEndIndex = endIndex;
            }
        }
        return toReturn;
    }

    private void writeResFinalVal(ResourceList resList, Time endTime) {
        Time finconTime = resList.getLatestResourceUsage();
        if (endTime != null && endTime.compareTo(finconTime) < 0) {
            finconTime = endTime;
        }

        List<Resource> listOfRelevantResources = resList.getListOfAllResources();

        for (int i = 0; i < listOfRelevantResources.size(); i++) {
            Resource<?> currentRes = listOfRelevantResources.get(i);
            if (currentRes.resourceHistoryHasElements()) {
                writer.print(TOLResourceValue.writeResValBlock(finconTime, currentRes.valueAt(finconTime), currentRes, "RES_FINAL_VAL"));
            }
        }
    }

    private void writeXMLFooter() {
        writer.println("</XML_TOL>");
    }


}
