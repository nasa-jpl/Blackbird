package gov.nasa.jpl.resource;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class DurationResource extends Resource<Duration> {
    private Duration profile;

    public DurationResource(Duration profile, String subsystem, String interpolation, String units, Duration minimum, Duration maximum) {
        super(subsystem, units, interpolation, minimum, maximum);
        this.profile = profile;
    }

    public DurationResource(Duration profile, String subsystem, String interpolation) {
        super(subsystem, interpolation);
        this.profile = profile;
    }

    public DurationResource(Duration profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public DurationResource(Duration profile, String subsystem) {
        super(subsystem);
        this.profile = profile;
    }

    public DurationResource() {
        super();
        profile = new Duration();
    }

    public Duration profile(Time t) {
        return profile;
    }

    public void add(Duration toAdd) {
        set(currentval().add(toAdd));
    }

    public void subtract(Duration toSubtract) {
        set(currentval().subtract(toSubtract));
    }

    @Override
    public void update() {
        set(currentval());
    }

}
