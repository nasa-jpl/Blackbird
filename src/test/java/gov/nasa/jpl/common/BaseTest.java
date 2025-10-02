package gov.nasa.jpl.common;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class BaseTest {
    @BeforeClass
    public static void setup() {
        Setup.initializeEngine();
    }

    @Before
    public void resetForTest() {
        ResourceList.getResourceList().resetResourceHistories();
        ConstraintInstanceList.getConstraintList().resetAllConstraints();
        ActivityInstanceList.getActivityList().clear();
        ModelingEngine.getEngine().resetEngine();
    }
}