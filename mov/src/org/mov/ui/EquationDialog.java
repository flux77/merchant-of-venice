package org.mov.ui;

import org.mov.parser.*;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

/**
 * Dialog for querying the user for an equation string.
 *
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

	OKButton = new JButton("OK");
	CancelButton = new JButton("Cancel");
	equationComboBox = new EquationComboBox(defaultEquation);

	JPanel panel = new JPanel();
	JLabel label = new JLabel(message);	    

	BorderLayout layout = new BorderLayout();
	layout.setHgap(50);
	layout.setVgap(5);

	panel.setLayout(layout);
	panel.add(label, BorderLayout.NORTH);
	panel.add(equationComboBox, BorderLayout.CENTER);

	OKButton.addActionListener (this);
	CancelButton.addActionListener (this);

	Object options[] = {OKButton, CancelButton};
	JOptionPane optionPane = new JOptionPane(panel,
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




