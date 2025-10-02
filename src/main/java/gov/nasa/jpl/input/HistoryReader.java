package gov.nasa.jpl.input;

import java.io.IOException;

public interface HistoryReader {
    void readInHistoryOfActivitiesAndResource(boolean areReadInResourcesFrozen, boolean shouldActivitiesDecompose) throws IOException;
}
