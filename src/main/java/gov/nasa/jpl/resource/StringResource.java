package gov.nasa.jpl.resource;

import java.util.List;

import gov.nasa.jpl.time.Time;

public class StringResource extends Resource<String> {
    private String profile;

    public StringResource(String profile, String subsystem, String units, String interpolation, List<String> possibleStates) {
        super(subsystem, units, interpolation);
        this.possibleStates = possibleStates;
        this.profile = profile;
    }

    public StringResource(String subsystem, String units, String interpolation, List<String> possibleStates) {
        super(subsystem, units, interpolation);
        this.possibleStates = possibleStates;
        this.profile = possibleStates.get(0);

    }

    public StringResource(String profile, String subsystem, String units, String interpolation) {
        super(subsystem, units, interpolation);
        this.profile = profile;
    }

    public StringResource(String profile, String subsystem, String units) {
        super(subsystem, units);
        this.profile = profile;
    }

    public StringResource(String profile, String subsystem) {
        super(subsystem);
        this.profile = profile;
    }

    public StringResource(String profile, List<String> possibleStates) {
        super();
        this.possibleStates = possibleStates;
        this.profile = profile;
    }

    public StringResource(List<String> possibleStates) {
        super();
        this.possibleStates = possibleStates;
        this.profile = possibleStates.get(0);
    }

    public StringResource() {
        super();
    }


    public String profile(Time t) {
        if (profile != null) {
            return profile;
        }
        else {
            return "";
        }
    }

    @Override
    public void update() {
        set(currentval());
    }
}
