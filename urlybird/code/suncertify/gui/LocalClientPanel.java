package suncertify.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.utils.Configuration;
import suncertify.utils.Configuration.ConfigurationKey;

/**
 * The panel for all local client related input fields.
 */
public class LocalClientPanel extends JPanel {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    private static final String BUTTON_BROWSE = "Browse...";
    private static final String LABEL_DATABASE = "Database location:";

    private JTextField dbLocationTextField;

    /**
     * Construct.
     */
    public LocalClientPanel() {
	setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 5, 5, 5);

	// The local database location
	JLabel dbLocationLabel = new JLabel(LABEL_DATABASE);
	dbLocationTextField = new JTextField(30);
	dbLocationTextField.setName("dbLocationTextField");
	dbLocationTextField.setText(Configuration.getInstance().getValue(
		ConfigurationKey.CLIENT_DATABASE_LOCATION));
	BrowseButton browseButton = new BrowseButton(BUTTON_BROWSE, this,
		JFileChooser.FILES_ONLY, null, dbLocationTextField);
	constraints.anchor = GridBagConstraints.EAST;
	add(dbLocationLabel, constraints);
	add(dbLocationTextField, constraints);
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	add(browseButton, constraints);
    }

    /**
     * @return the dbLocationTextField
     */
    public JTextField getDbLocationTextField() {
	return dbLocationTextField;
    }

}
