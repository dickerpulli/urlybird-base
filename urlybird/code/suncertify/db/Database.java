package suncertify.db;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The access the database entries. All data reading an manipulation is done in
 * this class.
 */
public class Database {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(Database.class
	    .getName());

    /** The character encoding of the file. */
    private static final String ENCODING = "US-ASCII";

    /** Byte indicating a delete record. */
    private static final byte DELETED_RECORD = 1;

    /** Byte indicating a delete record. */
    private static final byte VALID_RECORD = 0;

    /** Integer value for a magic cookie indicating correctness of a file. */
    private static final int MAGIC_COOKIE = 257;

    /** The file holding the data. */
    private RandomAccessFile databaseFile;

    /** Information which recNo is at which position in the database file. */
    private Map<Integer, Long> recNoToLocationInFile;

    /** The lock object for the recNoToLocationInFile-Map. */
    private ReadWriteLock recNoToLocationInFileLock;

    /** The read meta data of the database file. */
    private MetaData metaData;

    /**
     * Constructor. Initialization is done inside the constructor. The database
     * file will be read and analysed.
     * 
     * @param databaseFile
     *            The database file to access.
     * @throws IOException
     *             if some problems with the file occurs
     */
    public Database(File databaseFile) throws IOException {
	this.databaseFile = new RandomAccessFile(databaseFile, "rw");
	this.recNoToLocationInFileLock = new ReentrantReadWriteLock();
	try {
	    initializeDatabase();
	} catch (Exception e) {
	    throw new IOException(
		    "Error initializing database - wrong file format?", e);
	}
    }

    /**
     * Creates the recNoToLocationInFile-Map which hold the information about
     * what recNo is located at which position in the database file. Also
     * creates the meta data information reading out the file header.
     * 
     * @throws IOException
     *             if something went wrong in accessing the database file
     */
    private void initializeDatabase() throws IOException {
	recNoToLocationInFile = new HashMap<Integer, Long>();
	metaData = new MetaData();
	// First reset the file pointer to the start
	databaseFile.seek(0);
	// Read 4 bytes
	int magicCookie = databaseFile.readInt();
	if (magicCookie != MAGIC_COOKIE) {
	    throw new IOException("Magic cookie is not the one expected.");
	}
	metaData.setMagicCookie(magicCookie);
	// Read 4 bytes
	int recordLength = databaseFile.readInt();
	metaData.setRecordLength(recordLength);
	// Read 2 bytes
	int fieldCount = databaseFile.readShort();
	metaData.setFieldCount(fieldCount);
	// Then read the schema description section
	Map<Integer, Integer> fieldPositionToLength = new HashMap<Integer, Integer>();
	for (int i = 0; i < fieldCount; i++) {
	    int fieldNameLength = databaseFile.readShort();
	    byte[] fieldNameBytes = new byte[fieldNameLength];
	    databaseFile.readFully(fieldNameBytes);
	    int fieldLength = databaseFile.readShort();
	    fieldPositionToLength.put(i, fieldLength);
	}
	metaData.setFieldPositionToLength(fieldPositionToLength);
	LOGGER.fine("File header meta data: " + metaData);
	// Read the data entries
	boolean eof = false;
	int recNo = 0;
	while (!eof) {
	    try {
		// Fill in the location cache
		long pointer = databaseFile.getFilePointer();
		// Read out the data to log them
		byte deletedFlag = databaseFile.readByte();
		byte[] recordBytes = new byte[recordLength];
		databaseFile.read(recordBytes);
		String record = new String(recordBytes, ENCODING);
		if (deletedFlag == VALID_RECORD) {
		    LOGGER.finer("Found valid record: " + record);
		} else if (deletedFlag == DELETED_RECORD) {
		    LOGGER.finer("Found deleted record: " + record);
		} else {
		    throw new IOException("'Deleted' flag has an unknown value");
		}
		recNoToLocationInFile.put(recNo, pointer);
		recNo++;
	    } catch (EOFException e) {
		LOGGER.fine("End of file reached, stop reading");
		eof = true;
	    }
	}
	LOGGER.fine("Found records at positions: " + recNoToLocationInFile);
    }

