package suncertify.db;

/**
 * This interface provides access to the database.
 */
public interface DB {

    /**
     * Reads a record from the file. Returns an array where each element is a
     * record value.
     * 
     * @param recNo
     *            The record number
     * @return The found data
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     */
    public String[] read(int recNo) throws RecordNotFoundException;

    /**
     * Modifies the fields of a record. The new value for field n appears in
     * data[n].
     * 
     * @param recNo
     *            The record number
     * @param data
     *            The new values
     * @param lockCookie
     *            The lock cookie
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     * @throws SecurityException
     *             If the record is locked with a cookie other than lockCookie.
     */
    public void update(int recNo, String[] data, long lockCookie)
	    throws RecordNotFoundException, SecurityException;

    /**
     * Deletes a record, making the record number and associated disk storage
     * available for reuse.
     * 
     * @param recNo
     *            The record number
     * @param lockCookie
     *            The lock cookie
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     * @throws SecurityException
     *             If the record is locked with a cookie other than lockCookie
     */
    public void delete(int recNo, long lockCookie)
	    throws RecordNotFoundException, SecurityException;

    /**
     * Returns an array of record numbers that match the specified criteria.
     * Field n in the database file is described by criteria[n]. A null value in
     * criteria[n] matches any field value. A non-null value in criteria[n]
     * matches any field value that begins with criteria[n]. (For example,
     * "Fred" matches "Fred" or "Freddy".
     * 
     * @param criteria
     *            The search criteria
     * @return The result array of record numbers
     */
    public int[] find(String[] criteria);

    /**
     * Creates a new record in the database (possibly reusing a deleted entry).
     * Inserts the given data, and returns the record number of the new record.
     * 
     * @param data
     *            The values to create
     * @return The new record number
     * @throws DuplicateKeyException
     *             If the key for the new data already exists in the database.
     */
    public int create(String[] data) throws DuplicateKeyException;

    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * Returned value is a cookie that must be used when the record is unlocked,
     * updated, or deleted. If the specified record is already locked by a
     * different client, the current thread gives up the CPU and consumes no CPU
     * cycles until the record is unlocked.
     * 
     * @param recNo
     *            The record number to get the lock for
     * @return The lock cookie
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     */
    public long lock(int recNo) throws RecordNotFoundException;

    /**
     * Releases the lock on a record. Cookie must be the cookie returned when
     * the record was locked.
     * 
     * @param recNo
     *            The record number to release the lock for
     * @param cookie
     *            The lock cookie
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     * @throws SecurityException
     *             If the record is locked with a cookie other than cookie
     */
    public void unlock(int recNo, long cookie) throws RecordNotFoundException,
	    SecurityException;

}