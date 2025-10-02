package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Time;

public class ActivityNine extends Activity {
    public ActivityNine(Time t, Time endTime) {
        super(t, endTime);
        setEndByChangingDuration(endTime);
    }
}
