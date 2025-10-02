package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.exampleAdaptation.Res.ExampleBodyState;
import static gov.nasa.jpl.exampleAdaptation.Res.TestState;

public class ActivityEight extends Activity {
    String first;
    String second;

    public ActivityEight(Time t, String first, String second) {
        super(t, first, second);
        this.first = first;
        this.second = second;
    }

    public void model(){
        ExampleBodyState.get(first).get(second).add(1.0);
        TestState.set("SignalSent");
        waitFor(new Duration("00:00:05"));
        TestState.set("NoSignal");
    }
}
