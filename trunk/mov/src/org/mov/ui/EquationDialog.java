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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.mov.util.Locale;

/**
 * Dialog for querying the user for an equation string.
 *
 * @author Andrew Leppard
 * @see EquationComboBox
 */
public class EquationDialog implements ActionListener
{
    String option;
    JButton OKButton, CancelButton;
    EquationComboBox equationComboBox;
    JDialog textDialog;
    JInternalFrame textFrame;
    JPanel optionPanel;
    JComponent parent;

    boolean isDone;

    /**
     * Create new equation dialog.
     *
     * @param parent The parent component to tie the dialog to
     * @param message The question to ask the user
     * @param title The title to place on the dialog
     */
    public EquationDialog(JComponent parent, String message, String title)
    {
	newDialog(parent, message, title, "");
    }

    /**
     * Create new equation dialog.
     *
     * @param parent The parent component to tie the dialog to
     * @param message The question to ask the user
     * @param title The title to place on the dialog
     * @param defaultEquation The default equation to display in the equation ComboBox
     */
    public EquationDialog(JComponent parent, String message, String title,
			  String defaultEquation) {
	newDialog(parent, message, title, defaultEquation);
    }
	
    // Create a new equation dialog
    private void newDialog(JComponent parent, String message, String title, 
			   String defaultEquation) 
    {
	this.parent = parent;

	OKButton = new JButton(Locale.getString("OK"));
	CancelButton = new JButton(Locale.getString("CANCEL"));
	equationComboBox = new EquationComboBox(defaultEquation);

	JLabel label = new JLabel(message);	    

        // Make sure the label and the equation combo box are aligned
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        equationComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(label);
        box.add(Box.createVerticalStrut(5));
        box.add(equationComboBox);

	OKButton.addActionListener (this);
	CancelButton.addActionListener (this);

	Object options[] = {OKButton, CancelButton};
	JOptionPane optionPane = new JOptionPane(box,
						 JOptionPane.QUESTION_MESSAGE,
						 JOptionPane.OK_CANCEL_OPTION,
						 null, options, null);

	textFrame = optionPane.createInternalFrame(parent,
						   title);
	optionPane.getRootPane().setDefaultButton(OKButton);
    }
    
    /*
     * Pops up the dialog and waits for the user to enter in an equation. 
     *
     * @return the equation
     */
    public String showDialog()
    {
	isDone = false;
	
	textFrame.show();
	
	try {
	    while(!isDone) 
		Thread.sleep(10);
	    
	} catch (InterruptedException e) {
	}
	
	return option;
    }

    /**
     * ActionListener interface used for internal buttons.
     */
    public void actionPerformed(ActionEvent e)
    {
	if (e.getSource () == OKButton) {
	    option = equationComboBox.getEquationText();
	    isDone = true;
	}
	else if (e.getSource () == CancelButton) {
	    option = null;
	    isDone = true;
	}

	textFrame.dispose();
    }
}




