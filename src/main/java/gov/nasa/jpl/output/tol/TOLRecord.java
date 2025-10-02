package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.time.Time;

public interface TOLRecord extends Comparable<TOLRecord>{
    String toFlatTOL();

    String toXML();

    String toESJSON();

    String toPlanJSON();

    Time getTime();
}
