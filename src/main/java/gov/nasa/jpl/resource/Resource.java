package gov.nasa.jpl.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.input.parallel.ReaderThreadResource;
import gov.nasa.jpl.scheduler.CompareToValues;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.input.ReflectionUtilities;

import static java.lang.Integer.signum;

/**
 * The base abstract Resource class that all others inherit from. Contains data structure to track value of variable
 * throughout simulated time, as well as methods to hook into constraints so one can schedule off resource values.
 * @param <V> The parameterized type that represents the data type the resource is tracking throughout the simulation
 */
public abstract class Resource<V extends Comparable> implements ResourceContainer {
    // if we're just a resource by ourselves, we just have a name
    private String name;
    // we can be inside multiple nested arrayed resources, so we need a list of indices
    private List<String> indices;

    protected String units;
    protected String interpolation;
    protected String subsystem;
    protected V minimum;
    protected V maximum;
    protected List<V> possibleStates;

    // if resource has been read in and we don't want to modify it, set frozen boolean
    private boolean frozen;

    // other resources or conditions might want to listen to when we change, so we will keep a collection of listeners
    // we want a Set instead of a List because we never ever want to notify the same thing that we've changed twice - that could trigger duplicate events
    private Set<PropertyChangeListener> listeners;

    // save off just the last time and value we were set to, only during modeling, to speed up calls for large resources
    private Time currentLastTime;
    private V currentLastValue;

    // setting up main data structure that holds value history
    final TreeMap<Time, V> resourceHistory = new TreeMap<>();

    // we're overloading the resource constructor in order to let adapters easily specify different numbers of attributes
    public Resource(String subsystem, String units, String interpolation, V minimum, V maximum) {
        this.subsystem = subsystem;
        this.units = units;
        this.interpolation = interpolation;
        this.minimum = minimum;
        this.maximum = maximum;
        this.listeners = new HashSet<>();
        this.indices = new ArrayList<>();
        this.frozen = false;
    }

    public Resource(String subsystem, String units, String interpolation) {
        this(subsystem, units, interpolation, null, null);
    }

    public Resource(String subsystem, String units) {
        this(subsystem, units, "constant");
    }

    public Resource(String subsystem) {
        this(subsystem, "");
    }

    public Resource() {
        this("generic");
    }

    private void checkMutable() {
        if (isFrozen() && !ModelingEngine.getEngine().isCurrentlyReadingInFile()) {
            throw new RuntimeException("Tried to mutate frozen resource");
        }
    }

    /**
     * Clears the history of the resource by notifying its listeners that it is being reset, then pointing the resource history to a clean object.
     * Should not typically be called by adapters - is called before a REMODEL loop
     */
    public void clearHistory() {
        checkMutable();
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        Time lastTime = lastTimeSet();
        V lastValue = lastValue();
        notifyListeners(new AbstractMap.SimpleImmutableEntry(lastTime, lastValue), new AbstractMap.SimpleImmutableEntry(currentTime, profile(currentTime)));
        synchronized (resourceHistory) {
            resourceHistory.clear();
            currentLastTime = null;
            currentLastValue = null;
        }
    }

    /**
     * Inserts a new value into the resource's time history at the current simulated time.
     * This method is the main way adapters should append to resource histories.
     * Notifies any objects that may be listening to the resource, especially Condition objects.
     * @param inVal
     */
    public void set(V inVal) {
        checkMutable();
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        // need to do these before doing the put(), since we don't want the new value showing up as the 'old' value
        Time lastTime = lastTimeSet();
        V lastValue = lastValue();
        synchronized (resourceHistory) {
            resourceHistory.put(currentTime, inVal);
            currentLastTime = currentTime;
            currentLastValue = inVal;
        }

        // if we're not reading a file and are getting these values from activity modeling sections, we need to tell our listeners
        if (!ModelingEngine.getEngine().isCurrentlyReadingInFile()) {
            // we notify after we update in order to not get into an infinite loop with schedulers who want to update the resource that notifies them
            notifyListeners(new AbstractMap.SimpleImmutableEntry(lastTime, lastValue), new AbstractMap.SimpleImmutableEntry(currentTime, inVal));
        }
    }

