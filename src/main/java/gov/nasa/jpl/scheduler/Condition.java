package gov.nasa.jpl.scheduler;

import gov.nasa.jpl.engine.AdaptationException;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.time.Time;

import java.util.*;

public class Condition {
    // This class is a tree with a recursive evaluate function
    // Activities should always hold onto the root of the tree
    private Condition left = null;
    private Condition right = null;

    // This member is the kind of node we are (OR, AND, XOR, NOT, or BASE)
    ConditionNodeType nodeType;

    // If we are a leaf node, we have a resource, a CompareToValues enum, and a Comparable threshold
    private Resource<Comparable> valuesToScheduleOffOf;
    private CompareToValues inequality;
    private Comparable threshold;

    // this leaf can either be true or false, and we want to store what we were last
    private boolean lastEvaluatedTo;

    // This is the constructor of a base node
    public Condition(Resource valuesToScheduleOffOf, CompareToValues inequality, Comparable threshold) {
        if(valuesToScheduleOffOf == null){
            throw new AdaptationException("Condition created with null base resource.");
        }
        this.valuesToScheduleOffOf = valuesToScheduleOffOf;
        this.inequality = inequality;
        this.threshold = threshold;
        nodeType = ConditionNodeType.BASE;

        // we need to do this for nested schedulers where activities are created during the modeling loop, not at t=0, and so may have missed resource updates until now
        if (ModelingEngine.getEngine().isModeling()) {
            update(valuesToScheduleOffOf, valuesToScheduleOffOf.currentval());
        }
    }

    // private constructor used for quasi-constructor static methods below
    private Condition() {
    }

    // Builds an 'or' node
    // The reason we're passing an array instead of just two conditions to OR is that we want to support a use case
    // where you can easily pass in a lot of conditions and don't have to nest 'Condition.or(Condition.or(...),new Condition)'
    public static Condition or(Condition... conditions) {
        // I wish we could check for length at compile time but we obviously can't
        if (conditions == null || conditions.length < 2) {
            return null;
        }
        Condition toReturn = new Condition();
        toReturn.nodeType = ConditionNodeType.OR;

        // no matter what, right node always gets set to end of list which is then not passed to next layer down
        // this will build a lopsided tree but ok, we expect this to be maybe 5 layers deep max anyway
        toReturn.right = conditions[conditions.length - 1];

        // base case
        if (conditions.length == 2) {
            toReturn.left = conditions[0];
        }
        // recursive case
        else {
            toReturn.left = or(Arrays.copyOfRange(conditions, 0, conditions.length - 1));
        }

        return toReturn;
    }

    // Builds an 'and' node
    // The reason we're passing an array instead of just two conditions to AND is that we want to support a use case
    // where you can easily pass in a lot of conditions and don't have to nest 'Condition.and(Condition.and(...),new Condition)'
    public static Condition and(Condition... conditions) {
        // I wish we could check for length at compile time but we obviously can't
        if (conditions == null || conditions.length < 2) {
            return null;
        }
        Condition toReturn = new Condition();
        toReturn.nodeType = ConditionNodeType.AND;

        // no matter what, right node always gets set to end of list which is then not passed to next layer down
        // this will build a lopsided tree but ok, we expect this to be maybe 5 layers deep max anyway
        toReturn.right = conditions[conditions.length - 1];

        // base case
        if (conditions.length == 2) {
            toReturn.left = conditions[0];
        }
        // recursive case
        else {
            toReturn.left = and(Arrays.copyOfRange(conditions, 0, conditions.length - 1));
        }
        return toReturn;
    }

    // Builds a 'xor' node
    public static Condition xor(Condition one, Condition two) {
        Condition toReturn = new Condition();
        toReturn.nodeType = ConditionNodeType.XOR;
        toReturn.left = one;
        toReturn.right = two;
        return toReturn;
    }

    // Builds a 'not' node
    public static Condition not(Condition toBeNegated) {
        Condition toReturn = new Condition();
        toReturn.nodeType = ConditionNodeType.NOT;
        // see convention in 'evaluate'
        toReturn.left = toBeNegated;
        return toReturn;
    }

    /********** the next four methods are package protected on purpose - we only want Windows to access them ********/

    // we might want scheduling methods to know if we're a base node or not
    boolean isBaseNode() {
        if (nodeType == ConditionNodeType.BASE) {
            return true;
        }
        else {
            return false;
        }
    }

    Resource getResource() {
        return valuesToScheduleOffOf;
    }

    CompareToValues getInequality() {
        return inequality;
    }

    Comparable getThreshold() {
        return threshold;
    }

    public boolean isTrue() {
        return lastEvaluatedTo;
    }

    public boolean evaluate(Time t) {
        if (nodeType == ConditionNodeType.NOT) {
            // by convention, if we only have one leaf it is put in the left space
            return !left.evaluate(t);
        }
        else if (nodeType == ConditionNodeType.AND) {
            return left.evaluate(t) && right.evaluate(t);
        }
        else if (nodeType == ConditionNodeType.OR) {
            return left.evaluate(t) || right.evaluate(t);
        }
        else if (nodeType == ConditionNodeType.XOR) {
            return left.evaluate(t) ^ right.evaluate(t);
        }
        // base case is BASE
        else {
            return Integer.signum(valuesToScheduleOffOf.valueAt(t).compareTo(threshold)) == inequality.toInt();
        }
    }

    public void setEvaluatedTo(Time t) {
        if(left != null){
            left.setEvaluatedTo(t);
        }
        if(right != null){
            right.setEvaluatedTo(t);
        }
        lastEvaluatedTo = evaluate(t);
    }

    // this updates all children nodes that have the same resource as the parameter and any upstream nodes that contain them
    // the whole reason we're doing this is so that all resource values don't have to be pulled from history store which could get pretty big
    public void update(Resource r, Comparable v) {
        if (nodeType == ConditionNodeType.NOT) {
            left.update(r, v);
            lastEvaluatedTo = !left.lastEvaluatedTo;
        }
        else if (nodeType == ConditionNodeType.AND) {
            left.update(r, v);
            right.update(r, v);
            lastEvaluatedTo = left.lastEvaluatedTo && right.lastEvaluatedTo;
        }
        else if (nodeType == ConditionNodeType.OR) {
            left.update(r, v);
            right.update(r, v);
            lastEvaluatedTo = left.lastEvaluatedTo || right.lastEvaluatedTo;
        }
        else if (nodeType == ConditionNodeType.XOR) {
            left.update(r, v);
            right.update(r, v);
            lastEvaluatedTo = left.lastEvaluatedTo ^ right.lastEvaluatedTo;
        }
        // base case is BASE
        else {
            if (r == valuesToScheduleOffOf) {
                lastEvaluatedTo = (Integer.signum(v.compareTo(threshold)) == inequality.toInt());
            }
        }
    }

    // this should only be called externally on the root node by activities that own conditions
    public Set<Resource<?>> getAllResourcesRecursively() {
        Set<Resource<?>> toReturn = new HashSet<Resource<?>>();
        if (nodeType == ConditionNodeType.BASE) {
            toReturn.add(valuesToScheduleOffOf);
        }
        else {
            toReturn.addAll(left.getAllResourcesRecursively());
        }
        if (nodeType == ConditionNodeType.XOR || nodeType == ConditionNodeType.AND || nodeType == ConditionNodeType.OR) {
            toReturn.addAll(right.getAllResourcesRecursively());
        }
        return toReturn;
    }
}
