package suncertify.db;

import java.util.Map;

/**
 * Container class holding meta information of the database.
 */
public class MetaData {

    /** The magic cookie indicating the binary file. */
    private int magicCookie;

    /** The length in bytes of aech record. */
    private int recordLength;

    /** The number of fields per record. */
    private int fieldCount;

    /** The map that says which field position has how much bytes. */
    private Map<Integer, Integer> fieldPositionToLength;

    /**
     * Returns the magic cookie.
     * 
     * @return the magic cookie
     */
    public int getMagicCookie() {
	return magicCookie;
    }

    /**
     * Sets the magic cookie.
     * 
     * @param magicCookie
     *            the magic cookie to set
     */
    public void setMagicCookie(int magicCookie) {
	this.magicCookie = magicCookie;
    }

    /**
     * Returns the length of each record.
     * 
     * @return the record length
     */
    public int getRecordLength() {
	return recordLength;
    }

    /**
     * Sets the length of each record.
     * 
     * @param recordLength
     *            the record length to set
     */
    public void setRecordLength(int recordLength) {
	this.recordLength = recordLength;
    }

    /**
     * Returns the number of fields per record.
     * 
     * @return the field count
     */
    public int getFieldCount() {
	return fieldCount;
    }

    /**
     * Sets the number of fields per record.
     * 
     * @param fieldCount
     *            the field count to set
     */
    public void setFieldCount(int fieldCount) {
	this.fieldCount = fieldCount;
    }

    /**
     * Returns the mapping of field positions and field lengths.
     * 
     * @return the map
     */
    public Map<Integer, Integer> getFieldPositionToLength() {
	return fieldPositionToLength;
    }

    /**
     * Sets the mapping of field positions and field lengths.
     * 
     * @param fieldPositionToLength
     *            the map to set
     */
    public void setFieldPositionToLength(
	    Map<Integer, Integer> fieldPositionToLength) {
	this.fieldPositionToLength = fieldPositionToLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb = sb.append("magic cookie = " + magicCookie);
	sb = sb.append(", field count = " + fieldCount);
	sb = sb.append(", record length = " + recordLength);
	sb = sb.append(", map (field position -> length) = "
		+ fieldPositionToLength);
	return sb.toString();
    }

}
