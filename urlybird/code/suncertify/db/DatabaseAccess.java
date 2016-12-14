package suncertify.db;

import java.io.IOException;

/**
 * An interface to provide database access. Implementations can be either local
 * or remote. The interface provides all methods that are needed to read and
 * write data from and to the database.
 */
public interface DatabaseAccess {

    /**
     * Searches the database for records that match the given criteria. Field n
     * in the database file is described by criteria[n]. A null value in
     * criteria[n] matches any field value. A non-null value in criteria[n]
     * matches any field value that begins with criteria[n]. (For example,
     * "Fred" matches "Fred" or "Freddy".
     * 
     * @param criteria
     *            The search criteria
     * @return The array of found record numbers
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public int[] find(String[] criteria) throws IOException;

    /**
     * Creates a new record in database.
     * 
     * @param data
     *            The record fields
     * @return The created record number
     * @throws DuplicateKeyException
     *             If the record already exists
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public int create(String[] data) throws IOException, DuplicateKeyException;

    /**
     * Reads the record of the given number.
     * 
     * @param recNo
     *            The record number.
     * @return The record fields
     * @throws RecordNotFoundException
     *             If the record does not exist
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public String[] read(int recNo) throws IOException, RecordNotFoundException;

    /**
     * Deletes the record associated with the number.
     * 
     * @param recNo
     *            The record number to delete
     * @throws RecordNotFoundException
     *             If the record does not exist
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public void delete(int recNo) throws IOException, RecordNotFoundException;

    /**
     * Updates the record field values of the record with the given number.
     * 
     * @param recNo
     *            The record number
     * @param data
     *            The updated record fields
     * @throws RecordNotFoundException
     *             If the record does not exist
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public void update(int recNo, String[] data) throws IOException,
	    RecordNotFoundException;

    /**
     * Close database connection.
     * 
     * @throws IOException
     *             If an IO-error happens during execution
     */
    public void closeConnections() throws IOException;

}