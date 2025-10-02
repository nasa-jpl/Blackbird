package gov.nasa.jpl.activity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.input.ReflectionUtilities;
import gov.nasa.jpl.activity.annotations.*;

import static gov.nasa.jpl.input.ReflectionUtilities.LOCATION_OF_CUSTOM_CLASSES;
import static gov.nasa.jpl.input.ReflectionUtilities.getClassNameWithoutPackage;
import static gov.nasa.jpl.input.TypeNameConverters.convertDataTypeNameToLower;
import static gov.nasa.jpl.input.TypeNameConverters.typeEnumToWrapperName;

/**
 * Utility class for storing off metadata about activity types (which are only defined in the Activity base class)
 * for easier input/output.
 */
public class ActivityTypeList {
    private HashMap<String, ActivityType> activityTypes;

    private static final String activitySuperClassLocationInPackage = LOCATION_OF_CUSTOM_CLASSES + ".activity.Activity";

    // we'll use singleton design pattern here because we only need one of them
    private static ActivityTypeList instance = null;

    protected ActivityTypeList() {
        activityTypes = getAllActivitySubclassesByReflection();
    }

    public static ActivityTypeList getActivityList() {
        if (instance == null) {
            instance = new ActivityTypeList();
        }
        return instance;
    }

    private ActivityType getActivityType(String name)
    {
        ActivityType type = activityTypes.get(name);
        if (type == null) {
            throw new AdaptationException("Could not find activity type \"" + name
                    + "\". Make sure to initialize the engine before trying to create activities. Also, anonymous " +
                    "activity classes are not allowed. If you want similar functionality, static nested classes do work.");
        }
        return type;
    }

    /**
     * Returns the class of an input activity name.
     *
     * @param activityName
     * @return
     */
    public Class getActivityClass(String activityName) {
        return getActivityType(activityName).getActivityClass();
    }

    public List<String> getNamesOfAllDefinedTypes(){
        return new ArrayList<>(activityTypes.keySet());
    }

