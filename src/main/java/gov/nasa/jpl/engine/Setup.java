package gov.nasa.jpl.engine;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.constraint.ConstraintDeclaration;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.spice.Spice;

public class Setup {

    /*
     * Anything needing to run before Commands start getting issued, like creating Singletons
     * or performing reflection to grab all relevant classes, should be done here
     */
    public static void initializeEngine() {
        Spice spice = new Spice();
        ModelingEngine myEngine = ModelingEngine.getEngine();
        ParameterDeclaration.collectNamesOfAllParameters();
        ActivityTypeList allActivityTypes = ActivityTypeList.getActivityList();
        ResourceDeclaration.assignNamesToAllResources();
        ConstraintDeclaration.assignNamesToAllConstraints();
        ActivityInstanceList actList = ActivityInstanceList.getActivityList();
    }
}
