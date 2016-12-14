package suncertify.network.rmi;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

import suncertify.db.DatabaseAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.LocalDatabaseAccess;
import suncertify.db.RecordNotFoundException;

/**
 * The remote database access class. This class is the remote proxy that
 * delegates all calls to a local database access instance.
 */
public class RMIDatabaseAccess extends UnicastRemoteObject implements
	RemoteDatabaseAccess {

    /** Serial version UID for RMI serialization. */
    private static final long serialVersionUID = 1L;

    /** The internal database access. */
    private DatabaseAccess databaseAccess;

    /**
     * Constructor.
     * 
     * @param dbLocation
     *            The local database location.
     * @throws IOException
     *             if an error occurs in creating local database access.
     */
    public RMIDatabaseAccess(String dbLocation) throws IOException {
	databaseAccess = new LocalDatabaseAccess(dbLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] find(String[] criteria) throws IOException {
	return databaseAccess.find(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int create(String[] data) throws IOException, DuplicateKeyException {
	return databaseAccess.create(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] read(int recNo) throws IOException, RecordNotFoundException {
	return databaseAccess.read(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(int recNo) throws IOException, RecordNotFoundException {
	databaseAccess.delete(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int recNo, String[] data) throws IOException,
	    RecordNotFoundException {
	databaseAccess.update(recNo, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnections() throws IOException {
	databaseAccess.closeConnections();
    }

}
