package gov.nasa.jpl.constraint;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

import gov.nasa.jpl.time.Time;

public class ConstraintInstanceList {
    private ArrayList<Constraint> allConstraintInstances;
    private Map<String, Constraint> deactivatedInstances;

    // we're implementing the singleton design pattern for now, since we only need one global list
    private static ConstraintInstanceList instance = null;

    public ConstraintInstanceList() {
        allConstraintInstances = new ArrayList<>();
        deactivatedInstances = new HashMap<>();
    }

    public static ConstraintInstanceList getConstraintList() {
        if (instance == null) {
            instance = new ConstraintInstanceList();
        }
        return instance;
    }

    /*
     *  to be used before the beginning of the modeling loop when we want to start over
     */
    public void resetAllConstraints() {
        for (int i = 0; i < allConstraintInstances.size(); i++) {
            allConstraintInstances.get(i).clearViolationHistory();
        }
    }

    public void registerConstraint(Constraint c) {
        allConstraintInstances.add(c);
    }

    public void reactivateConstraints(List<String> constraintsToReactivate){
        for(String name : constraintsToReactivate){
            if(deactivatedInstances.containsKey(name)){
                Constraint c = deactivatedInstances.get(name);
                allConstraintInstances.add(c);
                c.hookToListeners();
                deactivatedInstances.remove(name);
            }
        }
    }

    public void deactivateConstraints(List<String> constraintsToDeactivate){
        for(Constraint c : allConstraintInstances){
            if(constraintsToDeactivate.contains(c.getName())){
                deactivatedInstances.put(c.getName(), c);
                c.unhookFromListeners();
                c.clearViolationHistory();
            }
        }
        allConstraintInstances.removeAll(deactivatedInstances.values());
    }

    public List<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> createListOfConstraintBeginAndEndTimes() {

        ArrayList<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> listOfAllBeginAndEndTimes = new ArrayList();
        for (int i = 0; i < allConstraintInstances.size(); i++) {
            Constraint currentConstraint = allConstraintInstances.get(i);
            Iterator<Map.Entry<Time, Time>> currentConstraintHistoryIterator = currentConstraint.historyIterator();
            // we need to find every violation for every different kind of constraint declared
            while (currentConstraintHistoryIterator.hasNext()) {
                Map.Entry<Time, Time> beginAndEndOfConstraintViolation = currentConstraintHistoryIterator.next();
                listOfAllBeginAndEndTimes.add(new SimpleImmutableEntry<>(beginAndEndOfConstraintViolation.getKey(), new SimpleImmutableEntry<>(true, currentConstraint)));
                listOfAllBeginAndEndTimes.add(new SimpleImmutableEntry<>(beginAndEndOfConstraintViolation.getValue(), new SimpleImmutableEntry<>(false, currentConstraint)));
            }
        }
        // now we have to sort this list since we have no idea when end times are
        Collections.sort(listOfAllBeginAndEndTimes, Map.Entry.comparingByKey());
        return listOfAllBeginAndEndTimes;
    }

    public List<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> createListOfConstraintBeginTimes() {
        ArrayList<Map.Entry<Time, Map.Entry<Boolean, Constraint>>> listOfAllBeginTimes = new ArrayList();
        for (int i = 0; i < allConstraintInstances.size(); i++) {
            Constraint currentConstraint = allConstraintInstances.get(i);
            Iterator<Map.Entry<Time, Time>> currentConstraintHistoryIterator = currentConstraint.historyIterator();
            // we need to find every violation for every different kind of constraint declared
            while (currentConstraintHistoryIterator.hasNext()) {
                Map.Entry<Time, Time> beginAndEndOfConstraintViolation = currentConstraintHistoryIterator.next();
                listOfAllBeginTimes.add(new SimpleImmutableEntry<>(beginAndEndOfConstraintViolation.getKey(), new SimpleImmutableEntry<>(true, currentConstraint)));
            }
        }
        // now we have to sort because the violations for different Constraints were added out of time order
        Collections.sort(listOfAllBeginTimes, Map.Entry.comparingByKey());
        return listOfAllBeginTimes;
    }

    public void finalizeAllConstraints() {
        for (int i = 0; i < allConstraintInstances.size(); i++) {
            allConstraintInstances.get(i).finalizeConstraintAfterModeling();
        }
    }

    public Constraint get(int i) {
        return allConstraintInstances.get(i);
    }

    public int length() {
        return allConstraintInstances.size();
    }
}
