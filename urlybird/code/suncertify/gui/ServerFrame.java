package suncertify.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import suncertify.gui.AlignedPanel.Alignment;
import suncertify.utils.Configuration;
import suncertify.utils.Configuration.ConfigurationKey;

/**
 * The server frame to start and stop the UrlyBird server.
 */
public class ServerFrame extends LayoutFrame {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    private static final String BUTTON_STOP = "Stop";
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_BROWSE = "Browse...";
    private static final String LABEL_PORT = "Local server port:";
    private static final String LABEL_DATABASE = "Database location:";
    private static final String BORDER_TITLE = "Server options";
    private static final String TITLE = "UrlyBird";

    /**
     * Constructor.
     * 
     * @param guiController
     *            The controller for business access.
     */
    public ServerFrame(GuiController guiController) {
	super(guiController);
	setTitle(TITLE);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initComponents(Object... initParams) {
	AlignedPanel alignedOptionsPanel = new AlignedPanel(Alignment.LEFT,
		new GridBagLayout());
	alignedOptionsPanel.setBorder(BorderFactory
		.createTitledBorder(BORDER_TITLE));
	JPanel optionsPanel = alignedOptionsPanel.getInternalPanel();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 5, 5, 5);

	// Database location text field
	JLabel dbLabel = new JLabel(LABEL_DATABASE);
	final JTextField dbTextField = new JTextField(30);
	dbTextField.setName("dbTextField");
	dbTextField.setText(Configuration.getInstance().getValue(
		ConfigurationKey.SERVER_DATABASE_LOCATION));
	final BrowseButton browseButton = new BrowseButton(BUTTON_BROWSE, this,
		JFileChooser.FILES_ONLY, null, dbTextField);
	constraints.anchor = GridBagConstraints.EAST;
	optionsPanel.add(dbLabel, constraints);
	optionsPanel.add(dbTextField, constraints);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	optionsPanel.add(browseButton, constraints);

	// Port text field
	JLabel portLabel = new JLabel(LABEL_PORT);
	final JTextField portTextField = new JTextField(10);
	portTextField.setName("portTextField");
	portTextField.setText(Configuration.getInstance().getValue(
		ConfigurationKey.SERVER_PORT));
	portTextField.addKeyListener(new KeyAdapter() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void keyTyped(KeyEvent e) {
		// Allow only numbers - any other character is ignored.
		try {
		    Integer.parseInt("" + e.getKeyChar());
		} catch (NumberFormatException e1) {
		    e.consume();
		}
	    }
	});
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	optionsPanel.add(portLabel, constraints);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	optionsPanel.add(portTextField, constraints);
	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
	contentPanel.add(alignedOptionsPanel);

	// Buttons to start and stop the server
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.setBorder(LineBorder.createGrayLineBorder());
	final JButton startButton = new JButton(BUTTON_START);
	buttonPanel.add(startButton);
	final JButton stopButton = new JButton(BUTTON_STOP);
	stopButton.setEnabled(false);
	buttonPanel.add(stopButton);

	// Start the server and disable the start button
	startButton.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		String port = portTextField.getText();
		String dbLocation = dbTextField.getText();
		if (guiController.startServer(dbLocation, port)) {
		    // Server is successfully started, disable start button and
		    // input fields and enable stop button
		    startButton.setEnabled(false);
		    stopButton.setEnabled(true);
		    dbTextField.setEnabled(false);
		    portTextField.setEnabled(false);
		    browseButton.setEnabled(false);
		}
	    }
	});

	// Stop the server and enable the start button again
	stopButton.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (guiController.stopServer()) {
		    // Server is successfully stopped, disable stop button and
		    // enable start button and all input fields
		    startButton.setEnabled(true);
		    stopButton.setEnabled(false);
		    dbTextField.setEnabled(true);
		    portTextField.setEnabled(true);
		    browseButton.setEnabled(true);
		}
	    }
	});

	// Save all inputs on close
	addWindowListener(new WindowAdapter() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void windowClosing(WindowEvent e) {
		Configuration.getInstance().setValue(
			ConfigurationKey.SERVER_DATABASE_LOCATION,
			dbTextField.getText());
		Configuration.getInstance().setValue(
			ConfigurationKey.SERVER_PORT, portTextField.getText());
		// Save the changed data in the properties file
		Configuration.getInstance().save();
	    }
	});
    }
}
