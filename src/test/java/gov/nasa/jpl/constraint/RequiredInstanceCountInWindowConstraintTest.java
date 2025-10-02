package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.exampleAdaptation.ActivityOne;
import gov.nasa.jpl.exampleAdaptation.ActivityThree;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.scheduler.CompareToValues;
import gov.nasa.jpl.scheduler.Window;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RequiredInstanceCountInWindowConstraintTest extends BaseTest {

    @Test
    public void propertyChange(){
        Constraint con1 = new RequiredInstanceCountInWindowConstraint(
                "ActivityOne",
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.GREATERTHAN,
                2,
                "",
                ViolationSeverity.WARNING
        );

        Constraint con2 = new RequiredInstanceCountInWindowConstraint(
                "ActivityOne",
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.GREATERTHAN,
                3,
                "",
                ViolationSeverity.WARNING
        );

        Constraint con3 = new RequiredInstanceCountInWindowConstraint(
                "ActivityOne",
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.LESSTHAN,
                1,
                "",
                ViolationSeverity.WARNING
        );

        Constraint con4 = new RequiredInstanceCountInWindowConstraint(
                "ActivityOne",
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.EQUALTO,
                3,
                "",
                ViolationSeverity.WARNING
        );

        Constraint con5 = new RequiredInstanceCountInWindowConstraint(
                "ActivityOne",
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.EQUALTO,
                5,
                "",
                ViolationSeverity.WARNING
        );

        Constraint con6 = new RequiredInstanceCountInWindowConstraint(
                Arrays.asList("ActivityOne", "ActivityThree"),
                new Window(new Time("2000-001T00:00:06"), new Time("2000-001T00:00:17")),
                CompareToValues.EQUALTO,
                4,
                "",
                ViolationSeverity.WARNING
        );

        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:10"), new Duration("00:00:01"));
        ActivityThree imposter = new ActivityThree(new Time("2000-001T00:00:10"), new Duration("00:00:01"), new ArrayList<>());
        ActivityOne green = new ActivityOne(new Time("2000-001T00:00:06"), new Duration("00:00:01"));

        ModelingEngine.getEngine().model();
        
        ActivityOne yellow = new ActivityOne(new Time("2000-001T00:00:16"), new Duration("00:00:01"));
        ActivityOne purple = new ActivityOne(new Time("2000-001T00:00:20"), new Duration("00:00:07"));

        ModelingEngine.getEngine().model();

        assertEquals(0, con1.listOfViolationBeginAndEndTimes.size());
        assertEquals(1, con2.listOfViolationBeginAndEndTimes.size());
        assertEquals(1, con3.listOfViolationBeginAndEndTimes.size());
        assertEquals(0, con4.listOfViolationBeginAndEndTimes.size());
        assertEquals(1, con5.listOfViolationBeginAndEndTimes.size());
        assertEquals(0, con6.listOfViolationBeginAndEndTimes.size());
    }
}