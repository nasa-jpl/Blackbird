package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.annotations.TypeData;
import gov.nasa.jpl.activity.annotations.Parameter;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.List;

@TypeData(description = "This is an activity that showcases a List parameter as well as class and parameter annotations")
public class ActivityThree extends Activity {
    public ActivityThree(
            Time t,
            @Parameter(defaultValue = "00:01:00", units = "hh:mm:ss", range = {"00:00:30", "00:10:00"}) Duration d,
            @Parameter(description = "This is a list") List<String> stringList) {
        super(t, d, stringList);
        setDuration(d);
    }

    public void model() {
    }
}
