package gov.nasa.jpl.resource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.input.ReflectionUtilities;

import static gov.nasa.jpl.input.ReflectionUtilities.LOCATION_OF_CUSTOM_CLASSES;

public class ResourceDeclaration {

    private static String resourceDeclarationInPackage = LOCATION_OF_CUSTOM_CLASSES + ".resource.ResourceDeclaration";
    private static Set<String> existingNames;

    public void getFields() {
        for (Field f : getClass().getDeclaredFields()) {
            try {
                if (ResourceContainer.class.isAssignableFrom(f.getType())) {
                    if (existingNames.contains(f.getName())) {
                        throw new AdaptationException("The resource " + f.getName() + " is defined twice. Unique resource names are needed for the engine to work.");
                    }
                    existingNames.add(f.getName());
                    ResourceContainer obj = (ResourceContainer) f.get(this);
                    // obj can be null if the resource is intentionally not loaded statically and then initialized by a method
                    // in a multi-mission model, but the method is never called by a particular adaptation - in this
                    // case, we want to harmlessly skip it but not throw an internal NPE that will be picked up by the debugger
                    if(obj != null) {
                        obj.setName(f.getName());
                        obj.registerResource();
                    }
                }
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("Could not find name of resource: " + f.getName());
            }
        }
    }

    public static void assignNamesToAllResources() {
        List<Class> allCustomClasses = ReflectionUtilities.getListOfAllCustomClasses();
        existingNames = new HashSet<>();

        for (Class loadedClass : allCustomClasses) {
            try {
                if (Class.forName(resourceDeclarationInPackage).isAssignableFrom(loadedClass) && !Class.forName(resourceDeclarationInPackage).equals(loadedClass)) {
                    Method m = loadedClass.getMethod("getFields");
                    ResourceDeclaration dec = (ResourceDeclaration) loadedClass.newInstance();
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
}
