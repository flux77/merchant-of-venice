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

import java.awt.Point;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.mov.main.CommandManager;
import org.mov.prefs.PreferencesManager;
import org.mov.prefs.PreferencesModule;
import org.mov.prefs.StoredEquation;
import org.mov.util.Locale;

/**
 * Extension of JComboBox used for displaying an editable equation field
 * in the venice application. This ComboBox allows the user to select
 * equations entered in the Functions preferences page. This way the user
 * does not have to keep typing in the same equations multiple times.
 *
 * <pre>
 *	EquationComboBox comboBox = new EquationComboBox();
 *	panel.add(comboBox);
 * </pre>
 */
public class EquationComboBox extends JComboBox implements PopupMenuListener {

    // Array of user's stored equations, entered via the prefs page or elsewhere
    static List storedEquations;

    private boolean isDialogUp = false;
    private JTextField textField;

    /**
     * Create a new equation combo box.
     */
    public EquationComboBox() {
	this("");
    }

    /**
     * Create a new equation combo box with the given default equation
     * text.
     *
     * @param	equationText	equation text to display
     */
    public EquationComboBox(String equationText) {
	super();

	setEditable(true);

	// We have to load in the stored equations from preferences
	// before we set the first equation - as it may have a name
	// Only do this the first time we are constructed - the stored
	// equations are not stored per instance
	if(storedEquations == null)
	    updateEquations();

	setEquationText(equationText);

        // The combo box must be big enough to hold this text. This makes it
        // as wide as the equation combo box. Yes but on 1.4.1 it makes them short!
        //	setPrototypeDisplayValue("avg(day_close, 15, 15) > 121");

	// We want to know just before the popup items become visible
	// so we can update them
	addPopupMenuListener(this);

	// Locate the JTextField so we can catch menu events on it and
	// also so we can read the text directly from it.
        for(int i = 0; i < getComponentCount(); i++) {
            Component component = getComponent(i);

            if(component instanceof JTextField)
		textField = (JTextField)component;
	}

        // We want to catch right mouse buttons on the text field
	textField.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent event) {
		    handleMouseClicked(event);
		}
	    });

	updateItems();
   }

    /**
     * Return the equation string in the ComboBox. If a name of an
     * equation is in the box then its equation will be returned.
     *
     * @return	equation string
     */
    public String getEquationText() {

	// Get text displayed in combo box
	String text = getText();

	// Check to see if its a stored equation name - if it is
	// well return the actual equation - not its name
	StoredEquation storedEquation = findStoredEquationByName(text);

	if(storedEquation != null)
	    return storedEquation.equation;
	else
	    return text;
    }

    /**
     * Return whether the current displayed equation is a stored equation.
     * A stored equation is one the user has entered and can refer
     * to by using a keyword.
     *
     * @return <code>true</code> if it is a stored equation.
     */
    public boolean isStoredEquation() {
	return findStoredEquationByName(getText()) != null;
    }

    /**
     * Set the equation string in the ComboBox. If the given equation
     * has a name, then the name will be displayed in the comboBox
     * instead of the equation.
     *
     * @param	equationText	equation text to display
     */
    public void setEquationText(String equationText) {
	// Check to see if the equation has a name. If it has then
	// display the name instead of the equation
	StoredEquation storedEquation = findStoredEquationByEquation(equationText);
	
	if(storedEquation != null)
	    setSelectedItem(storedEquation.name);
	else
	    setSelectedItem(equationText);
    }

    /**
     * Tell all equation ComboBoxes that the stored equations have
     * been modified by the user and that their popup menus need to be
     * changed.
     */
    public static void updateEquations() {
	// Load equations from preferences
	storedEquations = PreferencesManager.loadStoredEquations();
    }

    // Searches through list of equations for the one with the given name
    private StoredEquation findStoredEquationByName(String name) {
	for(Iterator iterator = storedEquations.iterator(); iterator.hasNext();) {
	    StoredEquation storedEquation = (StoredEquation)iterator.next();
	    if(storedEquation.name.equals(name))
		return storedEquation;
	}

	// If we got here we couldn't find it
	return null;
    }

    // Searches through list of equations for the one with the given equation
    private StoredEquation findStoredEquationByEquation(String equation) {
	for(Iterator iterator = storedEquations.iterator(); iterator.hasNext();) {
	    StoredEquation storedEquation = (StoredEquation)iterator.next();
	    if(storedEquation.equation.equals(equation))
		return storedEquation;
	}

	// If we got here we couldn't find it
	return null;
    }

    // Rebuild option items in this combo box
    private void updateItems() {
	// First construct a new menu that begins with the current equation
	// shown, then a sorted list of all the stored equations. If the
	// current equation is a stored equation, make sure we don't show it
	// twice.

	// Construct menu items
	List menuItems = new ArrayList();

	String current = getText();
	menuItems.add(current);

	List stored = new ArrayList();
	
	for(Iterator iterator = storedEquations.iterator(); iterator.hasNext();) {
	    StoredEquation storedEquation = (StoredEquation)iterator.next();

	    if(!storedEquation.name.equals(current))
		stored.add(storedEquation.name);
	}

	Collections.sort(stored);
	menuItems.addAll(stored);

	// Remove previous menu items
	removeAllItems();

	// Display new menu items
	for(Iterator iterator = menuItems.iterator(); iterator.hasNext();)
	    addItem((String)iterator.next());
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
	// nothing to do
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	// nothing to do
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	// Make sure menu is up to date
	updateItems();
    }

    private void showAddDialog() {
        Thread thread = new Thread(new Runnable() {
                public void run() {
                    if(!isDialogUp) {
                        isDialogUp = true;

			StoredEquation storedEquation = 
                            ExpressionEditorDialog.showAddDialog(storedEquations, 
								 Locale.getString("ADD_EQUATION"),
								 getText());

			if(storedEquation != null) {
			    setEquationText(storedEquation.name);
			    storedEquations.add(storedEquation);
			    PreferencesManager.saveStoredEquations(storedEquations);
			}

                        isDialogUp = false;
                    }
                }});
                                   
        thread.start();
    }

    private void showDeleteDialog() {
	if(!isDialogUp) {
	    isDialogUp = true;

	    StoredEquation storedEquation = findStoredEquationByName(getText());

	    if(storedEquation != null) {
		int option = 
		    JOptionPane.showInternalConfirmDialog(DesktopManager.getDesktop(),
							  Locale.getString("SURE_DELETE_EQUATION",
									   getText()),
							  Locale.getString("DELETE_EQUATION"),
							  JOptionPane.YES_NO_OPTION);
		if(option == JOptionPane.YES_OPTION) {
		    storedEquations.remove(storedEquation);
		    PreferencesManager.saveStoredEquations(storedEquations);
		    setEquationText("");
		}       
	    }		

	    isDialogUp = false;
	}       
    }

    private void showEditDialog() {
        Thread thread = new Thread(new Runnable() {
                public void run() {
                    if(!isDialogUp) {
                        isDialogUp = true;

			if(isStoredEquation()) {
			    // Edit the stored equation - provides an equation
			    // name field as well as the equation field.
			    StoredEquation storedEquation = 
				findStoredEquationByName(getText());
			    if (storedEquation != null) {
				storedEquation = 
				    ExpressionEditorDialog.showEditDialog(storedEquations,
									  Locale.getString("EDIT_EQUATION"),
									  storedEquation);
				setEquationText(storedEquation.equation);
				PreferencesManager.saveStoredEquations(storedEquations);
			    }
			}
			else {
			    // Edit the equation - but do not provide an
			    // equation name field as this isn't a stored
			    // equation.
			    String equationText = getEquationText();
			    String newEquationText = 
				ExpressionEditorDialog.showEditDialog(Locale.getString("EDIT_EQUATION"),
								      equationText);

			    setEquationText(newEquationText);
			}
                        isDialogUp = false;
                    }
                }});
                                   
        thread.start();
    }

    // When we want to read the text displayed in this widget we read
    // directly from the textfield. We do this because the getSelectedItem()
    // function sometimes does not return the currently displayed text.
    private String getText() {
	return textField.getText();
    }

    private void handleMouseClicked(final MouseEvent event) {

        // Right click on the table - raise menu
        if(event.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu menu = new JPopupMenu();
            
            JMenuItem editMenuItem = new JMenuItem(Locale.getString("EDIT"));
            editMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showEditDialog();
                    }});
            
            menu.add(editMenuItem);

            boolean isStoredEquation = isStoredEquation();

            JMenuItem addMenuItem = new JMenuItem(Locale.getString("ADD"));
            addMenuItem.setEnabled(!isStoredEquation);
            addMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			showAddDialog();
                    }});
            menu.add(addMenuItem);
            
            JMenuItem deleteMenuItem = new JMenuItem(Locale.getString("DELETE"));
            deleteMenuItem.setEnabled(isStoredEquation);
            deleteMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			showDeleteDialog();
                    }});
            menu.add(deleteMenuItem);

	    menu.addSeparator();
	    
	    JMenuItem manageMenuItem = new JMenuItem(Locale.getString("MANAGE"));
	    manageMenuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			CommandManager commandManager = CommandManager.getInstance();
			commandManager.openPreferences(PreferencesModule.EQUATION_PAGE);
		    }});
	    menu.add(manageMenuItem);
          
            Point point = event.getPoint();
            menu.show(this, point.x, point.y);
        }
    }
}
