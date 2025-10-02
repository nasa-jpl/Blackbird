package gov.nasa.jpl.resource;

import gov.nasa.jpl.time.Time;

public class BooleanResource extends Resource<Boolean> {
    private boolean profile;

    public BooleanResource(Boolean profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public BooleanResource(Boolean profile, String subsystem, String units) {
        super(subsystem, units);
        this.profile = profile;
    }

    public BooleanResource(Boolean profile, String subsystem) {
        super(subsystem);
        this.profile = profile;
    }

    public BooleanResource() {
        super();
        this.profile = false;
    }

    public Boolean profile(Time t) {
        return profile;
    }

    @Override
    public void update() {
        set(currentval());
    }


}

