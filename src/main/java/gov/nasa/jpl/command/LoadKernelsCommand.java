package gov.nasa.jpl.command;

import gov.nasa.jpl.spice.Spice;
import spice.basic.SpiceErrorException;

import java.io.File;
import java.util.ArrayList;

public class LoadKernelsCommand implements Command {

    private ArrayList<String> kernels = new ArrayList<String>();

    /*
    Loads all available kernels in the kernels folder
     */
    public void loadKernelsFromConventionFolder() {
        File kfolder = new File("src/test/resources/gov/nasa/jpl/kernels/");
        File[] listOfFiles = kfolder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                kernels.add(listOfFiles[i].getAbsolutePath());
            }
        }
    }

    public LoadKernelsCommand(String filenames) {

        if (filenames != null && !filenames.equals("")) {
            String[] files = filenames.split(" ");
            for (String k : files) {
                kernels.add(k);
            }
        }
        else {
            loadKernelsFromConventionFolder();
        }

    }

    @Override
    public void execute() throws CommandException {
        try {
            for (String file : kernels) {
                Spice.loadKernel(file);
            }
        }
        catch (UnsatisfiedLinkError | SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }
    }

    @Override
    public void unExecute() throws CommandException {
        try {
            for (String file : kernels) {
                Spice.unLoadKernel(file);
            }
        }
        catch (UnsatisfiedLinkError | SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }
    }

}