    /**
     *  We don't want adapters using this, so we use something like C++'s friend mechanism. Used by parallel IO writer
     */
    public void insertRecord(ReaderThreadResource reader, Time t, V inVal){
        checkMutable();
        Objects.requireNonNull(reader);
        synchronized (resourceHistory) {
            resourceHistory.put(t, inVal);
        }
    }

    /**
     * No-op method that adapters can override in custom resources
     */
    public void update() {
        // in cases where we're updating just based on the current time
    }

    /**
     * Wraps valueAt(now())
     * @return Returns the value of the resource at the current simulated time.
     */
    public V currentval() {
        Time currentTime = ModelingEngine.getEngine().getCurrentTime();
        return valueAt(currentTime);
    }

    /**
     * Returns the value of the resource at the specified time. One of the main methods adapters use to query resource timelines.
     * @param t The Time one wants the value at.
     * @return The value (of whatever type) at the given time.
     */
    public V valueAt(Time t) {
        V attemptedAccess;
        synchronized (resourceHistory) {
            Time pastTime = resourceHistory.floorKey(t);
            if (pastTime == null) {
                attemptedAccess = (V) profile(t);
            }
            else {
                attemptedAccess = resourceHistory.get(pastTime);
            }
        }
        return attemptedAccess;
    }

    /**
     * @param start Times at or after this will be included in the search for the minimum value. If null, beginning of resource history is used.
     * @param end  Times at or before this will be included in the search for the minimum value. If null, end of resource history is used.
     * @return The minimum value of the resource between the bounds, as determined by the natural comparator of the type the Resource holds.
     *         If the minimum value is set before the query window and continues into some portion of the query window, that incoming
     *         value is returned, and the time returned is the 'start' query time
     */
    public Map.Entry<Time, V> min(Time start, Time end){
        return getExtremeValue(start, end, -1, Collections::min);
    }

    /**
     * @param start Times at or after this will be included in the search for the minimum value. If null, beginning of resource history is used.
     * @param end  Times at or before this will be included in the search for the minimum value. If null, end of resource history is used.
     * @return The maximum value of the resource between the bounds, as determined by the natural comparator of the type the Resource holds
     *         If the maximum value is set before the query window and continues into some portion of the query window, that incoming
     *         value is returned, and the time returned is the 'start' query time
     */
    public Map.Entry<Time,V> max(Time start, Time end){
        return getExtremeValue(start, end, 1, Collections::max);
    }

    private Map.Entry<Time,V> getExtremeValue(Time start, Time end, int sign, BiFunction<Collection, Comparator<Map.Entry<Time, V>>, Map.Entry<Time, V>> func){
        Map.Entry<Time, V> incomingEntry = null;
        if(start != null){
            incomingEntry = new AbstractMap.SimpleImmutableEntry<>(start, valueAt(start));
        }
        else{
            Time t = ModelingEngine.getEngine().getCurrentTime();
            incomingEntry = new AbstractMap.SimpleImmutableEntry<>(t, profile(t));
        }

        Map.Entry<Time, V> valueSetWithinWindow;
        try {
            valueSetWithinWindow = func.apply(getEntriesBetweenTimes(resourceHistory, start, end, false), Map.Entry.<Time, V>comparingByValue());
        }
        catch(NoSuchElementException e){
            return incomingEntry;
        }

        if(signum(valueSetWithinWindow.getValue().compareTo(incomingEntry.getValue())) == sign){
            return valueSetWithinWindow;
        }
        else{
            return incomingEntry;
        }

    }

    /**
     * @param start Times at or after this will be included in the returned list of value changes. If null, beginning of resource history is used.
     * @param end  Times at or before this will be included in the returned list of value changes. If null, end of resource history is used.
     * @return A chronological list of map entries, where each key is the time of the change and the value is the new resource value
     */
    public List<Map.Entry<Time, V>> getChangesDuringWindow(Time start, Time end){
        return new ArrayList<>(getEntriesBetweenTimes(resourceHistory, start, end, false));
    }

    /**
     * Returns the default value of the resource before it is set() to a new value. Must be abstract since different data types have different intuitive defaults.
     * @param t The time one wants the default value at. Used for custom resources that may have time-varying profiles.
     * @return
     */
    public abstract V profile(Time t);

