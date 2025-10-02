package gov.nasa.jpl.engine;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.resource.IntegerResource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModelingEngineTest extends BaseTest {

    @Test
    public void modelWithNoActivities() {
        ModelingEngine.getEngine().model();
    }

    @Test
    public void successfullyCatchBrokenActivity(){
        FailedActivity act = new FailedActivity(Time.getDefaultReferenceTime());
        BadActivity act2 = new BadActivity(Time.getDefaultReferenceTime().add(Duration.MINUTE_DURATION));

        try{
            ModelingEngine.getEngine().model();
            fail("Should not have proceeded past runtime exception");
        }
        catch (RuntimeException ex){
            assertTrue(ex.getMessage().contains("Catch this"));
        }
    }

    public static class FailedActivity extends Activity{
        public FailedActivity(Time t){
            super(t);
        }

        public void model(){
            throw new RuntimeException("Catch this");
        }
    }

    public static class BadActivity extends Activity{
        public BadActivity(Time t){
            super(t);
        }

        public void model() {
            int x = 1/0;
        }
    }
    public static class ActivityWithTimeLoop extends Activity {
        private final IntegerResource res;
        /**
         * Superconstructor for all Activity types - must be passed all arguments that child class is
         *
         * @param t   The time the activity instance should be started
         * @param end the time this activity ends.
         */
        public ActivityWithTimeLoop(Time t, Time end, IntegerResource res) {
            super(t, end, res);
            setEndByChangingDuration(end);
            this.res = res;
        }

        @Override
        public Waiter modelFunc() {
            Time end1 = getStart().add(Duration.fromMinutes(40));
            return waitLoop(getStart(), end1, Duration.MINUTE_DURATION,
                (t,et) -> res.add(1),         // executes for time range [getStart(),end1)
                (et) -> {                     // executes at time [end1]
                return waitLoop(et, getEnd(), Duration.MINUTE_DURATION,
                    (t,et2) -> res.add(10));}); // executes for time range [end1,getEnd()]
        }
    }

    @Test
    public void testLoopingActivity() {
        IntegerResource intRes = new IntegerResource();
        Activity act = new ActivityWithTimeLoop(Time.getDefaultReferenceTime(),
                Time.getDefaultReferenceTime().add(Duration.HOUR_DURATION),
                intRes);
        ModelingEngine.getEngine().model();
        // The resource should equal 40 + 210
        assertEquals(250, (int)intRes.currentval());
    }
}