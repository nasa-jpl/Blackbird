package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import gov.nasa.jpl.engine.Signal;

import java.util.HashMap;
import java.util.Map;

public class SignalSendingActivity extends Activity {

    public SignalSendingActivity(Time t, Duration d) {
        super(t, d);
        setDuration(d);
    }

    public void model() {
        Map<String, String> signalContents = new HashMap<>();
        Res.ResourceA.subtract(1);
        if (now().lessThan(new Time("2000-001T00:01:40"))) {
            signalContents.put("result", "before");
        }
        else {
            signalContents.put("result", "after");
        }
        Signal.send("SignalSendingActivity signal", signalContents);
    }

    @Override
    public Condition setCondition() {
        return Res.ResourceB.whenGreaterThan(25);
    }
}
