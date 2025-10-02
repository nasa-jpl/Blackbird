package gov.nasa.jpl.resource;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import gov.nasa.jpl.time.Time;

/**
 * This iterates in time order over all the resources handed to it in the constructor
 * We can't just use Guava's multi-iterator because instead of needing the &lt;Time, Value&gt;
 * pair, we need the Resource,Entry&lt;Time, V&gt; triple for XMLTOL and condition checking
 */
public class ResourcesIterator<V extends Comparable>{
    private List<Resource> resList;
    private ArrayList<Entry<Time, V>> allNextEntries;
    private ArrayList allIterators;

    public ResourcesIterator(List<Resource> resList, Time beginTime, Time endTime) {
        this.resList = resList;

        // set up list to store next entry of each resource
        this.allNextEntries = new ArrayList<Entry<Time, V>>();
        this.allIterators = new ArrayList<Iterator<Entry<Time, V>>>();
        for (int i = 0; i < resList.size(); i++) {
            this.allIterators.add(resList.get(i).historyIterator(beginTime, endTime));
            if (((Iterator<Entry<Time, V>>) this.allIterators.get(i)).hasNext()) {
                this.allNextEntries.add(((Iterator<Entry<Time, V>>) this.allIterators.get(i)).next());
            }
            else {
                this.allNextEntries.add(null);
            }
        }
    }

    /*
     * loops through all next entries to see if they are null
     */
    public boolean hasNext() {
        for (int i = 0; i < resList.size(); i++) {
            if (allNextEntries.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    /*
     * @return <Time, Resource> pair of next time and Resource that gets used at that Time
     */
    public Entry<Resource, Entry<Time, V>> next() {
        int nextIndex = -1;
        Time earliestTime = Time.MAX_TIME;
        V nextVal = null;
        Resource toReturn = null;
        for (int i = 0; i < resList.size(); i++) {
            if (allNextEntries.get(i) != null && allNextEntries.get(i).getKey().lessThan(earliestTime)) {
                earliestTime = allNextEntries.get(i).getKey();
                nextVal = allNextEntries.get(i).getValue();
                nextIndex = i;
            }
        }
        toReturn = resList.get(nextIndex);
        if (((Iterator) allIterators.get(nextIndex)).hasNext()) {
            Entry<Time, V> entryToExamine = ((Iterator<Entry<Time, V>>) this.allIterators.get(nextIndex)).next();
            allNextEntries.set(nextIndex, entryToExamine);
        }
        else {
            allNextEntries.set(nextIndex, null);
        }
        return new AbstractMap.SimpleImmutableEntry(toReturn, new AbstractMap.SimpleImmutableEntry<Time, V>(earliestTime, nextVal));
    }

    public Entry<Resource, Entry<Time, V>> peek() {
        int nextIndex = -1;
        Time earliestTime = Time.MAX_TIME;
        V nextVal = null;
        Resource toReturn = null;
        for (int i = 0; i < resList.size(); i++) {
            if (allNextEntries.get(i) != null && allNextEntries.get(i).getKey().lessThan(earliestTime)) {
                earliestTime = allNextEntries.get(i).getKey();
                nextVal = allNextEntries.get(i).getValue();
                nextIndex = i;
            }
        }
        // if we couldn't find any entries next, return null
        if (nextIndex == -1) {
            return null;
        }
        toReturn = resList.get(nextIndex);
        return new AbstractMap.SimpleImmutableEntry(toReturn, new AbstractMap.SimpleImmutableEntry<Time, V>(earliestTime, nextVal));
    }

    public void remove() {
        // no-op
    }

}