    /**
     * Reads a record from the file. Returns an array where each element is a
     * record field value.
     * 
     * @param recNo
     *            The record number
     * @return The found data
     * @throws RecordNotFoundException
     *             if specified record does not exist or is marked as deleted
     */
    public String[] read(int recNo) throws RecordNotFoundException {
	LOGGER.fine("Read record at position " + recNo);
	Long locationInFile;
	recNoToLocationInFileLock.readLock().lock();
	try {
	    locationInFile = recNoToLocationInFile.get(recNo);
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}
	if (locationInFile != null) {
	    String[] recordFields = retrieve(locationInFile, recNo);
	    return recordFields;
	} else {
	    throw new RecordNotFoundException("No record found at position "
		    + recNo);
	}
    }

    /**
     * Retrieves the record at the location in file.
     * 
     * @param locationInFile
     *            The location in the database file
     * @param recNo
     *            The record number
     * @return The found record
     * @throws RecordNotFoundException
     *             If the record was not found or is marked as 'deleted' or if
     *             an IO-error happens.
     */
    private String[] retrieve(long locationInFile, int recNo)
	    throws RecordNotFoundException {
	try {
	    // The size of the byte array is 1 byte for the 'deleted' flag plus
	    // the bytes for the record itself.
	    byte[] recordBytes = new byte[1 + metaData.getRecordLength()];
	    synchronized (databaseFile) {
		databaseFile.seek(locationInFile);
		databaseFile.readFully(recordBytes);
	    }
	    byte deletedFlag = recordBytes[0];
	    if (deletedFlag == DELETED_RECORD) {
		throw new RecordNotFoundException("The record at position "
			+ recNo + " is deleted");
	    }
	    byte[] recordData = Arrays.copyOfRange(recordBytes, 1,
		    recordBytes.length);
	    String[] recordFields = getRecordFields(recordData,
		    metaData.getFieldPositionToLength());
	    LOGGER.fine("Found record: " + Arrays.toString(recordFields));
	    return recordFields;
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    throw new RecordNotFoundException("The record at position " + recNo
		    + " was not found");
	}
    }

    /**
     * Parses the record byte array and creates an array of records fields. The
     * configuration data for this parsing is included in the given map.
     * 
     * @param recordBytes
     *            The record as byte array
     * @param fieldPositionToLength
     *            The map that says which field position has which length
     * @return The String array of encoded text
     * @throws IOException
     *             If the byte array parsing fails
     */
    private String[] getRecordFields(byte[] recordBytes,
	    Map<Integer, Integer> fieldPositionToLength) throws IOException {
	String record;
	try {
	    record = new String(recordBytes, ENCODING);
	} catch (UnsupportedEncodingException e) {
	    throw new IOException("Encoding " + ENCODING
		    + " is not supported by the system");
	}
	String[] fields = new String[fieldPositionToLength.size()];
	int offset = 0;
	for (Entry<Integer, Integer> entry : fieldPositionToLength.entrySet()) {
	    int fieldPosition = entry.getKey();
	    int fieldLength = entry.getValue();
	    fields[fieldPosition] = record.substring(offset,
		    offset + fieldLength).trim();
	    offset += fieldLength;
	}
	return fields;
    }

    /**
     * Parses the recordFields array and creates the corresponding byte array.
     * The configuration data for this parsing is included in the given map.
     * 
     * @param recordFields
     *            The record as String array
     * @param fieldPositionToLength
     *            The map that says which field position has which length
     * @return The byte array of record data
     * @throws IOException
     *             If the String array parsing fails
     */
    private byte[] getRecordBytes(String[] recordFields,
	    Map<Integer, Integer> fieldPositionToLength) throws IOException {
	byte[] record = new byte[metaData.getRecordLength()];
	int pos = 0;
	for (int i = 0; i < recordFields.length; i++) {
	    try {
		String field = recordFields[i];
		int length = fieldPositionToLength.get(i);
		byte[] fieldBytes = new byte[length];
		System.arraycopy(field.getBytes(ENCODING), 0, fieldBytes, 0,
			field.length());
		System.arraycopy(fieldBytes, 0, record, pos, length);
		pos += length;
	    } catch (UnsupportedEncodingException e) {
		throw new IOException("Encoding " + ENCODING
			+ " is not supported by the system");
	    }
	}
	return record;
    }

