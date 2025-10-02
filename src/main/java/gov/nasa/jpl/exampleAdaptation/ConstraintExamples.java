package gov.nasa.jpl.exampleAdaptation;

import gov.nasa.jpl.constraint.ConstraintDeclaration;
import gov.nasa.jpl.constraint.ForbiddenResourceConstraint;
import gov.nasa.jpl.constraint.RequiredPrecederConstraint;
import gov.nasa.jpl.constraint.ViolationSeverity;
import gov.nasa.jpl.time.Duration;

import static gov.nasa.jpl.exampleAdaptation.Res.*;

public class ConstraintExamples extends ConstraintDeclaration {
    public static ForbiddenResourceConstraint forbidden = new ForbiddenResourceConstraint(
            IntegratesA.whenGreaterThan(5000.0), "", ViolationSeverity.WARNING);
    public static RequiredPrecederConstraint TwoBeforeOne = new RequiredPrecederConstraint(
            "ActivityOne", "ActivityTwo", new Duration("00:01:40"),
            "ActivityOne must be preceded by an instance of ActivityTwo", ViolationSeverity.ERROR);
}
