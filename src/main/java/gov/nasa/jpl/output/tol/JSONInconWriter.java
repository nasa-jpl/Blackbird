package gov.nasa.jpl.output.tol;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.output.TOLWriter;
import gov.nasa.jpl.resource.Resource;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.time.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static gov.nasa.jpl.input.TypeNameConverters.bareJSONTypes;

public class JSONInconWriter extends TOLWriter {
    @Override
    public void writeFileContents(ActivityInstanceList actList, ResourceList resList, ConstraintInstanceList conList, Time startTime, Time endTime) {

        // if no endTime specified
        if (endTime == null) {
            // find the last time a resource was updated
            for (Resource res: resList.getListOfAllResources()) {
                if (endTime == null || endTime.lessThan(res.lastTimeSet())) {
                    endTime = res.lastTimeSet();
                }
            }

            if (endTime == null) {
                throw new RuntimeException("Could not determine end time for writing initial conditions.");
            }
        }

        // print header values
        writer.println("{");
        writer.println("    \"inconTime\": \"" + endTime.toUTC() + "\",");
        writer.println("    \"finalValues\": [");

        // print resource values
        boolean printComma = false;

        List<Resource> sortedResources = new ArrayList<>();
        for(Resource r: resList.getListOfAllResources()){
            sortedResources.add(r);
        }
        Collections.sort(sortedResources, Comparator.comparing(Resource::getUniqueName));

        for (Resource res: sortedResources) {
            Comparable value = res.valueAt(endTime);

            if (printComma) {
                writer.println(",");
            }

            writer.println("        {");
            writer.println("            \"resourceName\": \"" + res.getUniqueName() + "\",");

            // handle value types which don't need quotes
            if (bareJSONTypes.contains(value.getClass())) {
                writer.println("            \"finalValue\": " + value.toString());
            }
            // by default go ahead and put quotes around the value
            else {
                // handle chars which need to be escaped
                String val = value.toString();
                val = val.replace("\\", "\\\\");
                val = val.replace("\"", "\\\"");
                val = val.replace("\t", "\\t");
                writer.println("            \"finalValue\": \"" + val + "\"");
            }

            writer.print("        }");
            printComma = true;
        }

        // closing
        writer.println();
        writer.println("    ]");
        writer.println("}");
    }
}
