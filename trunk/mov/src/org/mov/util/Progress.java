package org.mov.util;

import java.awt.*;
import java.beans.*;
import javax.swing.*;

/** 
 * Singleton progress window for displaying task progress to the user.
 * <p>
 * This class was written from scratch not using the ProgressMonitor class
 * since I had troubles getting it to reset its progress bar back to 0
 * to display another progress. (I could only achieve this by closing the
 * window down which I did not want to do.
 * <p>
 * Before this class is used the method <i>setDesktop()</i> must be called.
 * <p>
 * To open a progress bar for a simple one-off load operation:
 *
 * <pre>
 * // Open progress window (if it isnt already) and return whether we are
 * // the "owner" of the window or are just "borrowing" it.
 *
 * public void loadData() {
 *	boolean owner = Progress.getInstance().open("Loading something", 100);
 *
 *	for(int i = 0; i < 100; i++) {
 *		// do something
 *		...
 *
 *		// move progress bar along one
 *		Progress.getInstance().next();
 *	}
 *
 *	// This will close the window but only if we are the owner 
 *	Progress.getInstance().close(owner);
 *
 * }
 *
 * </pre>
 *
 * To open a progress window for a nested operation where there are multiple
 * progresses to report:
 * <pre>
 * boolean owner = Progress.getInstance().open();
 *
 * // Each of these calls, as defined above, will reset the progress window,
 * // but they will not close the progress window because they do not own
 * // it - we do.
 * loadData();
 * loadData();
 *
 * // This will close the window but only if we are the owner (which in this
 * // example we are).
 * Progress.getInstance().close(owner);
 *
 * </pre>
 */
public class Progress {

    private static Progress instance = null;

    private JDesktopPane desktop = null;
    private JInternalFrame frame = null;
    private JProgressBar progress = null;
    private JLabel label = null;

    // Prevent public construction
    private Progress() {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return	Single instance of class.
     */
    public static Progress getInstance() {	
	if(instance == null) {
	    instance = new Progress();
	}
	return instance;
    }

    /**
     * Set the desktop to display on. This must be called once before
     * any progress window is opened.
     *
     * @param	desktop	the desktop to display on.
     */
    public void setDesktop(JDesktopPane desktop) {
	this.desktop = desktop;
    }

    // Resets the message and progress bar value and maximum
    private void set(String message, int maximum) {

	progress.setMaximum(maximum);
	progress.setValue(0);
	label.setText(message);

	if(frame.isClosed() == true)
	    frame.show();
    }

    /**
     * Move the progress bar along one. 
     */
    public void next() {
	if(frame != null)
	    progress.setValue(progress.getValue() + 1);	
    }

    /**
     * Try to claim ownership of the progress window but do not open it
     * yet. Do not open it yet - since we do not have any progress to report 
     * (following calls to open will declare the size and message).
     *
     * @return	whether we have ownership of the window or not.
     */
    public boolean open() {
	return open("", 0);
    }

    /**
     * Try to claim ownership of the progress window. If we manage to
     * claim ownership the window will be opened with the given message
     * and the progress bar set to 0 and maximum set to the given value.
     * <p>
     * If the progress window is already in use, the message and maximum
     * progress bar value will be changed to the given values. The progress
     * bar will also be reset back to 0.
     *
     * @param	message	the message to display to the user.
     * @param	maximum	the maximum value of the progress bar.
     * @return	whether we are the owner of the window. 
     */
    public boolean open(String message, int maximum) {

	if(frame == null) {
	    progress = 
		new JProgressBar(JProgressBar.HORIZONTAL, 0, maximum);
	    label = new JLabel(message);	    
	    	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());
	    panel.add(label, BorderLayout.NORTH);
	    panel.add(progress, BorderLayout.CENTER);
	    
	    JOptionPane pane = new JOptionPane(panel,
					       JOptionPane.INFORMATION_MESSAGE,
					       JOptionPane.OK_CANCEL_OPTION);
	    
	    frame = 
		pane.createInternalFrame(desktop, "Progress");
	    
	    // Someone is claiming ownershp of the progress bar but isnt
	    // ready for display yet
	    //	    if(maximum != 0)
	    frame.show();

	    return true;
	}
	else {
	    set(message, maximum);

	    return false;
	}
    }
    
    /**
     * Close the progress window if we are the owner.
     *
     * @param	owner	if we are the owner of the window or not.
     */
    public void close(boolean owner) {
	if(owner && frame != null) {
	    try {
		frame.setClosed(true);
		frame = null;
	    }
	    catch(PropertyVetoException e) {
		// shouldnt happen
	    }
	}
    }
}