    /**
     * Returns true if this individual resource is 'owned' by an ArrayedResource.
     * Should never be used by adapters.
     * @return
     */
    public boolean isIndexInArrayedResource() {
        return (indices.size() > 0);
    }

    /**
     * Returns true if the resource has any usage nodes in it.
     * @return
     */
    public boolean resourceHistoryHasElements() {
        synchronized (resourceHistory) {
            return !resourceHistory.isEmpty();
        }
    }

    /**
     * Returns number of values in the history of the resource
     * @return
     */
    public int getSize() {
        synchronized (resourceHistory) {
            return resourceHistory.size();
        }
    }

    /**
     *     Base case for recursive registerResource() in case of arrayed resource
     *     Called by reflection machinery when starting adaptation - not to be called by adapters
     */
    public void registerResource() {
        ResourceList resList = ResourceList.getResourceList();
        resList.registerResource(this);
    }

    /**
     * Returns the data type of the resource, using reflection. Used for IO.
     * @return
     */
    public String getType() {
        return ReflectionUtilities.getClassNameWithoutPackage(this.getClass().getName());
    }

    /**
     * Gets the name of the resource. Used for IO.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of a resource. Should only be called by ArrayedResources.
     * @param str
     */
    public void setName(String str) {
        name = str;
    }

    /**
     * Gets the 'indices' a resource bin belongs to. Used for IO
     * @return
     */
    public List<String> getIndices() {
        return indices;
    }

    /**
     * Sets the 'indices' of a resource. Should only be used by ArrayedResource and not adapters
     * @param index
     */
    public void setIndices(List<String> index) {
        indices = index;
    }

    /**
     * Getter for fully qualified name of resource
     * @return
     */
    public String getUniqueName() {
        return formUniqueName(getName(), getIndices());
    }

    /**
     * Encapsulated method to go from an arrayed resource to a well-defined string name
     * @param baseName
     * @param indices
     * @return
     */
    public static String formUniqueName(String baseName, List<String> indices){
        if (indices != null && indices.size() > 0) {
            return baseName + "[" + String.join("][", indices) + "]";
        }
        else {
            return baseName;
        }
    }

    /**
     * Returns the units string for the resource
     * @return
     */
    public String getUnits() {
        if (units == null) {
            return "";
        }
        else {
            return units;
        }
    }

    /**
     * Sets the units string for the resource
     * @param u
     */
    public void setUnits(String u) {
        units = u;
    }

    /**
     * Assigns the resource to a different subsystem
     * @param s
     */
    public void setSubsystem(String s) {
        subsystem = s;
    }

    /**
     * Gets the subsystem name the resource was assigned to
     * @return
     */
    public String getSubsystem() {
        return subsystem;
    }

    /**
     * Sets the interpolation mode of the resource. Usually 'constant' or 'linear'
     * @param i
     */
    public void setInterpolation(String i) {
        interpolation = i;
    }

    /**
     * Returns the interpolation mode of the resource
     * @return
     */
    public String getInterpolation() {
        return interpolation;
    }

    /**
     * Set the minimum value of a resource
     * @param minimum2
     */
    public void setMinimumLimit(V minimum2) {
        minimum = minimum2;
    }

    /**
     * Returns a string containing the minimum value of the resource
     * @return
     */
    public String getMinimumLimit() {
        if (minimum == null) {
            return "";
        }
        else {
            return minimum.toString();
        }
    }

    /**
     * Set the maximum value of a resource.
     * @param m
     */
    public void setMaximumLimit(V m) {
        maximum = m;
    }

    /**
     * Returns a string containing the maximum value of the resource
     * @return
     */
    public String getMaximumLimit() {
        if (maximum == null) {
            return "";
        }
        else {
            return maximum.toString();
        }
    }

    /**
     * Returns the possible states in a string resource if they were declared
     * @return
     */
    public List<V> getPossibleStates() {
        return possibleStates;
    }

    /**
     * Returns a string containing the type of data the resource is tracking. Mostly used by reflection.
     * @return
     */
    public String getDataType() {
        return returnedClass().toString().replace("class", "").trim();
    }

