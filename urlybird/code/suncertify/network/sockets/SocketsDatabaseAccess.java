package suncertify.network.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.db.DatabaseAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.network.sockets.Command.RequestType;

/**
 * The remote access point to the database.
 */
public class SocketsDatabaseAccess implements DatabaseAccess {

    /** The logger. */
    private static final Logger LOGGER = Logger
	    .getLogger(SocketsDatabaseAccess.class.getName());

    /** The client socket. */
    private Socket socket;

    /** The input from the server. */
    private ObjectInputStream serverIn;

    /** The output to the server. */
    private ObjectOutputStream serverOut;

    /**
     * Constructor.
     * 
     * @param hostname
     *            The server hostname/ip
     * @param port
     *            The server port
     * @throws IOException
     *             if some IO error occurs
     */
    public SocketsDatabaseAccess(String hostname, int port) throws IOException {
	socket = new Socket(hostname, port);
	serverOut = new ObjectOutputStream(socket.getOutputStream());
	serverIn = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Sends the command object to the server and receive the result. Important:
     * RecordNotFoundException and DuplicateKeyException inside the
     * result.getException() are not handled here. Please handle these
     * exceptione on your own.
     */
    private Result sendCommand(Command command) {
	try {
	    serverOut.writeObject(command);
	    Result result = (Result) serverIn.readObject();
	    if (result.getException() == null
		    || result.getException() instanceof RecordNotFoundException
		    || result.getException() instanceof DuplicateKeyException) {
		// Known exceptions should be handled by calling methods
		return result;
	    } else {
		// Unknown exceptions should be thrown again
		LOGGER.severe("Error in remote connection - maybe a reconnect is needed");
		LOGGER.fine("exception = "
			+ result.getException().getClass().getName()
			+ ", message = " + result.getException().getMessage());
		throw new RuntimeException(result.getException());
	    }
	} catch (Exception e) {
	    LOGGER.severe("Error in remote connection - maybe a reconnect is needed");
	    LOGGER.fine("exception = " + e.getClass().getName()
		    + ", message = " + e.getMessage());
	    throw new RuntimeException(e);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] read(int recNo) throws RecordNotFoundException {
	Command command = new Command();
	command.setRequestType(RequestType.READ);
	command.setRecNo(recNo);
	Result result = sendCommand(command);
	// If the record was not found, throw the exception to the caller
	if (result.getException() instanceof RecordNotFoundException) {
	    throw (RecordNotFoundException) result.getException();
	}
	return result.getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int create(String[] data) throws DuplicateKeyException {
	Command command = new Command();
	command.setRequestType(RequestType.CREATE);
	command.setData(data);
	Result result = sendCommand(command);
	// If the record already exists, throw the exception to the caller
	if (result.getException() instanceof DuplicateKeyException) {
	    throw (DuplicateKeyException) result.getException();
	}
	return result.getRecNo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] find(String[] criteria) {
	Command command = new Command();
	command.setRequestType(RequestType.FIND);
	command.setData(criteria);
	Result result = sendCommand(command);
	return result.getRecNos();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(int recNo) throws RecordNotFoundException {
	Command command = new Command();
	command.setRequestType(RequestType.DELETE);
	command.setRecNo(recNo);
	Result result = sendCommand(command);
	// If the record was not found, throw the exception to the caller
	if (result.getException() instanceof RecordNotFoundException) {
	    throw (RecordNotFoundException) result.getException();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int recNo, String[] data) throws RecordNotFoundException {
	Command command = new Command();
	command.setRequestType(RequestType.UPDATE);
	command.setRecNo(recNo);
	command.setData(data);
	Result result = sendCommand(command);
	// If the record was not found, throw the exception to the caller
	if (result.getException() instanceof RecordNotFoundException) {
	    throw (RecordNotFoundException) result.getException();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnections() {
	try {
	    serverOut.close();
	    serverIn.close();
	    socket.close();
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE,
		    "An I/O error occurs while closing all sockets", e);
	}
    }

}
