package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.engine.ParameterDeclaration;
import gov.nasa.jpl.time.Time;

public class AdaptationGlobals extends ParameterDeclaration {
    public static int NumStarTrackers = 3;
    public static double compressionRatio = 1.0;
    public static Time LANDING_EPOCH = new Time("2020-001T00:00:00");
}
