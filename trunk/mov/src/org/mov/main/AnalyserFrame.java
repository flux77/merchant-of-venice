package org.mov.main;

import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;

public class AnalyserFrame extends JInternalFrame
    implements PropertyChangeListener, InternalFrameListener
{
    // Property indicating window should be closed
    public static final String WINDOW_CLOSE_PROPERTY = "window close";

    // Property indicating title bar has changed
    public static final String TITLEBAR_CHANGED_PROPERTY = "titlebar changed";

    private final static int DEFAULT_LAYER = 2;

    private AnalyserModule module;

    public AnalyserFrame(AnalyserModule module,
			 int x, int y, int width, int height) {

	// Resizable, closable etc
	super(module.getTitle(), true, true, true, true);

	this.module = module;

	// Module can be enclosed in scroll pane if it desires to be
	if(module.encloseInScrollPane()) 
	    getContentPane().add(new JScrollPane(module.getComponent()));
	else
	    getContentPane().add(module.getComponent());

	setBounds(x, y, width, height);
	if(module.getJMenuBar() != null)
	    setJMenuBar(module.getJMenuBar());

	// Listen to events from module
	module.addModuleChangeListener(this);

	super.setFrameIcon(module.getFrameIcon());

	// We want to notify module when it is closing so it can save data
	addInternalFrameListener(this);

	show();	
    }

    public void propertyChange(PropertyChangeEvent event) {
	String property = event.getPropertyName();

	if(property.equals(WINDOW_CLOSE_PROPERTY))
	    dispose();
    }

    public void internalFrameActivated(InternalFrameEvent e) { }
    public void internalFrameClosed(InternalFrameEvent e) { 
	module.save();
    }
    public void internalFrameClosing(InternalFrameEvent e) { }
    public void internalFrameDeactivated(InternalFrameEvent e) { }
    public void internalFrameDeiconified(InternalFrameEvent e) { }
    public void internalFrameIconified(InternalFrameEvent e) { }
    public void internalFrameOpened(InternalFrameEvent e) { }


    
}

