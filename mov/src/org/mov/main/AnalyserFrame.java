package org.mov.main;

import java.beans.*;
import javax.swing.*;

public class AnalyserFrame extends JInternalFrame
    implements PropertyChangeListener
{
    // Property indicating window should be closed
    public static final String WINDOW_CLOSE_PROPERTY = "window close";

    // Property indicating title bar has changed
    public static final String TITLEBAR_CHANGED_PROPERTY = "titlebar changed";

    private final static int DEFAULT_LAYER = 2;

    public AnalyserFrame(AnalyserModule module,
			 int x, int y, int width, int height) {

	// Resizable, closable etc
	super(module.getTitle(), true, true, true, true);

	// Module can be enclosed in scroll pane if it desires to be
	if(module.encloseInScrollPane()) 
	    getContentPane().add(new JScrollPane(module.getComponent()));
	else
	    getContentPane().add(module.getComponent());

	setBounds(x, y, width, height);
	if(module.getJMenuBar() != null)
	    setJMenuBar(module.getJMenuBar());

	// Listen to events from module
	module.addPropertyChangeListener(this);

	super.setFrameIcon(module.getFrameIcon());
	show();	
    }

    public void propertyChange(PropertyChangeEvent event) {
	String property = event.getPropertyName();

	if(property.equals(WINDOW_CLOSE_PROPERTY))
	    dispose();
    }
}

