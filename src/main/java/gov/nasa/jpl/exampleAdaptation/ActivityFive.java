package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.Map;
import java.util.List;

public class ActivityFive extends Activity {
    public ActivityFive(Time t, Duration d, Map<String, List<String>> stringListMap) {
        super(t, d, stringListMap);
        setDuration(d);
    }

    public void model() {
    }
}