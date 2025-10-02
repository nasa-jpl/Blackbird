package gov.nasa.jpl.sequencing;

import gov.nasa.jpl.time.Time;

import java.util.*;

public class SequenceMap {
    private Map<String, Sequence> sequenceMap;
    private Map<String, List<SequenceFragment>> orphanFragmentsBySeqid; // store fragments until sequence is added

    // singleton design pattern
    private static SequenceMap instance = null;

    protected SequenceMap() {
        sequenceMap = new HashMap<>();
        orphanFragmentsBySeqid = new HashMap<>();
    }

    public static SequenceMap getSequenceMap() {
        if(instance == null) {
            instance = new SequenceMap();
        }
        return instance;
    }

    public Collection<Sequence> getAllSequences(){
        return sequenceMap.values();
    }

    /**
     * Adds as sequence fragment to the appropriate seqid.
     * @param seqFragment
     * @param seqid
     */
    public void addSequenceFragment(SequenceFragment seqFragment, String seqid) {
        if(sequenceMap.containsKey(seqid)) {
            sequenceMap.get(seqid).addSequenceFragment(seqFragment);
        }
        // If the sequence with this seqid has not been added yet then store the fragment
        // in orphanFragmentsBySeqid so it can be added to the sequence when it is eventually created
        else {
            if(orphanFragmentsBySeqid.containsKey(seqid)) {
                orphanFragmentsBySeqid.get(seqid).add(seqFragment);
            }
            else {
                List<SequenceFragment> orphanFragments = new ArrayList<SequenceFragment>();
                orphanFragments.add(seqFragment);
                orphanFragmentsBySeqid.put(seqid, orphanFragments);
            }
        }
    }

    /**
     * Adds a sequence object to the sequence map by seqid.
     * @param sequence
     */
    public void addSequence(Sequence sequence) {
        if(sequenceMap.containsKey(sequence.getSeqid())) {
            throw new RuntimeException("Multiple sequences with the same seqid " + sequence.getSeqid()
                    + " were added to the plan.");
        }
        else {
            addOrphanFragmentsToSequence(sequence);
            sequenceMap.put(sequence.getSeqid(), sequence);
        }
    }

    /**
     * Adds any stored orphan sequence fragments to an input sequence.
     * This is broken out into a separate method so that additional checks can be added,
     * such as checking that the type of the sequence and the fragments match, etc.
     * @param sequence
     */
    private void addOrphanFragmentsToSequence(Sequence sequence) {
        // If there are any orphan fragments for this sequence then add them to the sequence
        if(orphanFragmentsBySeqid.containsKey(sequence.getSeqid())) {
            for(SequenceFragment sequenceFragment : orphanFragmentsBySeqid.get(sequence.getSeqid())) {
                sequence.addSequenceFragment(sequenceFragment);
            }
        }
    }

    /**
     * Clears the sequence map and orphan fragments list.
     * This is needed before executing the sequence sections of activities again.
     */
    public void resetSequences() {
        sequenceMap = new HashMap<>();
        orphanFragmentsBySeqid = new HashMap<>();
    }
}
