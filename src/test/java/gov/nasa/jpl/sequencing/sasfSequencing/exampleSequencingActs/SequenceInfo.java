package gov.nasa.jpl.sequencing.sasfSequencing.exampleSequencingActs;

public class SequenceInfo {

    public static String createSequenceHeader(String seqid) {
        StringBuilder sb = new StringBuilder();

        sb.append("CCSD3ZF0000100000001NJPL3KS0L015$$MARK$$;\n");
        sb.append("MISSION_NAME = MARS_RECONNAISSANCE_ORBITER;\n");
        sb.append("SPACECRAFT_NAME = MARS_RECONNAISSANCE_ORBITER;\n");
        sb.append("DATA_SET_ID = SPACECRAFT_ACTIVITY_SEQUENCE;\n");
        sb.append("FILE_NAME = " + seqid + ".sasf;\n");
        sb.append("APPLICABLE_START_TIME = 2000-001T00:00:00.000;\n");
        sb.append("APPLICABLE_STOP_TIME = 2030-001T00:00:00.000;\n");
        sb.append("PRODUCER_ID = SEQ;\n");
        sb.append("SEQ_ID = BLACKBIRD;\n");
        sb.append("CCSD3RE00000$$MARK$$NJPL3IF0M01300000001;\n");
        sb.append("$$MRO       SPACECRAFT ACTIVITY SEQUENCE FILE\n");
        sb.append("**********************************************************************\n");
        sb.append("*SC_MODEL\n");
        sb.append("*CATALOG\n");
        sb.append("*LEGENDS\n");
        sb.append("*SEQUENCE\n");
        sb.append("*RESOLUTION\n");
        sb.append("*LIGHTTIME\n");
        sb.append("*RULES\n");
        sb.append("*DEFINITION\n");
        sb.append("*CLOCK\n");
        sb.append("*REQUESTS\n");
        sb.append("*CONDITIONS\n");
        sb.append("*SCRIPT\n");
        sb.append("*MASK\n");
        sb.append("*ALLOCATION\n");
        sb.append("*VIEWPERIOD\n");
        sb.append("*TELEMETRY\n");
        sb.append("*GEOMETRY\n");
        sb.append("*BG_SEQUENCE\n");
        sb.append("*CONTEXT\n");
        sb.append("*DEP_CONTEXT\n");
        sb.append("*REDUNDANT\n");
        sb.append("*OPTG_FD\n");
        sb.append("*EVENTS\n");
        sb.append("**********************************************************************\n");
        sb.append("$$EOH\n");
        sb.append("$$EOD\n");

        return sb.toString();
    }
}
