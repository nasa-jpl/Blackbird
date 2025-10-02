package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.UUID;

import static gov.nasa.jpl.input.TypeNameConverters.bareJSONTypes;
import static gov.nasa.jpl.output.tol.JSONTOLWriter.writeJSONResourceNames;

public class TOLResourceValue implements TOLRecord{
    private Time nodeTime;
    private Comparable valueOut;
    private Resource thisResource;

    public TOLResourceValue(Time t, Comparable valueOut, Resource r){
        nodeTime = t;
        this.valueOut = valueOut;
        thisResource = r;
    }

    @Override
    public String toFlatTOL() {
        String fullName, valAsString;
        Comparable val = valueOut;

        if(String.class.isAssignableFrom(val.getClass())){
            valAsString = "\"" + val.toString() + "\"";
        }
        else{
            valAsString = val.toString();
        }
        if(thisResource.isIndexInArrayedResource()){
            fullName = thisResource.getName() + "[\"" + String.join("\"][\"", thisResource.getIndices()) + "\"]";
        }
        else{
            fullName = thisResource.getName();
        }

        return nodeTime.toUTC() + ",RES," + fullName + "=" + valAsString + ";\n";
    }

    @Override
    public String toXML() {
        return writeResValBlock(nodeTime, valueOut, thisResource, "RES_VAL");
    }

    @Override
    public String toESJSON() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("    \"recordType\": \"resource\",\n");
        sb.append("    \"id\": \"" + UUID.randomUUID().toString() + "\",\n");
        writeJSONResourceNames(sb, thisResource);
        sb.append("    \"component\": \"" + thisResource.getSubsystem() + "\",\n");
        sb.append("    \"dataTimestamp\": \"" + nodeTime.toUTC() + "\",\n");
        // we only want to put quotes around non-primitive values
        if(!bareJSONTypes.contains(valueOut.getClass())) {
            sb.append("    \"dataValue\": \"" + valueOut.toString() + "\"\n");
        }
        else{
            sb.append("    \"dataValue\": " + valueOut.toString() + "\n");
        }
        // we let the overall writer put in the comma and newline for multiple records in a row
        sb.append("}");

        return sb.toString();
    }

    // the plan JSON does not contain resource values at this time
    @Override
    public String toPlanJSON() {
        return "";
    }

    @Override
    public Time getTime() {
        return nodeTime;
    }

    public static String writeResValBlock(Time useTime, Comparable valueOut, Resource currentRes, String blockName) {
        StringBuilder sb = new StringBuilder();

        sb.append("    <TOLrecord type=\"" + blockName + "\">\n");
        sb.append("        <TimeStamp>"  + useTime.toUTC() + "</TimeStamp>\n");
        sb.append("        <Resource>\n");
        sb.append("            <Name>" + currentRes.getName() + "</Name>\n");
        if(currentRes.isIndexInArrayedResource()) {
            for(int i = 0; i < currentRes.getIndices().size(); i++){
                sb.append("            <Index level=\"" + i + "\">" + currentRes.getIndices().get(i) + "</Index>\n");
            }
        }
        // we need to put milliseconds with durations since they aren't primitives in most programming languages or GUIs
        if(Duration.class.isAssignableFrom(valueOut.getClass())){
            sb.append("            <DurationValue milliseconds=\"" + ((Duration)valueOut).getMilliseconds() + "\">" + valueOut.toString() + "</DurationValue>\n");
        }
        else if(Time.class.isAssignableFrom(valueOut.getClass())){
            Time outTime = ((Time) valueOut);
            sb.append("            <TimeValue milliseconds=\"" + (outTime.subtract(Time.getUnixEpoch())).getMilliseconds() + "\">" + outTime.toUTC() + "</TimeValue>\n");
        }
        else {
            sb.append("            <" + ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()) + "Value>" + valueOut.toString() + "</" + ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()) + "Value>\n");
        }
        sb.append("        </Resource>\n");
        sb.append("    </TOLrecord>\n");

        return sb.toString();
    }

    @Override
    public int compareTo(TOLRecord o) {
        return nodeTime.compareTo(o.getTime());
    }
}
