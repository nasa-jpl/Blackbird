package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.Map;

public class ActivityFour extends Activity {
    public ActivityFour(Time t, Duration d, Map<String, String> stringMap) {
        super(t, d, stringMap);
        setDuration(d);
    }

    public void model() {
    }
}
