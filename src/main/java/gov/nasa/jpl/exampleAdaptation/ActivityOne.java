package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.time.Duration;

import static gov.nasa.jpl.exampleAdaptation.Res.*;

public class ActivityOne extends Activity {
    public ActivityOne(Time t, Duration d) {
        super(t, d);
        setDuration(d);
    }

    public void model() {
        ResourceA.set(15.0);
        ResourceB.set(5);
        waitFor(new Duration("00:01:00"));
        ResourceA.add(20.5);
        ResourceB.add(AdaptationGlobals.NumStarTrackers * 10);
        PositionVector.get("y").add(0.01);
    }
}
