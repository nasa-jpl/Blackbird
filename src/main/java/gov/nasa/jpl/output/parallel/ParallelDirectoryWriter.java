package gov.nasa.jpl.output.parallel;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static gov.nasa.jpl.input.RegexUtilities.*;

public class ParallelDirectoryWriter extends TOLWriter {
    private String dirName;
    private ExecutorService exec;

    public ParallelDirectoryWriter(){
        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    }

    private boolean recurisvelyDelete(File fileToDelete) {
        if (fileToDelete.isDirectory()) {
            File[] allContents = fileToDelete.listFiles();
            for (File file : allContents) {
                recurisvelyDelete(file);
            }
        }
        return fileToDelete.delete();
    }

    @Override
    public void createFile(String dirName){
        this.dirName = dirName;
        File mainDir = new File(dirName);
        recurisvelyDelete(mainDir);
        mainDir.mkdirs();
        new File(dirName + File.separator + ACT).mkdirs();
        new File(dirName + File.separator + RES).mkdirs();
        new File(dirName + File.separator + CON).mkdirs();
    }

    @Override
    public void closeFile(){
        exec.shutdown();
    }

    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {
        // set up Callable (Runnable) tasks to send to the parallel executor
        List<Callable<Object>> tasks = new ArrayList<>();

        for(Map.Entry<String, List<Activity>> entry : sortActivitiesByType(actList, startTime, endTime).entrySet()){
            tasks.add(Executors.callable(new WriterThreadActivity(entry.getKey(), entry.getValue(), dirName)));
        }

        for(Resource r : resList.getListOfAllResources()){
            tasks.add(Executors.callable(new WriterThreadResource(r, dirName, startTime, endTime)));
        }

        for(int i = 0; i<conList.length(); i++){
            if(conList.get(i).getName() != null) {
                tasks.add(Executors.callable(new WriterThreadConstraint(conList.get(i), dirName, startTime, endTime)));
            }
        }

        List<Exception> exceptionList = new ArrayList<Exception>();
        // actually write all the files of all kinds
        try {
            List<Future<Object>> futures = exec.invokeAll(tasks);

            for (Future<Object> f: futures) {
                try {
                    f.get();
                }
                catch (Exception ex) {
                    exceptionList.add(ex);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Directory writer was interrupted while threads were running: " + e.getMessage());
        } finally {
            exec.shutdown();
        }

        if (!exceptionList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Caught " + exceptionList.size() + " exceptions while writing directory:\n");
            for (Exception ex: exceptionList) {
                sb.append(ex.toString());
                sb.append('\n');
            }

            throw new RuntimeException(sb.toString());
        }
    }

    private Map<String, List<Activity>> sortActivitiesByType(ActivityInstanceList actList, Time startTime, Time endTime){
        Map<String, List<Activity>> actsByType = new HashMap<>();

        for(int i = 0; i<actList.length(); i++){
            Activity act = actList.get(i);

            if((startTime == null || act.getStart().greaterThanOrEqualTo(startTime)) && (endTime == null || act.getStart().lessThanOrEqualTo(endTime))) {
                if (!actsByType.containsKey(act.getType())) {
                    actsByType.put(act.getType(), new ArrayList<>());
                }
                actsByType.get(act.getType()).add(act);
            }
        }

        return actsByType;
    }
}
