package gov.nasa.jpl.activity;

import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.util.List;
import java.util.Map;

/**
 * Invoking the INCON command to the engine with a file ends up creating one of these activities,
 * which sits in the modeling queue just like any other activity and jumps all resources it is given
 * to the values it is given at the handoff time
 */
public class InitialConditionActivity extends Activity {
    Map<String, Comparable> initialResourceValues;

    public InitialConditionActivity(Time t, Map<String, Comparable> initialValues) {
        super(t, initialValues);
        initialResourceValues = initialValues;
    }

    @Override
    public void model() {
        List<Resource> allResources = ResourceList.getResourceList().getListOfAllResources();

        for (int i = 0; i < allResources.size(); i++) {
            // we only add the history node if it exists in the initial conditions map
            Resource currentRes = allResources.get(i);
            if (initialResourceValues.containsKey(currentRes.getUniqueName())) {
                Resource<Comparable> resource = ((Resource<Comparable>) allResources.get(i));
                resource.set(initialResourceValues.get(currentRes.getUniqueName()));
            }
        }
    }
}
