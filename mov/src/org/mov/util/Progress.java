package org.mov.util;

// TODO: Finish

import java.beans.*;
import javax.swing.*;

public class Progress {

    private static Progress instance = null;
    private static JDesktopPane desktop = null;

    public static Progress getInstance() {	
	if(instance == null) {
	    instance = new Progress();
	}
	return instance;
    }
    
    public static void setDesktop(JDesktopPane desktop) {
	Progress.desktop = desktop;
    }

    private Progress() {
	// Nothing to do
    }

    public void show() {
	JProgressBar progressBar = 
	    new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
    
	JLabel label = new JLabel("Loading dates");


	Box box = Box.createVerticalBox();

	box.add(label);
	box.add(progressBar);

	JOptionPane pane = new JOptionPane(box,
					   JOptionPane.INFORMATION_MESSAGE,
					   JOptionPane.OK_CANCEL_OPTION);
			  
	JInternalFrame frame = 
	    pane.createInternalFrame(desktop, "Progress");

	frame.show();
	
	    /*
	super("Progress Window", false, false, false, true);
	setBounds(0, 0, 100, 100);
	

	JComponent c = (JComponent)getContentPane();
	c.add(new JProgressBar(JProgressBar.VERTICAL, 0, 100));

	super.show();
	    */
    }
    
    public void hide() {

    }

}
