package org.mov.main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;
import javax.swing.*;

import org.mov.util.*;
import org.mov.ui.*;

public class Analyser extends JFrame implements WindowListener {
    
    private static final String PREFS_FILE = "prefs.xml";
    private JDesktopPane desktop;
    private AnalyserMenu menu;

    public Analyser() {
	loadPreferences();
	setTitle("Venice");
	Preferences p = Preferences.userRoot().node("/display");
	setSize(p.getInt("default_width", 800),
		p.getInt("default_height", 600));
	setLocation(p.getInt("default_x", 0),
		    p.getInt("default_y", 0));

	desktop = new JDesktopPane();
	desktop.setDesktopManager(new AnalyserDesktopManager(desktop));
	Progress.getInstance().setDesktop(desktop);
	menu = new AnalyserMenu(this, desktop);
	setContentPane(desktop);
	addWindowListener(this);
    }

    private void loadPreferences() {
	// Read in the system preferences
	try {
	    java.net.URL prefs_url = 
		ClassLoader.getSystemClassLoader().getResource(PREFS_FILE);
	    if (prefs_url != null) {
		InputStream is = prefs_url.openStream();
		Preferences.importPreferences(is);
		is.close();
	    }
	} catch (java.io.IOException ioe) {
	    System.err.println("IO Exception thrown while opening "+
			       PREFS_FILE+
			       ":\n"+
			       ioe.getMessage());
	} catch (java.util.prefs.InvalidPreferencesFormatException ipfe) {
	    System.err.println("Invalid Preferences format in "+
			       PREFS_FILE+
			       ":\n"+
			       ipfe.getMessage());
	}
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
	// Save window dimensions in prefs file
	Preferences p = Preferences.userRoot().node("/display");
	p.putInt("default_width", getWidth());
	p.putInt("default_height", getHeight());
	p.putInt("default_x", getX());
	p.putInt("default_y", getY());

	dispose();	
	System.exit(0);
    }
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    public static void main(String[] args) {
	Analyser analyser = new Analyser();

	analyser.setVisible(true);
    }
}


