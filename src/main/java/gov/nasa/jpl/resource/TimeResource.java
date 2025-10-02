package gov.nasa.jpl.resource;

import gov.nasa.jpl.time.Time;

public class TimeResource extends Resource<Time> {
    private Time profile;

    public TimeResource(Time profile, String subsystem, String interpolation, String units, Time minimum, Time maximum) {
        super(subsystem, units, interpolation, minimum, maximum);
        this.profile = profile;
    }

    public TimeResource(Time profile, String subsystem, String interpolation) {
        super(subsystem, interpolation);
        this.profile = profile;
    }

    public TimeResource(Time profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public TimeResource(Time profile, String subsystem) {
        super(subsystem);
        this.profile = profile;
    }

    public TimeResource() {
        super();
        profile = Time.getDefaultReferenceTime();
    }

    public Time profile(Time t) {
        return profile;
    }

    @Override
    public void update() {
        set(currentval());
    }

}
