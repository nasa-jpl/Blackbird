package gov.nasa.jpl.input;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;

public class TypeNameConverters {

    public final static List<Class> bareJSONTypes = new ArrayList<>();
    static{
        bareJSONTypes.add(Boolean.class);
        bareJSONTypes.add(Double.class);
        bareJSONTypes.add(Integer.class);
        bareJSONTypes.add(Long.class);
        bareJSONTypes.add(Float.class);
    }

    /**
     * Needed by SetParameterCommand because fields can be primitive types whereas we need to make wrapper objects via reflection
     * @param type
     * @return
     */
    public static String primitiveToWrapperType(String type) {
        if (type.equals("boolean")) {
            return "Boolean";
        }
        else if (type.equals("byte")) {
            return "Byte";
        }
        else if (type.equals("int")) {
            return "Integer";
        }
        else if (type.equals("long")) {
            return "Long";
        }
        else if (type.equals("float")) {
            return "Float";
        }
        else if (type.equals("double")) {
            return "Double";
        }
        else {
            return type;
        }
    }

    /**
     * Used by ActivityTypeList once we've done reflection to store off type internally - most of what it does is convert to wrapper types
     * @param type
     * @return
     */
    public static String typeEnumToWrapperName(Type type){
        String typeName = type.getTypeName();
        if (type == Double.TYPE) {
            typeName = "java.lang.Double";
        }
        else if (type == Float.TYPE) {
            typeName = "java.lang.Float";
        }
        else if (type == Integer.TYPE) {
            typeName = "java.lang.Integer";
        }
        else if (type == Long.TYPE) {
            typeName = "java.lang.Long";
        }
        else if (type == Byte.TYPE) {
            typeName = "java.lang.Byte";
        }
        else if (type == Boolean.TYPE) {
            typeName = "java.lang.Boolean";
        }

        return typeName;
    }

    /**
     * Used in JSON output to write out type names and XML resource metadata - going FROM wrapper types to primitives
     * @param type
     * @return
     */
    public static String convertDataTypeNameToLower(String type, boolean removeGenerics) {
        try {
            if (type.equals("Duration")) {
                return "duration";
            }
            else if (type.equals("double") || type.equals("Double") || type.equals("Float")) {
                return "float";
            }
            else if (type.equals("Integer") || type.equals("Long")) {
                return "integer";
            }
            else if (type.equals("Boolean")) {
                return "boolean";
            }
            else if (type.equals("String")) {
                return "string";
            }
            else if (type.equals("Byte")) {
                return "byte";
            }
            else if (type.equals("Time")) {
                return "time";
            }
            else if (removeGenerics && List.class.isAssignableFrom(Class.forName(type.replaceAll("<.*>$", "")))) {
                return "list";
            }
            else if(List.class.isAssignableFrom(Class.forName(type.replaceAll("<.*>$", "")))){
                return type.toLowerCase().replaceAll("double", "float").replaceAll("long", "integer");
            }
            else if (removeGenerics && Map.class.isAssignableFrom(Class.forName(type.replaceAll("<.*>$", "")))) {
                return "map";
            }
            else if(Map.class.isAssignableFrom(Class.forName(type.replaceAll("<.*>$", "")))){
                return type.toLowerCase().replaceAll("double", "float").replaceAll("long", "integer");
            }
            else {
                return type;
            }
        }
        catch (ClassNotFoundException e){
            return type;
        }
    }

    /**
     * Used in returnValueOf to make sure all wrapper types have their full paths before looking up their valueOf methods
     * @param typeString
     * @return
     */
    public static String getWrapperDataType(String typeString) {
        switch (typeString) {
            case "java.lang.String":
            case "String":
                return "java.lang.String";

            case "java.lang.Integer":
            case "Integer":
                return "java.lang.Integer";

            case "java.lang.Long":
            case "Long":
                return "java.lang.Long";

            case "java.lang.Float":
            case "Float":
                return "java.lang.Float";

            case "java.lang.Double":
            case "Double":
                return "java.lang.Double";

            case "java.lang.Boolean":
            case "Boolean":
                return "java.lang.Boolean";

            case "java.lang.Byte":
            case "Byte":
                return "java.lang.Byte";

            default:
                return null;
        }
    }
}
