package gov.nasa.jpl.spice;

import gov.nasa.jpl.common.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class SpiceTest extends BaseTest {

    @Test
    public void getSpeedOfLight() {
        Assert.assertEquals(299792, Spice.getSpeedOfLight(), 1);
    }

}