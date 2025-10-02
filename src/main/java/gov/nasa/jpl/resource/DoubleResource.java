package gov.nasa.jpl.resource;

import gov.nasa.jpl.time.Time;

import java.util.TreeMap;

public class DoubleResource extends Resource<Double> {
    private double profile;

    public DoubleResource(double profile, String subsystem, String units, String interpolation, Double minimum, Double maximum) {
        super(subsystem, units, interpolation, minimum, maximum);
        this.profile = profile;
    }

    public DoubleResource(String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = 0.0;
    }

    public DoubleResource(double profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public DoubleResource(double profile, String subsystem, String units) {
        super(subsystem, units);
        this.profile = profile;
    }

    public DoubleResource(double profile, String subsystem) {
        super(subsystem);
        this.profile = profile;
    }

    public DoubleResource(double profile) {
        super();
        this.profile = profile;
    }

    public DoubleResource() {
        super();
        this.profile = 0.0;
    }

    public Double profile(Time t) {
        return profile;
    }

    public void add(double toAdd) {
        set(currentval() + toAdd);
    }

    public void subtract(double toSubtract) {
        set(currentval() - toSubtract);
    }

    /**
     * Returns the value of the resource at a time, linearly interpolating if the query time is not one in which the resource was
     * explicitly set. This is in contrast to valueAt, which, if faced with querying the value at a time the resource was not set, returns
     * the most recent update to the resource
     * @param t The time at which you want the interpolated value of the resource
     * @return The value of the resource as interpolated between two explicitly calculated values
     */
    public Double interpval(Time t) {
        return getInterpolatedValue(t, this);
    }

    /**
     * Performs the logic for interpolation for the input time and resource.
     * This method is abstracted from interpval because IntegratingResource also
     * has the same logic.
     */
    protected Double getInterpolatedValue(Time t, DoubleResource r) {
        Time pastTime;
        Time ceilingTime;
        double pastValue;
        double futureValue;
        synchronized (r.resourceHistory) {
            pastTime = r.resourceHistory.floorKey(t);
            ceilingTime = r.resourceHistory.ceilingKey(t);

            // if pastTime or ceilingTime can't be found, we are requesting a time outside the calculated values for the resource, so we default to either the profile or the very last usage node respectively
            if (pastTime == null || ceilingTime == null) {
                return valueAt(t);
            }

            // if pastTime equals ceilingTime, we are requesting a value where there is an exact known value, so we don't have to interpolate
            if(pastTime.equals(ceilingTime)){
                return resourceHistory.get(pastTime);
            }

            // if we've gotten here, we know pastTime and ceilingTime are distinct and non-null, so we can query the tree for them
            pastValue = resourceHistory.get(pastTime);
            futureValue = resourceHistory.get(ceilingTime);
        }

        return linearlyInterpolate(t, pastTime, ceilingTime, pastValue, futureValue);
    }

    /**
     * Performs linear interpolation using y = mx + b
     */
    protected Double linearlyInterpolate(Time interpolateTime, Time t1, Time t2, Double v1, Double v2) {
        double slope = (v2 - v1) / ((t2.subtract(t1)).totalSeconds());
        return v1 + slope * (interpolateTime.subtract(t1).totalSeconds());
    }

    @Override
    public void update() {
        set(currentval());
    }
}
