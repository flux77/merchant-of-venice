package org.mov.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class implements a standard text input dialog, with OK and Cancel buttons working correctly.  
 * This probably will not be required once JDK1.4 works properly, but for now gives the correct functionality
 */
public class TextDialog implements ActionListener
{
    
    String option;
    JButton OKButton, CancelButton;
    JTextField DataTextField;
    JDialog textDialog;
    JInternalFrame textFrame;
    JPanel optionPanel;

    /**
     * Default constructor
     * @param parent The parent component to tie the dialog to
     * @param message The question to ask the user
     * @param title The title to place on the dialog
     */
    public TextDialog(JComponent parent, String message, String
		      title)
    {
	
	OKButton = new JButton("OK");
	CancelButton = new JButton("Cancel");
	DataTextField = new JTextField();
	BorderLayout layout = new BorderLayout();
	layout.setHgap(50);
	layout.setVgap(5);

	optionPanel = new JPanel(layout);

	optionPanel.add(DataTextField, BorderLayout.NORTH);
	optionPanel.add(OKButton, BorderLayout.WEST);
	optionPanel.add(CancelButton, BorderLayout.EAST);
	OKButton.addActionListener (this);
	CancelButton.addActionListener (this);
	
	Object options[] = new Object[]{optionPanel};


	JOptionPane optionPane = new JOptionPane(
						 message,
						 JOptionPane.QUESTION_MESSAGE,
						 JOptionPane.OK_CANCEL_OPTION,
						 null, options, null);
	/*	textDialog = optionPane.createDialog(parent,
		title);
	*/
	textFrame = optionPane.createInternalFrame(parent,
							   title);
	optionPane.getRootPane().setDefaultButton(OKButton);
    }
    
    /*
     * Pops up the dialog and waits for feedback
     * @return the string value the user has typed in
     */
    public String showDialog()
    {
	textFrame.show();
	try {
	    while(option == null) {
		Thread.sleep(10);
	    }
	} catch (Exception e) {
	}
	return option;
    }
    
    /**
     * ActionListener interface used for internal buttons.
     */
    public void actionPerformed(ActionEvent e)
    {
	if (e.getSource () == OKButton)
	    {
		option = DataTextField.getText();
	    }
	else if (e.getSource () == CancelButton)
	    {
		option = null;
	    }
	textFrame.dispose();
    }
}




