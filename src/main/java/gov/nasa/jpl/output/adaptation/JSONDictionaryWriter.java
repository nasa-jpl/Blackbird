package gov.nasa.jpl.output.adaptation;

import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.resource.ResourceList;

import java.util.List;
import java.util.Map;

public class JSONDictionaryWriter extends AdaptationWriter{
    @Override
    public void writeFileContents(List<String> actTypeList, ResourceList resList, ConstraintInstanceList conList) {
        writeJSONHeader();

        writeActivityTypes(actTypeList);

        // writer.print(",");

        // writeResourceTypes();

        // writer.print(",");

        // writeConstraintTypes();

        writeJSONFooter();
    }

    private void writeActivityTypes(List<String> actTypeList){
        writer.println("    \"activities\":{");
        for(int i = 0; i<actTypeList.size(); i++){
            writer.print(activityTypeToJson(actTypeList.get(i)));

            if(i < actTypeList.size()-1){
                writer.print(",\n");
            }
        }
        writer.println("\n    }");
    }

    public String activityTypeToJson(String thisType){
        StringBuilder sb = new StringBuilder();

        List<Map<String, String>> paramObjects = ActivityTypeList.getActivityList().getParameters(thisType);

        sb.append("        \"" + thisType + "\":{\n");
        sb.append("            \"description\":\"" + ActivityTypeList.getActivityList().getDescription(thisType) + "\",\n");
        sb.append("            \"subsystem\":\"" + ActivityTypeList.getActivityList().getSubsystem(thisType) + "\",\n");
        sb.append("            \"parameters\":[\n");
        for(int j = 0; j<paramObjects.size(); j++){
            Map<String, String> param = paramObjects.get(j);
            sb.append("                {\n");
            sb.append("                    \"name\":\"" + param.get("name") + "\",\n");
            sb.append("                    \"type\":\"" + param.get("simpleType") + "\",\n");
            sb.append("                    \"default\":\"" + param.get("defaultValue") + "\",\n");
            sb.append("                    \"units\":\"" + param.get("units") + "\",\n");
            sb.append("                    \"description\":\"" + param.get("description") + "\",\n");
            sb.append("                    \"range\":\"" + param.get("range") + "\"\n");
            sb.append("                }");
            if(j < paramObjects.size() - 1){
                sb.append(",\n");
            }
        }
        sb.append("\n            ]\n");
        sb.append("        }");

        return sb.toString();
    }

    private void writeJSONHeader(){
        // currently we don't need anything else in the header like a schema but that could change
        writer.println("{");
    }

    private void writeJSONFooter(){
        writer.println("}");
    }

}
