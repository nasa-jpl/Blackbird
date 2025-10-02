package gov.nasa.jpl.constraint;

public enum ViolationSeverity {
    WARNING("WARNING"),
    ERROR("ERROR");

    private final String text;

    ViolationSeverity(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
