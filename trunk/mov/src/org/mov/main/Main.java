package org.mov.main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.prefs.*;
import javax.swing.*;

import org.mov.util.*;
import org.mov.portfolio.*;
import org.mov.prefs.PreferencesManager;
import org.mov.quote.*;
import org.mov.ui.*;

/**
 * The top level class which contains the main() function. This class builds 
 * the outer frame and creates the desktop.
 */
public class Main extends JFrame implements WindowListener {
    
    private JDesktopPane desktop;
    private static MainMenu menu;
    private static Main venice;

    /**
     * Get the main frame for the current application
     * @returns The frame
     */
    public static JFrame getApplicationFrame() {
	return Main.venice;
    }

    // Go!
    private Main() {
	Preferences p = PreferencesManager.getUserNode("/display");
	setSize(p.getInt("default_width", 800),
		p.getInt("default_height", 600));
	setLocation(p.getInt("default_x", 0),
		    p.getInt("default_y", 0));
	setTitle("Venice");

	desktop = new JDesktopPane();
	desktop.setDesktopManager(new org.mov.ui.DesktopManager(desktop));
	CommandManager.getInstance().setDesktop(desktop);

	menu = new MainMenu(this, desktop);
	setContentPane(desktop);
	addWindowListener(this);
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
	// Save window dimensions in prefs file
	Preferences p = PreferencesManager.getUserNode("/display");
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

    /**
     * Start the application. Currently the application ignores all 
     * command line arguments.
     */
    public static void main(String[] args) {
	venice = new Main();

	venice.setVisible(true);
	SkinManager.loadSkin();
    }
}



