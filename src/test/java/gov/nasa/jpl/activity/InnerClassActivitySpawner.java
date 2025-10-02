package gov.nasa.jpl.activity;
import gov.nasa.jpl.activity.annotations.TypeData;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

@TypeData(subsystem = "outerSubsystem", description = "lives in test directory")
public class InnerClassActivitySpawner extends Activity
{
    public InnerClassActivitySpawner(Time t) { super(t); }

    @Override
    public void decompose()
    {
        // try instantiating an anonymous class.
        // this should fail immediately!
        try {
            spawn(new Activity(now()) {
                @Override
                public void model() {
                    System.out.println("Never hit");
                }
            });
            throw new RuntimeException("Spawning anonymous activity didn't fail.");
        }
        catch (AdaptationException ex) {
            // good!
        }

        // this should work
        spawn(new InnerClassActivity(now(), Duration.MINUTE_DURATION));
    }

    @TypeData(subsystem = "innerClassTesting", description = "lives in test directory")
    public static class InnerClassActivity extends Activity
    {
        public InnerClassActivity(Time t, Duration d) {
            super(t, d);
            setDuration(d);
        }

        @Override
        public void model()
        {
            // System.out.println("Here we are!");
        }
    }

    public static class SecondInnerClassActivity extends Activity
    {
        public SecondInnerClassActivity(Time t, Duration d) {
            super(t, d);
            setDuration(d);
        }

        @Override
        public void model()
        {
            // System.out.println("Here we are!");
        }
    }
}



