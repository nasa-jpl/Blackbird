package gov.nasa.jpl.sequencing.sasfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.sasfSequencing.SASFSequence;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class ScheduleMRORequests extends Activity {
    String seqid;

    public ScheduleMRORequests(Time t, Duration d, String seqid) {
        super(t, d, seqid);
        setDuration(d);
        this.seqid = seqid;
    }

    @Override
    public void sequence() {
        new SASFSequence(seqid, getStart(), SequenceInfo.createSequenceHeader(seqid));
    }

    @Override
    public void decompose() {
        Time currentStart = getStart();
        List<String> hgaStates = new ArrayList<>();
        hgaStates.add("track");
        hgaStates.add("rewind");
        hgaStates.add("park");

        // decompose into 24 requests, one per hour
        // alternate between track, rewind and park requests
        for(int i=0; i<24; i++) {
            spawn(new HGA_MGR_Request(currentStart, MROActivityConstants.HgaMgrRequestDuration, seqid, hgaStates.get(i%3)));
            currentStart = currentStart.add(new Duration("01:00:00"));
        }
    }
}
