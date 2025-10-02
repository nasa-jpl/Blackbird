package gov.nasa.jpl.sequencing.sasfSequencing.exampleSequencingActs;

import gov.nasa.jpl.time.Duration;

public class MROActivityConstants {
    public static final Duration HgaMgrRequestDuration = new Duration("00:10:00");

    public static final Integer hga_park_igc = 0; // HGA park inner gimbal coarse position
    public static final Integer hga_park_igf = 0; // HGA park inner gimbal fine position
    public static final Integer hga_park_ogc = -1209; // HGA park outer gimbal coarse position
    public static final Integer hga_park_ogf = 0; // HGA park outer gimbal fine position
    public static final Boolean hga_park_roll_angle_init = true; // HGA park roll angle initialization flag
    public static final Integer hga_rewind_igc = 2795; // HGA rewind inner gimbal coarse position
    public static final Integer hga_rewind_igf = 0; // HGA rewind inner gimbal fine position
    public static final Integer hga_rewind_ogc = -1209; // HGA rewind outer gimbal coarse position
    public static final Integer hga_rewind_ogf = 0; // HGA rewind outer gimbal fine position

    public static final Duration hga_rewind_delay = new Duration("00:14:00"); // duration allocated for HGA rewind
    public static final Duration rwa_desat_duration = new Duration("00:25:00"); // allowed duration for desat
    public static final Duration rwa_desat_duration_2 = new Duration("00:05:00"); // allowed duration for 2nd half of desat
    public static final Duration hga_unpark_delay = new Duration("00:11:00"); // duration allocated for HGA unpark
    public static final Duration sa_vt_delay = new Duration("00:13:30"); // duration allocated for SA unpark
}
