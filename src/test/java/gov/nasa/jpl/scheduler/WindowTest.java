package gov.nasa.jpl.scheduler;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.resource.DoubleResource;
import gov.nasa.jpl.resource.StringResource;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class WindowTest extends BaseTest {

    @Test
    public void getWindows() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        myEngine.setTime(new Time("2000-001T00:00:07"));
        ResourceA.set(5.0);
        myEngine.setTime(new Time("2000-001T00:00:12"));
        ResourceA.set(9.0);
        myEngine.setTime(new Time("2000-001T00:00:15"));
        ResourceA.set(-15.0);
        myEngine.setTime(new Time("2000-001T00:00:20"));
        ResourceA.set(6.0);

        DoubleResource ResourceB = new DoubleResource(0.0, "subsystem1");
        myEngine.setTime(new Time("2000-001T00:00:07"));
        ResourceB.set(0.0);
        myEngine.setTime(new Time("2000-001T00:00:15"));
        ResourceB.set(6.0);
        myEngine.setTime(new Time("2000-001T00:00:20"));
        ResourceB.set(-5.1);

        StringResource ResourceC = new StringResource("NONE", "subsystem1");
        myEngine.setTime(new Time("2000-001T00:00:03"));
        ResourceC.set("NOT_NONE");
        myEngine.setTime(new Time("2000-001T00:00:12"));
        ResourceC.set("NONE");
        myEngine.setTime(new Time("2000-001T00:00:19"));
        ResourceC.set("ALSO_NOT_NONE");
        myEngine.setTime(new Time("2000-001T00:00:20"));
        ResourceC.set("NONE");

        Condition aboveFiveA = ResourceA.whenGreaterThan(5.0);
        Condition aboveFiveB = ResourceB.whenGreaterThan(5.0);
        Condition notNoneC   = ResourceC.whenNotEqualTo("NONE");
        Condition noneC      = ResourceC.whenEqualTo("NONE");
        Condition eitherAboveFive = Condition.or(aboveFiveA, aboveFiveB);
        Condition bothAboveFive = Condition.and(aboveFiveA, aboveFiveB);

        Window[] aboveFiveWindows = Window.getWindows(aboveFiveA, new Time("2000-001T00:00:12"), new Time("2000-001T00:00:21"));
        Window[] expectedWindows = new Window[2];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:15"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:20"), new Time("2000-001T00:00:21"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        aboveFiveWindows = Window.getWindows(aboveFiveA, new Time("2000-001T00:00:13"), new Time("2000-001T00:00:14"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:13"), new Time("2000-001T00:00:14"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        aboveFiveWindows = Window.getWindows(aboveFiveA, new Time("2000-001T00:00:23"), new Time("2000-001T00:00:25"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:23"), new Time("2000-001T00:00:25"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        Condition belowFive = ResourceA.whenLessThan(5.0);
        Window[] belowFiveWindows = Window.getWindows(belowFive, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05"));
        assertArrayEquals(expectedWindows, belowFiveWindows);

        DoubleResource ResourceEmpty = new DoubleResource(0.0, "subsystem2");
        Condition checkEmptyResource = ResourceEmpty.whenLessThan(5.0);
        belowFiveWindows = Window.getWindows(checkEmptyResource, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05"));
        assertArrayEquals(expectedWindows, belowFiveWindows);

        Window[] combinedWindows = Window.getWindows(eitherAboveFive, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:21"));
        expectedWindows = new Window[]{new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:21"))};
        assertArrayEquals(expectedWindows, combinedWindows);

        combinedWindows = Window.getWindows(eitherAboveFive, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:15"));
        expectedWindows = new Window[]{new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:15"))};
        assertArrayEquals(expectedWindows, combinedWindows);

        combinedWindows = Window.getWindows(bothAboveFive, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:21"));
        expectedWindows = new Window[0];
        assertArrayEquals(expectedWindows, combinedWindows);

        Window[] notNoneWindows = Window.getWindows(notNoneC, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:21"));
        Window[] noneWindows    = Window.getWindows(noneC, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:21"));
        Window notNoneWindow1   = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:12"));
        Window notNoneWindow2   = new Window(new Time("2000-001T00:00:19"), new Time("2000-001T00:00:20"));
        Window noneWindow1      = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:03"));
        Window noneWindow2      = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:19"));
        Window noneWindow3      = new Window(new Time("2000-001T00:00:20"), new Time("2000-001T00:00:21"));

        Window[] notNoneExpectedWindows = new Window[] {notNoneWindow1, notNoneWindow2};
        Window[] noneExpectedWindows    = new Window[] {noneWindow1, noneWindow2, noneWindow3};

        assertArrayEquals(noneExpectedWindows, noneWindows);
        assertArrayEquals(notNoneExpectedWindows, notNoneWindows);

        Condition nullOrCondition1 = Condition.or();
        assertNull(nullOrCondition1);

        Condition nullOrCondition2 = Condition.or(ResourceA.whenEqualTo(0.0));
        assertNull(nullOrCondition2);

        Condition nullAndCondition1 = Condition.and();
        assertNull(nullAndCondition1);

        Condition nullAndCondition2 = Condition.and(ResourceA.whenEqualTo(0.0));
        assertNull(nullAndCondition2);
    }

    @Test
    public void interpWindows() {
        ModelingEngine myEngine = ModelingEngine.getEngine();
        DoubleResource ResourceA = new DoubleResource(0.0, "subsystem1");
        myEngine.setTime(new Time("2000-001T00:00:07"));
        ResourceA.set(5.0);
        myEngine.setTime(new Time("2000-001T00:00:12"));
        ResourceA.set(9.0);
        myEngine.setTime(new Time("2000-001T00:00:15"));
        ResourceA.set(-15.0);
        myEngine.setTime(new Time("2000-001T00:00:20"));
        ResourceA.set(6.0);

        DoubleResource ResourceB = new DoubleResource(0, "");
        myEngine.setTime(new Time("2000-001T00:00:00"));
        ResourceB.set(10.0);
        myEngine.setTime(new Time("2000-001T00:00:07"));
        ResourceB.set(7.0);
        myEngine.setTime(new Time("2000-001T00:00:10"));
        ResourceB.set(4.0);
        myEngine.setTime(new Time("2000-001T00:00:15"));
        ResourceB.set(0.0);

        DoubleResource ResourceC = new DoubleResource(0, "");
        myEngine.setTime(new Time("2026-055T12:55:00.000000"));
        ResourceC.set(13.602934500753085);
        myEngine.setTime(new Time("2026-055T13:00:00.000000"));
        ResourceC.set(14.529111467892298);
        myEngine.setTime(new Time("2026-055T13:05:00.000000"));
        ResourceC.set(15.458207337280664);
        myEngine.setTime(new Time("2026-055T13:45:00.000000"));
        ResourceC.set(22.976673937459);
        myEngine.setTime(new Time("2026-055T13:50:00.000000"));
        ResourceC.set(23.92478454977616);
        myEngine.setTime(new Time("2026-055T13:55:00.000000"));
        ResourceC.set(24.87420019309698);

        DoubleResource ResourceD = new DoubleResource(0, "");
        myEngine.setTime(new Time("2026-163T06:40:00.000000"));
        ResourceD.set(21.772779754499766);
        myEngine.setTime(new Time("2026-163T06:45:00.000000"));
        ResourceD.set(21.09873791312974);
        myEngine.setTime(new Time("2026-163T06:50:00.000000"));
        ResourceD.set(20.411179440333424);

        Condition aboveFiveA = ResourceA.whenGreaterThan(5.0);
        Condition aboveFiveB = ResourceB.whenGreaterThan(5.0);
        Condition realLifeCondition  = ResourceC.whenGreaterThan(24.755136939495333);
        Condition realLifeCondition2 = ResourceD.whenGreaterThan(21.455506677595082);

        Condition lessThanFour = ResourceA.whenLessThan(4.0);
        Condition belowFive = ResourceA.whenLessThan(5.0);
        Condition greatherThanNegativeFifteen = ResourceA.whenGreaterThan(-15.0);

        Window[] aboveFiveWindows = Window.interpWindows(aboveFiveA, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:21"));
        Window[] expectedWindows = new Window[2];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:07"), new Time("2000-001T00:00:12.500"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:19.761904760"), new Time("2000-001T00:00:21"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        aboveFiveWindows = Window.interpWindows(aboveFiveA, new Time("2000-001T00:00:10"), new Time("2000-001T00:00:11"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:10"), new Time("2000-001T00:00:11"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        aboveFiveWindows = Window.interpWindows(aboveFiveA, new Time("2000-001T00:00:23"), new Time("2000-001T00:00:25"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:23"), new Time("2000-001T00:00:25"));
        assertArrayEquals(expectedWindows, aboveFiveWindows);

        Window[] checkSameStartTimeWindows = Window.interpWindows(aboveFiveA, new Time("2000-001T00:00:07"), new Time("2000-001T00:00:12"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:07"), new Time("2000-001T00:00:12"));
        assertArrayEquals(expectedWindows, checkSameStartTimeWindows);

        checkSameStartTimeWindows = Window.interpWindows(lessThanFour, new Time("2000-001T00:00:13"), new Time("2000-001T00:00:18"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:13"), new Time("2000-001T00:00:18"));
        assertArrayEquals(expectedWindows, checkSameStartTimeWindows);

        Window[] checkHandlingOfStartWindow = Window.interpWindows(lessThanFour, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:14"));
        expectedWindows = new Window[]{new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05.6")), new Window(new Time("2000-001T00:00:12.625000"), new Time("2000-001T00:00:14"))};
        assertArrayEquals(expectedWindows, checkHandlingOfStartWindow);

        checkSameStartTimeWindows = Window.interpWindows(greatherThanNegativeFifteen, new Time("2000-001T00:00:12"), new Time("2000-001T00:00:15"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:15"));
        assertArrayEquals(expectedWindows, checkSameStartTimeWindows);

        Window[] belowFiveWindows = Window.interpWindows(belowFive, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:05"));
        assertArrayEquals(expectedWindows, belowFiveWindows);

        Window[] decreasingStartWindows = Window.interpWindows(aboveFiveB, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:14"));
        expectedWindows = new Window[]{new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:09"))};
        assertArrayEquals(expectedWindows, decreasingStartWindows);

        Window[] realLifeWindows = Window.interpWindows(realLifeCondition, new Time("2026-055T13:02:00.000000"), new Time("2026-055T13:52:23.316000"));
        expectedWindows = new Window[0];
        assertArrayEquals(expectedWindows, realLifeWindows);

        Window[] interpWindowAtStart = Window.interpWindows(realLifeCondition2, new Time("2026-163T06:43:49.915000"), new Time("2026-163T07:26:00.000000"));
        expectedWindows = new Window[0];
        assertArrayEquals(expectedWindows, interpWindowAtStart);

    }

    @Test
    public void returnIntersectingWindowInList(){
        Window ofInterest = new Window(new Time("2000-001T00:10:00"), new Time("2000-001T00:15:00"));
        TreeSet<Window> empty = new TreeSet<>();
        assertEquals(null, ofInterest.getIntersectingWindowInList(empty));

        TreeSet<Window> entriesOnlyAround = new TreeSet<>();
        entriesOnlyAround.add(new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:05:00")));
        entriesOnlyAround.add(new Window(new Time("2000-001T00:20:00"), new Time("2000-001T00:30:00")));
        assertEquals(null, ofInterest.getIntersectingWindowInList(entriesOnlyAround));

        TreeSet<Window> conflictBefore = new TreeSet<>();
        conflictBefore.add(new Window(new Time("2000-001T00:08:00"), new Time("2000-001T00:12:00"), "first"));
        entriesOnlyAround.add(new Window(new Time("2000-001T00:20:00"), new Time("2000-001T00:30:00"), "second"));
        assertEquals(conflictBefore.first(), ofInterest.getIntersectingWindowInList(conflictBefore));
        assertEquals("first", ofInterest.getIntersectingWindowInList(conflictBefore).getType());

        TreeSet<Window> conflictAfter = new TreeSet<>();
        conflictAfter.add(new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:05:00")));
        conflictAfter.add(new Window(new Time("2000-001T00:12:00"), new Time("2000-001T00:20:00")));
        assertEquals(conflictAfter.last(), ofInterest.getIntersectingWindowInList(conflictAfter));

        TreeSet<Window> conflictContained = new TreeSet<>();
        conflictContained.add(new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:05:00")));
        conflictContained.add(new Window(new Time("2000-001T00:06:00"), new Time("2000-001T00:07:00")));
        conflictContained.add(new Window(new Time("2000-001T00:12:00"), new Time("2000-001T00:14:00"), "mytest"));
        conflictContained.add(new Window(new Time("2000-001T00:20:00"), new Time("2000-001T00:30:00")));
        conflictContained.add(new Window(new Time("2000-001T00:40:00"), new Time("2000-001T00:50:00")));
        assertEquals(new Window(new Time("2000-001T00:12:00"), new Time("2000-001T00:14:00")), ofInterest.getIntersectingWindowInList(conflictContained));
        assertEquals("mytest", ofInterest.getIntersectingWindowInList(conflictContained).getType());
    }

    @Test
    public void and() {
        Window[] one = new Window[3];
        Window[] two = new Window[3];
        Window[] three = new Window[0];
        Window[] four = new Window[1];
        Window[] expectedWindows;
        Window[] andWindows;

        one[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:04"));
        one[1] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:12"));
        one[2] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));

        two[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07"));
        two[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:15"));
        two[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));

        andWindows = Window.and(one, two);
        expectedWindows = new Window[3];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:04"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:12"));
        expectedWindows[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));
        assertArrayEquals(expectedWindows, andWindows);
        andWindows = Window.and(two, one);
        assertArrayEquals(expectedWindows, andWindows);

        andWindows = Window.and(one, three);
        expectedWindows = new Window[0];
        assertArrayEquals(expectedWindows, andWindows);

        four[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:26"));
        andWindows = Window.and(two, four);
        expectedWindows = new Window[3];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:15"));
        expectedWindows[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));
        assertArrayEquals(expectedWindows, andWindows);

    }

    @Test
    public void or() {
        Window[] one = new Window[3];
        Window[] two = new Window[3];
        Window[] three = new Window[0];
        Window[] four = new Window[1];
        Window[] expectedWindows;
        Window[] orWindows;

        one[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:04"));
        one[1] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:12"));
        one[2] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));

        two[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07"));
        two[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:15"));
        two[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));

        orWindows = Window.or(one, two);
        expectedWindows = new Window[3];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:07"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:15"));
        expectedWindows[2] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));
        assertArrayEquals(expectedWindows, orWindows);
        orWindows = Window.or(two, one);
        assertArrayEquals(expectedWindows, orWindows);

        orWindows = Window.or(one, three);
        assertArrayEquals(one, orWindows);

        four[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:26"));
        orWindows = Window.or(two, four);
        assertArrayEquals(four, orWindows);
    }

    @Test
    public void xor() {
        Window[] one = new Window[3];
        Window[] two = new Window[3];
        Window[] three = new Window[0];
        Window[] four = new Window[1];
        Window[] expectedWindows;
        Window[] xorWindows;

        one[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:04"));
        one[1] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:12"));
        one[2] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));

        two[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07"));
        two[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:15"));
        two[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));

        xorWindows = Window.xor(one, two);
        expectedWindows = new Window[6];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:03"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:04"), new Time("2000-001T00:00:07"));
        expectedWindows[2] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:11"));
        expectedWindows[3] = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:15"));
        expectedWindows[4] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:17"));
        expectedWindows[5] = new Window(new Time("2000-001T00:00:18"), new Time("2000-001T00:00:20"));
        assertArrayEquals(expectedWindows, xorWindows);

        xorWindows = Window.xor(one, three);
        assertArrayEquals(one, xorWindows);

        four[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:26"));
        xorWindows = Window.xor(one, four);
        expectedWindows = new Window[3];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:04"), new Time("2000-001T00:00:08"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:16"));
        expectedWindows[2] = new Window(new Time("2000-001T00:00:20"), new Time("2000-001T00:00:26"));
        assertArrayEquals(expectedWindows, xorWindows);
    }

    @Test
    public void not() {
        Window[] one = new Window[3];
        Window[] two = new Window[1];
        Window[] expectedWindows;
        Window[] notWindows;

        one[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:04"));
        one[1] = new Window(new Time("2000-001T00:00:08"), new Time("2000-001T00:00:12"));
        one[2] = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));

        notWindows = Window.not(one, new Time("2000-001T00:00:00"), new Time("2000-001T00:00:20"));
        expectedWindows = new Window[2];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:04"), new Time("2000-001T00:00:08"));
        expectedWindows[1] = new Window(new Time("2000-001T00:00:12"), new Time("2000-001T00:00:16"));
        assertArrayEquals(expectedWindows, notWindows);

        two[0] = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T00:00:10"));
        notWindows = Window.not(two, new Time("2000-001T00:00:05"), new Time("2000-001T00:00:15"));
        expectedWindows = new Window[1];
        expectedWindows[0] = new Window(new Time("2000-001T00:00:10"), new Time("2000-001T00:00:15"));
        assertArrayEquals(expectedWindows, notWindows);
    }

    @Test
    public void merge(){
        Window[] two = new Window[3];

        two[0] = new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07"));
        two[1] = new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:15"));
        two[2] = new Window(new Time("2000-001T00:00:17"), new Time("2000-001T00:00:18"));

        // test 1: single window merged with itself should result in the same window
        assertArrayEquals(new Window[]{two[0]}, Window.merge(new Window[]{two[0]}, new Duration("00:10:00")));

        // test 2: for this set of windows, choose a threshold such that it merges one pair together but not all three
        assertArrayEquals(
                new Window[]{new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:07")), new Window(new Time("2000-001T00:00:11"), new Time("2000-001T00:00:18"))},
                Window.merge(two, new Duration("00:00:02")));

        // test 3: windows merged with very short threshold should not merge and stay identical to input array
        assertArrayEquals(two, Window.merge(two, new Duration("00:00:00.001")));

        // test 4: with a very large threshold, all the windows should be merged together
        assertArrayEquals(new Window[]{new Window(new Time("2000-001T00:00:03"), new Time("2000-001T00:00:18"))}, Window.merge(two, new Duration("01:00:00")));

        // test 5: empty list
        assertArrayEquals(new Window[0], Window.merge(new Window[0], Duration.SECOND_DURATION));

        // test 6: merging with a zero threshold should recover the original list
        assertArrayEquals(two, Window.merge(two, Duration.ZERO_DURATION));

        // test 7: smaller window contained within larger window, simple case
        Window[] containedList = new Window[4];

        containedList[0] = new Window(new Time("2024-364T18:50:00"), new Time("2024-364T20:50:00"));
        containedList[1] = new Window(new Time("2024-365T17:05:00"), new Time("2024-366T02:05:00"));
        containedList[2] = new Window(new Time("2024-365T17:05:00"), new Time("2024-365T18:05:00"));
        containedList[3] = new Window(new Time("2025-002T16:45:00"), new Time("2025-002T20:45:00"));

        Window[] flippedList = new Window[]{containedList[0], containedList[2], containedList[1], containedList[3]}; // need to make sure order does not matter

        assertArrayEquals(new Window[]{containedList[0], containedList[1], containedList[3]}, Window.merge(containedList, Duration.ZERO_DURATION));
        assertArrayEquals(new Window[]{containedList[0], containedList[1], containedList[3]}, Window.merge(containedList, Duration.HOUR_DURATION));
        assertArrayEquals(new Window[]{containedList[0], containedList[1], containedList[3]}, Window.merge(flippedList, Duration.ZERO_DURATION));

        // test 8: smaller window contained within larger window, compound case
        Window[] compoundList = new Window[4];

        compoundList[0] = new Window(new Time("2024-352T12:30:00"), new Time("2024-352T14:10:00"));
        compoundList[1] = new Window(new Time("2024-352T12:30:00"), new Time("2024-352T13:30:00"));
        compoundList[2] = new Window(new Time("2024-352T14:00:00"), new Time("2024-352T20:45:00"));
        compoundList[3] = new Window(new Time("2024-352T20:35:00"), new Time("2024-352T23:30:00"));

        flippedList = new Window[]{compoundList[1], compoundList[0], compoundList[2], compoundList[3]}; // need to make sure order does not matter

        assertArrayEquals(new Window[]{new Window(new Time("2024-352T12:30:00"), new Time("2024-352T23:30:00"))}, Window.merge(compoundList, Duration.ZERO_DURATION));
        assertArrayEquals(new Window[]{new Window(new Time("2024-352T12:30:00"), new Time("2024-352T23:30:00"))}, Window.merge(compoundList, Duration.HOUR_DURATION));
        assertArrayEquals(new Window[]{new Window(new Time("2024-352T12:30:00"), new Time("2024-352T23:30:00"))}, Window.merge(flippedList, Duration.HOUR_DURATION));

        compoundList[1] = new Window(new Time("2024-352T12:31:00"), new Time("2024-352T13:30:00"));
        assertArrayEquals(new Window[]{new Window(new Time("2024-352T12:30:00"), new Time("2024-352T23:30:00"))}, Window.merge(compoundList, Duration.ZERO_DURATION));

    }

    @Test
    public void getMidpoint(){
       Window one = new Window(new Time("2000-001T00:00:16"), new Time("2000-001T00:00:20"));
       assertEquals(new Time("2000-001T00:00:18"), one.getMidpoint());
    }

    @Test
    public void intersects(){
        Window w1 = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T01:00:00"));
        Window w2 = new Window(new Time("2000-001T00:30:00"), new Time("2000-001T01:30:00"));
        Window w3 = new Window(new Time("2000-001T03:00:00"), new Time("2000-001T04:00:00"));
        Window w4 = new Window(new Time("2000-001T02:00:00"), new Time("2000-001T05:00:00"));

        assertTrue(w1.intersects(w2));
        assertTrue(w2.intersects(w1));
        assertFalse(w1.intersects(w3));
        assertFalse(w3.intersects(w1));
        assertTrue(w4.intersects(w3));
        assertTrue(w3.intersects(w4));
    }

    @Test
    public void contains() {
        Window w1 = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T01:00:00"));
        Window w2 = new Window(new Time("2000-001T00:30:00"), new Time("2000-001T01:30:00"));
        Window w3 = new Window(new Time("2000-001T03:00:00"), new Time("2000-001T04:00:00"));
        Window w4 = new Window(new Time("2000-001T00:00:10"), new Time("2000-001T00:59:50"));

        assertTrue(w1.contains(new Time("2000-001T00:00:00")));
        assertTrue(w1.contains(new Time("2000-001T01:00:00")));
        assertTrue(w1.contains(new Time("2000-001T00:00:01")));
        assertTrue(w1.contains(new Time("2000-001T00:59:59")));

        assertFalse(w1.contains(new Time("1999-365T23:59:59.999999")));
        assertFalse(w1.contains(new Time("2000-001T01:00:00.000001")));

        assertTrue(w1.contains(w1));
        assertFalse(w1.contains(w2));
        assertFalse(w1.contains(w3));
        assertTrue(w1.contains(w4));
    }

    public void sum() {
        Window w1 = new Window(new Time("2000-001T00:00:00"), new Time("2000-001T01:00:00"));
        Window w2 = new Window(new Time("2000-001T00:30:00"), new Time("2000-001T01:30:00"));
        Window w3 = new Window(new Time("2000-001T03:00:00"), new Time("2000-001T04:00:00"));
        Window w4 = new Window(new Time("2000-001T00:00:10"), new Time("2000-001T00:59:50"));

        Window[] ws1 = new Window[1];
        Window[] ws2 = new Window[2];
        Window[] ws3 = new Window[3];
        Window[] ws4 = new Window[0];

        ws1[0] = w4;
        ws2[0] = w1; ws2[1] = w2;
        ws3[0] = w1; ws3[1] = w2; ws3[2] = w3;

        assertEquals(Window.sum(ws1), new Duration("00:59:40"));
        assertEquals(Window.sum(ws2), new Duration("02:00:00"));
        assertEquals(Window.sum(ws3), new Duration("03:00:00"));
        assertEquals(Window.sum(ws4), Duration.ZERO_DURATION);
    }
}
