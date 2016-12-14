package suncertify.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Access local database including locking management.
 */
public class LocalDatabaseAccess implements DatabaseAccess {

    /** The logger. */
    private static final Logger LOGGER = Logger
	    .getLogger(LocalDatabaseAccess.class.getName());

    /** The local records database. */
    private DB database;

    /** The local path to the DB. */
    private String dbLocation;

    /**
     * Constructor. Every instance of this database access uses the same DB
     * instance.
     * 
     * @param dbLocation
     *            The database file location
     * @throws IOException
     *             if the database cannot be connected
     */
    public LocalDatabaseAccess(String dbLocation) throws IOException {
	if (database == null) {
	    File databaseFile = new File(dbLocation);
	    if (!databaseFile.exists()) {
		throw new FileNotFoundException("File not found at "
			+ databaseFile.getAbsolutePath());
	    }
	    database = new Data(databaseFile);
	    this.dbLocation = dbLocation;
	} else if (this.dbLocation != dbLocation) {
	    LOGGER.warning("Only one database can be accessed with this "
		    + "DatabaseAccess-Instance. Actual dbLocation is '"
		    + this.dbLocation + "'. Ignoring new location '"
		    + dbLocation + "'");
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnections() {
	// nothing to close in local mode
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
    public String[] read(int recNo) throws RecordNotFoundException {
	return database.read(recNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(int recNo) throws RecordNotFoundException {
	long cookie = database.lock(recNo);
	try {
	    database.delete(recNo, cookie);
	} finally {
	    database.unlock(recNo, cookie);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(int recNo, String[] data) throws RecordNotFoundException {
	long cookie = database.lock(recNo);
	try {
	    database.update(recNo, data, cookie);
	} finally {
	    database.unlock(recNo, cookie);
	}
    }

}
