package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.annotations.TypeData;
import gov.nasa.jpl.engine.Waiter;
import gov.nasa.jpl.time.Duration;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.exampleAdaptation.Res.*;

@TypeData(subsystem = "testSubsystem2")
public class ActivityTwo extends Activity {
    private double amount;

    public ActivityTwo(Time t, double amount) {
        super(t, amount);
        this.amount = amount;
        setGroup("testLegend");
    }

    public void decompose() {
        spawn(new ActivityOne(getStart().add(new Duration("00:00:00")), new Duration("00:00:01")));
        spawn(new ActivityOne(getStart().add(new Duration("00:00:100")), new Duration("00:00:01")));
    }

    public Waiter modelFunc() {
        ResourceA.add(amount);
        return waitFor(new Duration("00:02:00"), () -> {
            ResourceA.set(calculateAmount(amount));
            PositionVector.get("x").add(amount);
        });
    }

    private double calculateAmount(double x) {
        return Math.PI * x;
    }
}