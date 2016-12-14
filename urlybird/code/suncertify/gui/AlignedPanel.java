package suncertify.gui;

import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Aligned panel. A panel that helps to align a whole panel to the center or the
 * left or right border of another JPanel.
 */
public class AlignedPanel extends JPanel {

    /** default id for serialization. */
    private static final long serialVersionUID = 1L;

    /** Internal panel. */
    private JPanel internalPanel;

    /**
     * The provided alignments of the panel.
     */
    public enum Alignment {
	/** Aligned on the left. */
	LEFT,
	/** Aligned on the right. */
	RIGHT,
	/** Aligned in the middle. */
	CENTER
    }

    /**
     * Constructor.
     * 
     * @param alignment
     *            The alignment of the panel.
     * @param layoutManager
     *            The internal layout manager.
     */
    public AlignedPanel(Alignment alignment, LayoutManager layoutManager) {
	int flow;
	switch (alignment) {
	case LEFT:
	    flow = FlowLayout.LEFT;
	    break;
	case CENTER:
	    flow = FlowLayout.CENTER;
	    break;
	case RIGHT:
	    flow = FlowLayout.RIGHT;
	    break;
	default:
	    throw new EnumConstantNotPresentException(Alignment.class,
		    alignment.toString());
	}
	setLayout(new FlowLayout(flow));
	internalPanel = new JPanel(layoutManager);
	add(internalPanel);
    }

    /**
     * Returns the internal aligned panel.
     * 
     * @return The panel
     */
    public JPanel getInternalPanel() {
	return internalPanel;
    }

}
