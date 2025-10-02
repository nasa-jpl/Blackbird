package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.time.*;

import java.util.List;
import java.util.Map;

import static gov.nasa.jpl.input.TypeNameConverters.bareJSONTypes;
import static gov.nasa.jpl.input.TypeNameConverters.convertDataTypeNameToLower;

public class TOLActivityBegin implements TOLRecord{
    private Activity recordedAct;

    public TOLActivityBegin(Activity act){
        recordedAct = act;
    }

    @Override
    public String toFlatTOL() {
        StringBuilder sb = new StringBuilder();

        Object[] paramValues = recordedAct.getParameterObjects();
        String[] paramNames  = ActivityTypeList.getActivityList().getParameterNames(recordedAct.getType());
        if(paramNames.length != paramValues.length){
            throw new AdaptationException("Mismatch between number of parameter names and values for activity type " + recordedAct.getType() + ".\nTo appear correctly in TOL, all parameters to activity must be passed to super().");
        }

        sb.append(recordedAct.getStart().toString());
        sb.append(",ACT_START," + recordedAct.getName());
        sb.append(",d," + recordedAct.getDuration().toString());
        sb.append(",\"VISIBLE\",");
        sb.append("type=" + recordedAct.getType() + ",");
        sb.append("node_id=" + recordedAct.getID() + ",");
        sb.append("description=\"\",");
        sb.append("attributes=(\"Subsystem\"=\"" + ActivityTypeList.getActivityList().getSubsystem(recordedAct.getType()) + "\"),");
        sb.append("parameters=(");
        for(int i = 0; i<paramValues.length; i++) {
            sb.append(paramNames[i] + "=");

            try {
                writeParameterValuetoString(sb, paramValues[i], true, false);
            }
            catch (AdaptationException e){
                throw new AdaptationException("Parameter '" + paramNames[i] + "' for activity of type " + recordedAct.getType() + " at time " + recordedAct.getStart().toString() + " is null or has a null member when writing out to file, which is not allowed");
            }
            if(i < paramValues.length - 1){
                sb.append(",");
            }
        }
        sb.append(");\n");
        return sb.toString();
    }

    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();

        Object[] paramValues = recordedAct.getParameterObjects();
        String[] paramNames  = ActivityTypeList.getActivityList().getParameterNames(recordedAct.getType());
        if(paramNames.length != paramValues.length){
            throw new AdaptationException("Mismatch between number of parameter names and values for activity type " + recordedAct.getType() + ".\nTo appear correctly in TOL, all parameters to activity must be passed to super().");
        }

        // at the activity start, we print out everything about the activity
        sb.append("    <TOLrecord type=\"ACT_START\">\n");
        sb.append("        <TimeStamp>"  + recordedAct.getStart().toUTC() + "</TimeStamp>\n");
        sb.append("        <Instance>\n");
        sb.append("            <ID>" + recordedAct.getIDString() + "</ID>\n");
        sb.append("            <Name>" + recordedAct.getName() + "</Name>\n");
        sb.append("            <Type>" + recordedAct.getType() + "</Type>\n");
        sb.append("            ");
        if(recordedAct.getParent() != null){
            sb.append("<Parent>" + recordedAct.getParent().getID());
        }
        else{
            sb.append("<Parent>");
        }
        sb.append("</Parent>\n");
        sb.append("            <Visibility>visible</Visibility>\n");
        sb.append("            <Attributes>\n");
        sb.append("                <Attribute>\n");
        sb.append("                    <Name>start</Name>\n");
        sb.append("                    <TimeValue milliseconds=\"" + recordedAct.getStart().getMilliseconds() + "\">" + recordedAct.getStart().toUTC() + "</TimeValue>\n");
        sb.append("                </Attribute>\n");
        sb.append("                <Attribute>\n");
        sb.append("                    <Name>span</Name>\n");
        sb.append("                    <DurationValue milliseconds=\"" + recordedAct.getDuration().getMilliseconds() + "\">" + recordedAct.getDuration().toString() + "</DurationValue>\n");
        sb.append("                </Attribute>\n");
        sb.append("                <Attribute>\n");
        sb.append("                    <Name>subsystem</Name>\n");
        sb.append("                    <StringValue>" + ActivityTypeList.getActivityList().getSubsystem(recordedAct.getType()) + "</StringValue>\n");
        sb.append("                </Attribute>\n");
        sb.append("                <Attribute>\n");
        sb.append("                    <Name>legend</Name>\n");
        sb.append("                    <StringValue>" + recordedAct.getGroup() + "</StringValue>\n");
        sb.append("                </Attribute>\n");
        sb.append("            </Attributes>\n");
        sb.append("            <Parameters>\n");
        for(int i = 0; i<paramValues.length; i++) {
            sb.append("                <Parameter>\n");
            sb.append("                    <Name>" + paramNames[i] + "</Name>\n");

            try {
                writeParameterValueToXML(sb, "                    ", paramValues[i]);
            }
            catch(AdaptationException e){
                throw new AdaptationException("Parameter '" + paramNames[i] + "' for activity of type " + recordedAct.getType() + " at time " + recordedAct.getStart().toString() + " is null or has a null member when writing out to file, which is not allowed.");
            }
            sb.append("                </Parameter>\n");
        }
        sb.append("            </Parameters>\n");
        sb.append("        </Instance>\n");
        sb.append("    </TOLrecord>\n");

