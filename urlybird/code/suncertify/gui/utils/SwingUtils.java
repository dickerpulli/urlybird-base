package suncertify.gui.utils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Utilities for Swing objects.
 */
public final class SwingUtils {

    /**
     * Centers a given window on the screen.
     * 
     * @param window
     *            The window, frame or dialog
     */
    public static void centerOnScreen(Window window) {
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	int x = (int) ((d.getWidth() - window.getWidth()) / 2);
	int y = (int) ((d.getHeight() - window.getHeight()) / 2);
	window.setLocation(x, y);
    }

    private SwingUtils() {
	// utility class
    }

}
