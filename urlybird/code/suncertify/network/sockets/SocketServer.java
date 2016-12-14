package suncertify.network.sockets;

import suncertify.network.Server;

/**
 * The server class for starting and stopping the socket server threads.
 */
public class SocketServer implements Server {

    /** The internal server thread */
    private ServerThread serverThread;

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
    public SocketServer(int port, String dbLocation) {
	this.port = port;
	this.dbLocation = dbLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
	serverThread = new ServerThread(dbLocation, port);
	serverThread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
	// TODO: wait for clean up of database
	serverThread.stopThread();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
	return serverThread != null && serverThread.isAlive();
    }

}
