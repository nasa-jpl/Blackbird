package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.scheduler.Scheduler;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class ConstraintScheduleTestAct extends Activity implements Scheduler {
    public ConstraintScheduleTestAct(Time t) {
        super(t);
    }

    @Override
    public Condition setCondition() {
        return Res.ConstraintScheduleTestRes.whenEqualTo("ready");
    }

    @Override
    public void dispatchOnCondition() {
        spawn(new ActivityOne(now(), new Duration("00:00:05")));
    }

    @Override
    public void model() {
        Res.ResourceA.set(1.0);
    }
}
