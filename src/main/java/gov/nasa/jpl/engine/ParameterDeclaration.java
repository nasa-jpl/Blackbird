package gov.nasa.jpl.engine;

import gov.nasa.jpl.input.ReflectionUtilities;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static gov.nasa.jpl.input.ReflectionUtilities.LOCATION_OF_CUSTOM_CLASSES;

public class ParameterDeclaration {
    private static String parameterDeclarationInPackage = LOCATION_OF_CUSTOM_CLASSES + ".engine.ParameterDeclaration";

    // first layer of map has key of name of containing class, and second layer has key field name with value field
    private static HashMap<String, HashMap<String, Field>> allFieldsInAdaptation;

    public static String getTypeOfParameter(String className, String fieldName) {
        Field toLookUp = allFieldsInAdaptation.get(className).get(fieldName);
        return toLookUp.getType().getName();
    }

    public static Object getParameterValue(String className, String fieldName) throws IllegalAccessException {
        Field toLookUp = allFieldsInAdaptation.get(className).get(fieldName);
        return toLookUp.get(null);
    }

    public static void modifyAdaptationParameter(String className, String fieldName, Object newValue) throws IllegalAccessException {
        Field toModify = allFieldsInAdaptation.get(className).get(fieldName);
        toModify.set(null, newValue);
    }

    public static void collectNamesOfAllParameters() {
        allFieldsInAdaptation = new HashMap<>();

        List<Class> allCustomClasses = ReflectionUtilities.getListOfAllCustomClasses();
        for (Class loadedClass : allCustomClasses) {
            try {
                if (Class.forName(parameterDeclarationInPackage).isAssignableFrom(loadedClass) && !Class.forName(parameterDeclarationInPackage).equals(loadedClass)) {
                    Class.forName(loadedClass.getName(), true, loadedClass.getClassLoader());
                    Field[] fieldsInThisClass = loadedClass.getDeclaredFields();
                    if (fieldsInThisClass != null && fieldsInThisClass.length > 0) {
                        String unqualifiedName = ReflectionUtilities.getClassNameWithoutPackage(loadedClass.getName());
                        allFieldsInAdaptation.put(unqualifiedName, new HashMap<>());
                        for (Field f : fieldsInThisClass) {
                            allFieldsInAdaptation.get(unqualifiedName).put(f.getName(), f);
                        }
                    }
                }
            }
            catch (ClassNotFoundException e) {
                // if we can't load the class, we'll just move on to the next one
            }
        }
    }
}
