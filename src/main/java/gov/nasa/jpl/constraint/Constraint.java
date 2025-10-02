package gov.nasa.jpl.constraint;

import gov.nasa.jpl.time.Time;

import java.beans.PropertyChangeListener;
import java.util.*;

// constraints are passive and not used for rearranging any activities - they just tell you when something might be wrong, for one to go investigate manually
public abstract class Constraint implements PropertyChangeListener {
    // these are the fields every constraint has
    protected String name;
    protected String message;
    protected ViolationSeverity severity;

    // we need a list and not a map here because multiple violations could start at the same time and we want to preserve all of them
    protected List<Map.Entry<Time, Time>> listOfViolationBeginAndEndTimes;

    protected Time mostRecentTimeViolationBegan = null;

    public Constraint(String message, ViolationSeverity severity) {
        this.message = message;
        this.severity = severity;
        listOfViolationBeginAndEndTimes = new ArrayList<>();
        // we need to register ourselves with the global list of constraints in order to be output to file later
        ConstraintInstanceList globalListOfConstraints = ConstraintInstanceList.getConstraintList();
        globalListOfConstraints.registerConstraint(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String assign) {
        name = assign;
    }

    public String getMessage() {
        return message;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public Iterator<Map.Entry<Time, Time>> historyIterator() {
        return listOfViolationBeginAndEndTimes.iterator();
    }

    protected void addViolation(Time begin, Time end) {
        listOfViolationBeginAndEndTimes.add(new AbstractMap.SimpleImmutableEntry<>(begin, end));
    }

    abstract void hookToListeners();

    abstract void unhookFromListeners();

    public abstract void clearViolationHistory();

    // to be called after modeling, in case the end time of a violation hasn't been calculated (so it can be set to the end of modeling)
    public abstract void finalizeConstraintAfterModeling();

}
