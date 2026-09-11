package gov.nasa.jpl.engine;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityTwo;
import gov.nasa.jpl.exampleAdaptation.Res;
import gov.nasa.jpl.resource.IntegerResource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

import java.io.IOException;

import static org.junit.Assert.*;

public class ModelingEngineTest extends BaseTest {

    @Test
    public void modelWithNoActivities() {
        ModelingEngine.getEngine().model();
    }

    @Test
    public void successfullyCatchBrokenActivity(){
        FailedActivity act = new FailedActivity(Time.getDefaultReferenceTime());
        FailedActivity actDos = new FailedActivity(Time.getDefaultReferenceTime());

        try{
            ModelingEngine.getEngine().model();
            fail("Should not have proceeded past runtime exception");
        }
        catch (RuntimeException ex){
            assertTrue(ex.getMessage().contains("Catch this"));
        }

        ActivityInstanceList.getActivityList().clear();
        for(int i = 0; i<100; i++) {
            ActivityOne workingAct = new ActivityOne(Time.getDefaultReferenceTime(), Duration.SECOND_DURATION);
        }

        BadActivity act2 = new BadActivity(Time.getDefaultReferenceTime().add(new Duration("00:00:30")));
        try{
            ModelingEngine.getEngine().model();
            fail("Should not have proceeded past runtime exception");
        }
        catch (RuntimeException ex){
            assertTrue(ex.getMessage().contains("/ by zero"));
        }

        ActivityInstanceList.getActivityList().clear();
        for(int i = 0; i<100; i++) {
            ActivityTwo workingAct = new ActivityTwo(Time.getDefaultReferenceTime(), 5.0);
        }
        BrokenFuncActivity act3 = new BrokenFuncActivity(Time.getDefaultReferenceTime().add(Duration.MINUTE_DURATION));
        BrokenFuncActivity act4 = new BrokenFuncActivity(Time.getDefaultReferenceTime().add(Duration.MINUTE_DURATION));
        try{
            ModelingEngine.getEngine().model();
            fail("Should not have proceeded past runtime exception");
        }
        catch (RuntimeException ex){
            assertTrue(ex.getMessage().contains("oops"));
        }

        ActivityInstanceList.getActivityList().clear();
        for(Time t = Time.getDefaultReferenceTime(); t.lessThan(Time.getDefaultReferenceTime().add(Duration.DAY_DURATION)); t = t.add(Duration.MINUTE_DURATION)) {
            CaughtRuntimeExceptionAct caughtAct = new CaughtRuntimeExceptionAct(t);
            SwallowingCheckedErrorActivity actErr = new SwallowingCheckedErrorActivity(t);
        }
        ActivityOne workingAct = new ActivityOne(Time.getDefaultReferenceTime().add(Duration.HOUR_DURATION), Duration.SECOND_DURATION);
        ModelingEngine.getEngine().model();
        assertEquals(3, Res.ResourceA.getSize());

        ActivityInstanceList.getActivityList().clear();
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
            waitFor(new Duration("00:05:00"));
            waitFor(new Duration("00:00:15"));
            int x = 1/0;
            waitFor(new Duration("00:05:00"));
            waitFor(new Duration("00:00:05"));
        }
    }

    public static class BrokenFuncActivity extends Activity{
        public BrokenFuncActivity(Time t){
            super(t);
        }

        public Waiter modelFunc(){
            throw new NullPointerException("oops");
        }
    }

    public static class CaughtRuntimeExceptionAct extends Activity{
        public CaughtRuntimeExceptionAct(Time t){
            super(t);
        }

        public void model(){
            waitFor(new Duration("00:05:00"));
            waitFor(new Duration("00:00:15"));
            try{
                throw new AdaptationException("oops");
            }
            catch(AdaptationException e){
                // do nothing
            }
            waitFor(new Duration("00:05:00"));
            waitFor(new Duration("00:00:15"));
        }
    }

    public static class SwallowingCheckedErrorActivity extends Activity{
        public SwallowingCheckedErrorActivity(Time t){
            super(t);
        }

        public void model(){
            waitFor(new Duration("00:05:00"));
            waitFor(new Duration("00:05:00"));
            try{
                CSPICE.spkezr("test", 0.0, "test", "test", "test", new double[0], new double[0]);
            }
            catch(SpiceErrorException e){
                return;
            }
            waitFor(new Duration("00:10:00"));
            waitFor(new Duration("00:05:00"));
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