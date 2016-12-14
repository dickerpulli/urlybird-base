package suncertify.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.utils.Configuration;
import suncertify.utils.Configuration.ConfigurationKey;

/**
 * The panel for all remote client related input fields.
 */
public class RemoteClientPanel extends JPanel {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    private static final String LABEL_PORT = "Server port:";
    private static final String LABEL_HOSTNAME = "Server hostname or IP:";

    private JTextField hostnameTextField;

    private JTextField portTextField;

    /**
     * Constructor.
     */
    public RemoteClientPanel() {
	setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 5, 5, 5);

	// The server hostname to connect to
	JLabel hostnameLabel = new JLabel(LABEL_HOSTNAME);
	hostnameTextField = new JTextField(30);
	hostnameTextField.setName("hostnameTextField");
	hostnameTextField.setText(Configuration.getInstance().getValue(
		ConfigurationKey.CLIENT_SERVER_LOCATION));
	constraints.anchor = GridBagConstraints.EAST;
	add(hostnameLabel, constraints);
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	add(hostnameTextField, constraints);

	// The server port to connect to
	JLabel portLabel = new JLabel(LABEL_PORT);
	portTextField = new JTextField(10);
	portTextField.setName("portTextField");
	portTextField.setText(Configuration.getInstance().getValue(
		ConfigurationKey.CLIENT_SERVER_PORT));
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
	add(portLabel, constraints);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	add(portTextField, constraints);
    }

    /**
     * @return the hostnameTextField
     */
    public JTextField getHostnameTextField() {
	return hostnameTextField;
    }

    /**
     * @return the portTextField
     */
    public JTextField getPortTextField() {
	return portTextField;
    }

}
