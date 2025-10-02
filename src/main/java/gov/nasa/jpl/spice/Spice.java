package gov.nasa.jpl.spice;

import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

import java.io.File;
import java.util.ArrayList;

public class Spice {

    private static boolean spiceImported = false;

    static {
        /*
        IMPORTANT! FOR THIS TO WORK WITH TOMCAT, THE JNISPICE PROJECT LIBRARY NEEDS TO BE SET TO THE OUTPUT SO IT CAN
        BE INCLUDED IN TOMCAT, OTHERWISE THE WEB SERVER WON'T FIND IT.
         */
        try {
            System.loadLibrary("JNISpice");
            spiceImported = true;
        }
        catch (UnsatisfiedLinkError e) {
            // do nothing for now, since spiceImported is already false
        }
    }

    public static void loadKernel(String filename) throws SpiceErrorException, UnsatisfiedLinkError {

        // before we load a kernel, check if spice was imported
        if (spiceImported) {
            File file = new File(filename);
            String absolutePath = file.getAbsolutePath();
            CSPICE.furnsh(absolutePath);
        }
        else {
            throw new UnsatisfiedLinkError("Error using SPICE call to load kernel. Spice was not imported. Make sure that -Djava.library.path is set correctly.");
        }
    }

    public static void unLoadKernel(String filename) throws SpiceErrorException, UnsatisfiedLinkError {
        if (spiceImported) {
            File file = new File(filename);
            String absolutePath = file.getAbsolutePath();

            CSPICE.unload(absolutePath);
        }
        else {
            throw new UnsatisfiedLinkError("Error using SPICE call to unload kernel. Spice was not imported. Make sure that -Djava.library.path is set correctly.");
        }
    }

    /**
     * returns the exact spicekernels loaded including file path
     *
     * @return
     * @throws spice.basic.SpiceErrorException
     */
    public static ArrayList<String> getLoadedKernelsInfo() throws SpiceErrorException {

        ArrayList<String> loadedKernels = new ArrayList<String>();

        String kind = "ALL";
        int[] handle = new int[1];
        String[] file = new String[1];
        String[] filtyp = new String[1];
        String[] source = new String[1];
        boolean[] found = new boolean[1];

        int totalKernelsLoaded = CSPICE.ktotal(kind);

        for (int which = 0; which < totalKernelsLoaded; which++) {
            CSPICE.kdata(which, kind, file, filtyp, source, handle, found);

            for (String name : file) {
                if (name != null)
                    loadedKernels.add(name);
            }
        }

        return loadedKernels;
    }

    public static double getSpeedOfLight() {
        if (spiceImported) {
            return CSPICE.clight();
        }
        else {
            throw new UnsatisfiedLinkError("Error using SPICE call to get the speed of light. Spice was not imported. Make sure that -Djava.library.path is set correctly.");
        }
    }
}