package gov.nasa.jpl.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import static gov.nasa.jpl.activity.Activity.now;

public class IntegratingResource extends DoubleResource implements PropertyChangeListener {
    long dtInTics;
    DoubleResource toFollow;

    public IntegratingResource(DoubleResource toFollow, long dtInTics, String subsystem, String units, String interpolation, Double minimum, Double maximum) {
        super(0.0, subsystem, units, interpolation, minimum, maximum);
        toFollow.addChangeListener(this);
        this.dtInTics = dtInTics;
        this.toFollow = toFollow;
    }

    public IntegratingResource(DoubleResource toFollow, long dtInTics, String subsystem, String units, String interpolation) {
        this(toFollow, dtInTics, subsystem, units, interpolation, null, null);
    }

    public IntegratingResource(DoubleResource toFollow, long dtInTics, String subsystem, String units) {
        this(toFollow, dtInTics, subsystem, units, "constant");
    }

    public IntegratingResource(DoubleResource toFollow, long dtInTics, String subsystem) {
        this(toFollow, dtInTics, subsystem, "");
    }

    public IntegratingResource(DoubleResource toFollow, long dtInTics) {
        this(toFollow, dtInTics, "generic");
    }

    public IntegratingResource(DoubleResource toFollow, Duration dt, String subsystem, String units, String interpolation, Double minimum, Double maximum) {
        this(toFollow, dt.getTics(), subsystem, units, interpolation, minimum, maximum);
    }

    public IntegratingResource(DoubleResource toFollow, Duration dt, String subsystem, String units, String interpolation) {
        this(toFollow, dt.getTics(), subsystem, units, interpolation, null, null);
    }

    public IntegratingResource(DoubleResource toFollow, Duration dt, String subsystem, String units) {
        this(toFollow, dt.getTics(), subsystem, units, "constant");
    }

    public IntegratingResource(DoubleResource toFollow, Duration dt, String subsystem) {
        this(toFollow, dt.getTics(), subsystem, "");
    }

    public IntegratingResource(DoubleResource toFollow, Duration dt) {
        this(toFollow, dt.getTics(), "generic");
    }

    // this constructor should only be used by arrayed resources
    IntegratingResource() {
        super();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Map.Entry<Time, Double> oldEntry = (Map.Entry<Time, Double>) evt.getOldValue();
        Map.Entry<Time, Double> newEntry = (Map.Entry<Time, Double>) evt.getNewValue();

        // if this is the first time the observed variable gets a value, we set the integrator to its profile
        if (oldEntry.getKey() == null || oldEntry.getKey().greaterThan(newEntry.getKey())) {
            set(profile(ModelingEngine.getEngine().getCurrentTime()));
            return;
        }
        // don't do anything if the new and old time are the same
        if (oldEntry.getKey().equals(newEntry.getKey())) {
            return;
        }
        // this means we had an old and new rate that we must integrate to add to our value
        Duration t = newEntry.getKey().subtract(oldEntry.getKey());
        set(valueAt(oldEntry.getKey()) + (oldEntry.getValue() * ((double) t.getTics() / dtInTics)));
    }

    public Double profile(Time t) {
        return 0.0;
    }


    // for instantiating these resources from an arrayed resource
    void setResourceToFollow(DoubleResource toFollow) {
        this.toFollow = toFollow;
        toFollow.addChangeListener(this);
    }

    void setIntegrationTime(long dtInTics) {
        this.dtInTics = dtInTics;
    }

    /**
     * Updates the resource that this IntegratingResource is following at the current time.
     * This will only update the value if there is not already a data point at the current
     * time and if we are currently modeling.
     */
    public void updateFollowResource() {
        synchronized (toFollow.resourceHistory) {
            boolean isModeling = ModelingEngine.getEngine().isModeling();
            if (!isModeling) {
                return;
            }
            boolean resourceNodeExists = toFollow.resourceHistory.containsKey(now());
            if (resourceNodeExists) {
                return;
            }
            toFollow.update();
        }
    }

