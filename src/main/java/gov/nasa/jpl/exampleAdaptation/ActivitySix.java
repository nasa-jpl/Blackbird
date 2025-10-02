package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.List;

public class ActivitySix extends Activity {
    public ActivitySix(Time t, Duration d, List<Time> timeList) {
        super(t, d, timeList);
        setDuration(d);
    }

    public void model() {
    }
}