    /**
     * Freezes or unfreezes a resource. Should be called by IO machinery and not adapter
     * @param isFrozen
     */
    public void setFrozen(boolean isFrozen) {
        frozen = isFrozen;
    }

    /**
     * Returns true if the resource is frozen, which could have happened if it was read in from a file
     * @return
     */
    public boolean isFrozen() {
        return frozen;
    }


    /**
     * Returns an iterator over (Time, V) pairs in the resource's history timeline
     * @param begin
     * @param end
     * @return
     */
    public Iterator<Map.Entry<Time, V>> historyIterator(Time begin, Time end) {
        return getEntriesBetweenTimes(resourceHistory, begin, end, true).iterator();
    }

    private Set<Map.Entry<Time,V>> getEntriesBetweenTimes(NavigableMap<Time, V> inMap, Time start, Time end, boolean findEntriesAroundBounds){
        NavigableMap<Time,V> truncatedHistory = inMap;
        Time begincutoff = start;
        Time finalcutoff = end;

        synchronized (resourceHistory) {
            // if findEntriesAroundBounds is set, we need to grab the keys before begin and after end so we don't return nothing if someone searches for a window
            // that is between two nodes that do exist. the filtering logic to exclude them if relevant will be in those methods - this filtering is just for speed
            if (begincutoff != null) {
                if (findEntriesAroundBounds) {
                    begincutoff = resourceHistory.floorKey(begincutoff);
                    if (begincutoff == null) {
                        begincutoff = start;
                    }
                }
                truncatedHistory = truncatedHistory.tailMap(begincutoff, true);
            }
            if (finalcutoff != null) {
                if (findEntriesAroundBounds) {
                    finalcutoff = resourceHistory.ceilingKey(end);
                    if (finalcutoff == null) {
                        finalcutoff = end;
                    }
                }
                truncatedHistory = truncatedHistory.headMap(finalcutoff, true);
            }
            return truncatedHistory.entrySet();
        }
    }

    /**
     * Returns the first Time the resource was set
     * @return
     */
    public Time firstTimeSet() {
        if (!resourceHistory.isEmpty()) {
            return resourceHistory.firstKey();
        }
        else {
            return null;
        }
    }

