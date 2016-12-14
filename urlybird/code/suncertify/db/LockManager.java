package suncertify.db;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * The class to manage locking on the database.
 */
public class LockManager {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(LockManager.class
	    .getName());

    /** One record lock cookie for each recNo. */
    private ConcurrentMap<Integer, Long> lockCookies;

    /** The lock for the lock cookies access. */
    private ReentrantLock lock;

    /** The condition for the lock. */
    private Condition condition;

    /**
     * Default constructor.
     */
    public LockManager() {
	this.lock = new ReentrantLock();
	this.condition = lock.newCondition();
	this.lockCookies = new ConcurrentHashMap<Integer, Long>();
    }

    /**
     * Locks the database record with the given record number.
     * 
     * @param recNo
     *            The record number
     * @return The lock object
     */
    public long lock(int recNo) {
	LOGGER.fine("Try to lock record at position " + recNo);
	lock.lock();
	try {
	    while (lockCookies.containsKey(recNo)) {
		LOGGER.fine("Waiting for lock cookie of record at position "
			+ recNo);
		condition.await();
	    }
	    // Create the new lock cookie and put it into the lock cookie map
	    long lockCookie = createLockCookie();
	    lockCookies.put(recNo, lockCookie);
	    LOGGER.fine("New lock cookie created for record at position "
		    + recNo);
	    return lockCookie;
	} catch (InterruptedException e) {
	    throw new RuntimeException("Waiting for lock was interrupted", e);
	} finally {
	    lock.unlock();
	}
    }

    /**
     * Creates a new unique lock cookie. Uniqueness is guarantied by using the
     * current system timestamp after waiting at least 1 millisecond and by
     * synchronizing this method on this class object.
     * 
     * @return The lock cookie
     */
    private synchronized long createLockCookie() {
	// Wait 1 millisecond to that the lock cookie is really unique
	long now = System.currentTimeMillis();
	while (now + 1 > System.currentTimeMillis()) {
	    // loop until the time moved one millisecond to future
	}
	return System.currentTimeMillis();
    }

    /**
     * Unlock the given database entry.
     * 
     * @param recNo
     *            The record to unlock
     * @param cookie
     *            The lock cookie
     * @throws SecurityException
     *             If the given cookie is not the required one
     */
    public void unlock(int recNo, long cookie) throws SecurityException {
	LOGGER.fine("Try to unlock record at position " + recNo);
	lock.lock();
	try {
	    Long lockCookie = lockCookies.get(recNo);
	    if (lockCookie != null) {
		if (!lockCookie.equals(new Long(cookie))) {
		    // If the locked cookie is not the same as passed as
		    // parameter the Thread is not the owner of the lock
		    throw new SecurityException(
			    "The passed cookie is not the required cookie");
		} else {
		    // Remove the lock from the list and signal a waiting Thread
		    // to wake up, because the lock cookie may be free again
		    lockCookies.remove(recNo);
		    condition.signal();
		    LOGGER.fine("Lock released for record at position " + recNo);
		}
	    } else {
		LOGGER.warning("Record with number " + recNo
			+ " is not locked, ignoring unlock request");
	    }
	} finally {
	    lock.unlock();
	}
    }

    /**
     * Checks if the lock cookie is the correct cookie for this record.
     * 
     * @param recNo
     *            The record number
     * @param lockCookie
     *            The lock cookie
     * @return <code>true</code> if the cookie is valid
     */
    public boolean isValidCookie(int recNo, long lockCookie) {
	if (lockCookies.get(recNo) == null
		|| lockCookies.get(recNo) != lockCookie) {
	    return false;
	}
	return true;
    }

}
