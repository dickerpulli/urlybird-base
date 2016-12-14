package suncertify.network.rmi;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import suncertify.db.DatabaseAccess;

/**
 * The server registrator that manages to bind and unbind the database server in
 * RMI registry.
 */
public class RMIRegistrator {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(RMIRegistrator.class
	    .getName());

    /** The name on that the database server is registered in RMI registry. */
    public static final String REGISTRY_NAME = "UrlyBird";

    /**
     * Default private constructor to deny instantiation.
     */
    private RMIRegistrator() {
	// nothing
    }

    /**
     * Registering the records server in RMI registry.
     * 
     * @param dbLocation
     *            The local database location.
     * @param port
     *            The port to register.
     * @throws IOException
     *             if some IO-failure raises.
     */
    public static void register(String dbLocation, int port) throws IOException {
	Registry r = LocateRegistry.getRegistry(port);
	try {
	    r.rebind(REGISTRY_NAME, new RMIDatabaseAccess(dbLocation));
	} catch (ConnectException e) {
	    LOGGER.info("Creating a new registry on port " + port);
	    r = LocateRegistry.createRegistry(port);
	    r.rebind(REGISTRY_NAME, new RMIDatabaseAccess(dbLocation));
	}
	LOGGER.info("Server started on port " + port);
    }

    /**
     * Checks if the server is registered in the RMI registry.
     * 
     * @param port
     *            The port to check in the registry.
     * @return <code>true</code> if the server is registered on the given port.
     */
    public static boolean isRegistered(int port) {
	Remote remote = null;
	try {
	    Registry r = LocateRegistry.getRegistry(port);
	    remote = r.lookup(REGISTRY_NAME);
	} catch (AccessException e) {
	    return false;
	} catch (RemoteException e) {
	    return false;
	} catch (NotBoundException e) {
	    return false;
	}
	return remote != null;
    }

    /**
     * Unregisteres the database server at the given port.
     * 
     * @param port
     *            The port, the server is registered.
     * @throws RemoteException
     *             if the connection to RMI server fails.
     * @throws NotBoundException
     *             if the database server is not bound on the port.
     */
    public static void unregister(int port) throws RemoteException,
	    NotBoundException {
	Registry r = LocateRegistry.getRegistry(port);
	r.unbind(REGISTRY_NAME);
	LOGGER.info("Server stopped on port " + port);
    }

    /**
     * Gives the database access to get data from.
     * 
     * @param hostname
     *            The hostname of the RMI server.
     * @param port
     *            The port of the server.
     * @return The database access.
     * @throws IOException
     *             if the connection to RMI server fails.
     */
    public static DatabaseAccess getClient(String hostname, int port)
	    throws IOException {
	String url = "rmi://" + hostname + ":" + port + "/"
		+ RMIRegistrator.REGISTRY_NAME;
	try {
	    return (DatabaseAccess) Naming.lookup(url);
	} catch (NotBoundException e) {
	    throw new IOException("Failed to lookup RMI server", e);
	} catch (IOException e) {
	    throw new IOException("Failed to lookup RMI server", e);
	}
    }

}
