package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.time.Time;

import java.util.UUID;

public class TOLConstraintViolationBegin implements TOLRecord{
    private Time violationTime;
    private Constraint constraintViolated;

    public TOLConstraintViolationBegin(Time violationTime, Constraint constraintViolated){
        this. violationTime = violationTime;
        this.constraintViolated = constraintViolated;
    }

    @Override
    public String toFlatTOL() {
        return violationTime.toString() + "," + constraintViolated.getSeverity().toString() + "," + constraintViolated.getName() + ",\"" + constraintViolated.getMessage() + "\";\n";
    }

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();

        sb.append("    <TOLrecord type=\"" + constraintViolated.getSeverity().toString() + "\">\n");
        sb.append("        <TimeStamp>"  + violationTime.toUTC() + "</TimeStamp>\n");
        sb.append("        <Rule>\n");
        sb.append("            <Name>" + constraintViolated.getName() + "</Name>\n");
        sb.append("        </Rule>\n");
        sb.append("    </TOLrecord>\n");

        return sb.toString();
    }

    @Override
    public String toESJSON() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        // severity comes out as either ERROR or WARNING (for XML default) but JSON likes lower case
        sb.append("    \"recordType\": \"" + constraintViolated.getSeverity().toString().toLowerCase() + "\",\n");
        sb.append("    \"id\": \"" + UUID.randomUUID().toString() + "\",\n");
        sb.append("    \"dataTimestamp\": \"" + violationTime.toUTC() + "\",\n");
        sb.append("    \"message\": \"" + constraintViolated.getMessage() + "\"\n");

        // we let the overall writer put in the comma and newline for multiple records in a row
        sb.append("}");

        return sb.toString();
    }

    @Override
    public String toPlanJSON(){
        // plan JSON does not have constraints in it currently
        return "";
    }

    @Override
    public Time getTime() {
        return violationTime;
    }

    @Override
    public int compareTo(TOLRecord o) {
        return violationTime.compareTo(o.getTime());
    }
}
