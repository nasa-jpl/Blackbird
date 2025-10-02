package gov.nasa.jpl.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Time;

public class ResourceList {
    private Map<String, Resource> allResourceInstances;

    // we keep a main resource list with all resources in it in this class
    // however we do make other instances of this class for filtering resources for writing outfiles
    private static ResourceList instance = null;

    public ResourceList() {
        allResourceInstances = new HashMap<>();
    }

    public static ResourceList getResourceList() {
        if (instance == null) {
            instance = new ResourceList();
        }
        return instance;
    }

    /*
     *  to be used before the beginning of the modeling loop when we want to start over
     */
    public void resetResourceHistories() {
        for (Resource r : allResourceInstances.values()) {
            // we're excluding 'frozen' resources that were read in from a file and are not to be changed
            if (!r.isFrozen()) {
                r.clearHistory();
            }
        }
    }

    /*
     * Before modeling loop happens, all resources get set to their profile at the first activity start time
     * (since resources can't be set by an activity before it starts)
     */
    public void makeAllResourcesUseTheirProfileAtInitialTime() {
        // since this is only called by the engine, it should have set its current time to the first activity before calling this
        Time activityStartTime = ModelingEngine.getEngine().getCurrentTime();

        for (Resource r : allResourceInstances.values()) {
            // we're excluding 'frozen' resources that were read in from a file and are not to be changed
            if (!r.isFrozen()) {
                r.set(r.profile(activityStartTime));
            }
        }
    }

    /*
     * Returns first resource in list to be used
     */
    public Resource getFirstResourceUsed() {
        Time smallestTime = Time.MAX_TIME;
        Time candidateTime;
        Resource toReturn = null;
        for (Resource r : allResourceInstances.values()) {
            candidateTime = r.firstTimeSet();
            if (candidateTime != null && candidateTime.lessThan(smallestTime)) {
                smallestTime = candidateTime;
                toReturn = r;
            }
        }
        // if our resource usage really was so many seconds into the future, there was probably an error
        if (toReturn == null) {
            throw new RuntimeException("Could not find any resource usage nodes in plan.");
        }
        return toReturn;
    }

    /*
     * Gets first time any resource was used - used for initializing resource profiles
     */
    public Time getFirstResourceUsage() {
        Time smallestTime = Time.MAX_TIME;
        Time candidateTime;
        for (Resource r : allResourceInstances.values()) {
            candidateTime = r.firstTimeSet();
            if (candidateTime != null && candidateTime.lessThan(smallestTime)) {
                smallestTime = candidateTime;
            }
        }
        // if our resource usage really was so many seconds into the future, there was probably an error
        if (smallestTime.equals(Time.MAX_TIME)) {
            throw new RuntimeException("Could not find any resource usage nodes in plan.");
        }
        return smallestTime;
    }

    /*
     * Gets last time any resource was used - used for initializing resource profiles
     */
    public Time getLatestResourceUsage() {
        Time largestTime = new Time();
        Time candidateTime;
        for (Resource r : allResourceInstances.values()) {
            candidateTime = r.lastTimeSet();
            if (candidateTime != null && candidateTime.greaterThan(largestTime)) {
                largestTime = candidateTime;
            }
        }
        return largestTime;
    }

    // in the adaptation, some method that calls these needs to be run in order to add anything to this ArrayList
    public void registerResource(Resource toBeAdded) {
        allResourceInstances.put(toBeAdded.getUniqueName(), toBeAdded);
    }

    public ResourcesIterator getResourcesIterator(Time queryStart, Time queryEnd) {
        return new ResourcesIterator(getListOfAllResources(), queryStart, queryEnd);
    }

    /**
     * Since ResourceList is now stored internally as a map to making reading in values faster, sometimes for operations
     * that only happen once and can be slow we support putting out a list to be iterated over
     *
     * @return listOfAllResources, a list of all Resources defined in the adaptation
     */
    public List<Resource> getListOfAllResources() {
        return new ArrayList<>(allResourceInstances.values());
    }

    public List<String> getNamesOfAllResourcesWithSubsystem(String subsystem){
        return allResourceInstances.entrySet().stream().filter(r -> r.getValue().getSubsystem().equals(subsystem)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Returns ResourceList sorted by number of nodes in each resource. Intended as diagnostic tool that
     * adapters can use to tell which operations are taking extra time
     * @return
     */
    public List<Resource> getListOfAllResourcesSortedByDecreasingSize(){
        List<Resource> toReturn = new ArrayList<>(allResourceInstances.values());
        toReturn.sort((a,b) -> b.getSize() - a.getSize());
        return toReturn;
    }

    public Resource<?> get(String key) {
        if (allResourceInstances.get(key) != null) {
            return allResourceInstances.get(key);
        }
        else {
            throw new AdaptationException("Could not find " + key + " in list of instantiated resources. If it is declared, make sure it is in a class that extends ResourceDeclaration");
        }
    }

    public int length() {
        return allResourceInstances.size();
    }

}
