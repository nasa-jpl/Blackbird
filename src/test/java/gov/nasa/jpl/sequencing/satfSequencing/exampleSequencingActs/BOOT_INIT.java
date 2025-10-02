package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class BOOT_INIT extends Activity {
    String seqid;

    public BOOT_INIT(Time t, Duration d, String seqid) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    public void sequence() {
    }
}