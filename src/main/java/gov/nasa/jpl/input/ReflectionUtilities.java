package gov.nasa.jpl.input;

import com.google.common.reflect.ClassPath;
import gov.nasa.jpl.activity.Activity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.serialization.ConvertableFromString;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import static gov.nasa.jpl.input.TypeNameConverters.getWrapperDataType;
import static gov.nasa.jpl.time.Time.TIME_PATTERN;

public class ReflectionUtilities {
    public static final String LOCATION_OF_CUSTOM_CLASSES = "gov.nasa.jpl";
    public static final String ABSOLUTE_TIME_CLASS_PACKAGE = "gov.nasa.jpl.time.Time";
    public static final String RELATIVE_TIME_CLASS_PACKAGE = "gov.nasa.jpl.time.EpochRelativeTime";
    public static final String DURATION_CLASS_PACKAGE = "gov.nasa.jpl.time.Duration";
    private static List<Class> classesInGovNasaJpl;
    private static Map<String,Method> classToValueOf = new HashMap<>();

    // because this is a utilities class with just static methods, we never want anyone to instantiate
    // an object of this, so we hide the public constructor by making a private one
    private ReflectionUtilities() {
    }

    /**
     * This method takes in the name of a fully qualified package and returns the relative name (without prefixes)
     *
     * @param withPackage - name of the string that contains a package prefix
     * @return - a string with only the relative name (last level of hierarchy)
     */
    public static String getClassNameWithoutPackage(String withPackage) {
        String toReturn = withPackage;
        // grab everything past the last '.'
        int lastDot = toReturn.lastIndexOf('.');
        if (lastDot != -1) {
            toReturn = toReturn.substring(lastDot + 1);
        }
        // to support inner classes, we take everything past the last '$'
        int lastDollar = toReturn.lastIndexOf('$');
        if (lastDollar != -1) {
            toReturn = toReturn.substring(lastDollar + 1);
        }
        return toReturn;
    }

