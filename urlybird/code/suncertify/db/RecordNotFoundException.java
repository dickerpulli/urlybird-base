package suncertify.db;

/**
 * Is thrown if a specified record does not exist or is marked as deleted in the
 * database file.
 */
public class RecordNotFoundException extends Exception {

    /** A serial id. */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public RecordNotFoundException() {
	super();
    }

    /**
     * Constructor with message parameter.
     * 
     * @param message
     *            The message
     */
    public RecordNotFoundException(String message) {
	super(message);
    }

}
