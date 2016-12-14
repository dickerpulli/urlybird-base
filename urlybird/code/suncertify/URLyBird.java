package suncertify;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import suncertify.gui.ClientFrame;
import suncertify.gui.ConnectionMode;
import suncertify.gui.GuiController;
import suncertify.gui.ServerFrame;
import suncertify.gui.utils.SwingUtils;

/**
 * Main class for UrlyBird.
 * 
 * @author Thomas Bosch
 * @version 1.0
 */
public class URLyBird {

    static {
	// Get the root logger for all application loggers and set the common
	// logging level to Level.INFO.
	Logger logger = Logger.getLogger("suncertify");
	logger.setLevel(Level.INFO);
	for (Handler handler : logger.getHandlers()) {
	    handler.setLevel(Level.INFO);
	}
    }

    /**
     * Main method.
     * 
     * @param args
     *            Arguments for application mode. Possible arguments are<br>
     *            server : Server mode<br>
     *            alone : Standalone client mode<br>
     *            With no extra argument the network client mode will be started
     * 
     * @throws Exception
     *             if the application gets an error in startup
     */
    public static void main(String[] args) throws Exception {
	// The number of arguments must be 0 or 1
	if (args.length > 1) {
	    printUsage();
	}

	// Start the application according to the argument passed
	// the arguments that are allowed are: -server and -alone
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	if (args.length == 0) {
	    ClientFrame clientFrame = new ClientFrame(new GuiController(),
		    ConnectionMode.REMOTE);
	    SwingUtils.centerOnScreen(clientFrame);
	    clientFrame.setVisible(true);
	} else if (args[0].equalsIgnoreCase("server")) {
	    ServerFrame serverFrame = new ServerFrame(new GuiController());
	    SwingUtils.centerOnScreen(serverFrame);
	    serverFrame.setVisible(true);
	} else if (args[0].equalsIgnoreCase("alone")) {
	    ClientFrame clientFrame = new ClientFrame(new GuiController(),
		    ConnectionMode.LOCAL);
	    SwingUtils.centerOnScreen(clientFrame);
	    clientFrame.setVisible(true);
	} else {
	    printUsage();
	}
    }

    /**
     * Prints out the usage of {@link URLyBird} execution.
     */
    private static void printUsage() {
	System.out.println("Usage: java -jar URLyBird [server | alone]");
	System.out.println(" 'no param': run the network client");
	System.out.println("     server: run the network server");
	System.out.println("      alone: run the client in standalone mode "
		+ "(without network)");
    }

}
