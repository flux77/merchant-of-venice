package org.mov.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MenuHelper {

    /**
     * Creates a menu item and attaches it to a menu
     * @param parent The menu to attach the menu item to
     * @param title The title of the menu item
     */
    public static JMenuItem addMenuItem(ActionListener listener, 
					JMenuItem parent, String title) {
	return addMenuItem(listener, parent, title, (char)0);
    }

    /**
     * Creates a menu item and attaches it to a menu
     * @param parent The menu to attach the menu item to
     * @param title The title of the menu item
     * @param key Accelerator key
     */
    public static JMenuItem addMenuItem(ActionListener listener, 
					JMenuItem parent, String title, 
					char key) {
	JMenuItem menuItem = new JMenuItem(title);
	if (key != 0) {
	    KeyStroke keyStroke = 
		KeyStroke.getKeyStroke("ctrl " + key);

	    menuItem.setAccelerator(keyStroke);
	}
	menuItem.addActionListener(listener);
	parent.add(menuItem);

	return menuItem;
    } 

    /**
     * Creates a menu and attaches it to a component
     * @param parent The component to attach the menu to
     * @param title The title of the menu
     * @param key The accelerator key for the menu
     */
    public static JMenu addMenu(JComponent parent, String title, char key) {
	JMenu menu = new JMenu(title);
	if (key != 0)
	    menu.setMnemonic(key);
	parent.add(menu);
	
	return menu;
    }

    /**
     * Creates a menu and attaches it to a component
     * @param parent The component to attach the menu to
     * @param title The title of the menu
     */
    public static JMenu addMenu(JComponent parent, String title) {
	return addMenu(parent, title, (char)0);
    }
}
