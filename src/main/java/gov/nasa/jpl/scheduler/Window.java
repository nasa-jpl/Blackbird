package gov.nasa.jpl.scheduler;

import java.util.*;

import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourcesIterator;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class Window implements Comparable<Window>{
    private Time begin;
    private Time end;
    private String type;

    public Window(Time begin, Time end) {
        this.begin = begin;
        this.end = end;
    }

    public Window(Time begin, Time end, String type){
        this(begin, end);
        this.type = type;
    }

    public Time getStart() {
        return begin;
    }

    public Time getEnd() {
        return end;
    }

    public String getType(){
        return type;
    }

    public boolean hasEnd() {
        return end != null;
    }

    public Duration getDuration() {
        return end.subtract(begin);
    }

    public Time getMidpoint(){
        return getStart().add(getDuration().divide(2.0));
    }

    /**
     * Returns true if the parameter time is within the calling Window, inclusive
     * @param t
     * @return
     */
    public boolean contains(Time t) {
        return t.greaterThanOrEqualTo(begin) && t.lessThanOrEqualTo(end);
    }

    /**
     * Returns true if the parameter Window is within the calling Window, inclusive
     * @param w
     * @return
     */
    public boolean contains(Window w){
        return contains(w.getStart()) && contains(w.getEnd());
    }

    /**
     * Returns true if the parameter Window intersects the calling Window
     * @param w
     * @return
     */
    public boolean intersects(Window w){
        return contains(w.getStart()) || contains(w.getEnd()) || w.contains(getStart()) || w.contains(getEnd());
    }

    public boolean hasLengthZero() {
        return getDuration().equals(Duration.ZERO_DURATION);
    }

    // not intended to be used for deserialization, mostly for debugging purposes
    @Override
    public String toString(){
        return "[" + begin.toString() + ", " + end.toString() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Window)) {
            return false;
        }
        Window otherWindow = (Window) obj;
        return begin.equals(otherWindow.begin) && end.equals(otherWindow.end);
    }

    @Override
    /**
     * We're comparing based on begin times since standard sorts of windows seem to want start time as natural ordering
     */
    public int compareTo(Window w) {
        return begin.compareTo(w.getStart());
    }

    @Override
    public int hashCode() {
        return begin.hashCode() ^ end.hashCode();
    }

    /**
     * Returns a duration equal to the sum of all the durations in the provided list
     * @param windows
     * @return
     */
    public static Duration sum(Window[] windows){
        Duration d = Duration.ZERO_DURATION;
        for(Window window : windows){
            d = d.plus(window.getDuration());
        }
        return d;
    }

    /**
     * Returns an array of window objects where the input condition is met within
     * the start and end time provided.
     * @param condition
     * @param queryStart
     * @param queryEnd
     * @return
     */
    public static Window[] getWindows(Condition condition, Time queryStart, Time queryEnd) {
        // throw error if start time is not before end time
        if (queryStart.greaterThan(queryEnd)) {
            throw new RuntimeException("Error in getWindows call: queryStart " + queryStart.toUTC() + " is " +
                    "after queryEnd " + queryEnd.toUTC() + ". The start time of the getWindows call must be before " +
                    "the end time.");
        }


        ArrayList<Window> windows = new ArrayList<>();
        boolean inAWindow = false;
        ResourcesIterator resHistory = new ResourcesIterator(new ArrayList(condition.getAllResourcesRecursively()), queryStart, queryEnd);
        Map.Entry<Resource, Map.Entry<Time, Comparable>> currentEntry; // this comes from resHistory and is for readability
        Time currentTime; // also for readability

        condition.setEvaluatedTo(queryStart);
        // the profile might be within the window constraints, so we have to look from the queryStart time
        if ((!resHistory.hasNext() || queryStart.lessThan(((Map.Entry<Time, Comparable>) resHistory.peek().getValue()).getKey())) && condition.isTrue()) {
            inAWindow = true;
            windows.add(new Window(queryStart, null));
        }

        while (resHistory.hasNext()) {
            // we want to update the condition for all resource changes that happened at the present time
            do {
                currentEntry = resHistory.next();
                currentTime = currentEntry.getValue().getKey();
                condition.update(currentEntry.getKey(), currentEntry.getValue().getValue());
            } while(resHistory.hasNext() && currentTime.equals(((Map.Entry<Time, Comparable>) resHistory.peek().getValue()).getKey()));
            // if we are greater than or less than (whichever is specified) the value specified, we should be in the window
            if (currentTime.greaterThanOrEqualTo(queryStart) && currentTime.lessThanOrEqualTo(queryEnd)) {
                if (condition.isTrue()) {
                    if (!inAWindow) {
                        inAWindow = true;
                        windows.add(new Window(currentTime, null));
                    }
                }
                else {
                    if (inAWindow) {
                        inAWindow = false;
                        windows.get(windows.size() - 1).end = currentTime;
                    }
                }
            }
            // this case comes in if queryStart is partway through the resource profile and it is true where we start
            else if (currentTime.lessThan(queryStart)) {
                if ((!resHistory.hasNext() || queryStart.lessThan(((Map.Entry<Time, Comparable>) resHistory.peek().getValue()).getKey())) && condition.isTrue()) {
                    inAWindow = true;
                    windows.add(new Window(queryStart, null));
                }
            }
            // if we're done with going through the loop, we don't want to waste time iterating when it won't do anything
            else if (currentTime.greaterThan(queryEnd)) {
                break;
            }
        }

        // if we're at the end of the resource history or query time and the last window still doesn't have an end time, add the end time
        if (!windows.isEmpty() && !windows.get(windows.size() - 1).hasEnd()) {
            windows.get(windows.size() - 1).end = queryEnd;
        }

        return windows.toArray(new Window[windows.size()]);
    }

    /**
     * Returns an array of window objects where the input condition is met within
     * the start and end time provided. Unlike getWindows, this method interprets
     * double resources to determine the time when the value crosses a threshold.
     * Currently only works for single-node conditions whose resource is a DoubleResource.
     * @param condition
     * @param queryStart
     * @param queryEnd
     * @return
     */
    public static Window[] interpWindows(Condition condition, Time queryStart, Time queryEnd) {
        // throw error if start time is not before end time
        if (queryStart.greaterThan(queryEnd)) {
            throw new RuntimeException("Error in getWindows call: queryStart " + queryStart.toUTC() + " is " +
                    "after queryEnd " + queryEnd.toUTC() + ". The start time of the interpWindows call must be before " +
                    "the end time.");
        }

        if (!condition.isBaseNode() || !(condition.getResource() instanceof DoubleResource)) {
            // right now we can only pass single-node conditions whose resources are doubles to interp windows
            return null;
        }

        DoubleResource res = (DoubleResource) condition.getResource();
        CompareToValues inequality = condition.getInequality();
        Double valueToCompareTo = (Double) condition.getThreshold();

        ArrayList<Window> windows = new ArrayList<>();
        Iterator<Map.Entry<Time, Double>> thisResourceHistory = res.historyIterator(queryStart, queryEnd);
        Map.Entry<Time, Double> lastEntry = null;
        Map.Entry<Time, Double> currentEntry;
        boolean inAWindow = false;
        Time interpolatedTime;

        // the profile might be within the window constraints, so we have to look from the queryStart time
        if (queryStart.lessThan(res.firstTimeSet())) {
            lastEntry = new AbstractMap.SimpleImmutableEntry<>(queryStart, res.profile(queryStart));
            if (Integer.signum(res.profile(queryStart).compareTo(valueToCompareTo)) == inequality.toInt()) {
                inAWindow = true;
                windows.add(new Window(queryStart, null));
            }
        }
        else if(Integer.signum(res.interpval(queryStart).compareTo(valueToCompareTo)) == inequality.toInt()){
            lastEntry = new AbstractMap.SimpleImmutableEntry<>(queryStart, res.valueAt(queryStart));
            inAWindow = true;
            windows.add(new Window(queryStart, null));
        }

        while (thisResourceHistory.hasNext()) {
            currentEntry = thisResourceHistory.next();
            // in addition to making sure we're in the time bounds, we should skip the first entries if they are before queryStart
            if (lastEntry != null && currentEntry.getKey().greaterThan(queryStart) && lastEntry.getKey().lessThan(queryEnd)) {
                // by now if the last value was appropriate we should be in the window, so we only need to check currentEntry not lastEntry
                if (Integer.signum(currentEntry.getValue().compareTo(valueToCompareTo)) == inequality.toInt()) {
                    if (!inAWindow && timeResourceCrossesLimit(lastEntry.getKey(), currentEntry.getKey(), lastEntry.getValue(), currentEntry.getValue(), valueToCompareTo).lessThan(queryEnd)) {
                        inAWindow = true;
                        // need to adjust for possibility that the interpolated time is before queryStart, even if the end point of the interval is after it
                        interpolatedTime = Time.max(queryStart, timeResourceCrossesLimit(lastEntry.getKey(), currentEntry.getKey(), lastEntry.getValue(), currentEntry.getValue(), valueToCompareTo));
                        windows.add(new Window(interpolatedTime, null));
                    }
                }
                else {
                    if (inAWindow) {
                        inAWindow = false;
                        // need to adjust for possibility that interpolated time is after queryEnd, even if the begin point of the interval is before it
                        interpolatedTime = Time.min(queryEnd, timeResourceCrossesLimit(lastEntry.getKey(), currentEntry.getKey(), lastEntry.getValue(), currentEntry.getValue(), valueToCompareTo));
                        windows.get(windows.size() - 1).end = interpolatedTime;
                    }
                }
            }
            else if (lastEntry != null && lastEntry.getKey().greaterThan(queryEnd)) {
                break;
            }
            lastEntry = currentEntry;
        }

        // if we're at the end of the resource history or query time and the last window still doesn't have an end time, add the end time
        if (!windows.isEmpty() && !windows.get(windows.size() - 1).hasEnd()) {
            windows.get(windows.size() - 1).end = queryEnd;
        }

        return windows.toArray(new Window[windows.size()]);
    }

    /**
     * Returns original Window object from windowsToCheck that intersects this/self, or null if there are no intersections. This is
     * different from Window.and() because it preserves the full length and type of the conflicting window, not just the intersection portion
     * @param windowsToCheck Windows to check for intersections with win - ASSUMPTION is that no windows in windowsToCheck overlap
     * @return the intersecting original Window object, or null if there are no intersections. If multiple windows in windowsToCheck
     *         intersect this/self, the first one is returned
     */
    public Window getIntersectingWindowInList(NavigableSet<Window> windowsToCheck){
        Window priorKeepout = windowsToCheck.floor(this);
        if(priorKeepout != null && priorKeepout.intersects(this)){
            return priorKeepout;
        }
        Window nextKeepout = windowsToCheck.ceiling(this);
        if(nextKeepout != null && nextKeepout.intersects(this)){
            return nextKeepout;
        }
        return null;
    }

    public static Window[] and(Window[] win1, Window[] win2) {
        ArrayList<Window> windows = new ArrayList<>();
        int i = 0, j = 0;
        boolean inWin1 = false, inWin2 = false;
        Time nextTimeForWin1, nextTimeForWin2;
        if (win1.length == 0 || win2.length == 0) {
            return new Window[0];
        }
        while (i < win1.length && j < win2.length) {
            nextTimeForWin1 = (inWin1) ? win1[i].getEnd() : win1[i].getStart();
            nextTimeForWin2 = (inWin2) ? win2[j].getEnd() : win2[j].getStart();
            if (nextTimeForWin1.lessThan(nextTimeForWin2)) {
                if (!inWin1) {
                    inWin1 = true;
                    if (inWin2) {
                        windows.add(new Window(win1[i].getStart(), null));
                    }
                }
                else {
                    if (!windows.isEmpty() && inWin2) windows.get(windows.size() - 1).end = win1[i].getEnd();
                    inWin1 = false;
                    i++;
                }
            }
            else {
                if (!inWin2) {
                    inWin2 = true;
                    if (inWin1) {
                        windows.add(new Window(win2[j].getStart(), null));
                    }
                }
                else {
                    if (!windows.isEmpty() && inWin1) windows.get(windows.size() - 1).end = win2[j].getEnd();
                    inWin2 = false;
                    j++;
                }
            }
        }
        return windows.toArray(new Window[windows.size()]);
    }

    public static Window[] or(Window[] win1, Window[] win2) {
        ArrayList<Window> windows = new ArrayList<>();
        int i = 0, j = 0;
        boolean inWin1 = false, inWin2 = false;
        Time nextTimeForWin1, nextTimeForWin2;
        if (win1.length == 0 && win2.length == 0) {
            return new Window[0];
        }
        while (i < win1.length || j < win2.length) {
            nextTimeForWin1 = (i < win1.length) ? ((inWin1) ? win1[i].getEnd() : win1[i].getStart()) : null;
            nextTimeForWin2 = (j < win2.length) ? ((inWin2) ? win2[j].getEnd() : win2[j].getStart()) : null;
            if (nextTimeForWin2 == null || (nextTimeForWin1 != null && nextTimeForWin1.lessThan(nextTimeForWin2))) {
                if (!inWin1) {
                    inWin1 = true;
                    if (!inWin2) {
                        windows.add(new Window(win1[i].getStart(), null));
                    }
                }
                else {
                    if (!windows.isEmpty() && !inWin2) windows.get(windows.size() - 1).end = win1[i].getEnd();
                    inWin1 = false;
                    i++;
                }
            }
            else {
                if (!inWin2) {
                    inWin2 = true;
                    if (!inWin1) {
                        windows.add(new Window(win2[j].getStart(), null));
                    }
                }
                else {
                    if (!windows.isEmpty() && !inWin1) windows.get(windows.size() - 1).end = win2[j].getEnd();
                    inWin2 = false;
                    j++;
                }
            }
        }
        return windows.toArray(new Window[windows.size()]);
    }

    public static Window[] xor(Window[] win1, Window[] win2) {
        ArrayList<Window> windows = new ArrayList<>();
        int i = 0, j = 0;
        boolean inWin1 = false, inWin2 = false;
        Time nextTimeForWin1, nextTimeForWin2;
        if (win1.length == 0 && win2.length == 0) {
            return new Window[0];
        }
        while (i < win1.length || j < win2.length) {
            nextTimeForWin1 = (i < win1.length) ? ((inWin1) ? win1[i].getEnd() : win1[i].getStart()) : null;
            nextTimeForWin2 = (j < win2.length) ? ((inWin2) ? win2[j].getEnd() : win2[j].getStart()) : null;
            if (nextTimeForWin2 == null || (nextTimeForWin1 != null && nextTimeForWin1.lessThan(nextTimeForWin2))) {
                if (!inWin1) {
                    inWin1 = true;
                    if (!inWin2) {
                        windows.add(new Window(win1[i].getStart(), null));
                    }
                    else {
                        if (!windows.isEmpty()) windows.get(windows.size() - 1).end = win1[i].getStart();
                    }
                }
                else {
                    if (inWin2) {
                        windows.add(new Window(win1[i].getEnd(), null));
                    }
                    else {
                        if (!windows.isEmpty()) windows.get(windows.size() - 1).end = win1[i].getEnd();
                    }
                    inWin1 = false;
                    i++;
                }
            }
            else {
                if (!inWin2) {
                    inWin2 = true;
                    if (!inWin1) {
                        windows.add(new Window(win2[j].getStart(), null));
                    }
                    else {
                        if (!windows.isEmpty()) windows.get(windows.size() - 1).end = win2[j].getStart();
                    }
                }
                else {
                    if (inWin1) {
                        windows.add(new Window(win2[j].getEnd(), null));
                    }
                    else {
                        if (!windows.isEmpty()) windows.get(windows.size() - 1).end = win2[j].getEnd();
                    }
                    inWin2 = false;
                    j++;
                }
            }
        }

        // this algorithm can result in zero duration windows being added if two windows start at the same time, so here we remove all empty windows
        int k = 0;
        while (k < windows.size()) {
            if (windows.get(k).hasLengthZero()) {
                windows.remove(k);
            }
            else {
                k++;
            }
        }
        return windows.toArray(new Window[windows.size()]);
    }

    public static Window[] not(Window[] win1, Time queryBegin, Time queryEnd) {
        ArrayList<Window> windows = new ArrayList<>();
        Window[] unfilteredWindows = xor(win1, new Window[]{new Window(queryBegin, queryEnd)});

        for (int i = 0; i < unfilteredWindows.length; i++) {
            if (!unfilteredWindows[i].getStart().lessThan(queryBegin) && !unfilteredWindows[i].getEnd().greaterThan(queryEnd)) {
                windows.add(unfilteredWindows[i]);
            }
        }

        return windows.toArray(new Window[windows.size()]);
    }

    /**
     * Loops through list of windows and returns list where entries separated by less than Duration parameter are merged
     * @param toBeMerged List of Windows, must be sorted already
     * @param threshold Duration between adjacent windows below which they will be merged together
     * @return A new Window array containing the merged list
     */
    public static Window[] merge(Window[] toBeMerged, Duration threshold){
        List<Window> toReturn = new ArrayList<>();

        int i = 0;
        while(i < toBeMerged.length){
            // every time we get to this line in the loop, it is the beginning of a merged window
            Time thisStart = toBeMerged[i].getStart();
            Time thisEnd = toBeMerged[i].getEnd();
            while(i < toBeMerged.length-1 && thisEnd.add(threshold).greaterThanOrEqualTo(toBeMerged[i+1].getStart())){
                thisEnd = toBeMerged[i+1].getEnd().greaterThan(thisEnd) ? toBeMerged[i+1].getEnd() : thisEnd; // false case is window 'i+1' contained in window 'i'
                i++;
            }
            toReturn.add(new Window(thisStart, thisEnd));
            i++;
        }

        return toReturn.toArray(new Window[toReturn.size()]);
    }

    private static Time timeResourceCrossesLimit(Time begin, Time end, Double beginVal, Double endVal, Double limit) {
        double slope = (endVal - beginVal) / (end.subtract(begin).totalSeconds());
        // s = (limit-beginVal)/(returnTime - begin) -> returnTime = ((limit-beginVal)/s)+begin
        return (begin.add(new Duration("00:00:01").multiply((limit - beginVal) / slope)));
    }

}