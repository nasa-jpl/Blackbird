package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.time.Time;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class TOLConstraintIterator implements Iterator<TOLRecord> {
    List<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> allConstraintViolations;
    int i; // iterating variable

    public TOLConstraintIterator(List<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> allConstraintViolations){
        this.allConstraintViolations = allConstraintViolations;
        i = 0;
    }

    @Override
    public boolean hasNext() {
        return i < allConstraintViolations.size();
    }

    @Override
    public TOLRecord next() throws NoSuchElementException{
        if(!hasNext()){
            throw new NoSuchElementException("Tried to iterate past end of constraint violation list");
        }
        Map.Entry<Time, Map.Entry<Boolean, Constraint>> nextEntry = allConstraintViolations.get(i);
        i++;
        if(nextEntry.getValue().getKey() == true){
            return new TOLConstraintViolationBegin(nextEntry.getKey(), nextEntry.getValue().getValue());
        }
        else{
            return new TOLConstraintViolationEnd(nextEntry.getKey(),   nextEntry.getValue().getValue());
        }
    }
}