    /**
     * Returns the last Time the resource was set
     * @return
     */
    public Time lastTimeSet() {
        if(ModelingEngine.getEngine().isModeling()){
            return currentLastTime;
        }
        else{
            synchronized (resourceHistory) {
                if (!resourceHistory.isEmpty()) {
                    return resourceHistory.lastKey();
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Intended to be used in decompose() methods when the resource histories are already filled out after a remodel
     * @param queryTime The time at which to begin looking earlier for the prior time the resource was set
     * @param inclusive Whether to return a resource set node at exactly queryTime if one exists - if inclusive is set to false and there is a node at queryTime, it will be ignored and the closest one prior to it will be returned
     * @return The latest Time the resource was set earlier than (or equal to, if inclusive is set to true) the query time, or null if the resource was not set prior
     */
    public Time priorTimeSet(Time queryTime, boolean inclusive){
        if(inclusive){
            return resourceHistory.floorKey(queryTime);
        }
        else{
            return resourceHistory.lowerKey(queryTime);
        }
    }

    /**
     * Intended to be used in decompose() methods when the resource histories are already filled out after a remodel
     * @param queryTime The time at which to begin looking later for the next time the resource is set
     * @param inclusive Whether to return a resource set node at exactly queryTime if one exists - if inclusive is set to false and there is a node at queryTime, it will be ignored and the closest one after it will be returned
     * @return The earliest Time the resource is set later than (or equal to, if inclusive is set to true) the query time, or null if the resource was not set afterwards
     */
    public Time nextTimeSet(Time queryTime, boolean inclusive){
        if(inclusive){
            return resourceHistory.ceilingKey(queryTime);
        }
        else{
            return resourceHistory.higherKey(queryTime);
        }
    }

    /**
     * Returns the most recently set value of the resource
     * @return
     */
    public V lastValue() {
        if(ModelingEngine.getEngine().isModeling()){
            return currentLastValue;
        }
        else{
            synchronized (resourceHistory) {
                if (!resourceHistory.isEmpty()) {
                    return resourceHistory.get(lastTimeSet());
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Gets the Time following queryTime when a resource is set to a specified value
     * Intended to be used in decompose() methods when the resource histories are already filled out after a remodel
     * @param val a value to compare to
     * @param queryTime the Time after which the sought-after Time must occur
     * @return the next Time the calling Resource is set to val. null if it is not set to that val anytime after
     */
    public Time nextTimeResourceSetToValue(V val, Time queryTime){
        synchronized (resourceHistory) {
            Time curr = resourceHistory.ceilingKey(queryTime);
            while (curr != null) {
                if (resourceHistory.get(curr).equals(val)) {
                    return curr;
                }
                curr = resourceHistory.higherKey(curr);
            }
            return null;
        }
    }

    /**
     * Gets the Time before queryTime when a resource is set to a specified value
     * Intended to be used in decompose() methods when the resource histories are already filled out after a remodel
     * @param val a value to compare to
     * @param queryTime the Time before which the sought-after Time must occur
     * @return the prior Time the calling Resource is set to val. null if it is not set to that val anytime before
     */
    public Time priorTimeResourceSetToValue(V val, Time queryTime){
        synchronized (resourceHistory) {
            Time curr = resourceHistory.floorKey(queryTime);
            while (curr != null) {
                if (resourceHistory.get(curr).equals(val)) {
                    return curr;
                }
                curr = resourceHistory.lowerKey(curr);
            }
            return null;
        }
    }

    /**
     *     Called by Conditions and other objects in core - not to be used by adapters
     *     Following Observer design pattern
     */
    public void addChangeListener(PropertyChangeListener newListener) {
        listeners.add(newListener);
    }

    /**
     *     Needed to not schedule schedulers every time a remodel is run
     *     Not to be called by adapters.
     */
    public void removeChangeListener(PropertyChangeListener toBeRemoved) {
        listeners.remove(toBeRemoved);
    }

    private void notifyListeners(Map.Entry<Time, V> oldValue, Map.Entry<Time, V> newValue) {
        for (PropertyChangeListener name : listeners) {
            name.propertyChange(new PropertyChangeEvent(this, "ResourceValue", oldValue, newValue));
        }
    }

    /**
     * This is a core piece of reflection that allows resources to figure out their own types and report to output files
     * @return a Class object of the type the Resource should write out to files to describe itself
     */
    private Class returnedClass() {
        Type parent = getClass().getGenericSuperclass();

        // this loop is needed to deal with a resource ResourceSubclassSubclass which extends DoubleResource that extends Resource<Double>
        while(! (parent != null && ParameterizedType.class.isAssignableFrom(parent.getClass()))){
            // Type is an interface and we assume if it is not a ParameterizedType, it is a class and we look for ITS superclass
            parent = ((Class) parent).getGenericSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) parent;

        // assuming here that resources are only parameterized by one type
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Returns a condition representing &gt;
     * @param value
     * @return
     */
    public Condition whenGreaterThan(Comparable value) {
        return new Condition(this, CompareToValues.GREATERTHAN, value);
    }

    /**
     * Returns a condition representing &gt;=
     * @param value
     * @return
     */
    public Condition whenGreaterThanOrEqualTo(Comparable value) {
        return Condition.or(this.whenGreaterThan(value), this.whenEqualTo(value));
    }

    /**
     * Returns a condition representing &lt;
     * @param value
     * @return
     */
    public Condition whenLessThan(Comparable value) {
        return new Condition(this, CompareToValues.LESSTHAN, value);
    }

    /**
     * Returns a condition representing &lt;=
     * @param value
     * @return
     */
    public Condition whenLessThanOrEqualTo(Comparable value) {
        return Condition.or(this.whenLessThan(value), this.whenEqualTo(value));
    }

    /**
     * Returns a condition representing ==
     * @param value
     * @return
     */
    public Condition whenEqualTo(Comparable value) {
        return new Condition(this, CompareToValues.EQUALTO, value);
    }

    /**
     * Returns a condition representing !=
     * @param value
     * @return
     */
    public Condition whenNotEqualTo(Comparable value) {
        return Condition.or(this.whenGreaterThan(value), this.whenLessThan(value));
    }
}
