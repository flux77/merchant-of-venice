/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
*/

package org.mov.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MenuHelper {

    /**
     * Creates a check box menu item and attaches it to a menu
     * @param parent The menu to attach the menu item to
     * @param title The title of the menu item
     */
    public static JCheckBoxMenuItem addCheckBoxMenuItem(ActionListener 
							listener, 
							JMenuItem parent, 
							String title) {

	JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(title);
	menuItem.addActionListener(listener);
	parent.add(menuItem);

	return menuItem;
    }

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
