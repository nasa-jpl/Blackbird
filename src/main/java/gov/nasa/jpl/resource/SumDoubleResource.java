package gov.nasa.jpl.resource;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

public class SumDoubleResource extends Resource<Double> implements PropertyChangeListener {
    Resource one;
    Resource two;

    public SumDoubleResource(Resource one, Resource two, String subsystem, String units, String interpolation, Double minimum, Double maximum) {
        super(subsystem, units, interpolation, minimum, maximum);
        if (!one.getDataType().equals(two.getDataType())) {
            throw new AdaptationException("Resources put into a SumIntegerResource must be the same type");
        }
        this.one = one;
        this.two = two;
        one.addChangeListener(this);
        two.addChangeListener(this);
    }

    public SumDoubleResource(Resource one, Resource two, String subsystem, String units, String interpolation) {
        this(one, two, subsystem, units, interpolation, null, null);
    }

    public SumDoubleResource(Resource one, Resource two, String subsystem, String units) {
        this(one, two, subsystem, units, "constant");
    }

    public SumDoubleResource(Resource one, Resource two, String subsystem) {
        this(one, two, subsystem, "");
    }

    public SumDoubleResource(Resource one, Resource two) {
        this(one, two, "generic");
    }

    public Double profile(Time t) {
        return 0.0;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // if the first resource we're tracking emitted the event, take its newValue and add it to currentval of other
        if (evt.getSource() == one) {
            set(((Map.Entry<Time, Double>) evt.getNewValue()).getValue() + (Double) two.currentval());
        }
        else {
            set(((Map.Entry<Time, Double>) evt.getNewValue()).getValue() + (Double) one.currentval());
        }
    }
}
