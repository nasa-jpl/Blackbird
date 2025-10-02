package gov.nasa.jpl;

import gov.nasa.jpl.activity.ActivityInstanceList;
import gov.nasa.jpl.activity.ActivityTypeList;
import gov.nasa.jpl.command.CommandController;
import gov.nasa.jpl.constraint.ConstraintDeclaration;
import gov.nasa.jpl.engine.ModelingEngine;
import gov.nasa.jpl.engine.ParameterDeclaration;
import gov.nasa.jpl.engine.Setup;
import gov.nasa.jpl.resource.ResourceDeclaration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Blackbird {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Blackbird needs to read in a script file");
        }

        // initialize activities, resources, and constraints
        Setup.initializeEngine();

        // for now we assume the only argument is file name
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(args[0]));
            String line = br.readLine();
            while (line != null) {
                // first word is the command, the rest is the "command string"
                String arr[] = line.split(" ", 2);
                if (arr.length > 1) {
                    CommandController.issueCommand(arr[0], arr[1]);
                }
                else {
                    CommandController.issueCommand(arr[0], "");
                }
                line = br.readLine();
            }

        }
        catch (FileNotFoundException e) {
            System.out.println("Can't find input file: " + args[0]);
            System.exit(-1);
        }
        catch (IOException e) {
            System.out.println("Error while reading file: " + args[0]);
            System.exit(-1);
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            }
            catch (IOException e) {
            }
        }

    }
}
