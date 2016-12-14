package suncertify.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * Button for browsing in file system. The click action on this button opens a
 * JFileChooser dialog to browse.
 */
public class BrowseButton extends JButton {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Constuctor.
     * 
     * @param label
     *            The label
     * @param parent
     *            The parent frame
     * @param fileSelectionMode
     *            The mode, see {@link JFileChooser}
     * @param dir
     *            The start directory
     * @param relatedTextField
     *            The related text field, that will be written after choosing
     *            file/dir
     */
    public BrowseButton(String label, final Component parent,
	    final int fileSelectionMode, final String dir,
	    final JTextField relatedTextField) {
	super(label);
	addActionListener(new ActionListener() {

	    /**
	     * {@inheritDoc}
	     */
	    public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setFileSelectionMode(fileSelectionMode);
		chooser.setMultiSelectionEnabled(false);
		int option = chooser.showOpenDialog(parent);
		if (option == JFileChooser.APPROVE_OPTION) {
		    relatedTextField.setText(chooser.getSelectedFile()
			    .getAbsolutePath());
		}
	    }
	});
    }

}
