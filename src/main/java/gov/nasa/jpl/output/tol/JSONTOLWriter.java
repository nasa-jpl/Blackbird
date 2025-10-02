package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;

/**
 * This class produces a JSON TOL originally meant to provide input to Elasticsearch. It therefore mainpulates the data
 * (removing special characters, flattening lists and maps, etc) in ways that may make it hard to read back into the engine
 * at a later time. That being said, other scripts could use these JSON TOLs if they are
 * only looking to extract certain data about the plan.
 */
public class JSONTOLWriter extends TOLWriter {
    @Override
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        writeJSONHeader();
        writeActivityMetadata(actList);
        writeResourceMetadata(resList);
        writeTOLRecords(actList, resList, conList, startTime, endTime);
        writeJSONFooter();
    }

    private void writeJSONHeader(){
        // currently we don't need anything else in the header like a schema but that could change
        writer.println("[");
    }

    private void writeActivityMetadata(ActivityInstanceList actList){
        // keeps track of names we've added so far so we don't have duplicates
        Set<String> addedNames = new HashSet<>();

        for(int i = 0; i<actList.length(); i++){
            if(!addedNames.contains(actList.get(i).getType())){
                addedNames.add(actList.get(i).getType());
                writer.print(new TOLActivityMetadata(actList.get(i).getType()).toESJSON());
            }
        }
    }

    private void writeResourceMetadata(ResourceList resList) {
        List<Resource> listOfRelevantResources = resList.getListOfAllResources();
        // keeps track of resources we've added so far and throw exception if we have duplicates once processed, since this means the JSON will be ambiguous
        Set<String> addedNames = new HashSet<>();

        for (int i = 0; i < listOfRelevantResources.size(); i++) {
            Resource<?> currentRes = listOfRelevantResources.get(i);
            String jsonName = writeJSONFullNameWithUnderscores(currentRes);
            if(!addedNames.contains(jsonName)) {
                addedNames.add(jsonName);
                writer.print(new TOLResourceMetadata(currentRes).toESJSON());
            }
            else{
                throw new AdaptationException("Multiple individual resources ended up having the same fully qualified name when dimensional brackets were replaced with underscores for JSON. This will result in elasticsearch not being able to properly index records.\n" +
                        "To fix, change indices of one of the conflicting arrayed resources to ensure full names are unique when underscores denote indices.\n" +
                        "Resource full name that caused the problem: " + jsonName);
            }
        }
    }

    /*
     * Loop through interleaved activities and resources to write JSON blocks
     */
    private void writeTOLRecords(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList constraintList, Time startTime, Time endTime){
        List<Iterator<TOLRecord>> allTOLRecords = new ArrayList<>();
        allTOLRecords.add(new TOLActivityIterator(actList.createListOfActivityBeginTimes()));
        allTOLRecords.add(new TOLResourceIterator(resList.getResourcesIterator(startTime, endTime)));
        allTOLRecords.add(new TOLConstraintIterator(constraintList.createListOfConstraintBeginTimes()));

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
                writer.print(record.toESJSON());
                first = false;
            }
        }
    }

    private void writeJSONFooter(){
        writer.println("\n]");
    }

    /**
     * Elasticsearch needs a name it can query on that only has letters and underscores, whereas resource names or indices usually
     * have no such restriction. This method writes to the stringbuilder one name that conforms to those standards,
     * and another with full expressiveness that will be used to display
     * @param sb
     * @param r
     */
    static void writeJSONResourceNames(StringBuilder sb, Resource r){
        if(r.isIndexInArrayedResource()) {
            sb.append("    \"displayName\": \"" + r.getUniqueName() + "\",\n");
            sb.append("    \"name\": \"" + turnAllSpecialCharactersIntoUnderscores(r.getName() + "_" + String.join("_", r.getIndices())) + "\",\n");
        }
        else{
            sb.append("    \"displayName\": \"" + r.getName() + "\",\n");
            sb.append("    \"name\": \"" + turnAllSpecialCharactersIntoUnderscores(r.getName()) + "\",\n");
        }
    }

    private String writeJSONFullNameWithUnderscores(Resource r) {
        if (r.isIndexInArrayedResource()) {
            return turnAllSpecialCharactersIntoUnderscores(r.getName() + "_" + String.join("_", r.getIndices()));
        } else {
            return turnAllSpecialCharactersIntoUnderscores(r.getName());
        }
    }

    private static String turnAllSpecialCharactersIntoUnderscores(String in){
        return in.replaceAll("\\W","_");
    }
}
