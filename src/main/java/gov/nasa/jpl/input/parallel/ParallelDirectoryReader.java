package gov.nasa.jpl.input.parallel;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.input.HistoryReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static gov.nasa.jpl.input.RegexUtilities.ACT;
import static gov.nasa.jpl.input.RegexUtilities.RES;
import static gov.nasa.jpl.input.XMLTOLHistoryReader.rebuildInstanceHierarchy;

public class ParallelDirectoryReader implements HistoryReader {
    private String dirName;
    private ExecutorService exec;

    public ParallelDirectoryReader(String dirName){
        this.dirName = dirName;
    }

    @Override
    public void readInHistoryOfActivitiesAndResource(boolean areReadInResourcesFrozen, boolean shouldActivitiesDecompose) throws IOException {
        // this map IS going to get added to by a lot of threads
        ConcurrentHashMap<String, Map.Entry<Activity, String>> mapOfAllIDsToActivitiesAndTheirParentIDs = new ConcurrentHashMap<>();

        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        ModelingEngine.getEngine().setCurrentlyReadingInFile(true);

        // set up Callable (Runnable) tasks to send to the parallel executor
        List<Callable<Object>> tasks = new ArrayList<>();

        File[] activityFiles = new File(dirName + File.separator + ACT).listFiles();
        if(activityFiles != null) {
            for (File actFile : activityFiles) {
                tasks.add(Executors.callable(new ReaderThreadActivity(actFile, mapOfAllIDsToActivitiesAndTheirParentIDs)));
            }
        }

        File[] resourceFiles = new File(dirName + File.separator + RES).listFiles();
        if(resourceFiles != null) {
            for (File resFile : resourceFiles) {
                tasks.add(Executors.callable(new ReaderThreadResource(resFile, areReadInResourcesFrozen)));
            }
        }

        List<Exception> exceptionList = new ArrayList<Exception>();
        // actually read in all the files of all kinds
        try {
            List<Future<Object>> futures = futures = exec.invokeAll(tasks);

            for (Future<Object> f: futures) {
                try {
                    // waits for the future to complete and will throw an exception if
                    // the future threw an exception.
                    f.get();
                }
                catch (Exception ex) {
                    exceptionList.add(ex);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Directory reader was interrupted while threads were running: " + e.getMessage());
        } finally {
            exec.shutdown();
        }

        if (!exceptionList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Caught " + exceptionList.size() + " exception(s) while reading:\n");
            for (Exception ex : exceptionList) {
                sb.append(ex.toString() + "\n");
            }
            throw new RuntimeException(sb.toString());
        }

        // now that we've grabbed all the activity instances, we still need to re-associate parents and children
        rebuildInstanceHierarchy(mapOfAllIDsToActivitiesAndTheirParentIDs);

        ModelingEngine.getEngine().setCurrentlyReadingInFile(false);

    }
}
