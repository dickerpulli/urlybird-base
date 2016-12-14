package suncertify.db;

import java.io.Serializable;

/**
 * The database record holding the hotel room.
 */
public class Record implements Serializable {

    /**
     * The enum maps every record field to the index of the String array that is
     * given to the constructor of Record.
     */
    public enum RecordField {
	/** The name of the hotel. */
	Name(0),
	/** The city location of the hotel. */
	Location(1),
	/** The room size. */
	Size(2),
	/** Smoking or non-smoking room. */
	Smoking(3),
	/** The price per night. */
	Rate(4),
	/** The single night this record relates. */
	Date(5),
	/** The customer Id or blank if not sold. */
	Owner(6);

	private int index;

	private RecordField(int index) {
	    this.index = index;
	}

	/**
	 * Get the index of the record field mapping.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
	    return index;
	}

	/**
	 * Give the enum element that is placed at the given index position.
	 * 
	 * @param index
	 *            The index.
	 * @return The record field or NULL if nothing was found
	 */
	public static RecordField getByIndex(int index) {
	    RecordField[] recordFields = values();
	    for (RecordField recordField : recordFields) {
		if (recordField.getIndex() == index) {
		    return recordField;
		}
	    }
	    return null;
	}
    }

    /** version number for serialization and deserialization. */
    private static final long serialVersionUID = 1L;

    /** The internal data of the record. */
    private String[] data;

    /** The internal record number of the record. */
    private int recNo;

    /**
     * Default constructor. The <code>data</code> parameter is the data array,
     * that must match to the mapping provided by {@link RecordField}.
     * 
     * @param data
     *            The data array to parse for getting the fields
     * @param recNo
     *            The record number in the database
     */
    public Record(String[] data, int recNo) {
	this.data = data;
	this.recNo = recNo;
    }

    /**
     * Return the String array representation of the data field of this record.
     * 
     * @return The data
     */
    public String[] getData() {
	return data;
    }

    /**
     * Returns the record number of the record in the database.
     * 
     * @return The recNo
     */
    public int getRecNo() {
	return recNo;
    }

}
