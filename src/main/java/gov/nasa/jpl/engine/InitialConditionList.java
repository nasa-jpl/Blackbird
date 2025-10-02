package gov.nasa.jpl.engine;

import gov.nasa.jpl.time.Time;

import java.util.HashMap;
import java.util.Map;

public class InitialConditionList {
    private HashMap<String, Comparable> mapOfResourceNameToInitialValue;
    private Time inconTime;

    public InitialConditionList() {
        mapOfResourceNameToInitialValue = new HashMap<>();
        inconTime = new Time();
    }

    public void addToInconList(String key, Comparable value) {
        mapOfResourceNameToInitialValue.put(key, value);
    }

    public Time getInconTime() {
        return inconTime;
    }

    public void setInconTime(Time t) {
        inconTime = t;
    }

    public Comparable valueOfResource(String name) {
        if (!mapOfResourceNameToInitialValue.containsKey(name)) {
            return null;
        }
        else {
            return mapOfResourceNameToInitialValue.get(name);
        }
    }

    public Map getEntireInconMap() {
        return mapOfResourceNameToInitialValue;
    }
}
