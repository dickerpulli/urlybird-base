package suncertify.network.sockets;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.db.DatabaseAccess;

/**
 * The request thread for one client.
 */
public class RequestThread extends Thread {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(RequestThread.class
	    .getName());

    /** The client socket. */
    private Socket socket;

    /** The database to connect to. */
    private DatabaseAccess database;

    /**
     * Constructor.
     * 
     * @param socket
     *            The socket to the server
     * @param database
     *            The database to connect to
     */
    public RequestThread(Socket socket, DatabaseAccess database) {
	this.socket = socket;
	this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
	try {
	    ObjectOutputStream clientOut = new ObjectOutputStream(
		    socket.getOutputStream());
	    ObjectInputStream clientIn = new ObjectInputStream(
		    socket.getInputStream());
	    while (true) {
		try {
		    Command cmd = (Command) clientIn.readObject();
		    LOGGER.fine("New command received: " + cmd);
		    Result result = processCommand(cmd);
		    LOGGER.fine("Result is going to be sent: " + result);
		    clientOut.writeObject(result);
		} catch (EOFException eof) {
		    LOGGER.info("Socket was closed, blocking readObject() operation killed");
		    break;
		}
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "An I/O error occurs in deserialization",
		    e);
	} catch (ClassNotFoundException e) {
	    LOGGER.log(Level.SEVERE,
		    "Class of a serialized object cannot be found", e);
	}
    }

    /**
     * Processes the command object and resturns the result.
     * 
     * @param cmd
     *            The request
     * @return The response
     */
    private Result processCommand(Command cmd) {
	Result result = new Result();
	try {
	    switch (cmd.getRequestType()) {
	    case READ:
		String[] data = database.read(cmd.getRecNo());
		result.setData(data);
		break;
	    case CREATE:
		int recNo = database.create(cmd.getData());
		result.setRecNo(recNo);
		break;
	    case FIND:
		int[] recNos = database.find(cmd.getData());
		result.setRecNos(recNos);
		break;
	    case DELETE:
		database.delete(cmd.getRecNo());
		break;
	    case UPDATE:
		database.update(cmd.getRecNo(), cmd.getData());
		break;
	    default:
		throw new IllegalArgumentException("Illegal request type "
			+ cmd.getRequestType());
	    }
	} catch (Exception e) {
	    result.setException(e);
	}
	return result;
    }

    /**
     * Stops the request thread.
     */
    public void stopThread() {
	try {
	    socket.close();
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE,
		    "An I/O error occurs in closing the socket", e);
	}
    }

}
