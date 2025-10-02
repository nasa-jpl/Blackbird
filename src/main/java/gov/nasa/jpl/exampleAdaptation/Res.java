package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.resource.*;
import gov.nasa.jpl.time.Duration;

import java.util.Arrays;

public class Res extends ResourceDeclaration {
    public static String[] celestialBodies = new String[]{"Sun", "Earth", "Moon"};
    public static String[] vectorComponents = new String[]{"x", "y", "z"};
    public static DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
    public static IntegerResource ResourceB = new IntegerResource();
    public static Duration dur = Duration.ZERO_DURATION;
    public static DurationResource ResourceC = new DurationResource(dur, "subsystem2");
    public static ArrayedResource<DoubleResource> PositionVector = new ArrayedResource<DoubleResource>(vectorComponents) {};
    public static ArrayedResource<ArrayedResource<DoubleResource>> ExampleBodyState = new ArrayedResource<ArrayedResource<DoubleResource>>(celestialBodies, vectorComponents) {};
    public static ArrayedResource<DoubleResource> ResourceWithSpacesInBin = new ArrayedResource<DoubleResource>(new String[]{"my bin"}) {};
    public static IntegratingResource IntegratesA = new IntegratingResource(ResourceA, Duration.ONE_SECOND);
    public static ArrayedResource<IntegratingResource> IntegratesPosition = new ArrayedResource<IntegratingResource>("pointing", "", "linear", PositionVector, Duration.MINUTE_DURATION) {};
    public static StringResource TestState = new StringResource(Arrays.asList((new String[]{"NoSignal", "SignalSent"})));
    public static StringResource ConstraintScheduleTestRes = new StringResource("notready", "subsystem1");
    public static TimeResource TimeRes = new TimeResource();
    public static DoubleResource unitializedRes;

    public static ResourceSubclassSubclass TestSubclassing = new ResourceSubclassSubclass();
    public static ArrayedResource<ResourceSubclassSubclass> TestArrayedSubclassing = new ArrayedResource<ResourceSubclassSubclass>(vectorComponents) {};
}
