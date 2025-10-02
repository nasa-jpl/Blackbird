package gov.nasa.jpl.sequencing.sasfSequencing.exampleSequencingActs;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.sequencing.sasfSequencing.SASFRequest;
import gov.nasa.jpl.sequencing.sasfSequencing.SASFSpawn;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class HGA_MGR_Request extends Activity {
    String seqid;
    String hgaState;

    public HGA_MGR_Request(Time time, Duration duration, String seqid, String hgaState) {
        super(time, duration, seqid, hgaState);
        this.seqid = seqid;
        this.hgaState = hgaState;
    }

    @Override
    public void sequence() {
        new SASFRequest(seqid, getStart(), "HGA_MGR_Request", "\"VC2\"", "\"FSW\"",
                new SASFSpawn(getStart(), "hga_mgr", getHgaMgrArgs()));

    }

    /**
     * Returns a list of hga_mgr block arguments based on the
     * stored hgaState.
     *
     * hga_mgr block arguments are:
     * hga_state
     * pos_in_ga_ang_coar
     * pos_in_ga_ang_fine
     * pos_out_ga_ang_coar
     * pos_out_ga_ang_fine
     * roll_angle_init_flag
     * rewind_delay
     * unpark_delay
     * @return
     */
    private List<String> getHgaMgrArgs() {
        List<String> hgaMgrArgs = new ArrayList<>();
        hgaMgrArgs.add(SequenceFormattingFunctions.addQuotes(hgaState));

        if(hgaState.equals("track")) {
            hgaMgrArgs.add("0"); // pos_in_ga_ang_coar
            hgaMgrArgs.add("0"); // pos_in_ga_ang_fine
            hgaMgrArgs.add("0"); // pos_out_ga_ang_coar
            hgaMgrArgs.add("0"); // pos_out_ga_ang_fine
            hgaMgrArgs.add(MROActivityConstants.hga_park_roll_angle_init.toString().toUpperCase()); // roll_angle_init_flag
            hgaMgrArgs.add("00:00:00"); // rewind_delay
            hgaMgrArgs.add(MROActivityConstants.hga_unpark_delay.toString()); // unpark_delay
        }
        else if(hgaState.equals("rewind")) {
            hgaMgrArgs.add(MROActivityConstants.hga_rewind_igc.toString()); // pos_in_ga_ang_coar
            hgaMgrArgs.add(MROActivityConstants.hga_rewind_igf.toString()); // pos_in_ga_ang_fine
            hgaMgrArgs.add(MROActivityConstants.hga_rewind_ogc.toString()); // pos_out_ga_ang_coar
            hgaMgrArgs.add(MROActivityConstants.hga_rewind_ogf.toString()); // pos_out_ga_ang_fine
            hgaMgrArgs.add("FALSE"); // roll_angle_init_flag
            hgaMgrArgs.add(MROActivityConstants.hga_rewind_delay.toString()); // rewind_delay
            hgaMgrArgs.add("00:00:00"); // unpark_delay
        }
        else if(hgaState.equals("park")) {
            hgaMgrArgs.add(MROActivityConstants.hga_park_igc.toString()); // pos_in_ga_ang_coar
            hgaMgrArgs.add(MROActivityConstants.hga_park_igf.toString()); // pos_in_ga_ang_fine
            hgaMgrArgs.add(MROActivityConstants.hga_park_ogc.toString()); // pos_out_ga_ang_coar
            hgaMgrArgs.add(MROActivityConstants.hga_park_ogf.toString()); // pos_out_ga_ang_fine
            hgaMgrArgs.add(MROActivityConstants.hga_park_roll_angle_init.toString().toUpperCase()); // roll_angle_init_flag
            hgaMgrArgs.add("00:00:00"); // rewind_delay
            hgaMgrArgs.add("00:00:00"); // unpark_delay
        }
        else {
            throw new RuntimeException("Error in getHgaMgrArgs - hgaState was " + hgaState + "but can only "
                + "be track, rewind, or park.");
        }

        return hgaMgrArgs;
    }
}
