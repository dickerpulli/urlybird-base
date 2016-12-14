package suncertify.db;

import java.io.File;
import java.io.IOException;

/**
 * Provides the implementation of access to the database.
 */
public class Data implements DB {

    /** The database access object. */
    private Database database;

    /** The manager for locking operations. */
    private LockManager lockManager;

    /**
     * Constructor with the database file to load.
     * 
     * @param databaseFile
     *            The database file
     * @throws IOException
     *             if something went wrong in accessing the database file
     */
    public Data(File databaseFile) throws IOException {
	this.lockManager = new LockManager();
	this.database = new Database(databaseFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] read(int recNo) throws RecordNotFoundException {
	return database.read(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int recNo, String[] data, long lockCookie)
	    throws RecordNotFoundException, SecurityException {
	if (!lockManager.isValidCookie(recNo, lockCookie)) {
	    throw new SecurityException(
		    "The given cookie for the record at position " + recNo
			    + " is not valid");
	}
	database.update(recNo, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(int recNo, long lockCookie)
	    throws RecordNotFoundException, SecurityException {
	if (!lockManager.isValidCookie(recNo, lockCookie)) {
	    throw new SecurityException(
		    "The given cookie for the record at position " + recNo
			    + " is not valid");
	}
	database.delete(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] find(String[] criteria) {
	return database.find(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int create(String[] data) throws DuplicateKeyException {
	return database.create(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long lock(int recNo) throws RecordNotFoundException {
	if (database.isDeleted(recNo)) {
	    throw new RecordNotFoundException("The record at postion " + recNo
		    + " is marked as deleted");
	}
	return lockManager.lock(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlock(int recNo, long cookie) throws RecordNotFoundException,
	    SecurityException {
	if (!lockManager.isValidCookie(recNo, cookie)) {
	    throw new SecurityException(
		    "The given cookie for the record at position " + recNo
			    + " is not valid");
	}
	if (database.isDeleted(recNo)) {
	    // No exception should be thrown here, because unlocking of deleted
	    // record must be possible - unlocking is neccessary!!!
	}
	lockManager.unlock(recNo, cookie);
    }

}
