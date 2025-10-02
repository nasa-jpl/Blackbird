package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Time;

public class TOLActivityEnd implements TOLRecord{
    private Activity recordedAct;

    public TOLActivityEnd(Activity act){
        recordedAct = act;
    }

    @Override
    public String toFlatTOL() {
        StringBuilder sb = new StringBuilder();
        sb.append(recordedAct.getEnd().toString());
        sb.append(",ACT_END," + recordedAct.getIDString() + ";\n");
        return sb.toString();
    }

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();

        sb.append("    <TOLrecord type=\"ACT_END\">\n");
        sb.append("        <TimeStamp>"  + recordedAct.getEnd().toUTC() + "</TimeStamp>\n");
        sb.append("        <ActivityID>" + recordedAct.getIDString() + "</ActivityID>\n");
        sb.append("    </TOLrecord>\n");

        return sb.toString();
    }

    /*
     * We decided that the JSON would only contain Activities as single entries, indexed by start time
     */
    @Override
    public String toESJSON() {
        return "";
    }

    /*
     * We decided that the JSON would only contain Activities as single entries, indexed by start time
     */
    @Override
    public String toPlanJSON() {
        return "";
    }

    @Override
    public Time getTime() {
        return recordedAct.getEnd();
    }

    @Override
    public int compareTo(TOLRecord o) {
        return recordedAct.getEnd().compareTo(o.getTime());
    }
}
