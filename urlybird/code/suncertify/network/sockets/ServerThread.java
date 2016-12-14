package suncertify.network.sockets;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.db.DatabaseAccess;
import suncertify.db.LocalDatabaseAccess;

/**
 * The server thread.
 */
public class ServerThread extends Thread {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ServerThread.class
	    .getName());

    /** The local port. */
    private int port;

    /** The database location. */
    private String dbLocation;

    /** The server socket. */
    private ServerSocket serverSocket;

    /**
     * Constructor.
     * 
     * @param dbLocation
     *            The database location
     * @param port
     *            The server port
     */
    public ServerThread(String dbLocation, int port) {
	this.dbLocation = dbLocation;
	this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
	Set<RequestThread> requestThreads = new HashSet<RequestThread>();
	try {
	    serverSocket = new ServerSocket(port);
	    DatabaseAccess database = new LocalDatabaseAccess(dbLocation);
	    LOGGER.info("Server started on port " + port + ", database = "
		    + dbLocation);
	    while (!serverSocket.isClosed()) {
		Socket clientSocket = serverSocket.accept();
		LOGGER.fine("New client connected to the server");
		RequestThread requestThread = new RequestThread(clientSocket,
			database);
		requestThread.start();
		requestThreads.add(requestThread);
	    }
	} catch (BindException e) {
	    LOGGER.log(Level.SEVERE, "Server could not be started", e);
	} catch (SocketException e) {
	    LOGGER.info("Server stopped");
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE,
		    "An I/O error occurs when accepting the connection", e);
	} finally {
	    stopThread();
	    for (RequestThread requestThread : requestThreads) {
		requestThread.stopThread();
	    }
	}
    }

    /**
     * Stops the server thread by closing the socket.
     */
    public void stopThread() {
	try {
	    serverSocket.close();
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE,
		    "An I/O error occurs when accepting the connection", e);
	}
    }
}
