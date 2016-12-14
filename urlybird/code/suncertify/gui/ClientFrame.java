package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import suncertify.gui.AlignedPanel.Alignment;
import suncertify.utils.Configuration;
import suncertify.utils.Configuration.ConfigurationKey;

/**
 * The client frame to connect to the UrlyBird server.
 */
public class ClientFrame extends LayoutFrame {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    private static final String BUTTON_CONNECT = "Connect";
    private static final String TITLE = "UrlyBird Client";
    private static final String BORDER_TITLE = "Client options";

    /**
     * Constructor.
     */
    public ClientFrame(GuiController guiController, ConnectionMode mode) {
	super(guiController, mode);
	setTitle(TITLE);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initComponents(Object... initParams) {
	// Content is in separated JPanels dependent on the connection mode
	final ConnectionMode mode = (ConnectionMode) initParams[0];
	final JPanel specificContentPanel;
	switch (mode) {
	case LOCAL:
	    specificContentPanel = new LocalClientPanel();
	    break;
	case REMOTE:
	    specificContentPanel = new RemoteClientPanel();
	    break;
	default:
	    throw new EnumConstantNotPresentException(ConnectionMode.class,
		    mode.toString());
	}
	AlignedPanel alignedPanel = new AlignedPanel(Alignment.LEFT,
		new BorderLayout());
	alignedPanel.setBorder(BorderFactory.createTitledBorder(BORDER_TITLE));
	JPanel optionsPanel = alignedPanel.getInternalPanel();
	optionsPanel.add(specificContentPanel);
	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
	contentPanel.add(alignedPanel);

	// The connect button for the client
	final JButton connectButton = new JButton(BUTTON_CONNECT);
	buttonPanel.add(connectButton);

	// Actions on the buttons are depending on the connection mode because
	// the parameters vary on this mode.
	final JFrame thisFrame = this;
	connectButton.addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void actionPerformed(ActionEvent e) {
		switch (mode) {
		case LOCAL:
		    String dbLocation = ((LocalClientPanel) specificContentPanel)
			    .getDbLocationTextField().getText();
		    guiController.connectLocal(dbLocation, thisFrame);
		    break;
		case REMOTE:
		    String port = ((RemoteClientPanel) specificContentPanel)
			    .getPortTextField().getText();
		    String hostname = ((RemoteClientPanel) specificContentPanel)
			    .getHostnameTextField().getText();
		    guiController.connectServer(hostname, port, thisFrame);
		    break;
		default:
		    throw new EnumConstantNotPresentException(
			    ConnectionMode.class, mode.toString());
		}
	    }
	});

	// Save input on close depending on what text fields are shown in the
	// connection mode
	addWindowListener(new WindowAdapter() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void windowClosing(WindowEvent e) {
		switch (mode) {
		case LOCAL:
		    Configuration.getInstance().setValue(
			    ConfigurationKey.CLIENT_DATABASE_LOCATION,
			    ((LocalClientPanel) specificContentPanel)
				    .getDbLocationTextField().getText());
		    break;
		case REMOTE:
		    Configuration.getInstance().setValue(
			    ConfigurationKey.CLIENT_SERVER_LOCATION,
			    ((RemoteClientPanel) specificContentPanel)
				    .getHostnameTextField().getText());
		    Configuration.getInstance().setValue(
			    ConfigurationKey.CLIENT_SERVER_PORT,
			    ((RemoteClientPanel) specificContentPanel)
				    .getPortTextField().getText());
		    break;
		default:
		    throw new EnumConstantNotPresentException(
			    ConnectionMode.class, mode.toString());
		}
		// Save the changed data in the properties file
		Configuration.getInstance().save();
	    }
	});
    }

}
