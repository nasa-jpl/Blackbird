package gov.nasa.jpl.input;

import gov.nasa.jpl.engine.InitialConditionList;

import java.io.IOException;

public interface InconReader {
    InitialConditionList getInitialConditions() throws IOException;
}
