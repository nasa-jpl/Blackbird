package gov.nasa.jpl.common;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.EpochRelativeTime;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;

public abstract class BaseTest {
    protected static String REGRESSION_TEST_OUTPUT_DIR = "test_outputs/";

    @BeforeClass
    public static void setup() {
        Setup.initializeEngine();
        File testDir = new File(REGRESSION_TEST_OUTPUT_DIR);
        if(!testDir.exists()) {
            testDir.mkdirs();
        }
    }

    @Before
    public void resetForTest() {
        for(Resource r : ResourceList.getResourceList().getListOfAllResources()){
            r.setFrozen(false);
        }
        ResourceList.getResourceList().resetResourceHistories();
        ConstraintInstanceList.getConstraintList().resetAllConstraints();
        ActivityInstanceList.getActivityList().clear();
        ModelingEngine.getEngine().resetEngine();
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
    }
}