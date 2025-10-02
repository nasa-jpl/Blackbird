package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.time.Time;
import org.apache.commons.lang3.NotImplementedException;

public class TOLActivityMetadata implements TOLRecord{

    private String activityTypeName;

    public TOLActivityMetadata(String activityTypeName){
        this.activityTypeName = activityTypeName;
    }

    // flat TOL does not contain activity metadata
    @Override
    public String toFlatTOL() {
        return "";
    }

    // XMLTOL does not contain activity metadata
    @Override
    public String toXML() {
        return "";
    }

    @Override
    public String toESJSON() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("    \"recordType\": \"activity_metadata\",\n");
        sb.append("    \"name\": \"" + activityTypeName + "\",\n");
        sb.append("    \"displayName\": \"" + activityTypeName + "\"\n");

        // we can assume there is always a comma here because there will be at least one resource usage or activity instance
        sb.append("},\n");

        return sb.toString();
    }

    // Plan JSON does not contain activity metadata
    @Override
    public String toPlanJSON(){
        return "";
    }

    @Override
    public Time getTime() {
        throw new NotImplementedException("Calling getTime() on activity metadata is not supported");
    }

    @Override
    public int compareTo(TOLRecord o) {
        return 0;
    }
}
