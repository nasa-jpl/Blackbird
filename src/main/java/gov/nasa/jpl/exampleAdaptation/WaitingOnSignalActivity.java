package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.Waiter;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class WaitingOnSignalActivity extends Activity {
    public WaitingOnSignalActivity(Time t) {
        super(t);
    }

    public Waiter modelFunc() {
        Res.TestState.set("NoSignal");
        return waitForSignal("SignalSendingActivity signal", (result) -> {
            Res.TestState.set("SignalSent: " + result.get("result"));
            waitFor(new Duration("00:00:05")); // old style wait
            return this.modelFunc(); // this is like a loop!
        });
    }
}
