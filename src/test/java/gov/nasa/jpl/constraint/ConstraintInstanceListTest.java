package gov.nasa.jpl.constraint;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConstraintInstanceListTest extends BaseTest {

    @Test
    public void deactivateConstraints() {
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
        ConstraintInstanceList conList = ConstraintInstanceList.getConstraintList();

        List<String> constraintsInExampleAdaptation = Arrays.asList("TwoBeforeOne", "forbidden");

        CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityOne (2018-330T13:00:00.000, 00:01:00)"
        );

        CommandController.issueCommand(
                "NEW_ACTIVITY",
                "ActivityOne (2018-335T13:00:00.000, 00:01:00)"
        );

        CommandController.issueCommand("REMODEL", "");

        // we tried to at least trip the 'forbidden constraint'
        boolean didWeTrip = false;
        for(Map.Entry<Time, Map.Entry<Boolean, Constraint>> violatedConstraint : conList.createListOfConstraintBeginTimes()){
            if(constraintsInExampleAdaptation.contains(violatedConstraint.getValue().getValue().getName())){
                didWeTrip = true;
            }
        }
        if(!didWeTrip){
            fail();
        }

        conList.deactivateConstraints(constraintsInExampleAdaptation);

        CommandController.issueCommand("REMODEL", "");

        // with them deactivated, we should not see any matching violations
        for(Map.Entry<Time, Map.Entry<Boolean, Constraint>> violatedConstraint : conList.createListOfConstraintBeginTimes()){
            if(constraintsInExampleAdaptation.contains(violatedConstraint.getValue().getValue().getName())){
                fail();
            }
        }

        conList.reactivateConstraints(constraintsInExampleAdaptation);

        CommandController.issueCommand("REMODEL", "");

        // with them reactivated, we should see those violations again
        didWeTrip = false;
        for(Map.Entry<Time, Map.Entry<Boolean, Constraint>> violatedConstraint : conList.createListOfConstraintBeginTimes()){
            if(constraintsInExampleAdaptation.contains(violatedConstraint.getValue().getValue().getName())){
                didWeTrip = true;
            }
        }
        if(!didWeTrip){
            fail();
        }
    }
}