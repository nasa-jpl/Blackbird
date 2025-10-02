package gov.nasa.jpl.scheduler;

public enum CompareToValues {
    LESSTHAN(-1),
    EQUALTO(0),
    GREATERTHAN(1);

    private int compareToInt;

    private CompareToValues(int i) {
        compareToInt = i;
    }

    public int toInt() {
        return compareToInt;
    }
}

