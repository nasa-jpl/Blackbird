package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class GetWindowsActivity extends Activity {

    public GetWindowsActivity(Time t) {
        super(t);
    }

    public void decompose() {
        Window[] highWindows = Window.getWindows(getCondition(SignalSendingActivity.class), new Time("2000-001T00:00:00"), new Time("2000-001T01:00:00"));
        for (Window window : highWindows) {
            spawn(new SignalSendingActivity(window.getStart(), window.getEnd().subtract(window.getStart())));
        }
    }

}
