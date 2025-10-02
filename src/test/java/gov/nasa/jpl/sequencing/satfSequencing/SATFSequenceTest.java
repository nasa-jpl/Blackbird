package gov.nasa.jpl.sequencing.satfSequencing;

import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs.WAKE;
import gov.nasa.jpl.spice.Spice;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Before;
import org.junit.Test;

public class SATFSequenceTest extends BaseTest {

    @Before
    public void setUp(){
        ModelingEngine.getEngine().setTime(Time.getDefaultReferenceTime());
    }

    @Test
    public void satfSequenceTest1() {
        WAKE wake1 = new WAKE(new Time("2019-001T12:00:00"), new Duration("01:00:00"),
                "nf9030_02", false, new Time("2019-001T14:30:00"), "nf9030_03");
        wake1.decompose();
        WAKE wake2 = new WAKE(new Time("2019-001T14:30:00"), new Duration("00:11:00"),
                "nf9030_03", true, new Time("2019-001T17:00:00"), "nf9030_04");
        wake2.decompose();
        Boolean editActTestStatus1 = CommandController.issueCommand(
                "SEQUENCE", "START 2019-001T00:00:00 END 2019-002T00:00:00"
        );
    }
}
