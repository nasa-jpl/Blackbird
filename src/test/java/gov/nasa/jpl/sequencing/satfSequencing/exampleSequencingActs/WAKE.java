package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class WAKE extends Activity {
    String seqid;
    Boolean diagnostic;
    Time nextWakeStart;
    String nextSeqid;

    public WAKE(Time t, Duration d, String seqid, Boolean diagnostic, Time nextWakeStart, String nextSeqid) {
        super(t,d, seqid, diagnostic);
        setDuration(d);
        this.seqid = seqid;
        this.diagnostic = diagnostic;
        this.nextWakeStart = nextWakeStart;
        this.nextSeqid = nextSeqid;
    }

    public void decompose() {
        spawn(new BOOT_INIT(getStart(), MasterActivityConstants.BootInitDuration, seqid));
        spawn(new MASTER(getStart().add(MasterActivityConstants.BootInitDuration),
                getDuration().subtract(MasterActivityConstants.BootInitDuration),
                seqid, diagnostic, nextWakeStart, nextSeqid));
    }
}
