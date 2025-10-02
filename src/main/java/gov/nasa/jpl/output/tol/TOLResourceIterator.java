package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourcesIterator;
import gov.nasa.jpl.time.Time;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class TOLResourceIterator implements Iterator<TOLRecord> {
    ResourcesIterator resIterator;

    public TOLResourceIterator(ResourcesIterator resIterator){
        this.resIterator = resIterator;
    }


    @Override
    public boolean hasNext() {
        return resIterator.hasNext();
    }

    @Override
    public TOLRecord next() throws NoSuchElementException{
        if(!hasNext()){
            throw new NoSuchElementException("Tried to iterate past end of all resource histories");
        }
        Map.Entry<Resource,Map.Entry<Time, Comparable>> nextEntry = resIterator.next();
        return new TOLResourceValue(nextEntry.getValue().getKey(), nextEntry.getValue().getValue(), nextEntry.getKey());
    }
}
