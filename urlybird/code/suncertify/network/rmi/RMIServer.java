package suncertify.network.rmi;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.network.Server;

/**
 * The server class for starting and stopping the RMI server.
 */
public class RMIServer implements Server {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(RMIServer.class
	    .getName());

    /** The port of the server. */
    private int port;

    /** The database location. */
    private String dbLocation;

    /**
     * Constructor.
     * 
     * @param port
     *            The server port.
     * @param dbLocation
     *            The location of the server's internal database.
     */
    public RMIServer(int port, String dbLocation) {
	this.port = port;
	this.dbLocation = dbLocation;
    }

    /**
     * Starts the server thread.
     */
    public void start() {
	try {
	    RMIRegistrator.register(dbLocation, port);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Server could not be startet", e);
	}
    }

    /**
     * Stops the server thread.
     */
    public void stop() {
	try {
	    RMIRegistrator.unregister(port);
	} catch (RemoteException e) {
	    LOGGER.log(Level.SEVERE, "Server could not be stopped", e);
	} catch (NotBoundException e) {
	    LOGGER.log(Level.SEVERE, "Server is not started", e);
	}
    }

    /**
     * Checks if the server thread is running.
     * 
     * @return <code>true</code> if the server is running and alive.
     */
    public boolean isRunning() {
	return RMIRegistrator.isRegistered(port);
    }

}
