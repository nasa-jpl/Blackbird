package gov.nasa.jpl.sequencing;

import gov.nasa.jpl.time.Time;

public interface SequenceFragment {
    /**
     * Any sequence "fragment" such as commands, block spawns, ground events,
     * etc. need to be able to write a line to a sequence
     * @return
     */
    public String toSequenceString(Integer stepNumber);

    public Time getAbsoluteStartTime(Time latestStartTime);
}
