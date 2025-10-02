package gov.nasa.jpl.resource;

import java.util.List;

/**
 * This interface is implemented by Resources and Arrayed Resources (which have a composition, not inheritance, relation with Resource)
 * It is used *only* by the reflection methods to print things out to the XMLTOL, and should never be referred to in modeling
 */
public interface ResourceContainer {

    /**
     * This names either the Resource or ArrayedResource with the provided name, which is gotten from reflection
     *
     * @param s The string you want the instance to be named
     */
    void setName(String s);

    /**
     * Set the name of the index of this object inside the containing arrayed resource
     *
     * @param list Index name
     */
    void setIndices(List<String> list);

    /**
     * Once the name is assigned, we add the resource (or resource units inside arrayed resource) to the global resource map
     */
    void registerResource();

    /**
     * Updates either just the resource or all resources in the container
     */
    void update();

}
