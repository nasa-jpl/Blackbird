package gov.nasa.jpl.input.parallel;

import gov.nasa.jpl.activity.Activity;
import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.input.ReflectionUtilities;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static gov.nasa.jpl.input.ReflectionUtilities.getActivityParameterTypeArray;
import static gov.nasa.jpl.time.Time.TIME_PATTERN;

public class ReaderThreadActivity implements Runnable{
    private File toRead;
    private String typeName;
    private Class<?> actType;
    private String[] paramTypes;
    private ConcurrentHashMap<String, Map.Entry<Activity, String>> mapOfAllIDsToActivitiesAndTheirParentIDs;

    public ReaderThreadActivity(File toRead, ConcurrentHashMap<String, Map.Entry<Activity, String>> mapOfAllIDsToActivitiesAndTheirParentIDs){
        this.toRead = toRead;
        this.typeName = toRead.toPath().getFileName().toString().replaceAll(".csv","");
        actType = ActivityTypeList.getActivityList().getActivityClass(typeName);
        paramTypes = getActivityParameterTypeArray(ActivityTypeList.getActivityList().getParameters(typeName));
        this.mapOfAllIDsToActivitiesAndTheirParentIDs = mapOfAllIDsToActivitiesAndTheirParentIDs;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(toRead))) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                // grabbing commas this way is faster than regex
                int c1 = line.indexOf(',');
                int c2 = line.indexOf(',', c1+1);
                int c3 = line.indexOf(',', c2+1);
                lineNo++;

                if (c1 == -1 || c2 == -1 || c3 == -1) {
                    throw new RuntimeException("Invalid format at line " + lineNo + " in file " + toRead);
                }

                // example in line:
                // 2000-001T00:00:35.000000,05a81259-a245-4b3b-a40c-b6e669d1a7e5,,00:00:45.000000,{"kg"=[],"42"=["This","is","the","remix"]}
                String startTime = line.substring(0, c1);
                String actID = line.substring(c1+1, c2);
                String parentID = line.substring(c2+1, c3);
                String otherParameters = line.substring(c3+1);

                String parameterString = otherParameters.equals("") ? startTime : startTime + "," + otherParameters;

                // check for absolute vs relative time without using regex
                if (startTime.length() >= 5 &&
                    Character.isDigit(startTime.charAt(0)) &&
                    Character.isDigit(startTime.charAt(1)) &&
                    Character.isDigit(startTime.charAt(2)) &&
                    Character.isDigit(startTime.charAt(3)) &&
                    startTime.charAt(4) == '-') {
                    paramTypes[0] = ReflectionUtilities.ABSOLUTE_TIME_CLASS_PACKAGE;
                } else {
                    paramTypes[0] = ReflectionUtilities.RELATIVE_TIME_CLASS_PACKAGE;
                }

                Object[] args = ReflectionUtilities.parseActivityParameters(parameterString, paramTypes);

                Activity activityInstance;
                // these lines create the activity instance, add it to the global list, and set it up to be spawned() correctly later
                synchronized (ActivityInstanceList.getActivityList()) {
                    activityInstance = (Activity) ConstructorUtils.invokeConstructor(actType, args);
                }
                activityInstance.setID(UUID.fromString(actID));
                mapOfAllIDsToActivitiesAndTheirParentIDs.put(actID, new AbstractMap.SimpleEntry<>(activityInstance, parentID));
            }
        } catch ( IOException e) {
            throw new RuntimeException("Could not find or create file " + toRead.toString() + " when trying to read activities from directory. Error: " + e.getMessage());
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find constructor for activity type " + typeName);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Could not execute constructor for activity type " + typeName);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Did not have permission to execute constructor for activity type " + typeName);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Could not invoke constructor for activity type " + typeName);
        }

    }
}
