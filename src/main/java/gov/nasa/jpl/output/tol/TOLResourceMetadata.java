package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Time;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

import static gov.nasa.jpl.input.TypeNameConverters.convertDataTypeNameToLower;
import static gov.nasa.jpl.output.tol.JSONTOLWriter.writeJSONResourceNames;

public class TOLResourceMetadata implements TOLRecord {
    private Resource currentRes;

    public TOLResourceMetadata(Resource r){
        this.currentRes = r;
    }

    // the flat TOL does not contain resource metadata
    @Override
    public String toFlatTOL() {
        return "";
    }

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("        <ResourceSpec>\n");
        sb.append("            <Name>" + currentRes.getName() + "</Name>\n");
        if (currentRes.isIndexInArrayedResource()) {
            for (int i = 0; i < currentRes.getIndices().size(); i++) {
                sb.append("            <Index level=\"" + i + "\">" + currentRes.getIndices().get(i) + "</Index>\n");
            }
        }
        sb.append("            <DataType>" + convertDataTypeNameToLower(ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()), true) + "</DataType>\n");
        if (currentRes.getPossibleStates() != null) {
            sb.append("            <PossibleStates>\n");
            for (Object state : currentRes.getPossibleStates()) {
                sb.append("                <" + ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()) + "Value>" + state.toString() + "</" + ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()) + "Value>\n");
            }
            sb.append("            </PossibleStates>\n");

        }
        sb.append("            <Units>" + currentRes.getUnits() + "</Units>\n");
        sb.append("            <Interpolation>" + currentRes.getInterpolation() + "</Interpolation>\n");
        sb.append("            <Maximum>" + currentRes.getMaximumLimit() + "</Maximum>\n");
        sb.append("            <Minimum>" + currentRes.getMinimumLimit() + "</Minimum>\n");
        sb.append("            <Subsystem>" + currentRes.getSubsystem() + "</Subsystem>\n");
        sb.append("        </ResourceSpec>\n");

        return sb.toString();
    }

    @Override
    public String toESJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"recordType\": \"resource_metadata\",\n");
        writeJSONResourceNames(sb, currentRes);
        sb.append("    \"component\": \"" + currentRes.getSubsystem() + "\",\n");
        sb.append("    \"resourceType\": \"" + convertDataTypeNameToLower(ReflectionUtilities.getClassNameWithoutPackage(currentRes.getDataType()), true) + "\",\n");
        sb.append("    \"interpolation\": \"" + currentRes.getInterpolation() + "\",\n");
        if(currentRes.getMinimumLimit() != null && !currentRes.getMinimumLimit().equals("")) {
            sb.append("    \"minLimit\": " + currentRes.getMinimumLimit() + ",\n");
        }
        if(currentRes.getMaximumLimit() != null && !currentRes.getMaximumLimit().equals("")) {
            sb.append("    \"maxLimit\": " + currentRes.getMaximumLimit() + ",\n");
        }
        if(currentRes.getPossibleStates() != null) {
            sb.append("    \"possibleStates\": " + possibleStatesString(currentRes.getPossibleStates()) + ",\n");
        }
        sb.append("    \"unit\": \"" + currentRes.getUnits() + "\"\n");

        // we can assume there is always a comma here because there will be at least one resource usage or activity instance
        sb.append("},\n");
        
        return sb.toString();
    }

    // the plan JSON does not contain resource metadata
    @Override
    public String toPlanJSON() {
        return "";
    }

    @Override
    public Time getTime() {
        throw new NotImplementedException("Calling getTime() on resource metadata is not supported");
    }

    @Override
    public int compareTo(TOLRecord o) {
        return 0;
    }

    private String possibleStatesString(List possibleStates){
        StringBuilder sb = new StringBuilder();
        sb.append("[\"");
        sb.append(String.join("\",\"", possibleStates));
        sb.append("\"]");
        return sb.toString();
    }
}