    /**
     * Overrides the parent DoubleResource interpval to also update the followed
     * resource so this resource will be correct during modeling.
     */
    @Override
    public Double interpval(Time t) {
        if (canInterpolate(t, this)) {
            return super.interpval(t);
        }
        return this.valueAt(t);
    }

    private boolean canInterpolate(Time t, DoubleResource r) {
        Time pastTime;
        Time ceilingTime;
        double pastValue;
        double futureValue;
        Double interpolatedValue;
        synchronized (r.resourceHistory) {
            pastTime = r.resourceHistory.floorKey(t);
            ceilingTime = r.resourceHistory.ceilingKey(t);

            // if pastTime and ceilingTime are defined then we can interpolate normally
            // if either are null then we need special logic for interpolating
            return pastTime != null && ceilingTime != null;
        }
    }

    /**
     * Overrides the parent valueAt.
     * This always performs interpolation when available for the values defined in
     * IntegratingResource. However, this method does not interpolate the followed
     * resource values when calculating the integrating resource value.
     */
    @Override
    public Double valueAt(Time t) {
        synchronized (resourceHistory) {
            boolean exactTimeExists = this.resourceHistory.containsKey(t);
            if (exactTimeExists) {
                return super.valueAt(t);
            }
        }

        boolean thisHasElements = this.resourceHistoryHasElements();
        boolean followedDoesntHaveElements = !toFollow.resourceHistoryHasElements();
        if (thisHasElements && followedDoesntHaveElements) {
            return super.valueAt(t);
        }

        if (!thisHasElements) {
            return this.profile(t);
        }

        // if toFollow has elements, then this resource will always have elements at the
        // same times, because it is a listener

        // first get the most recent value of this resource, then we need to add the
        // integrated value of the toFollow resource at this time
        Time mostRecentIntegratedTime;
        Double mostRecentIntegratedValue;
        Time mostRecentFollowTime;
        Double mostRecentFollowValue;

        synchronized (this.resourceHistory) {
            boolean timeBeforeFirstElement = t.lessThan(this.resourceHistory.firstKey());
            if (timeBeforeFirstElement) {
                return this.profile(t);
            }

            mostRecentIntegratedTime = this.resourceHistory.floorKey(t);
            mostRecentIntegratedValue = this.resourceHistory.get(mostRecentIntegratedTime);
        }
        synchronized (toFollow.resourceHistory) {
            // the integrated times will always be a superset of followed values because
            // it is a listener, so we use mostRecentIntegratedTime for floorKey here in case
            // mostRecentIntegratedTime is after floorKey(t)
            mostRecentFollowTime = toFollow.resourceHistory.floorKey(mostRecentIntegratedTime);
            mostRecentFollowValue = toFollow.resourceHistory.get(mostRecentFollowTime);
        }

        Duration d = t.subtract(mostRecentIntegratedTime);
        return mostRecentIntegratedValue + (mostRecentFollowValue * ((double) d.getTics() / dtInTics));
    }

    /**
     * Overrides currentval to always use interpval for integrating resources.
     */
    @Override
    public Double currentval() {
        return this.valueAt(now());
    }

    /**
     * Integrating resource does not support the add method because it only tracks the
     * followed resource changes.
     */
    @Override
    public void add(double toAdd) {
        throw new UnsupportedOperationException("IntegratingResource does not support direct addition. Values are computed through integration.");
    }

    /**
     * Integrating resource does not support the subtract method because it only tracks the
     * followed resource changes.
     */
    @Override
    public void subtract(double toSubtract) {
        throw new UnsupportedOperationException("IntegratingResource does not support direct subtraction. Values are computed through integration.");
    }

    /**
     * The update method for this class only updates the followed resource so the current
     * value of this resource is correct.
     */
    @Override
    public void update() {
        updateFollowResource();
    }
}
