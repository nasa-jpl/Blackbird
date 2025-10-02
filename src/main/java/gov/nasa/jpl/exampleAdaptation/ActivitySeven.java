package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.activity.Activity;

import java.util.Map;

import static gov.nasa.jpl.exampleAdaptation.Res.ResourceC;

public class ActivitySeven extends Activity {

    public ActivitySeven(Time t, Duration d) {
        super(t, d);
        Duration duration2 = new Duration("00:00:01");
        setDuration(d.add(duration2));
    }

    public void model() {
        ResourceC.add(getDuration());
    }
}
