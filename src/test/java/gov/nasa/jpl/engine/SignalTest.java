package gov.nasa.jpl.engine;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.exampleAdaptation.Res;
import gov.nasa.jpl.exampleAdaptation.WaitingOnSignalActivity;
import gov.nasa.jpl.exampleAdaptation.SignalSendingActivity;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Duration;

import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SignalTest extends BaseTest {

    @Test
    public void activityWaitingOnSignalForever(){
        Activity a = new WaitingOnSignalActivity(Time.getDefaultReferenceTime());

        ModelingEngine.getEngine().model();
        assertEquals(Time.getDefaultReferenceTime(), Res.TestState.lastTimeSet());
    }

    @Test
    public void activitySuccessfullyReceivesSignal(){
        Activity a = new WaitingOnSignalActivity(Time.getDefaultReferenceTime());
        Activity b = new SignalSendingActivity(Time.getDefaultReferenceTime().add(Duration.SECOND_DURATION), Duration.SECOND_DURATION);

        ModelingEngine.getEngine().model();
        assertEquals(Time.getDefaultReferenceTime().add(new Duration("00:00:06")), Res.TestState.lastTimeSet());
    }
}
