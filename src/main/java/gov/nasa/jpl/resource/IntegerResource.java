package gov.nasa.jpl.resource;

import gov.nasa.jpl.time.Time;

public class IntegerResource extends Resource<Integer> {
    private int profile;

    public IntegerResource(int profile, String subsystem, String units, String interpolation, Integer minimum, Integer maximum) {
        super(subsystem, units, interpolation, minimum, maximum);
        this.profile = profile;
    }

    public IntegerResource(String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = 0;
    }

    public IntegerResource(int profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public IntegerResource(int profile, String subsystem, String units) {
        super(subsystem, units);
        this.profile = profile;
    }

    public IntegerResource(String subsystem) {
        super(subsystem);
        this.profile = 0;
    }

    public IntegerResource() {
        super();
        this.profile = 0;
    }

    public Integer profile(Time t) {
        return profile;
    }

    public void add(int toAdd) {
        set(currentval() + toAdd);
    }

    public void subtract(int toSubtract) {
        set(currentval() - toSubtract);
    }

    @Override
    public void update() {
        set(currentval());
    }
}
