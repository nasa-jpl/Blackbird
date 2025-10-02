package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.scheduler.Scheduler;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class ExampleScheduler extends Activity implements Scheduler {

    public ExampleScheduler(Time t) {
        super(t);
    }

    @Override
    public Condition setCondition() {
        return Res.PositionVector.get("y").whenGreaterThan(5.0);
    }

    @Override
    public void dispatchOnCondition() {
        spawn(new ActivityOne(now(), new Duration("00:00:05")));
        Res.PositionVector.get("y").subtract(3.7);
    }
}
