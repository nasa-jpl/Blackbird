package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class FILE_COPY extends Activity {
    String seqid;
    String nextSeqid;

    public FILE_COPY(Time t, Duration d, String seqid, String nextSeqid) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
        this.nextSeqid = nextSeqid;
    }

    public void sequence() {
        List<String> FileCopyArgs = new ArrayList<>();
        FileCopyArgs.add("\"d:/seq/" + nextSeqid + ".abs\"");
        FileCopyArgs.add("\"f2:/seq/fpe_wakeup.mod\"");
        FileCopyArgs.add("\"OVERWRITE\"");
        new SATFAbsoluteStep(seqid, getStart(), "command", "FILE_COPY", FileCopyArgs,
                MasterActivityFunctions.createLmstComment(getStart()));
    }
}