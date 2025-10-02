package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFSequence;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

public class MASTER extends Activity {
    String seqid;
    Boolean diagnostic;
    Time nextWakeStart;
    String nextSeqid;

    public MASTER(Time t, Duration d, String seqid, Boolean diagnostic, Time nextWakeStart, String nextSeqid) {
        super(t,d, seqid, diagnostic);
        setDuration(d);
        this.seqid = seqid;
        this.diagnostic = diagnostic;
        this.nextWakeStart = nextWakeStart;
        this.nextSeqid = nextSeqid;
    }

    public void sequence() {
        new SATFSequence(seqid, getStart(), SequenceInfo.masterSequenceType, SequenceInfo.flags, SequenceInfo.createSequenceHeader(seqid));
    }

    public void decompose() {
        Time currentStart = getStart();

        spawn(new LOAD_BLOCK_LIB(currentStart, MasterActivityConstants.LoadBlockLibDuration, seqid));
        currentStart = currentStart.add(MasterActivityConstants.LoadBlockLibDuration);

        // diagnostic wake
        if(diagnostic) {
            // wakeup block
            spawn(new WAKEUP(currentStart, MasterActivityConstants.WakeupDiagDuration, seqid, diagnostic));
            currentStart = currentStart.add(MasterActivityConstants.WakeupDiagDuration);

            // delete current master/fpe_wakeup.mod, copy next master to fpe_wakeup.mod, set GV_WAKEUP_TIME
            spawn(new FILE_MGMT(currentStart, MasterActivityConstants.FileMgmtDuration, seqid, nextWakeStart));
            currentStart = currentStart.add(MasterActivityConstants.FileMgmtDuration);

            // spawn submaster
            spawn(new SUBMASTER(currentStart, MasterActivityConstants.SubmasterDiagDuration, seqid, diagnostic));
            currentStart = currentStart.add(MasterActivityConstants.SubmasterDiagDuration);
        }
        // full wake
        else {
            // wakeup block
            spawn(new WAKEUP(currentStart, MasterActivityConstants.WakeupNonDiagDuration, seqid, diagnostic));
            currentStart = currentStart.add(MasterActivityConstants.WakeupNonDiagDuration);

            // delete current master/fpe_wakeup.mod, copy next master to fpe_wakeup.mod, set GV_WAKEUP_TIME
            spawn(new FILE_MGMT(currentStart, MasterActivityConstants.FileMgmtDuration, seqid, nextWakeStart));
            currentStart = currentStart.add(MasterActivityConstants.FileMgmtDuration);

            // spawn submaster
            spawn(new SUBMASTER(currentStart, MasterActivityFunctions.calculateSubmasterDuration(getDuration()), seqid, diagnostic));
            currentStart = currentStart.add(MasterActivityFunctions.calculateSubmasterDuration(getDuration()));

            // FSW diagnostic, now only for 'full' wakeups
            spawn(new FSW_DIAG(currentStart, MasterActivityConstants.FswDiagDuration, seqid));
            currentStart = currentStart.add(MasterActivityConstants.FswDiagDuration);
        }

        // FILE_COPY for next master
        spawn(new FILE_COPY(currentStart, MasterActivityConstants.FileCopyDuration, seqid, nextSeqid));
        currentStart = currentStart.add(MasterActivityConstants.FileCopyDuration);

        // shutdown block
        if(diagnostic) {
            spawn(new SHUTDOWN(currentStart, MasterActivityConstants.ShutdownDiagDuration, seqid, diagnostic));
        }
        else {
            spawn(new SHUTDOWN(currentStart, MasterActivityConstants.ShutdownNonDiagDuration, seqid, diagnostic));
        }
    }
}