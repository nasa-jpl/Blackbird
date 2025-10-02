package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Time;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class TOLActivityIterator implements Iterator<TOLRecord>{
    List<Map.Entry<Time,Map.Entry<Boolean,Activity>>> listOfActBeginAndEndTimes;
    int i; // iterating variable

    public TOLActivityIterator(List<Map.Entry<Time,Map.Entry<Boolean,Activity>>> listOfActBeginAndEndTimes){
        this.listOfActBeginAndEndTimes = listOfActBeginAndEndTimes;
        i = 0;
    }

    @Override
    public boolean hasNext() {
        return i < listOfActBeginAndEndTimes.size();
    }

    @Override
    public TOLRecord next() throws NoSuchElementException{
        if(!hasNext()){
            throw new NoSuchElementException("Tried to iterate past end of activity list");
        }
        Map.Entry<Time,Map.Entry<Boolean,Activity>> nextEntry = listOfActBeginAndEndTimes.get(i);
        i++;
        if(nextEntry.getValue().getKey() == true){
            return new TOLActivityBegin(nextEntry.getValue().getValue());
        }
        else{
            return new TOLActivityEnd(nextEntry.getValue().getValue());
        }
    }
}
