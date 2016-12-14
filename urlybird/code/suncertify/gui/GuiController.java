package suncertify.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTable;

import suncertify.db.DatabaseAccess;
import suncertify.db.LocalDatabaseAccess;
import suncertify.db.Record;
import suncertify.db.Record.RecordField;
import suncertify.db.RecordNotFoundException;
import suncertify.network.Server;
import suncertify.network.rmi.RMIRegistrator;
import suncertify.network.rmi.RMIServer;

/**
 * The controller class that handles the business logic for the GUI elements.
 */
public class GuiController {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(GuiController.class
	    .getName());

    /** Keyword for "all" records. */
    private static final String ALL = "- all -";

    /** The local server instance. */
    private Server server;

    /** The client that can 'talk' to the server. */
    private DatabaseAccess client;

    /**
     * Connects to the server and opens a new dialog for interacting with the
     * server. After closing the dialog the connection to the server will be
     * closed.
     * 
     * @param hostname
     *            The server hostname
     * @param port
     *            The server port
     * @param clientFrame
     *            The client frame to block
     */
    public void connectServer(String hostname, String port, JFrame clientFrame) {
	try {
	    int portNumber = parsePort(port);
	    if (portNumber == -1) {
		LOGGER.severe("Illegal port number " + port);
		return;
	    }
	    client = RMIRegistrator.getClient(hostname, portNumber);
	    LOGGER.info("Connected to " + hostname + ":" + portNumber);
	    openRecordsFrame(clientFrame);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "No remote database connection possible",
		    e);
	}
    }

    /**
     * @param dbLocation
     *            The local database location
     * @param clientFrame
     *            The client frame to block
     */
    public void connectLocal(String dbLocation, JFrame clientFrame) {
	try {
	    client = new LocalDatabaseAccess(dbLocation);
	    LOGGER.info("Connected to " + dbLocation);
	    openRecordsFrame(clientFrame);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "No local database connection possible", e);
	}
    }

    /**
     * Opens the frame to manage records in database.
     * 
     * @param clientFrame
     *            The frame that should be set invisible
     */
    private void openRecordsFrame(final JFrame clientFrame) {
	RecordsFrame recordsFrame = new RecordsFrame(this);
	recordsFrame.setLocationRelativeTo(clientFrame);
	recordsFrame.addWindowListener(new WindowAdapter() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void windowClosed(WindowEvent e) {
		try {
		    client.closeConnections();
		} catch (IOException e1) {
		    LOGGER.log(Level.SEVERE, "Error in closing connection", e);
		}
		LOGGER.info("Closed connection");
		// Show the client frame again
		clientFrame.setVisible(true);
	    }
	});
	recordsFrame.setVisible(true);
	// Hide the client frame while record frame is visible
	clientFrame.setVisible(false);
    }

    /**
     * Starts the server with the given database on the given local port.
     * 
     * @param dbLocation
     *            The location of the database
     * @param port
     *            The port number
     * @return <code>true</code> if the server was successfully started.
     */
    public boolean startServer(String dbLocation, String port) {
	int portNumber = parsePort(port);
	if (portNumber == -1) {
	    LOGGER.severe("Illegal port number " + port);
	    return false;
	}
	if (dbLocation == null || dbLocation.trim().isEmpty()) {
	    LOGGER.severe("Illegal empty database location");
	    return false;
	}
	server = new RMIServer(portNumber, dbLocation);
	server.start();
	return server.isRunning();
    }

    /**
     * Parses a String port to an Integer. Also validates the possible port
     * range.<br/>
     * 0 &lt; port &le; 65535.
     * 
     * @param port
     *            The port
     * @return The port as number or '-1' if the port is out of range.
     */
    private int parsePort(String port) {
	int portNumber;
	try {
	    portNumber = Integer.parseInt(port);
	} catch (NumberFormatException e) {
	    return -1;
	}
	if (portNumber <= 0 || portNumber > 65535) {
	    return -1;
	}
	return portNumber;
    }

    /**
     * Stops the running server.
     * 
     * @return <code>true</code> if the server was successfully shut down.
     */
    public boolean stopServer() {
	server.stop();
	return !server.isRunning();
    }

    /**
     * Perform search in database.
     * 
     * @param nameComboBox
     *            The combobox for name
     * @param locationComboBox
     *            The combobox for location
     * @param recordsTable
     *            The table to fill with the search result
     */
    public void performSearchAction(JComboBox nameComboBox,
	    JComboBox locationComboBox, JTable recordsTable) {
	String name = getStringOfSelection(nameComboBox);
	String location = getStringOfSelection(locationComboBox);
	List<Record> records = getRecords(name, location);
	recordsTable.setModel(new RecordsTableModel(records));
	recordsTable.repaint();
    }

    /**
     * Reads all records from the server and converts them into a list of Record
     * elements.
     * 
     * @return The records
     */
    private List<Record> getAllRecords() {
	return getRecords("", "");
    }

    /**
     * Searches for all records that match the given name and location.
     * 
     * @param name
     *            The name of the hotel
     * @param location
     *            The city location
     * @return The found records
     */
    private List<Record> getRecords(String name, String location) {
	List<Record> list = new ArrayList<Record>();
	try {
	    int[] recNos = client.find(new String[] { name, location, "", "",
		    "", "", "" });
	    for (int recNo : recNos) {
		String[] recordFields = client.read(recNo);
		Record record = new Record(recordFields, recNo);
		list.add(record);
	    }
	} catch (RecordNotFoundException e) {
	    // Should not happen, because all elements in the table are read
	    // out of the database. If it happen, a severe programming
	    // failure was done, so throw an exception ...
	    throw new IllegalStateException(
		    "Record selected to update that does not exist", e);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while searching in database", e);
	}
	return list;
    }

    /**
     * Books the selected record in the table. Marks it in the database.
     * 
     * @param table
     *            The table with one selected row.
     * @param customerId
     *            The ID of the customer who books the record.
     */
    public void bookSelectedRecord(JTable table, String customerId) {
	int row = table.getSelectedRow();
	if (row == -1) {
	    // If no record was selected in the table, return
	    LOGGER.warning("No item selected in the table");
	    return;
	}
	if (customerId == null || customerId.trim().isEmpty()) {
	    // If nothing was type into the customer ID field, return
	    LOGGER.severe("Customer ID is left blank");
	    return;
	}

	// Get the selected record and its data and recNo
	Record record = ((RecordsTableModel) table.getModel())
		.getRecordAtRow(row);
	// ... and set the owner as given in the method parameter
	setOwner(customerId, record);
    }

    /**
     * Set the owner field of the given record.
     * 
     * @param owner
     *            The owner.
     * @param record
     *            The record to update.
     */
    private void setOwner(String owner, Record record) {
	int recNo = record.getRecNo();
	int ownerIndex = RecordField.Owner.getIndex();
	String[] data = record.getData();
	// Check if the record is already booked up
	try {
	    String[] actualData = client.read(recNo);
	    if (!actualData[ownerIndex].trim().isEmpty()) {
		LOGGER.severe("The record is already sold.");
		return;
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while reading actual record data",
		    e);
	    return;
	} catch (RecordNotFoundException e) {
	    // Should not happen, because all elements in the table are read
	    // out of the database. If it happen, a severe programming
	    // failure was done, so throw an exception ...
	    throw new IllegalStateException(
		    "Record selected to update that does not exist", e);
	}
	// Set the new owner to flag the record as 'booked up'
	data[ownerIndex] = owner;
	try {
	    client.update(recNo, data);
	    LOGGER.info("Record was updated");
	} catch (RecordNotFoundException e) {
	    // Should not happen, because all elements in the table are read
	    // out of the database. If it happen, a severe programming
	    // failure was done, so throw an exception ...
	    throw new IllegalStateException(
		    "Record selected to update that does not exist", e);
	} catch (ArrayIndexOutOfBoundsException e) {
	    LOGGER.severe("The length of the customer ID is out of range");
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Error while updating database", e);
	}
    }

    /**
     * Get an array of all locations.
     * 
     * @return Array
     */
    public Object[] getAllLocations() {
	return getAllFields(RecordField.Location);
    }

    /**
     * Get an array of all names.
     * 
     * @return Array
     */
    public Object[] getAllNames() {
	return getAllFields(RecordField.Name);
    }

    /**
     * Get an array of all fields defined in the parameter.
     * 
     * @param recordField
     *            The field to get the list of
     * @return Array
     */
    private Object[] getAllFields(RecordField recordField) {
	List<Record> allRecords = getAllRecords();
	Set<String> locations = new TreeSet<String>();
	locations.add(ALL);
	for (Record record : allRecords) {
	    locations.add(record.getData()[recordField.getIndex()]);
	}
	return locations.toArray();
    }

    /**
     * Gets the text of the selected item.
     * 
     * @param comboBox
     *            The combobox
     * @return Text
     */
    private String getStringOfSelection(JComboBox comboBox) {
	Object selectedItem = comboBox.getSelectedItem();
	if (selectedItem.toString().equals(ALL)) {
	    return "";
	}
	return selectedItem.toString();
    }

}
