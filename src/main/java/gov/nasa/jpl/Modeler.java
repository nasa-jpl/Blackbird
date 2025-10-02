package gov.nasa.jpl;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.command.CommandException;
import gov.nasa.jpl.command.NewActivityCommand;
import gov.nasa.jpl.command.RemoveActivityCommand;
import gov.nasa.jpl.constraint.ConstraintDeclaration;
import gov.nasa.jpl.constraint.ConstraintInstanceList;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.ParameterDeclaration;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.exampleAdaptation.*;
import gov.nasa.jpl.output.tol.XMLTOLWriter;
import gov.nasa.jpl.resource.ResourceDeclaration;
import gov.nasa.jpl.resource.ResourceList;
import gov.nasa.jpl.spice.Spice;
import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

import java.io.IOException;
import java.util.*;

public class Modeler {

    public static void main(String[] args) {
        Setup.initializeEngine();

        ActivityOne blue = new ActivityOne(new Time("2000-001T00:00:10"), new Duration("00:00:01"));
        ActivityOne red = new ActivityOne(new Time("2000-001T00:00:05"), new Duration("00:00:02"));
        ActivityOne teal = new ActivityOne(new Time("2000-001T00:18:00"), new Duration("00:00:02"));
        ActivityOne cyan = new ActivityOne(new Time("2000-001T00:18:01"), new Duration("00:00:03"));
        CommandController.issueCommand("NEW_ACTIVITY", "ActivitySeven (2000-001T00:18:01,00:05:00)");

        List<String> activityThreeInputs = new ArrayList<>();
        activityThreeInputs.add("Alpha");
        activityThreeInputs.add("Beta");
        activityThreeInputs.add("Gamma");
        ActivityThree pink = new ActivityThree(new Time("2000-001T00:00:25"), new Duration("00:00:23"), activityThreeInputs);
        pink.decompose();

        Map<String, String> activityFourInputs = new HashMap<>();
        activityFourInputs.put("Charlie", "Tango");
        activityFourInputs.put("Foxtrot", "Yankee");
        ActivityFour chartruse = new ActivityFour(new Time("2000-001T00:00:30"), new Duration("00:00:35"), activityFourInputs);
        chartruse.decompose();

        Map<String, List<String>> stringListMap = new HashMap<>();
        List firstList = new ArrayList<>();
        List secondList = new ArrayList<>();
        firstList.add("This");
        firstList.add("is");
        firstList.add("the");
        firstList.add("remix");
        stringListMap.put("42", firstList);
        stringListMap.put("kg", secondList);
        ActivityFive lavender = new ActivityFive(new Time("2000-001T00:00:35"), new Duration("00:00:45"), stringListMap);
        lavender.decompose();

        // test NewActivitycommand execute
        NewActivityCommand yellow = new NewActivityCommand("ActivityOne (2018-330T13:00:00.000, 00:01:00)");
        try {
            yellow.execute();
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }

        // test issueCommand for various commands
        CommandController.issueCommand("NEW_ACTIVITY", "ActivityOne (2018-330T13:00:00.000, 00:01:00)");
        UUID tealID = teal.getID();
        String tealIdString = tealID.toString();
        CommandController.issueCommand("REMOVE_ACTIVITY", tealIdString);
        //CommandController.issueCommand("QUIT", "");

        // test RemoveActivityCommand
        UUID cyanID = cyan.getID();
        RemoveActivityCommand maroon = new RemoveActivityCommand(cyanID.toString());
        try {
            maroon.execute();
            maroon.unExecute();
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 1000; i++) {
            ActivityTwo green = new ActivityTwo(new Time("2000-001T00:00:00").add(new Duration("00:00:01").multiply(i)), 24 * i);
            green.decompose();
        }
        // this does the same as myEngine.model()
        CommandController.issueCommand("REMODEL", "");

        NewActivityCommand purple = new NewActivityCommand("WaitingOnSignalActivity (1970-001T00:00:00.000)");
        try {
            purple.execute();
        }
        catch (CommandException e) {
            e.printStackTrace();
        }

        GetWindowsActivity orange = new GetWindowsActivity(new Time("2000-001T00:00:01"));
        orange.decompose();
        ExampleScheduler toSchedule = new ExampleScheduler(new Time("2000-001T00:00:01"));

        CommandController.issueCommand("REMODEL", "");

        XMLTOLWriter writer = new XMLTOLWriter();
        writer.dumpTimelinesToFile("out.tol.xml", ActivityInstanceList.getActivityList(), ResourceList.getResourceList(), ConstraintInstanceList.getConstraintList(), null, null);

        CommandController.issueCommand("WRITE", "filtered_out.tol.xml START 2018-330T00:00:00 END 2018-330T13:00:00 ACTIVITIES INCLUDE (ALL) RESOURCES EXCLUDE (ResourceB) CONSTRAINTS EXCLUDE (ALL)");

        CommandController.issueCommand("WRITE", "testthis.dir");

        CommandController.issueCommand("WRITE", "testthis.constraints.json");
    }
}