    /**
     * Modifies the fields of a record. The new value for field n appears in
     * data[n].
     * 
     * @param recNo
     *            The record number
     * @param data
     *            The new values
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     */
    public void update(int recNo, String[] data) throws RecordNotFoundException {
	LOGGER.fine("Update record at position " + recNo + " with "
		+ Arrays.toString(data));
	Long locationInFile;
	recNoToLocationInFileLock.readLock().lock();
	try {
	    locationInFile = recNoToLocationInFile.get(recNo);
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}
	try {
	    if (locationInFile != null) {
		byte[] recordBytes = getRecordBytes(data,
			metaData.getFieldPositionToLength());
		synchronized (databaseFile) {
		    databaseFile.seek(locationInFile);
		    byte deletedFlag = databaseFile.readByte();
		    if (deletedFlag == DELETED_RECORD) {
			throw new RecordNotFoundException(
				"The record at position " + recNo
					+ " is deleted");
		    }
		    databaseFile.write(recordBytes);
		    LOGGER.fine("Record at position " + recNo + " updated");
		}
	    } else {
		throw new RecordNotFoundException(
			"No record found at position " + recNo);
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    LOGGER.severe("Update of record canceled");
	}
    }

    /**
     * Deletes a record, making the record number and associated disk storage
     * available for reuse.
     * 
     * @param recNo
     *            The record number
     * @throws RecordNotFoundException
     *             If specified record does not exist or is marked as deleted
     */
    public void delete(int recNo) throws RecordNotFoundException {
	LOGGER.fine("Delete record at position " + recNo);
	Long locationInFile;
	recNoToLocationInFileLock.readLock().lock();
	try {
	    locationInFile = recNoToLocationInFile.get(recNo);
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}
	try {
	    if (locationInFile != null) {
		synchronized (databaseFile) {
		    databaseFile.seek(locationInFile);
		    byte deletedFlag = databaseFile.readByte();
		    if (deletedFlag == DELETED_RECORD) {
			throw new RecordNotFoundException(
				"No record found at position " + recNo);
		    }
		    databaseFile.seek(locationInFile);
		    databaseFile.write(DELETED_RECORD);
		    LOGGER.fine("Record at position " + recNo + " deleted");
		}
	    } else {
		throw new RecordNotFoundException(
			"No record found at position " + recNo);
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    LOGGER.severe("Deletion of record canceled");
	}
    }

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
    public int[] find(String[] criteria) {
	LOGGER.fine("Search for " + Arrays.toString(criteria));
	recNoToLocationInFileLock.readLock().lock();
	try {
	    Set<Entry<Integer, Long>> locations = recNoToLocationInFile
		    .entrySet();
	    int[] found = new int[0];
	    for (Entry<Integer, Long> location : locations) {
		long locationInFile = location.getValue();
		int recNo = location.getKey();
		byte[] recordBytes = new byte[metaData.getRecordLength()];
		byte deletedFlag;
		synchronized (databaseFile) {
		    databaseFile.seek(locationInFile);
		    deletedFlag = databaseFile.readByte();
		    databaseFile.readFully(recordBytes);
		}
		if (deletedFlag == DELETED_RECORD) {
		    // If the record is marked as deleted, it should not be
		    // found here, continue to the next file location
		    continue;
		}
		String[] recordFields = getRecordFields(recordBytes,
			metaData.getFieldPositionToLength());
		// Testing likeness
		LOGGER.finer("Comparing " + Arrays.toString(recordFields)
			+ " with " + Arrays.toString(criteria));
		if (isLike(recordFields, criteria)) {
		    found = Arrays.copyOf(found, found.length + 1);
		    found[found.length - 1] = recNo;
		    LOGGER.finer("Found a new likeness at position " + recNo);
		}
	    }
	    LOGGER.fine("Found likenesses at positions "
		    + Arrays.toString(found));
	    return found;
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    LOGGER.severe("Find record failed - return empty array");
	    return new int[0];
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}
    }

    /**
     * Searches the in the text array for criterias. A null value in criteria
     * matches any text value. A non-null value matches any value that begins
     * with the criteria. For example "Fred" matches "Freddy".
     * 
     * @param texts
     *            The text array
     * @param criterias
     *            The criteria array
     * @return <code>true</code> if all text array elements are like criteria
     *         array elements
     */
    private boolean isLike(String[] texts, String[] criterias) {
	if (texts.length != criterias.length) {
	    throw new IllegalArgumentException("The array length's differs");
	}
	for (int i = 0; i < texts.length; i++) {
	    String text = texts[i];
	    String criteria = criterias[i];
	    if (criteria == null) {
		continue;
	    } else if (text.startsWith(criteria)) {
		continue;
	    } else {
		return false;
	    }
	}
	return true;
    }

    /**
     * Creates a new record in the database (possibly reusing a deleted entry).
     * Inserts the given data, and returns the record number of the new record.
     * 
     * @param data
     *            The values to create
     * @return The new record number or -1 if something went wrong
     */
    public int create(String[] data) {
	LOGGER.fine("Creating new record: " + Arrays.toString(data));
	recNoToLocationInFileLock.readLock().lock();
	try {
	    Set<Entry<Integer, Long>> locations = recNoToLocationInFile
		    .entrySet();
	    // Search for an already existing record marked as deleted
	    for (Entry<Integer, Long> location : locations) {
		long locationInFile = location.getValue();
		int recNo = location.getKey();
		byte[] recordBytes = new byte[metaData.getRecordLength()];
		byte[] newRecordBytes = getRecordBytes(data,
			metaData.getFieldPositionToLength());
		byte deletedFlag;
		synchronized (databaseFile) {
		    databaseFile.seek(locationInFile);
		    deletedFlag = databaseFile.readByte();
		    databaseFile.readFully(recordBytes);
		    if (deletedFlag == DELETED_RECORD) {
			LOGGER.fine("Found deleted record - reuse the space");
			databaseFile.seek(locationInFile);
			databaseFile.writeByte(VALID_RECORD);
			databaseFile.write(newRecordBytes);
			LOGGER.fine("New record: " + Arrays.toString(data)
				+ " created");
			return recNo;
		    }
		}
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    LOGGER.severe("Create new record failed - return -1");
	    return -1;
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}

	// After trying to find deleted record, create the new entry
	recNoToLocationInFileLock.writeLock().lock();
	try {
	    // First sort the locations so that we can get the last location
	    List<Long> locationsInFile = new ArrayList<Long>(
		    recNoToLocationInFile.values());
	    Collections.sort(locationsInFile);
	    // Create new record in database at the end of the file
	    int lastIndex = locationsInFile.size() - 1;
	    byte[] recordBytes = getRecordBytes(data,
		    metaData.getFieldPositionToLength());
	    long writePosition;
	    synchronized (databaseFile) {
		databaseFile.seek(locationsInFile.get(lastIndex));
		databaseFile.skipBytes(1 + metaData.getRecordLength());
		writePosition = databaseFile.getFilePointer();
		databaseFile.writeByte(VALID_RECORD);
		databaseFile.write(recordBytes);
	    }
	    int recNo = locationsInFile.size();
	    recNoToLocationInFile.put(recNo, writePosition);
	    LOGGER.fine("New record: " + Arrays.toString(data) + " created");
	    return recNo;
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading database file", e);
	    LOGGER.severe("Create new record failed - return -1");
	    return -1;
	} finally {
	    recNoToLocationInFileLock.writeLock().unlock();
	}
    }

    /**
     * Checks if the record is deleted or not.
     * 
     * @param recNo
     *            The record number
     * @return <code>true</code> if the given record is marked as deleted
     * @throws RecordNotFoundException
     *             if the record does not exist in database
     */
    public boolean isDeleted(int recNo) throws RecordNotFoundException {
	Long location;
	recNoToLocationInFileLock.readLock().lock();
	try {
	    location = recNoToLocationInFile.get(recNo);
	} finally {
	    recNoToLocationInFileLock.readLock().unlock();
	}
	try {
	    if (location != null) {
		synchronized (databaseFile) {
		    databaseFile.seek(location);
		    byte deletedFlag = databaseFile.readByte();
		    return (deletedFlag == DELETED_RECORD);
		}
	    } else {
		throw new RecordNotFoundException("The record at position "
			+ recNo + " does not exist");
	    }
	} catch (IOException e) {
	    throw new RecordNotFoundException(
		    "Error while reading record at position " + recNo);
	}
    }

}
