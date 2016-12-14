package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * A frame that defines a default main layout for all frames of the application.
 * It holds a content panel, a button panel and a logging panel.
 */
public abstract class LayoutFrame extends JFrame {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    /** The GUI controller encapsulates the business calls. */
    protected GuiController guiController;

    /** The panel for all the content. */
    protected JPanel contentPanel;

    /** The panel for any buttons. */
    protected JPanel buttonPanel;

    /**
     * The default construtor.
     * 
     * @param guiController
     *            The GUI controller.
     * @param initParams
     *            Optional parameters for initialization
     */
    public LayoutFrame(GuiController guiController, Object... initParams) {
	this.guiController = guiController;
	initComponentsInternal();
	initComponents(initParams);
	pack();
    }

    /**
     * Initializes the frame and arranges its components.
     * 
     * @param initParams
     *            Optional parameters for initialization
     */
    protected abstract void initComponents(Object... initParams);

    /**
     * Initializes the main components of the layout frame.
     */
    private void initComponentsInternal() {
	setLayout(new BorderLayout());
	contentPanel = new JPanel();
	JPanel addonPanel = new JPanel();
	JPanel statusBar = new JPanel();
	add(contentPanel, BorderLayout.CENTER);
	add(addonPanel, BorderLayout.SOUTH);

	// Buttons are in a separate panel
	buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.setBorder(LineBorder.createGrayLineBorder());

	// The status is shown in a bottom panel
	statusBar.setBorder(LineBorder.createGrayLineBorder());
	statusBar.setLayout(new BorderLayout());
	JLabel statusLabel = new JLabel(" ");
	statusLabel.setName("statusLabel");
	statusBar.add(statusLabel);
	String className = this.getClass().getName();
	Logger logger = Logger.getLogger(className.substring(0,
		className.indexOf(".")));
	StatusBarHandler handler = new StatusBarHandler(statusLabel);
	handler.setLevel(Level.INFO);
	logger.addHandler(handler);
	logger.setLevel(Level.INFO);

	// Layout both panels in Y-axis
	addonPanel.setLayout(new BoxLayout(addonPanel, BoxLayout.Y_AXIS));
	addonPanel.add(buttonPanel);
	addonPanel.add(statusBar);
    }

}
