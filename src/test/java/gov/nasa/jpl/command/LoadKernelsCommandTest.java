package gov.nasa.jpl.command;

import gov.nasa.jpl.common.BaseTest;
import gov.nasa.jpl.spice.Spice;
import org.junit.Before;
import org.junit.Test;
import spice.basic.SpiceErrorException;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class LoadKernelsCommandTest extends BaseTest {
    private File kfolder = new File("kernels/");

    @Before
    public void executedBeforeEach() {
        LoadKernelsCommand loadKernelsCommand = new LoadKernelsCommand("");
        LoadKernelsCommand loadKernelsCommand2 = new LoadKernelsCommand("src/test/resources/naif0012.tls");
        try {
            loadKernelsCommand.unExecute();
            loadKernelsCommand2.unExecute();
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadOneKernel() {

        ArrayList<String> kernels = null;
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "LOAD_KERNELS",
                kfolder.getAbsolutePath()+"/phobos512.bds"
        );

        try {
            kernels = Spice.getLoadedKernelsInfo();
        } catch (SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }

        assertEquals(1, kernels.size());
    }

    @Test
    public void loadManyKernels() {

        ArrayList<String> kernels = null;
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "LOAD_KERNELS",
                kfolder.getAbsolutePath()+"/phobos512.bds " + kfolder.getAbsolutePath()+"/moon_080317.tf"

        );

        try {
            kernels = Spice.getLoadedKernelsInfo();
        } catch (SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }

        assertEquals(2, kernels.size());
    }

    @Test
    public void loadKernelsFromFolder() {
        ArrayList<String> kernels = null;
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "LOAD_KERNELS",
                ""
        );

        try {
            kernels = Spice.getLoadedKernelsInfo();
        } catch (SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }

        assertEquals(5, kernels.size());
    }

   @Test
    public void loadMetaKernel() {
        ArrayList<String> kernels = null;
        Boolean moveActTestStatus1 = CommandController.issueCommand(
                "LOAD_KERNELS",
                kfolder.getAbsolutePath()+"/e41.mk"
        );

        try {
            kernels = Spice.getLoadedKernelsInfo();
        } catch (SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }

        assertEquals(3, kernels.size());
    }

    @Test
    public void unloadKernels() {

        LoadKernelsCommand loadKernelsCommand = new LoadKernelsCommand("");
        try {
            loadKernelsCommand.execute();
            assertEquals(5, Spice.getLoadedKernelsInfo().size());

            loadKernelsCommand.unExecute();
            assertEquals(0, Spice.getLoadedKernelsInfo().size());

        } catch (CommandException e) {
            e.printStackTrace();
        } catch (SpiceErrorException spiceError) {
            spiceError.printStackTrace();
        }

    }
}
