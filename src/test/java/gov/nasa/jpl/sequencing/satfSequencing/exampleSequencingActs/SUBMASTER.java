package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.satfSequencing.SATFAbsoluteStep;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class SUBMASTER extends Activity {
    String seqid;
    Boolean diagnostic;
    String wakeType;

    public SUBMASTER(Time t, Duration d, String seqid, Boolean diagnostic) {
        super(t,d, seqid);
        setDuration(d);
        this.seqid = seqid;
        this.diagnostic = diagnostic;
        if(diagnostic) {
            this.wakeType = "\"DIAGNOSTIC\"";
        }
        else {
            this.wakeType = "\"FULL\"";
        }
    }

    public void sequence() {
        Integer seisRunoutSeconds = 0;
        Integer apssRunoutSeconds = 0;
        Integer hp3RunoutSeconds  = 0;
        String eventSeqid = "NONE";
        Time cleanupTime = new Time("2000-001T00:00:00");

        // update calculated durations for full wakes which are long enough
        if(!diagnostic && getDuration().subtract(MasterActivityConstants.RunoutShutdownMargin).
                    lessThan(MasterActivityConstants.MinProcDur)) {
            seisRunoutSeconds = (int) (getDuration().totalSeconds() * 0.5);
            apssRunoutSeconds = (int) (getDuration().totalSeconds() * 0.5);
            hp3RunoutSeconds  = (int) (getDuration().totalSeconds() * 0.9);
            eventSeqid = MasterActivityFunctions.createEventSeqid(seqid);
            cleanupTime = getStart().add(getDuration()).subtract(MasterActivityConstants.RunoutShutdownMargin);
        }

        // create list of arguments for block call
        List<String> subSpawnArgs = new ArrayList<>();
        subSpawnArgs.add(wakeType);
        subSpawnArgs.add(MasterActivityFunctions.masterToSubmasterSeqid(seqid));
        subSpawnArgs.add(Integer.toString(seisRunoutSeconds));
        subSpawnArgs.add(Integer.toString(apssRunoutSeconds));
        subSpawnArgs.add("\"" + eventSeqid + "\"");
        subSpawnArgs.add(Integer.toString(hp3RunoutSeconds));
        subSpawnArgs.add(cleanupTime.toString());

        new SATFAbsoluteStep(seqid, getStart(), "call", "sub_spawn", subSpawnArgs,
                MasterActivityFunctions.createLmstComment(getStart()));
    }
}