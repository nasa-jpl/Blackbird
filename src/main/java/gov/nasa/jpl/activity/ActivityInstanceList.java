package gov.nasa.jpl.activity;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

import gov.nasa.jpl.output.tol.JSONPlanWriter;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.input.RegexUtilities.EXCLUDE_ONGOING_STRING;

/**
 * A list of Activity objects. The main engine holds a semi-singleton instance, but other instances can be used for IO.
 */
public class ActivityInstanceList {
    private List<Activity> allActivities;

    // we keep a main activity instance list with all activities in it in this class
    // however we do make other instances of this class for filtering activities for writing outfiles
    private static ActivityInstanceList instance = null;

    public ActivityInstanceList() {
        allActivities = new ArrayList<>();
    }

    public static ActivityInstanceList getActivityList() {
        if (instance == null) {
            instance = new ActivityInstanceList();
        }
        return instance;
    }

    public void prepareActivitiesForModeling() {
        Collections.sort(allActivities);
        for (Activity act : allActivities) {
            // we want each condition to evaluate by default to its profile
            act.setInitialCondition();
        }
    }

    /**
     * For now we only need to sort the activities before sequencing.
     */
    public void prepareActivitiesForSequencing() {
        Collections.sort(allActivities);
    }

    public void stopAllSchedulers() {
        for (Activity act : allActivities) {
            act.stopScheduling();
        }
    }

    public void add(Activity act) {
        allActivities.add(act);
    }

    /**
     * Takes in an activity instance and removes it from the ActivityInstanceList.
     *
     * @param act
     */
    public void remove(Activity act) {
        allActivities.remove(act);
    }

    /**
     * Takes in an activity id and removes it from the ActivityInstanceList.
     *
     * @param id
     */
    public void remove(UUID id) {
        for (int i = 0; i < allActivities.size(); i++) {
            // if the input id matches the id of the activity then remove the activity and exit the loop
            if (allActivities.get(i).getIDString().equals(id.toString())) {
                allActivities.remove(i);
                return;
            }
        }
        // throw exception if activity id was not found
        throw new RuntimeException("Could not find activity instance with id " + id + ".");
    }

    public int length() {
        return allActivities.size();
    }

    public void clear() {
        allActivities = new ArrayList<>();
    }

    public Activity get(int i) {
        return allActivities.get(i);
    }

    /**
     *
     * @param activityTypes that you want to get all instances of
     * @return List of activity instances of given types
     */
    public List<Activity> getAllActivitiesOfType(Class... activityTypes) {
        List<Activity> toReturn = new ArrayList<>();

        for (int i = 0; i < allActivities.size(); i++) {
            if(isInstanceInTypes(activityTypes, allActivities.get(i))){
                toReturn.add(allActivities.get(i));
            }
        }
        return toReturn;
    }

    /**
     *
     * @return List of entries that map activity type name to a count of instances of that type
     * in the whole plan as loaded in memory. Generally to be used for diagnostic purposes when
     * profiling adaptation speed, especially I/O
     */
    public List<Map.Entry<String, Integer>> getInstanceCountForEachType(){
        TreeMap<String, Integer> instanceCount = new TreeMap<>();
        for(String type : ActivityTypeList.getActivityList().getNamesOfAllDefinedTypes()){
            instanceCount.put(type, 0);
        }

        for(Activity a : allActivities){
            int count = instanceCount.get(a.getType());
            instanceCount.remove(a.getType());
            instanceCount.put(a.getType(), count+1);
        }

        List<Map.Entry<String, Integer>> toReturn = new ArrayList<>(instanceCount.entrySet());
        toReturn.sort((a,b) -> b.getValue() - a.getValue());
        return toReturn;
    }

    /**
     *
     * @param start activities with a start Time equal to this or later (and <= end) will be returned
     * @param end activities with a start (or end, see 'mustContainAll') Time equal to or earlier than this (and >= start) will be returned
     * @param mustContainAll true if an activity must end before 'end', false if it only must start before 'end' to be returned
     * @param activityTypes the types you want to get instances of
     * @return
     */
    public List<Activity> getActivitiesOfTypeBetween(Time start, Time end, boolean mustContainAll, Class... activityTypes) {
        List<Activity> toReturn = new ArrayList<>();
        for(Activity act : allActivities){
            boolean meetsEndCondition = (!mustContainAll && act.getStart().lessThanOrEqualTo(end)) || act.getEnd().lessThanOrEqualTo(end);
            if(isInstanceInTypes(activityTypes, act) && start.lessThanOrEqualTo(act.getStart()) && meetsEndCondition){
                toReturn.add(act);
            }
        }
        return toReturn;
    }

    /**
     * @param subsystem activity instances with this subsystem will be returned
     * @param start activities with a start Time equal to this or later (and <= end) will be returned
     * @param end activities with a start (or end, see 'mustContainAll') Time equal to or earlier than this (and >= start) will be returned
     * @param mustContainAll true if an activity must end before 'end', false if it only must start before 'end' to be returned
     * @return
     */
    public List<Activity> getActivityInstancesWithSubsystem(String subsystem, Time start, Time end, boolean mustContainAll){
        ActivityTypeList allTypes = ActivityTypeList.getActivityList();

        List<Activity> toReturn = new ArrayList<>();
        for(Activity act : allActivities){
            boolean meetsEndCondition = (!mustContainAll && act.getStart().lessThanOrEqualTo(end)) || act.getEnd().lessThanOrEqualTo(end);
            if(allTypes.getSubsystem(act.getType()).equals(subsystem) && start.lessThanOrEqualTo(act.getStart()) && meetsEndCondition){
                toReturn.add(act);
            }
        }
        return toReturn;
    }

