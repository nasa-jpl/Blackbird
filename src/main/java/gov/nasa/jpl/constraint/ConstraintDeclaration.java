package gov.nasa.jpl.constraint;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.input.ReflectionUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static gov.nasa.jpl.input.ReflectionUtilities.LOCATION_OF_CUSTOM_CLASSES;

public class ConstraintDeclaration {

    private static String constraintDeclarationInPackage = LOCATION_OF_CUSTOM_CLASSES + ".constraint.ConstraintDeclaration";
    private static Set<String> existingNames;

    public void getFields() {
        for (Field f : getClass().getDeclaredFields()) {
            try {
                if (Constraint.class.isAssignableFrom(f.getType())) {
                    Constraint obj = (Constraint) f.get(this);
                    obj.setName(f.getName());
                    if(!existingNames.contains(f.getName())){
                        existingNames.add(f.getName());
                    }
                    else {
                        throw new AdaptationException("The constraint " + f.getName() + " is defined twice. Unique constraint names are needed for the engine to work.");
                    }
                }
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("Could not find name of constraint: " + f.getName());
            }
        }
    }

    public List<String> gatherNames(){
        List<String> toReturn = new ArrayList<>();

        for (Field f : getClass().getDeclaredFields()) {
            if (Constraint.class.isAssignableFrom(f.getType())) {
                toReturn.add(f.getName());
            }
        }

        return toReturn;
    }

    public static void assignNamesToAllConstraints() {
        List<Class> allCustomClasses = ReflectionUtilities.getListOfAllCustomClasses();
        existingNames = new HashSet<>();

        for (Class loadedClass : allCustomClasses) {
            try {
                if (Class.forName(constraintDeclarationInPackage).isAssignableFrom(loadedClass) && !Class.forName(constraintDeclarationInPackage).equals(loadedClass)) {
                    Method m = loadedClass.getMethod("getFields");
                    ConstraintDeclaration dec = (ConstraintDeclaration) loadedClass.newInstance();
                    m.invoke(dec, (Object[]) null);
                }
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                // if we can't load the class, we'll just move on to the next one
            }
            catch(InvocationTargetException ite){
                if(ite.getTargetException() instanceof AdaptationException){
                    ite.getTargetException().printStackTrace();
                }
            }
        }
    }

    public static List<String> getConstraintNamesWithClass(String... className){
        List<String> toReturn = new ArrayList<>();
        HashSet<String> allNames = new HashSet<>(Arrays.asList(className));

        List<Class> allCustomClasses = ReflectionUtilities.getListOfAllCustomClasses();

        for (Class loadedClass : allCustomClasses) {
            try {
                if (Class.forName(constraintDeclarationInPackage).isAssignableFrom(loadedClass) && !Class.forName(constraintDeclarationInPackage).equals(loadedClass) && allNames.contains(loadedClass.getSimpleName())) {
                    Method m = loadedClass.getMethod("gatherNames");
                    ConstraintDeclaration dec = (ConstraintDeclaration) loadedClass.newInstance();
                    toReturn = (List<String>) m.invoke(dec, (Object[]) null);
                }
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                // if we can't load the class, we'll just move on to the next one
            }
            catch(InvocationTargetException ite){
                if(ite.getTargetException() instanceof AdaptationException){
                    ite.getTargetException().printStackTrace();
                }
            }
        }

        return toReturn;
    }

}
