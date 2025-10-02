package gov.nasa.jpl.sequencing.sasfSequencing;

import gov.nasa.jpl.sequencing.SequenceFormattingFunctions;
import gov.nasa.jpl.sequencing.SequenceFragment;
import gov.nasa.jpl.sequencing.SequenceMap;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.List;

public class SASFRequest implements SequenceFragment {
    String seqid;
    Time absoluteStartTime;
    String requestName;
    String processor;
    String key;
    String description;
    String requestor;
    List<SASFStep> stepList;

    // **** constructors with multiple steps ****

    /**
     * This is the full SASFRequest constructor.
     * @param seqid
     * @param absoluteStartTime
     * @param processor
     * @param key
     * @param description
     * @param stepList
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, String description, String requestor, List<SASFStep> stepList) {
        this.seqid = seqid;
        this.absoluteStartTime = absoluteStartTime;
        this.requestName = requestName;
        this.processor = processor;
        this.key = key;
        this.description = description;
        this.requestor = requestor;
        this.stepList = stepList;
        SequenceMap.getSequenceMap().addSequenceFragment(this, seqid);
    }

    /**
     * This is the SASFRequest constructor without the requestor. Default requestor is double quotes instead
     * of empty string for backwards compatibility
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, String description, List<SASFStep> stepList) {
        this(seqid, absoluteStartTime, requestName, processor, key, description, "\"\"", stepList);
    }

    /**
     * This is the SASFRequest constructor without the description.
     * @param seqid
     * @param absoluteStartTime
     * @param processor
     * @param key
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, List<SASFStep> stepList) {
        this(seqid, absoluteStartTime, requestName, processor, key, "", stepList);
    }

    /**
     * This is the SASFRequest constructor without the stepList.
     * @param seqid
     * @param absoluteStartTime
     * @param processor
     * @param key
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, String description) {
        this(seqid, absoluteStartTime, requestName, processor, key, description, new ArrayList<>());
    }

    /**
     * This is the SASFRequest constructor without the description or stepList.
     * @param seqid
     * @param absoluteStartTime
     * @param processor
     * @param key
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key) {
        this(seqid, absoluteStartTime, requestName, processor, key, "", new ArrayList<>());
    }


    // **** constructors with a single step ****

    /**
     * This is the full sasf request constructor with a single step.
     * @param seqid
     * @param absoluteStartTime
     * @param requestName
     * @param processor
     * @param key
     * @param description
     * @param step
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, String description, String requestor, SASFStep step) {
        this(seqid, absoluteStartTime, requestName, processor, key, description, requestor, createSingleStepList(step));
    }

    /**
     * This is the sasf request constructor without a description or requestor and with a single step.
     * @param seqid
     * @param absoluteStartTime
     * @param requestName
     * @param processor
     * @param key
     * @param step
     */
    public SASFRequest(String seqid, Time absoluteStartTime, String requestName, String processor,
                       String key, SASFStep step) {
        this(seqid, absoluteStartTime, requestName, processor, key, "", createSingleStepList(step));
    }



    /**
     * This method takes a single SASF step and returns a list with only this item.
     * This is needed for the constructor using a single step.
     * @param step
     * @return
     */
    private static List<SASFStep> createSingleStepList(SASFStep step) {
        List<SASFStep> singleStepList = new ArrayList<>();
        singleStepList.add(step);
        return singleStepList;
    }

    /**
     * Writes out the request and all of its steps.
     * @param requestNumber
     * @return
     */
    public String toSequenceString(Integer requestNumber) {
        StringBuilder sb = new StringBuilder();
        Integer stepNumber = 1;

        sb.append(writeRequestHeader());
        for(SASFStep step : stepList) {
            sb.append(step.writeStepHeader(stepNumber, absoluteStartTime));
            sb.append(step.writeStepBody());
            sb.append(step.writeStepFooter());
            stepNumber++;
        }
        sb.append(writeRequestFooter());

        return sb.toString();
    }

    private String writeRequestHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("request(" + requestName + ", REQUESTOR, " + requestor + ",\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("START_TIME, " + absoluteStartTime.toString() + ",\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("PROCESSOR, " + processor + ",\n");
        sb.append(SequenceFormattingFunctions.multipleIndents(3));
        sb.append("KEY, " + key);

        if(description.length() > 0) {
            sb.append(",\n");
            sb.append(SequenceFormattingFunctions.multipleIndents(3));
            sb.append("DESCRIPTION, " + description);
        }

        sb.append(")\n");

        return sb.toString();
    }

    private String writeRequestFooter() {
        return SequenceFormattingFunctions.multipleIndents(1) + "end;\n";
    }

    /**
     * Returns the start time of the request.
     * @param latestStartTime
     * @return
     */
    public Time getAbsoluteStartTime(Time latestStartTime) {
        return absoluteStartTime;
    }

    public void addSASFStep(SASFStep step) {
        stepList.add(step);
    }
}