    /**
     * Returns an activity instance from the ActivityInstanceList given an input activity id.
     *
     * @param activityID
     * @return
     */
    public Activity findActivityByID(UUID activityID) {
        for (int i = 0; i < allActivities.size(); i++) {
            // if the input id matches the id of the activity then return the activity
            if (allActivities.get(i).getIDString().equals(activityID.toString())) {
                return allActivities.get(i);
            }
        }
        // throw exception if activity id was not found
        throw new RuntimeException("Could not find activity instance with id " + activityID + ".");
    }

    /**
     * Returns true if the ID can be found in the activity instance list, false if not.
     *
     * @param activityID
     * @return
     */
    public boolean containsID(UUID activityID) {
        for (int i = 0; i < allActivities.size(); i++) {
            // if the input id matches the id of the activity then return the activity
            if (allActivities.get(i).getIDString().equals(activityID.toString())) {
                return true;
            }
        }
        return false;
    }

    public Time getFirstActivityStartTime() {
        // list should be sorted before we call this, so we can just take the first element
        if (allActivities.isEmpty()) {
            return null;
        }
        else {
            return allActivities.get(0).getStart();
        }
    }

    public Time getLastActivityEndTime() {
        // list should be sorted before we call this, so we can just take the last element
        if (allActivities.isEmpty()) {
            return null;
        }
        else {
            return allActivities.get(allActivities.size() - 1).getEnd();
        }
    }

    /*
     * The time is the time that the activity either begins or ends, the boolean
     * is true if the activity is a start and false if it is an end, and the activity
     * is the one that either starts or ends at that time
     */
    public List<Map.Entry<Time, Map.Entry<Boolean, Activity>>> createListOfActivityBeginAndEndTimes(Time start, Time end, String activitiesAtStart) {
        ArrayList<Map.Entry<Time, Map.Entry<Boolean, Activity>>> listOfAllBeginAndEndTimes = new ArrayList();
        for (int i = 0; i < allActivities.size(); i++) {
            Activity ofInterest = allActivities.get(i);
            if(shouldIncludeActivity(ofInterest, start, end, activitiesAtStart)) {
                listOfAllBeginAndEndTimes.add(new SimpleImmutableEntry<>(ofInterest.getStart(), new SimpleImmutableEntry<>(true, ofInterest)));
                listOfAllBeginAndEndTimes.add(new SimpleImmutableEntry<>(ofInterest.getEnd(), new SimpleImmutableEntry<>(false, ofInterest)));
            }
        }
        // now we have to sort this list since we have no idea when end times are
        Collections.sort(listOfAllBeginAndEndTimes, Map.Entry.comparingByKey());
        return listOfAllBeginAndEndTimes;
    }

    /*
     * Same logic as above, except only the begin times are put into output entries, since some formats only want to see activity info at act start
     */
    public List<Map.Entry<Time, Map.Entry<Boolean, Activity>>> createListOfActivityBeginTimes(Time start, Time end, String activitiesAtStart) {
        ArrayList<Map.Entry<Time, Map.Entry<Boolean, Activity>>> listOfAllBeginTimes = new ArrayList();
        for (int i = 0; i < allActivities.size(); i++) {
            Activity ofInterest = allActivities.get(i);
            // we only have start times in this list, so unlike beginAndEndTimes we always put 'true' as the Boolean
            if(shouldIncludeActivity(ofInterest, start, end, activitiesAtStart)) {
                listOfAllBeginTimes.add(new SimpleImmutableEntry<>(ofInterest.getStart(), new SimpleImmutableEntry<>(true, ofInterest)));
            }
        }
        // we should sort this because activities could have gotten added during modeling
        Collections.sort(listOfAllBeginTimes, Map.Entry.comparingByKey());
        return listOfAllBeginTimes;
    }

    /**
     * Activity time filter that admits activities between start and end, if they're defined
     * @param act The activity in question
     * @param startTime Filter start - if null, not compared against
     * @param endTime Filter end - if null, not compared against
     * @param activitiesAtStart If "excludeOngoingActs", the filter only admits activities whose start time falls between startTime and endTime. If "includeOngoingActs", filter additionally admits activities that end between startTime and endTime
     * @return whether the activity is accepted by the filter or not
     */
    public static boolean shouldIncludeActivity(Activity act, Time startTime, Time endTime, String activitiesAtStart){
        if(activitiesAtStart.equals(EXCLUDE_ONGOING_STRING)){
            return (startTime == null || act.getStart().greaterThanOrEqualTo(startTime)) && (endTime == null || act.getStart().lessThanOrEqualTo(endTime));
        }
        else{
            return (startTime == null || act.getEnd().greaterThanOrEqualTo(startTime)) && (endTime == null || act.getStart().lessThanOrEqualTo(endTime));
        }
    }

    private boolean isInstanceInTypes(Class[] types, Activity instance){
        for(Class type : types){
            if(type.isAssignableFrom(instance.getClass())){
                return true;
            }
        }
        return false;
    }
}
