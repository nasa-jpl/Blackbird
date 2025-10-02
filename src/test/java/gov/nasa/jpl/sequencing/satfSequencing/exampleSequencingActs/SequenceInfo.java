package gov.nasa.jpl.sequencing.satfSequencing.exampleSequencingActs;

import java.util.ArrayList;
import java.util.List;

public class SequenceInfo {

    public static final String masterSequenceType = "ABSOLUTE";
    public static final List<String> flags;

    static{
        flags = new ArrayList<String>();
        flags.add("AUTOEXECUTE");
        flags.add("AUTOUNLOAD");
    }

    public static String createSequenceHeader(String seqid) {
        StringBuilder sb = new StringBuilder();
        sb.append("CCSD3ZF0000100000001NJPL3KS0L015$$MARK$$;\n");
        sb.append("DATA_SET_ID = SPACECRAFT_ACTIVITY_TYPE;\n");
        sb.append("MISSION_NAME = INSIGHT;\n");
        sb.append("SPACECRAFT_NAME = INSIGHT_NSYT;\n");
        sb.append("SPACECRAFT_ID = 189;\n");
        sb.append("FILE_NAME = " + seqid + ".satf;\n");
        sb.append("PRODUCER_ID = nsyt;\n");
        sb.append("APPLICABLE_START_TIME = 2001-001T00:00:00.000;\n");
        sb.append("APPLICABLE_STOP_TIME = 2030-001T00:00:00.000;\n");
        sb.append("SEQ_ID = nf9001_03;\n");
        sb.append("SOURCE_FILE_NAME = " + seqid + ".satf;\n");
        sb.append("HOST_ID = nsytsmsa2.fltops.jpl.nasa.gov;\n");
        sb.append("CCSD3RE00000$$MARK$$NJPL3IF0M01400000001;\n");
        sb.append("$$nsyt   SPACECRAFT ACTIVITY TYPE FILE\n");
        sb.append("************************************************************\n");
        sb.append("*PROJECT    nsyt\n");
        sb.append("*SPACECRAFT 189\n");
        sb.append("*BEGIN      2001-001T00:00:00\n");
        sb.append("*CUTOFF     2030-001T00:00:00\n");
        sb.append("*TITLE      " + seqid + "\n");
        sb.append("************************************************************\n");
        sb.append("$$EOH\n");

        return sb.toString();
    }
}