    public List<String> getNamesOfAllTypesWithSubsystem(String subsystem){
        return activityTypes.entrySet().stream().filter(e -> e.getValue().subsystem.equals(subsystem)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public String getDescription(String activityName){
        return getActivityType(activityName).getDescription();
    }

    public String getSubsystem(String activityName){
        return getActivityType(activityName).getSubsystem();
    }

    public String[] getParameterNames(String activityName) {
        List<Map<String, String>> parameters = getActivityType(activityName).getParameters();
        String[] parameterNames = new String[parameters.size()];
        for (int i = 0; i < parameterNames.length; i++) {
            parameterNames[i] = parameters.get(i).get("name");
        }
        return parameterNames;
    }

    public List<Map<String, String>> getParameters(String activityName) {
        return getActivityType(activityName).getParameters();
    }

    public void addToObserverList(String activityName, PropertyChangeListener e) {
        getActivityType(activityName).listeners.add(e);
    }

    public void removeFromObserverList(String activityName, PropertyChangeListener e) {
        getActivityType(activityName).listeners.remove(e);
    }

    public void propertyChangeForActivityType(String activityName, Activity act) {
        getActivityType(activityName).notifyListeners(activityName, act);
    }

    private void recursiveReflection(Map<String,ActivityType> map, Class aClass) {
        try {
            for (Class anInnerClass: aClass.getDeclaredClasses()) {
                recursiveReflection(map, anInnerClass);
            }
            if (Class.forName(activitySuperClassLocationInPackage).isAssignableFrom(aClass) && !Class.forName(activitySuperClassLocationInPackage).equals(aClass)) {
                Annotation[] activityAnnotations = aClass.getAnnotations();

                String description = "";
                String subsystem = "generic";
                for(Annotation annotation: activityAnnotations){
                    if(annotation instanceof TypeData){
                        TypeData activityTypeData = (TypeData) annotation;
                        description = activityTypeData.description();
                        subsystem = activityTypeData.subsystem();
                    }
                }

                String simpleName = getClassNameWithoutPackage(aClass.getName());

                // we grab and get names for all parameters for this activity type
                Constructor[] allConstructors = aClass.getConstructors();

                // here we prevent against multiple constructors, since in this scheme all activities have a set number/type of parameters.
                if(allConstructors.length > 1){
                    throw new AdaptationException("Activity type " + simpleName + " has multiple constructors, which is not allowed");
                }

                // the compiler prevents the case of no constructors from existing, since classes are extending Activity
                // HOWEVER getDeclaredConstructors() only returns public constructors, so we throw an error if constructors are not public
                if(allConstructors.length == 0){
                    throw new AdaptationException("The constructor for activity type " + simpleName + " is not public, which it must be");
                }

                Constructor constructor = allConstructors[0];
                java.lang.reflect.Parameter[] constructorParameters = constructor.getParameters();
                Type[] constructorParameterTypes = constructor.getGenericParameterTypes();
                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

                if (constructorParameters.length > 0 && constructorParameters[0].getName().startsWith("this$")) {
                    throw new AdaptationException("Invalid first parameter. Nested classes should be declared static.");
                }

                List<Map<String, String>> parameters = new ArrayList<>();
                for (int i = 1; i < constructorParameters.length; i++) {
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("name", constructorParameters[i].getName());
                    Type type = constructorParameterTypes[i];
                    String typeName = typeEnumToWrapperName(type);
                    String simpleType = convertDataTypeNameToLower(typeName.replaceAll("gov.nasa.jpl.time.","").replaceAll("java.lang.", ""), false).replaceAll("java.util.", "");

                    paramMap.put("type", typeName);
                    paramMap.put("simpleType", simpleType);

                    Annotation[] annotationsForThisParameter = parameterAnnotations[i];
                    for(Annotation annotation : annotationsForThisParameter) {
                        // even though isAssignableFrom is preferable, here that doesn't work for some reason and we have to use instanceof
                        if (annotation instanceof Parameter) {
                            Parameter thisParametersMetadata = (Parameter) annotation;
                            paramMap.put("description", thisParametersMetadata.description());
                            paramMap.put("units", thisParametersMetadata.units());
                            paramMap.put("defaultValue", thisParametersMetadata.defaultValue());
                            paramMap.put("range", "[" + String.join(",", thisParametersMetadata.range()) + "]");
                        }
                    }

                    // if we didn't have a parameter annotation, put in empty strings
                    if(!paramMap.containsKey("description")){
                        paramMap.put("description", "");
                        paramMap.put("units", "");
                        paramMap.put("defaultValue", "");
                        paramMap.put("range", "");
                    }

                    parameters.add(paramMap);
                }
                ActivityType newType = new ActivityType(simpleName, description, subsystem, aClass, parameters);
                if (map.containsKey(simpleName)){
                    throw new AdaptationException("Found two activity types with name " + simpleName + " , which is not allowed. Please change one of them to not be identical.");
                }
                map.put(simpleName, newType);
            }
        }
        catch (ClassNotFoundException e) {
            // if we can't load the class, we'll just move on to the next one
        }
    }

    private HashMap<String, ActivityType> getAllActivitySubclassesByReflection() {
        HashMap<String, ActivityType> activityTypesToReturn = new HashMap<>();

        for (Class loadedClass : ReflectionUtilities.getListOfAllCustomClasses()) {
            recursiveReflection(activityTypesToReturn, loadedClass);
        }

        return activityTypesToReturn;
    }

    /**
     * this private class stores all type-level information for each activity type without having to instantiate instances
     * it is private so adapters don't try to make copies or get confused about the difference - it is ONLY for reflection storing off information for output products
     */
    private class ActivityType {
        private Class activityClass;
        private String typeName;
        private String description;
        private String subsystem;
        private List<Map<String, String>> parameters;
        private List<PropertyChangeListener> listeners;

        private ActivityType(String typeName, String description, String subsystem, Class activityClass, List<Map<String, String>> parameterNames) {
            this.typeName = typeName;
            this.description = description;
            this.subsystem = subsystem;
            this.activityClass = activityClass;
            this.parameters = parameterNames;
            listeners = new ArrayList<>();
        }

        private Class getActivityClass() {
            return activityClass;
        }

        private String getDescription(){
            return description;
        }

        private String getSubsystem(){
            return subsystem;
        }

        private List<Map<String, String>> getParameters() {
            return parameters;
        }

        private List getListeners() {
            return listeners;
        }

        private void notifyListeners(String activityName, Activity act) {
            for (PropertyChangeListener name : listeners) {
                name.propertyChange(new PropertyChangeEvent(this, activityName, null, act));
            }
        }
    }
}