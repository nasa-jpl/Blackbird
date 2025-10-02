package gov.nasa.jpl.constraint;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.exampleAdaptation.ConstraintExamples;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ConstraintDeclarationTest extends BaseTest {

    @Test
    public void getConstraintNamesWithClass() {
        Setup.initializeEngine();
        List<String> allConstraints = ConstraintDeclaration.getConstraintNamesWithClass("ConstraintExamples");

        assertTrue(1 < allConstraints.size());
        assertTrue(allConstraints.contains("TwoBeforeOne"));

        List<String> noConstraints = ConstraintDeclaration.getConstraintNamesWithClass("");
        assertEquals(0, noConstraints.size());

        List<String> notAConstraintFile = ConstraintDeclaration.getConstraintNamesWithClass("Res");
        assertEquals(0, notAConstraintFile.size());

    }
}