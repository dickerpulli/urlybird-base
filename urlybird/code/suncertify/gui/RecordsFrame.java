package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import suncertify.db.Record;

/**
 * A dialog that shows all record entries and allows to communicate with the
 * server.
 */
public class RecordsFrame extends LayoutFrame {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    private static final String BORDER_TITLE_SEARCH = "Search filter";
    private static final String BORDER_TITLE_RECORDS = "Record entries";
    private static final String BORDER_TITLE_BOOK = "Booking";
    private static final String BUTTON_BOOK = "Book selected record";
    private static final String BUTTON_DISCONNECT = "Disconnect";
    private static final String LABEL_LOCATION = "Location:";
    private static final String LANEL_NAME = "Name:";
    private static final String LABEL_CUSTOMERID = "Customer ID:";
    private static final String TITLE = "UrlyBird Records";

    /**
     * Constructor.
     * 
     * @param guiController
     *            The GUI controller
     */
    public RecordsFrame(GuiController guiController) {
	super(guiController);
	setTitle(TITLE);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * {@inheritDoc}
     */
    protected void initComponents(Object... initParams) {
	JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	searchPanel.setBorder(BorderFactory
		.createTitledBorder(BORDER_TITLE_SEARCH));
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 5, 5, 5);

	// The search filter drop down boxes
	JLabel nameLabel = new JLabel(LANEL_NAME);
	constraints.anchor = GridBagConstraints.EAST;
	searchPanel.add(nameLabel, constraints);
	Object[] names = guiController.getAllNames();
	final JComboBox nameComboBox = new JComboBox(names);
	constraints.anchor = GridBagConstraints.WEST;
	searchPanel.add(nameComboBox, constraints);
	JLabel locationLabel = new JLabel(LABEL_LOCATION);
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	searchPanel.add(locationLabel, constraints);
	Object[] locations = guiController.getAllLocations();
	final JComboBox locationComboBox = new JComboBox(locations);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	searchPanel.add(locationComboBox, constraints);

	// The results table with the found records
	JPanel tablePanel = new JPanel();
	tablePanel.setLayout(new BorderLayout());
	tablePanel.setBorder(BorderFactory
		.createTitledBorder(BORDER_TITLE_RECORDS));
	List<Record> records = new ArrayList<Record>();
	final JTable recordsTable = new JTable(new RecordsTableModel(records));
	recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	recordsTable.setAutoCreateRowSorter(true);
	JScrollPane scrollPane = new JScrollPane(recordsTable);
	tablePanel.add(scrollPane);

	// Perform a search after changing the values of the combo box for name
	// or location of the record to display
	nameComboBox.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		guiController.performSearchAction(nameComboBox,
			locationComboBox, recordsTable);
	    }
	});
	locationComboBox.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		guiController.performSearchAction(nameComboBox,
			locationComboBox, recordsTable);
	    }
	});

	// For booking you need a textfield and two buttons to booking and
	// releasing the selected records in the table
	JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	bookPanel
		.setBorder(BorderFactory.createTitledBorder(BORDER_TITLE_BOOK));
	constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 5, 5, 5);

	// The customer ID text field and the "book" button
	JLabel customerLabel = new JLabel(LABEL_CUSTOMERID);
	constraints.anchor = GridBagConstraints.EAST;
	bookPanel.add(customerLabel, constraints);
	final JTextField customerTextField = new JTextField(20);
	constraints.anchor = GridBagConstraints.WEST;
	bookPanel.add(customerTextField, constraints);
	JButton bookButton = new JButton(BUTTON_BOOK);
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	bookPanel.add(bookButton, constraints);
	bookButton.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		guiController.bookSelectedRecord(recordsTable,
			customerTextField.getText());
		// After booking a record the data changed. This requires a
		// reload.
		guiController.performSearchAction(nameComboBox,
			locationComboBox, recordsTable);
	    }

	});

	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
	contentPanel.add(searchPanel);
	contentPanel.add(bookPanel);
	contentPanel.add(tablePanel);

	// Buttons in LayoutFrame's button panel

	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.setBorder(LineBorder.createGrayLineBorder());
	final JButton disconnectButton = new JButton(BUTTON_DISCONNECT);
	buttonPanel.add(disconnectButton);

	// Disconnection results in closing the record frame and displaying the
	// client frame again.
	disconnectButton.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});

	// Initially start a search with the default search parameters
	guiController.performSearchAction(nameComboBox, locationComboBox,
		recordsTable);
    }

}
