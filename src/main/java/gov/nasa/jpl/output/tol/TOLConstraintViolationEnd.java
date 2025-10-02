package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.constraint.Constraint;
import gov.nasa.jpl.time.Time;

public class TOLConstraintViolationEnd implements TOLRecord{
    private Time violationEndTime;
    private Constraint constraintViolated;

    public TOLConstraintViolationEnd(Time violationEndTime, Constraint constraintViolated){
        this.violationEndTime = violationEndTime;
        this.constraintViolated = constraintViolated;
    }

    @Override
    public String toFlatTOL() {
        return violationEndTime.toString() + ",RELEASE," + constraintViolated.getName() + ",\"END OF VIOLATION " + constraintViolated.getMessage() + "\";\n";
    }

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();

        sb.append("    <TOLrecord type=\"RELEASE\">\n");
        sb.append("        <TimeStamp>"  + violationEndTime.toUTC() + "</TimeStamp>\n");
        sb.append("        <Rule>\n");
        sb.append("            <Name>" + constraintViolated.getName() + "</Name>\n");
        sb.append("        </Rule>\n");
        sb.append("    </TOLrecord>\n");

        return sb.toString();
    }

    /*
     * in JSON, we will represent the constraint as one object, not as beginning and end
     */
    @Override
    public String toESJSON() {
        return "";
    }

    @Override
    public String toPlanJSON(){
        // plan JSON does not have constraints in it currently
        return "";
    }

    @Override
    public Time getTime() {
        return violationEndTime;
    }

    @Override
    public int compareTo(TOLRecord o) {
        return violationEndTime.compareTo(o.getTime());
    }
}
