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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mov.main.ModuleFrame;
import org.mov.prefs.PreferencesManager;
import org.mov.prefs.StoredEquation;
import org.mov.util.Locale;

public class ExpressionEditorDialog {

    private boolean isUp = true;
    private boolean wasCancelled = false;
    private JInternalFrame internalFrame;

    // Equation we are working with
    private String name;
    private String equation;

    // Width of text field: Name: [<-width->]
    private final static int NAME_WIDTH = 20;

    // Minimum & preferred size to display equation */
    private final static int EQUATION_ROWS = 14;
    private final static int EQUATION_COLUMNS = 30;

    // Whether we should display just the OK button or the OK and 
    // the cancel button
    private final static int OK_BUTTON        = 0;
    private final static int OK_CANCEL_BUTTON = 1;

    private ExpressionEditorDialog(String title, boolean displayName, 
                                   String name, String equation,
                                   int buttonArray, boolean isEditable) {
	this.name = name;
        this.equation = equation;
        assert buttonArray == OK_BUTTON || buttonArray == OK_CANCEL_BUTTON;

        buildDialog(title, displayName, buttonArray, isEditable);
    }

    private void buildDialog(String title, final boolean displayName, int buttonArray, 
			     boolean isEditable) {
        internalFrame = new JInternalFrame(title,
                                           true, /* resizable */
                                           false, /* closable */
                                           false, /* maximisible */
                                           false); /* iconifiable */
	JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

	final JTextField nameField = new JTextField(name, NAME_WIDTH);

        if(displayName) {
            JPanel innerNamePanel = new JPanel();
            innerNamePanel.add(new JLabel(Locale.getString("NAME")));
            innerNamePanel.add(nameField);
            
            JPanel namePanel = new JPanel();
            namePanel.setLayout(new BorderLayout());
            namePanel.add(innerNamePanel, BorderLayout.WEST);
            panel.add(namePanel, BorderLayout.NORTH);
        }

        JPanel equationPanel = new JPanel();
        final JTextArea equationEditor = new JTextArea(EQUATION_ROWS,
						       EQUATION_COLUMNS);
        equationEditor.setText(equation);
        equationEditor.setEditable(isEditable);

        TitledBorder titledBorder = new TitledBorder(Locale.getString("EQUATION"));
        equationPanel.setLayout(new BorderLayout());
        equationPanel.setBorder(titledBorder);
        equationPanel.add(new JScrollPane(equationEditor));

        panel.add(equationPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton(Locale.getString("OK"));
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Update equation
		    if(displayName)
			setName(nameField.getText());

                    setEquation(equationEditor.getText());
		    wasCancelled = false;
                    close();
                }});
        buttonPanel.add(okButton);

        // The cancel button may not be displayed
        if(buttonArray == OK_CANCEL_BUTTON) {
            JButton cancelButton = new JButton(Locale.getString("CANCEL"));
            cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // User cancelled dialog so don't modify equation
			wasCancelled = true;
                        close();
                    }});
            buttonPanel.add(cancelButton);
        }

        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        internalFrame.getContentPane().add(panel);

        Dimension preferred = internalFrame.getPreferredSize();
        internalFrame.setMinimumSize(preferred);
        ModuleFrame.setSizeAndLocation(internalFrame, DesktopManager.getDesktop(),
                                      true, true);
        DesktopManager.getDesktop().add(internalFrame);
        internalFrame.show();

	try {
	    internalFrame.setSelected(true);
	}
	catch(PropertyVetoException v) {
	    // ignore
	}
	
	internalFrame.moveToFront();		    
    }

    private void close() {
        isUp = false;
        try {
            internalFrame.setClosed(true);
        }
        catch(PropertyVetoException e) {
            // nothing to do
        }
    }

    private boolean isUp() {
        return isUp;
    }

    private String getName() {
	return name;
    }

    private void setName(String name) {
	this.name = name;
    }

    private String getEquation() {
        return equation;
    }

    private void setEquation(String equation) {
        this.equation = equation;
    }

    private boolean waitUntilClosed() {
	try {
	    while(isUp()) 
		Thread.sleep(10);

	} catch (InterruptedException e) {
            // Finish.
	}        

	return wasCancelled;
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static StoredEquation showAddDialog(List storedEquations, String title,
					       String equation) {
	boolean isValid = false;
	String name = "";
	StoredEquation storedEquation = null;
	
	while(!isValid) {
	    ExpressionEditorDialog dialog = new ExpressionEditorDialog(title, true, 
								       name,
								       equation,
								       OK_CANCEL_BUTTON,
								       true);
	    boolean wasCancelled = dialog.waitUntilClosed();
	    name = dialog.getName();
	    equation = dialog.getEquation();

	    if(!wasCancelled) {
		isValid = validateStoredEquation(storedEquations, null, name);

		if(isValid) 
		    storedEquation = new StoredEquation(name, equation);
	    }
	    else
		isValid = true;
	}
		
	return storedEquation;
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static StoredEquation showAddDialog(List storedEquations, String title) {
	return showAddDialog(storedEquations, title, "");
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static StoredEquation showAddDialog(String title, String equation) {
	List storedEquations = PreferencesManager.loadStoredEquations();
	StoredEquation storedEquation = showAddDialog(storedEquations, title, equation);

	// If the user added an equation, save it to the preferences and make
	// sure all the combo boxes are updated.
	if(storedEquation != null) {
	    storedEquations.add(storedEquation);
	    PreferencesManager.saveStoredEquations(storedEquations);
	    EquationComboBox.updateEquations();
	}

	return storedEquation;
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static String showEditDialog(String title, String equation) {
        ExpressionEditorDialog dialog = new ExpressionEditorDialog(title, false, "",
                                                                   equation,
                                                                   OK_CANCEL_BUTTON,
                                                                   true);
        dialog.waitUntilClosed();
        return dialog.getEquation();
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static StoredEquation showEditDialog(List storedEquations, String title, 
						StoredEquation storedEquation) {
	boolean isValid = false;
	String oldName = new String(storedEquation.name);
	String name = storedEquation.name;
	String equation = storedEquation.equation;

	while(!isValid) {
	    ExpressionEditorDialog dialog = new ExpressionEditorDialog(title, true, 
								       name,
								       equation,
								       OK_CANCEL_BUTTON,
								       true);
	    boolean wasCancelled = dialog.waitUntilClosed();
	    name = dialog.getName();
	    equation = dialog.getEquation();

	    if(!wasCancelled) {
		isValid = validateStoredEquation(storedEquations, oldName, name);

		if(isValid) {
		    storedEquation.name = name;
		    storedEquation.equation = equation;
		}
	    }
	    else
		isValid = true;
	}

	return storedEquation;
    }

    // make sure you run this in its own thread - not in the swing dispatch thread!
    public static void showViewDialog(String title, String equation) {
        ExpressionEditorDialog dialog = new ExpressionEditorDialog(title, false, "", 
                                                                   equation,
                                                                   OK_BUTTON,
                                                                   false);
        dialog.waitUntilClosed();
    }

    // Check that a stored equation is valid after the user has modified it.
    // Check for things like missing equation name or duplicate equation names.
    // Don't check the equation for syntax as we can't do this without knowing
    // the variables that will be predefined for that equation.
    private static boolean validateStoredEquation(List storedEquations, String oldName,
						  String newName) {

	boolean isValid = true;

	if(newName.length() == 0) {
	    JOptionPane.showInternalMessageDialog(DesktopManager.getDesktop(),
						  Locale.getString("MISSING_EQUATION_NAME"),
						  Locale.getString("ERROR_STORING_EQUATION"),
						  JOptionPane.ERROR_MESSAGE);
	    isValid = false;
	}

	// If the name was changed, make sure it wasn't changed to an
	// existing stored equation's name.
	else if(oldName == null || !newName.equals(oldName)) {
	    boolean isDuplicateName = false;
	    
	    for(Iterator iterator = storedEquations.iterator(); iterator.hasNext();) {
		StoredEquation traverse = (StoredEquation)iterator.next();
		if(traverse.name.equals(newName))
		    isDuplicateName = true;
		
	    }
	    
	    if(isDuplicateName) {
		JOptionPane.showInternalMessageDialog(DesktopManager.getDesktop(),
                                                      Locale.getString("DUPLICATE_EQUATION_NAME",
                                                                       newName),
						      Locale.getString("ERROR_STORING_EQUATION"),
						      JOptionPane.ERROR_MESSAGE);
		isValid = false;
	    }
	}

	return isValid;
    }
}
