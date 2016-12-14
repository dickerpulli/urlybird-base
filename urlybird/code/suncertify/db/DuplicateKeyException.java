package suncertify.db;

/**
 * Is thrown if the key already exists in database.
 */
public class DuplicateKeyException extends Exception {

    /** A serial id. */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DuplicateKeyException() {
	super();
    }

    /**
     * Constructor with message parameter.
     * 
     * @param message
     *            The message
     */
    public DuplicateKeyException(String message) {
	super(message);
    }

}
