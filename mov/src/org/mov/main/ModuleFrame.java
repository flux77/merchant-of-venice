package org.mov.main;

import java.awt.Dimension;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import org.mov.ui.DesktopManager;

/**
 * An internal frame designed specifically for holding Module objects.  Every visible Module should run within
 * an ModuleFrame, and is supplied to it upon construction.
 */
public class ModuleFrame extends JInternalFrame
    implements PropertyChangeListener, InternalFrameListener
{
    // Property indicating window should be closed
    public static final String WINDOW_CLOSE_PROPERTY = "window close";

    // Property indicating title bar has changed
    public static final String TITLEBAR_CHANGED_PROPERTY = "titlebar changed";

    private final static int DEFAULT_LAYER = 2;

    // Each module frame contains a single module
    private Module module;

    // Preferred width and height of frame
    static private int DEFAULT_FRAME_WIDTH = 535;
    static private int DEFAULT_FRAME_HEIGHT = 475;

    /**
     * Construct a new frame around the given module and display.
     *
     * @param module The module to feed to the frame
     * @param isPreferences	Is the frame a preferences frame that should
     *				be centred and size honoured?
     */
    public ModuleFrame(Module module, 
		       boolean isPreferences) {

	// Resizable, closable etc
	super(module.getTitle(), true, true, true, true);

	this.module = module;

	JDesktopPane desktop = DesktopManager.getDesktop();

	// Module can be enclosed in scroll pane if it desires to be
	if(module.encloseInScrollPane()) 
	    getContentPane().add(new JScrollPane(module.getComponent()));
	else
	    getContentPane().add(module.getComponent());

	setSizeAndLocation(desktop, isPreferences);

	if(module.getJMenuBar() != null)
	    setJMenuBar(module.getJMenuBar());

	// Listen to events from module
	module.addModuleChangeListener(this);

	super.setFrameIcon(module.getFrameIcon());

	// We want to notify module when it is closing so it can save data
	addInternalFrameListener(this);

	show();	
    }

    // Set the size and location of the new frame, taking care of out of
    // bounds frames.
    private void setSizeAndLocation(JDesktopPane desktop, 
				    boolean isPreferences) {
	int x, y, width, height;

	// Preference window is centred and custom size
	if(isPreferences) {
	    Dimension preferred = getPreferredSize();	    
	    x = (desktop.getWidth() - preferred.width) / 2;
	    y = (desktop.getHeight() - preferred.height) / 2;
	    width = preferred.width;
	    height = preferred.height;
	}

	// Main windows are set at (0, 0) and set to a fixed size
	else {
	    x = 0;
	    y = 0;
	    width = DEFAULT_FRAME_WIDTH;
	    height = DEFAULT_FRAME_HEIGHT;
	}

	// Make sure new frame is within window bounds
	if(x > desktop.getWidth())
	    x = desktop.getWidth() - width;
	if(y > desktop.getHeight())
	    y = desktop.getWidth() - height;
	if(x < 0) 
	    x = 0;
	if(y < 0)
	    y = 0;
	if(width > x + desktop.getWidth())
	    width = desktop.getWidth() - x;
	if(height > y + desktop.getHeight())
	    height = desktop.getHeight() - y;

	// Set size and location
	setBounds(x, y, width, height);
    }

    /**
     * Gives a reference to the module running inside the ModuleFrame
     *
     * @return The module running in the frame
     */
    public Module getModule() {
	return module;
    }

    /** 
     * Standard property change handler that listens for a WINDOW_CLOSE event 
     */
    public void propertyChange(PropertyChangeEvent event) {
	String property = event.getPropertyName();

	if(property.equals(WINDOW_CLOSE_PROPERTY))
	    dispose();
    }


    /* Make sure the internal modules saves its information before destroying it
     */
    public void internalFrameClosed(InternalFrameEvent e) { 
	module.save();
    }
    /**
     * Standard InternalFrame functions
     */
    public void internalFrameActivated(InternalFrameEvent e) { }
    public void internalFrameClosing(InternalFrameEvent e) { }
    public void internalFrameDeactivated(InternalFrameEvent e) { }
    public void internalFrameDeiconified(InternalFrameEvent e) { }
    public void internalFrameIconified(InternalFrameEvent e) { }
    public void internalFrameOpened(InternalFrameEvent e) { }


    
}


