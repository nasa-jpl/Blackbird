package gov.nasa.jpl.resource;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.time.Duration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base ArrayedResource class allows adapters to declare many base resources in one line,
 * and then index into them in activities with string indices.
 * @param <V> Parameterized type must be either another ArrayedResource or a base Resource
 */
public abstract class ArrayedResource<V extends ResourceContainer> implements ResourceContainer {
    // ArrayedResource is special because it has a composition, not inheritance, relationship with Resource

    private HashMap<String, V> individualResources;
    private String name;
    private List<String> indices;

    private ArrayedResource(Class concreteResource, String subsystem, String units, String interpolation, String[] entries, String[]... higherDimensionIndices) {
        indices = new ArrayList<>();
        Class leafResource = concreteResource;
        individualResources = new HashMap<>();

        // we need to figure out the concrete resource that will be the leaf now, before the information gets lost
        if (leafResource == null) {
            leafResource = returnedClass();
        }

        if(entries == null){
            throw new AdaptationException("Tried to create ArrayedResource with a null object for the list of entries. " +
                    "Make sure that your array gets initialized above the ArrayedResource in its class definition, " +
                    "or from a class that extends ParameterDeclaration");
        }

        // if this arrayed resource will contain concrete resource itself
        if (higherDimensionIndices == null || higherDimensionIndices.length == 0) {
            for (int i = 0; i < entries.length; i++) {
                try {
                    // returnedClass() returns the type that is V, so this will work
                    V resourceInstance = (V) leafResource.newInstance();
                    ((Resource) resourceInstance).setSubsystem(subsystem);
                    ((Resource) resourceInstance).setUnits(units);
                    ((Resource) resourceInstance).setInterpolation(interpolation);

                    individualResources.put(entries[i], resourceInstance);

                }
                catch (IllegalAccessException | InstantiationException e) {
                    throw new AdaptationException("Could not create resource of type " + returnedClass().toString() + " during creation of ArrayedResource.");
                }
            }
        }
        // else if this arrayed resource has as children another layer of arrayed resources
        else {
            int remainingNumberOfDimensions = higherDimensionIndices.length - 1;
            String[] newEntries = higherDimensionIndices[0];
            String[][] oneFewerDimensionIndices = new String[remainingNumberOfDimensions][];
            if (higherDimensionIndices.length > 1) {
                System.arraycopy(higherDimensionIndices, 1, oneFewerDimensionIndices, 0, remainingNumberOfDimensions);
            }

            for (int i = 0; i < entries.length; i++) {
                individualResources.put(entries[i], (V) (new ArrayedResource(leafResource, subsystem, units, interpolation, newEntries, oneFewerDimensionIndices) {}));
            }
        }
    }

    public ArrayedResource(String subsystem, String units, String interpolation, String[] entries, String[]... higherDimensionIndices) {
        this(null, subsystem, units, interpolation, entries, higherDimensionIndices);
    }

    // this constructor should only be used when you want to integrate every member of another arrayed resource
    public ArrayedResource(String subsystem, String units, String interpolation, ArrayedResource toIntegrate, long dtInMilliseconds) {
        this(subsystem, units, interpolation, toIntegrate.getEntries(), toIntegrate.getHigherDimensionalIndices());
        setIntegrationParametersForAllContainedResources(toIntegrate, dtInMilliseconds);
    }

    public ArrayedResource(String subsystem, String units, String interpolation, ArrayedResource toIntegrate, Duration dt) {
        this(subsystem, units, interpolation, toIntegrate.getEntries(), toIntegrate.getHigherDimensionalIndices());
        setIntegrationParametersForAllContainedResources(toIntegrate, dt.getTics());
    }

    public ArrayedResource(String subsystem, String units, String[] entries, String[]... higherDimensionIndices) {
        this(subsystem, units, "constant", entries, higherDimensionIndices);
    }

    public ArrayedResource(String subsystem, String[] entries, String[]... higherDimensionIndices) {
        this(subsystem, "", entries, higherDimensionIndices);
    }

    public ArrayedResource(String[] entries, String[]... higherDimensionIndices) {
        this("generic", entries, higherDimensionIndices);
    }

    // the class has to be abstract and always instantiated with {} at the end in order for this method to work
    private Class returnedClass() {
        Type type = getClass().getGenericSuperclass();
        while (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        return (Class) type;
    }

    @Override
    public void update() {
        for (Map.Entry<String, V> singleRes : individualResources.entrySet()) {
            singleRes.getValue().update();
        }
    }

    public V get(String index) {
        V ofInterest = individualResources.get(index);
        if (ofInterest == null) {
            throw new IndexOutOfBoundsException("Index " + index + " not found in ArrayedResource " + name + ".\n" +
                    "Valid indices are: " + String.join(",",getEntries()));
        }
        else {
            return ofInterest;
        }
    }

    public boolean containsIndex(String index) {
        return individualResources.containsKey(index);
    }

    public void setName(String s) {
        name = s;
        // once we've named the container, we're going to go name all our child resources with the style Acceleration[x]
        for (Map.Entry<String, V> singleRes : individualResources.entrySet()) {
            // we need to set up nested indices
            ArrayList<String> childIndices = new ArrayList<>(indices);
            childIndices.add(singleRes.getKey());
            singleRes.getValue().setIndices(childIndices);
            singleRes.getValue().setName(s);
        }
    }

    @Override
    public void setIndices(List<String> index) {
        this.indices = index;
    }

    public String[] getEntries() {
        String[] indices = new String[individualResources.size()];
        return individualResources.keySet().toArray(indices);
    }

    private String[][] getHigherDimensionalIndices() {
        List<String[]> higherDimensionalIndices = new ArrayList<>();
        ResourceContainer current = (new ArrayList<ResourceContainer>(individualResources.values())).get(0);
        while (current instanceof ArrayedResource) {
            higherDimensionalIndices.add(((ArrayedResource) current).getEntries());
            current = (new ArrayList<ResourceContainer>(individualResources.values())).get(0);
        }
        return higherDimensionalIndices.toArray(new String[higherDimensionalIndices.size()][]);
    }

    private void setIntegrationParametersForAllContainedResources(ArrayedResource<ResourceContainer> toIntegrate, long dtInTics) {
        for (Map.Entry<String, ResourceContainer> entry : toIntegrate.individualResources.entrySet()) {
            ResourceContainer internalMatch = individualResources.get(entry.getKey());
            if (internalMatch instanceof IntegratingResource) {
                ((IntegratingResource) internalMatch).setIntegrationTime(dtInTics);
                ((IntegratingResource) internalMatch).setResourceToFollow((DoubleResource) entry.getValue());
            }
            else if (internalMatch instanceof ArrayedResource) {
                setIntegrationParametersForAllContainedResources((ArrayedResource<ResourceContainer>) internalMatch, dtInTics);
            }
            else {
                throw new AdaptationException("Can only use ArrayedResource constructor with ArrayedResource parameter for integrating arrayed resources");
            }
        }
    }

    // recursively goes through all maps and registers leaves with the global resource list
    public void registerResource() {
        for (Map.Entry<String, V> singleRes : individualResources.entrySet()) {
            singleRes.getValue().registerResource();
        }
    }
}