        return sb.toString();
    }

    @Override
    public String toESJSON(){
        StringBuilder sb = new StringBuilder();

        Object[] paramValues = recordedAct.getParameterObjects();
        String[] paramNames  = ActivityTypeList.getActivityList().getParameterNames(recordedAct.getType());
        if(paramNames.length != paramValues.length){
            throw new AdaptationException("Mismatch between number of parameter names and values for activity type " + recordedAct.getType() + ".\nTo appear correctly in TOL, all parameters to activity must be passed to super().");
        }

        sb.append("{\n");
        sb.append("    \"recordType\": \"activity\",\n");
        sb.append("    \"id\": \"" + recordedAct.getIDString() + "\",\n");
        if(recordedAct.getParent() != null) {
            sb.append("    \"parentId\": \"" + recordedAct.getParent().getIDString() + "\",\n");
        }
        sb.append("    \"startTime\": \"" + recordedAct.getStart().toUTC() + "\",\n");
        sb.append("    \"endTime\": \"" + recordedAct.getEnd().toUTC() + "\",\n");
        sb.append("    \"activityName\": \"" + recordedAct.getName() + "\",\n");
        sb.append("    \"activityType\": \"" + recordedAct.getType() + "\",\n");
        sb.append("    \"parameters\": [\n");
        for(int i = 0; i<paramValues.length; i++) {
            if(paramValues[i] == null){
                throw new AdaptationException("Parameter '" + paramNames[i] + "' for activity of type " + recordedAct.getType() + " at time " + recordedAct.getStart().toString() + " is null or has a null member when writing out to file, which is not allowed.");
            }

            // all blocks except the last one have commas after them
            String endOfParameterDelimiter = (i == paramValues.length - 1) ? "" : ",";

            sb.append("        {\n");
            sb.append("        \"name\": \"" + paramNames[i] + "\",\n");
            sb.append("        \"type\": \"");
            getTypeOfParameterForJSON(sb, paramValues[i]);
            sb.append(         "\",\n");
            try {
                if (bareJSONTypes.contains(paramValues[i].getClass())) {
                    // do not add quotes - also none of the bare types are recursive so we don't need to call writeParameterValue
                    sb.append("        \"value\": " + paramValues[i].toString() + "\n");
                } else {
                    // add quotes
                    sb.append("        \"value\": \"");
                    writeParameterValuetoString(sb, paramValues[i], false, true);
                    sb.append("\"\n");
                }
            }
            catch(AdaptationException e){
                throw new AdaptationException("Parameter '" + paramNames[i] + "' for activity of type " + recordedAct.getType() + " at time " + recordedAct.getStart().toString() + " is null or has a null member when writing out to file, which is not allowed.");
            }

            sb.append("        }" + endOfParameterDelimiter + "\n");
        }
        sb.append("        ],\n");
        sb.append("    \"legend\": \"" + recordedAct.getGroup() + "\"\n");

        // we let the overall writer put in the comma and newline for multiple records in a row
        sb.append("}");

        return sb.toString();

    }

    @Override
    public String toPlanJSON(){
        StringBuilder sb = new StringBuilder();

        sb.append("        {\n");
        sb.append("            \"type\": \"" + recordedAct.getType() + "\",\n");
        sb.append("            \"start\": \"" + recordedAct.getStart().toString() + "\",\n");
        sb.append("            \"parameters\": [\n");
        writeParametersToJSON(sb, recordedAct.getParameterObjects(), false);
        sb.append("            ],\n");
        sb.append("            \"notes\": \"" + recordedAct.getNotes() + "\",\n");
        sb.append("            \"id\": \"" + recordedAct.getIDString() + "\",\n");
        if(recordedAct.getParent() != null){
            sb.append("            \"parent\": \"" + recordedAct.getParent().getIDString() + "\"\n");
        }
        else{
            sb.append("            \"parent\": null\n");
        }
        // we let the overall writer put in the comma and newline for multiple records in a row
        sb.append("        }");

        return sb.toString();
    }

    public String toCSV(){
        Object[] paramValues = recordedAct.getParameterObjects();

        // Start,ID,Parent ID,Param 1 Value,Param 2 Value, ...
        StringBuilder sb = new StringBuilder();
        sb.append(recordedAct.getStart());
        sb.append(",");
        sb.append(recordedAct.getIDString());
        sb.append(",");
        if(recordedAct.getParent() != null) {
            sb.append(recordedAct.getParent().getIDString());
        }
        sb.append(",");
        for(int i = 0; i<paramValues.length; i++){

            writeParameterValuetoString(sb, paramValues[i], true, true);

            if(i < paramValues.length - 1){
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * We need this because lists and maps have complicated XML representations
     * We need a lot of reflection because it is stored internally as just an object and we need to write out its class
     * @param sb - the stringbuilder we're going to be writing the XML to
     * @param indentSpaces - a string including the number of spaces to indent
     * @param parameter - the actual parameter to be written out
     */
    private void writeParameterValueToXML(StringBuilder sb, String indentSpaces, Object parameter){
        if(parameter == null){
            throw new AdaptationException("");
        }
        // recursive cases: parameter is a list or a map
        else if(List.class.isAssignableFrom(parameter.getClass())){
            List paramList = (List) parameter;
            sb.append(indentSpaces + "<ListValue>\n");
            for(int i = 0; i < paramList.size(); i++){
                sb.append(indentSpaces + "    <Element index=\"" + i + "\">\n");
                writeParameterValueToXML(sb, indentSpaces + "        ", paramList.get(i));
                sb.append(indentSpaces + "    </Element>\n");
            }
            sb.append(indentSpaces + "</ListValue>\n");
        }
        else if(Map.class.isAssignableFrom(parameter.getClass())){
            Map<Object, Object> paramMap = (Map) parameter;
            sb.append(indentSpaces + "<StructValue>\n");
            for(Map.Entry entry: paramMap.entrySet()){
                sb.append(indentSpaces + "    <Element index=\"" + entry.getKey().toString() + "\">\n");
                writeParameterValueToXML(sb, indentSpaces + "        ", entry.getValue());
                sb.append(indentSpaces + "    </Element>\n");
            }
            sb.append(indentSpaces + "</StructValue>\n");
        }
        // base case: parameter is not a collection so we can just print it
        else{
            if (parameter.getClass().equals(EpochRelativeTime.class)) {
                // convert the parameter to a regular Time since XMLTOL can't handle relative times
                parameter = new Time(((EpochRelativeTime) parameter).toUTC());
            }
            String parameterType = ReflectionUtilities.getClassNameWithoutPackage(parameter.getClass().getName());
            sb.append(indentSpaces + "<" + parameterType + "Value>" + parameter.toString() + "</" + parameterType + "Value>\n");
        }
    }

    private void writeParameterValuetoString(StringBuilder sb, Object parameter, boolean addQuotesToString, boolean useCurlyBracesForMaps){
        String mapStartCharacter = "[";
        String mapEndCharacter = "]";
        if(useCurlyBracesForMaps){
            mapStartCharacter = "{";
            mapEndCharacter = "}";
        }

        if(parameter == null){
            throw new AdaptationException("");
        }
        // recursive cases: parameter is a list or a map
        else if(List.class.isAssignableFrom(parameter.getClass())) {
            sb.append("[");
            List paramList = (List) parameter;
            for(int i = 0; i < paramList.size(); i++) {
                writeParameterValuetoString(sb, paramList.get(i), addQuotesToString, useCurlyBracesForMaps);
                if(i < paramList.size() - 1){
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        else if(Map.class.isAssignableFrom(parameter.getClass())) {
            sb.append(mapStartCharacter);
            Map<Object, Object> paramMap = (Map) parameter;
            int i = 0;
            for(Map.Entry entry: paramMap.entrySet()) {
                writeParameterValuetoString(sb, entry.getKey(), addQuotesToString, useCurlyBracesForMaps);
                sb.append("=");
                writeParameterValuetoString(sb, entry.getValue(), addQuotesToString, useCurlyBracesForMaps);
                if(i < paramMap.size() - 1){
                    sb.append(",");
                }
                i++;
            }
            sb.append(mapEndCharacter);
        }
        else if(addQuotesToString && String.class.isAssignableFrom(parameter.getClass())){
            sb.append("\"" + parameter.toString() + "\"");
        }
        // base case: parameter is not a collection so we can just print it
        else{
            if (parameter.getClass().equals(EpochRelativeTime.class)) {
                EpochRelativeTime et = (EpochRelativeTime) parameter;
                sb.append(et.toUTC());
            }
            else {
                sb.append(parameter.toString());
            }
        }
    }

    public void writeParametersToJSON(StringBuilder sb, Object[] paramValues, boolean useFullTypeName){
        List<Map<String, String>> paramObjects = ActivityTypeList.getActivityList().getParameters(recordedAct.getType());
        if(paramObjects.size() != paramValues.length){
            throw new AdaptationException("Mismatch between number of parameter names and values for activity type " + recordedAct.getType() + ".\nTo appear correctly in plan JSON, all parameters to activity must be passed to super().");
        }

        for(int i = 0; i<paramValues.length; i++) {
            // all blocks except the last one have commas after them
            String endOfParameterDelimiter = (i == paramValues.length - 1) ? "" : ",";
            sb.append("                {\n");
            sb.append("                    \"name\": \"" + paramObjects.get(i).get("name") + "\",\n");
            if(useFullTypeName) {
                sb.append("                    \"type\": \"" + paramObjects.get(i).get("type") + "\",\n");
            }
            else{
                sb.append("                    \"type\": \"" + paramObjects.get(i).get("simpleType") + "\",\n");
            }
            sb.append("                    \"value\": ");
            try {
                writeParameterValuetoJSON(sb, "                    ", paramValues[i]);
            }
            catch(AdaptationException e){
                throw new AdaptationException("Parameter '" + paramObjects.get(i).get("name") + "' for activity of type " + recordedAct.getType() + " at time " + recordedAct.getStart().toString() + " is null or has a null member, which is not allowed.");
            }
            sb.append("\n                }");
            sb.append(endOfParameterDelimiter + "\n");
        }
    }

    private void writeParameterValuetoJSON(StringBuilder sb, String indentSpaces, Object parameter){
        if(parameter == null){
            throw new AdaptationException("");
        }
        // recursive case 1
        else if(List.class.isAssignableFrom(parameter.getClass())) {
            List paramList = (List) parameter;
            sb.append("[\n");
            for(int i = 0; i < paramList.size(); i++){
                String endOfParameterDelimiter = (i == paramList.size() - 1) ? "" : ",";
                sb.append(indentSpaces + "    ");
                writeParameterValuetoJSON(sb, indentSpaces + "    ", paramList.get(i));
                sb.append(endOfParameterDelimiter + "\n");
            }
            sb.append(indentSpaces + "]");
        }
        // recursive case 2
        else if(Map.class.isAssignableFrom(parameter.getClass())) {
            Map<Object, Object> paramMap = (Map) parameter;
            sb.append("{\n");
            int i = 0;
            for(Map.Entry entry: paramMap.entrySet()){
                String endOfParameterDelimiter = (i == paramMap.size() - 1) ? "" : ",";
                sb.append(indentSpaces + "    ");
                writeParameterValuetoJSON(sb, indentSpaces + "    ", entry.getKey());
                sb.append(": ");
                writeParameterValuetoJSON(sb, indentSpaces + "    ", entry.getValue());
                sb.append(endOfParameterDelimiter + "\n");
                i++;
            }
            sb.append(indentSpaces + "}");
        }
        // base case 1
        else if(bareJSONTypes.contains(parameter.getClass())){
            // do not add quotes - also none of the bare types are recursive so we don't need to call writeParameterValue
            sb.append(parameter.toString());
        }
        // base case 2
        else{
            sb.append("\"" + parameter.toString().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\\n").replace("\r", "\\r") + "\"");
        }
    }

    private void getTypeOfParameterForJSON(StringBuilder sb, Object parameter){
        if (EpochRelativeTime.class.equals(parameter.getClass())) {
            sb.append("time");
        }
        else if (List.class.isAssignableFrom(parameter.getClass())){
            sb.append("list");
        }
        else if (Map.class.isAssignableFrom(parameter.getClass())){
            sb.append("map");
        }
        else{
            sb.append(convertDataTypeNameToLower(ReflectionUtilities.getClassNameWithoutPackage(parameter.getClass().getName()), true));
        }
    }

    @Override
    public Time getTime() {
        return recordedAct.getStart();
    }

    @Override
    public int compareTo(TOLRecord o) {
        return recordedAct.getStart().compareTo(o.getTime());
    }
}
