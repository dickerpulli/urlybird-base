package suncertify.gui;

import java.awt.Color;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JLabel;

/**
 * A logging handler that writes it's output to the internal JLabel.
 */
public class StatusBarHandler extends Handler {

    /** The label to write. */
    private JLabel statusLabel;

    /**
     * Constructor.
     * 
     * @param statusLabel
     *            The label to write
     */
    public StatusBarHandler(JLabel statusLabel) {
	this.statusLabel = statusLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record) {
	// Set the color of the message dependent on the severity
	if (record.getLevel() == Level.SEVERE) {
	    statusLabel.setForeground(Color.RED);
	} else if (record.getLevel() == Level.WARNING) {
	    statusLabel.setForeground(Color.BLUE);
	} else {
	    statusLabel.setForeground(Color.BLACK);
	}
	statusLabel.setText(record.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
	// Nothing to flush
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SecurityException {
	// Nothing to close
    }

}
