package org.mov.main;

import java.awt.*;
import java.io.InputStream;
import java.util.prefs.*;
import javax.swing.*;

import org.mov.util.*;

public class Analyser extends JFrame {
    
    private static final String PREFS_FILE = "prefs.xml";
    private JDesktopPane desktop;
    private AnalyserMenu menu;

    public Analyser() {
	// Read in the system preferences
	try {
	    java.net.URL prefs_url = ClassLoader.getSystemClassLoader().getResource(PREFS_FILE);
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

	setTitle("Venice");
	Preferences p = Preferences.userRoot().node("/display");
	setSize(p.getInt("default_width", 800),
		p.getInt("default_height", 600));

	setDefaultCloseOperation(EXIT_ON_CLOSE);
	desktop = new JDesktopPane();
	menu = new AnalyserMenu(this, desktop);
	setContentPane(desktop);
	Progress.setDesktop(desktop);
    }

    public static void main(String[] args) {
	Analyser analyser = new Analyser();

	analyser.setVisible(true);
    }
}

