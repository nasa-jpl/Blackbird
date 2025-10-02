package gov.nasa.jpl.activity;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.scheduler.CompareToValues;
import gov.nasa.jpl.scheduler.Condition;
import gov.nasa.jpl.scheduler.Scheduler;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.beans.PropertyChangeListener;

import static gov.nasa.jpl.exampleAdaptation.Res.ResourceB;
import static org.junit.Assert.fail;


public class DispatchOnConditionTest extends BaseTest {


    @Test
    public void dispatchOnConditionWithinDispatchOnCondition(){
        BadScheduler scheduler1 = new BadScheduler(new Time("2018-330T13:00:00.000"));
        BadScheduler scheduler2 = new BadScheduler(new Time("2018-330T13:00:00.000"));
        Activity1 instigatingAct = new Activity1(new Time("2018-330T13:00:00.000"));

        try {
            CommandController.issueCommand("REMODEL", "");
        }
        catch(RuntimeException e){
            if(!e.getMessage().contains("Cannot spawn scheduler from another scheduler")){
                fail();
            }
        }
        finally {
            ResourceB.removeChangeListener(scheduler1);
            ResourceB.removeChangeListener(scheduler2);
        }
    }

    @Test
    public void badConditionErrorMessage(){
        BadConditionScheduler scheduler1 = new BadConditionScheduler(Time.getDefaultReferenceTime());

        try {
            CommandController.issueCommand("REMODEL", "");
        }
        catch(RuntimeException e){
            if(!e.getMessage().contains("activity instance of type")){
                fail();
            }
        }
        finally{
            scheduler1.stopScheduling();
        }
    }


    public static class NestedScheduler extends Activity implements Scheduler {
        public NestedScheduler(Time t) {
            super(t);
        }

        @Override
        public Condition setCondition() {
            return new Condition(ResourceB, CompareToValues.GREATERTHAN, 0);
        }

        @Override
        public void dispatchOnCondition() {
        }
    }

    public static class BadScheduler extends Activity implements Scheduler {
        public BadScheduler(Time t) {
            super(t);
        }

        @Override
        public Condition setCondition() {
            return new Condition(ResourceB, CompareToValues.GREATERTHAN, 0);
        }

        @Override
        public void dispatchOnCondition() {
            spawn(new NestedScheduler(now()));
        }
    }

    public static class Activity1 extends Activity {

        public Activity1(Time t) {
            super(t);
        }

        public void model() {
            ResourceB.set(5);
        }
    }

    public static class BadConditionScheduler extends Activity implements Scheduler{
        public BadConditionScheduler(Time t) {
            super(t);
        }

        @Override
        public Condition setCondition() {
            return new Condition(ResourceB, CompareToValues.GREATERTHAN, "hello");
        }

        @Override
        public void dispatchOnCondition() {
            // doesn't matter for this test
        }
    }
}