    /**
     * Finds all the classes in current classloader and assigns to static variable those in our custom namespace,
     * so we don't have to waste time running getPackages or classLoader more than once ever
     *
     * @return a list of Class objects that represent all the ones the core or adapters have devised
     */
    public static List<Class> getListOfAllCustomClasses() {
        if (classesInGovNasaJpl == null) {
            classesInGovNasaJpl = new ArrayList<>();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            ClassPath classpath;
            try {
                classpath = ClassPath.from(loader); // scans the class path used by classloader
            }
            catch (IOException ie) {
                // if we can't use this loader, something is seriously wrong and we should just quit
                throw new RuntimeException("Something is very wrong - we couldn't get a classpath from the classloader:\n" + ie.getMessage());
            }
            for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(LOCATION_OF_CUSTOM_CLASSES)) {
                try {
                    classesInGovNasaJpl.add(classInfo.load());
                }
                catch (NoClassDefFoundError e) {
                    // if we can't load the class, we'll just move on to the next one
                }
            }
        }
        return classesInGovNasaJpl;
    }

    static Method getValueOfMethod(String className)
            throws ClassNotFoundException, NoSuchMethodException
    {
        synchronized (classToValueOf)
        {
            Method m = classToValueOf.get(className);
            if (m == null) {
                Class<?> classType = Class.forName(className);
                m = classType.getDeclaredMethod("valueOf", String.class);
                classToValueOf.put(className, m);
            }
            return m;
        }
    }

    /**
     * This method takes in the name of a class and a value as a string and returns
     * an object of the given class with the input value.
     *
     * @param typeString  - name of data class type (String, Time, Duration, etc.)
     * @param valueString - string of the value to be converted
     * @param removeDoubleQuotes - if a string comes in with double quotes, should they be removed?
     * @return - the input value string as an object of the input className
     */
    public static Object returnValueOf(String typeString, String valueString, boolean removeDoubleQuotes) {
        // if the typeString is String then we don't need to call valueOf
        typeString = typeString.trim();
        if (matchesString(typeString)) {
            // if there are double quotes around the value then we should remove them
            String trimmed = valueString.trim();
            if (removeDoubleQuotes && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return trimmed.substring(1, trimmed.length()-1);
            }
            else {
                return valueString;
            }
        }

        try {
            String wrapperType = getWrapperDataType(typeString);
            if (wrapperType != null) {
                // check if this class is one of the built-in types
                // if this is a built-in wrapper class then we can just
                // call valueOf() on the string
                Method m = getValueOfMethod(wrapperType);
                return m.invoke(null, valueString);
            }

            String listType = getListType(typeString);
            if (listType != null) {
                String listString = valueString.trim();
                if (listString.startsWith("[") && listString.endsWith("]")) {
                    listString = listString.substring(1, listString.length() - 1);
                }
                return initList(listType, listString);
            }

            String mapType = getMapType(typeString);
            if (mapType != null) {
                String mapString = valueString.trim();
                if (mapString.startsWith("{") && mapString.endsWith("}")) {
                    mapString = mapString.substring(1, mapString.length() - 1);
                }
                return initMap(mapType, mapString);
            }
            // if this is a custom data type then we need to use mutation and then return the value
            Class<?> classType = Class.forName(typeString);
            ConvertableFromString classInstance = (ConvertableFromString) classType.newInstance();
            classInstance.valueOf(valueString);
            return classInstance;
        }
        // if we could not find the class using forName then look for the class using getCustomDataType
        catch (ClassNotFoundException e) {
            Class<?> classType = getCustomDataType(typeString);
            try {
                // since this is a custom data type we will need to use mutation to get the value
                ConvertableFromString classInstance = (ConvertableFromString) classType.newInstance();
                classInstance.valueOf(valueString);
                return classInstance;
            }
            catch (IllegalAccessException | InstantiationException ex) {
                throw new RuntimeException("Error: could not instantiate data type " + classType.getName() + "\n");
            }
        }
        catch (InstantiationException | NoSuchMethodException
                | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException("Error: could not correctly parse and call .valueOf on " + typeString
                    + " for value: " + valueString + "\n");
        }
    }

    /**
     * Takes in the name of a custom data type, looks through all classes implementing
     * ConvertableFromString, and then returns an instance of the class which implements
     * the interface and has the same name as the input string. Throws a RuntimeException
     * if unable to find the input data type.
     *
     * @param dataTypeName - name of data class to look up
     * @return - custom data type class
     */
     static Class<?> getCustomDataType(String dataTypeName) {
        Class<?> dataTypeClass = null;

        List<Class> allCustomClasses = ReflectionUtilities.getListOfAllCustomClasses();
        for (Class loadedClass : allCustomClasses) {
            // check if the class has the same relative name and implements ConvertableFromString
            if (loadedClass.getName().endsWith(dataTypeName) && ConvertableFromString.class.isAssignableFrom(loadedClass)) {
                if (dataTypeClass != null) {
                    throw new RuntimeException("Error: Data type " + dataTypeName + " has been defined "
                            + "in multiple Packages implementing the interface ConvertableFromString:\n"
                            + dataTypeClass.getName() + "\n" + loadedClass.getName() + "\n");
                }
                // if dataTypeClass has not been assigned yet then populate it with this class
                dataTypeClass = loadedClass;
            }
        }

        if (dataTypeClass != null) {
            return dataTypeClass;
        }
        else {
            throw new RuntimeException("Error: Could not find data type " + dataTypeName + " in any of the available "
                    + "packages.");
        }
    }

    /**
     * Takes in a parameter string which contains zero or more parameters, separated by commas
     * and returns an object list of parameters.
     *
     * @param parameterString - String containing comma-separated parameterValues
     * @param paramTypes      - List of strings representing the type of each expected parameter
     * @return - returns array of arguments to be used for the activity constructor
     */
    public static Object[] parseActivityParameters(String parameterString, String[] paramTypes) {
        ArrayList<Object> args = new ArrayList<>();
        String[] paramValues = StringParsingUtilities.splitParamStringByChar(parameterString, ',');

        for (int i = 0; i < paramValues.length; i++) {
            String paramStringValue = paramValues[i];
            String paramType = paramTypes[i];
            if(paramType.equals(ABSOLUTE_TIME_CLASS_PACKAGE)) {
                Matcher absoluteTime = TIME_PATTERN.matcher(paramStringValue);
                if (absoluteTime.find()) {
                    paramType = ABSOLUTE_TIME_CLASS_PACKAGE;
                } else {
                    paramType = RELATIVE_TIME_CLASS_PACKAGE;
                }
            }
            Object paramValue = returnValueOf(paramType, paramStringValue, true);
            args.add(paramValue);
        }

        return args.toArray();
    }

    // this is pretty fragile and ensures adapters always have to use the abstract type and not concretions for signatures, but maybe that's what we want
     static String getListType(String typeString) {
        if (typeString.startsWith("java.util.List<")) {
            return typeString.substring(15, typeString.length() - 1);
        }
        return null;
    }

    // this is pretty fragile and ensures adapters always have to use the abstract type and not concretions for signatures, but maybe that's what we want
     static String getMapType(String typeString) {
        if (typeString.startsWith("java.util.Map<")) {
            return typeString.substring(14, typeString.length() - 1);
        }
        return null;
    }

    // not using regular expressions for speed, even though this isn't too elegant
     static boolean matchesString(String typeString) {
        return  typeString.equals("String") || typeString.equals("java.lang.String");
    }

    /**
     * This method creates an object array from an input string.
     *
     * @param typeString
     * @param valueString
     * @return
     */
    private static List<Object> initList(String typeString, String valueString) {
        List<Object> newList = new ArrayList<>();
        String[] splitValueStr = StringParsingUtilities.splitParamStringByChar(valueString, ',');

        // loop over each value pair in valueString and add the value to the list
        for (String singleValue : splitValueStr) {
            String adjustedType = typeString;
            if(typeString.equals(ABSOLUTE_TIME_CLASS_PACKAGE) && !TIME_PATTERN.matcher(singleValue).find()) {
                adjustedType = RELATIVE_TIME_CLASS_PACKAGE;
            }
            Object paramValue = returnValueOf(adjustedType, singleValue, true);
            newList.add(paramValue);
        }

        return newList;
    }

    /**
     * This method will be used to create a HashMap from an input string.
     *
     * @param typeString
     * @param valueString
     * @return
     */
    private static Map<Object, Object> initMap(String typeString, String valueString) {
        Map<Object, Object> newMap = new HashMap<>();

        String[] keyValueTypeArray = StringParsingUtilities.splitParamStringByChar(typeString, ',');

        // give error if the size of the array is not 2 (one key and one value)
        if (keyValueTypeArray.length != 2) {
            throw new RuntimeException("Error: Map key/value type string " + keyValueTypeArray
                    + " did not contain a single key/value pair separated by a comma."
                    + " Could not parse correctly.");
        }
        String keyType = keyValueTypeArray[0];
        String valueType = keyValueTypeArray[1];

        // don't add to map if the valueString is empty
        Matcher emptyStringMatch = RegexUtilities.EMPTY_PATTERN.matcher(valueString);
        if (!emptyStringMatch.find()) {
            String[] valueArray = StringParsingUtilities.splitParamStringByChar(valueString, ',');

            // loop over key/value pairs and add to the map
            for (int pairIndex = 0; pairIndex < valueArray.length; pairIndex++) {
                String[] keyValuePair = StringParsingUtilities.splitParamStringByChar(valueArray[pairIndex], '=');

                if (keyValuePair.length != 2) {
                    throw new RuntimeException("Error: map string " + valueString + " had a key/value pair"
                            + " which had " + keyValuePair.length + " values separated by =, when only 2 were expected."
                            + " Could not parse correctly.");
                }

                if(keyType.equals(ABSOLUTE_TIME_CLASS_PACKAGE) && !TIME_PATTERN.matcher(keyValuePair[0]).find()) {
                    keyType = RELATIVE_TIME_CLASS_PACKAGE;
                }
                else if(keyType.equals(ABSOLUTE_TIME_CLASS_PACKAGE)){
                    keyType = ABSOLUTE_TIME_CLASS_PACKAGE;
                }
                if(valueType.equals(ABSOLUTE_TIME_CLASS_PACKAGE) && !TIME_PATTERN.matcher(keyValuePair[1]).find()) {
                    valueType = RELATIVE_TIME_CLASS_PACKAGE;
                }
                else if(valueType.equals(ABSOLUTE_TIME_CLASS_PACKAGE)){
                    valueType = ABSOLUTE_TIME_CLASS_PACKAGE;
                }
                newMap.put(returnValueOf(keyType, keyValuePair[0], true), returnValueOf(valueType, keyValuePair[1], true));
            }
        }

        return newMap;
    }

    /**
     * Goes from List&lt;Map&lt;String, String&gt;&gt; to String[], where the only key pulled out is 'type' and 'Time' is artificially added as the zeroth element
     * Used for grabbing parameter types from the Activity type list so parseActivityParameters knows what to do with it
     * @param parameters
     * @return
     */
    public static String[] getActivityParameterTypeArray(List<Map<String, String>> parameters){
        String[] paramTypes = new String[parameters.size() + 1];
        // this gets changed to Absolute or Relative time once value is available
        paramTypes[0] = "Time";
        for (int i = 0; i < parameters.size(); i++) {
            paramTypes[i + 1] = parameters.get(i).get("type");
        }
        return paramTypes;
    }

    /**
     * Instantiates an activity and updates the actID.
     *
     * @param args
     * @param actID
     * @param actClass
     */
    public static void instantiateActivity(Class<?> actClass, Object[] args, UUID actID) {
        try {
            Activity newActInstance = (Activity) ConstructorUtils.invokeConstructor(actClass, args);
            newActInstance.setID(actID);
            newActInstance.decompose();
        }
        catch (NoSuchMethodException e) {
            throw new AdaptationException("Error: Could not find Time, Object[] constructor"
                    + " for " + actClass.getName() + ".");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Error: Could not instantiate new activity of type"
                    + actClass.getName() + ".");
        }
        catch (ClassCastException e) {
            throw new AdaptationException("Error: Could not cast new activity to Activity.");
        }
    }
}
