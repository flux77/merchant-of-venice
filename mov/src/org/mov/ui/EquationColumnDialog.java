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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.mov.parser.ExpressionException;
import org.mov.parser.Parser;

public class EquationColumnDialog extends JInternalFrame implements ActionListener {

    private JButton okButton;
    private JButton cancelButton;

    private JPanel mainPanel;
    private JPanel transactionPanel;

    // Fields of a transaction
    private JComboBox equationColumnComboBox;
    private JTextField columnNameTextField;
    private EquationComboBox equationComboBox;

    private boolean isDone = false;

    private EquationColumn[] equationColumns;
    private int currentEquationColumn = 0;

    private boolean OKButtonPressed;

    public EquationColumnDialog(int equationColumnCount) {
	super("Apply Equations");

	// Make sure we can't be hidden behind other windows
	setLayer(JLayeredPane.MODAL_LAYER);

	getContentPane().setLayout(new BorderLayout());

	mainPanel = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	mainPanel.setLayout(gridbag);
	mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

	c.weightx = 1.0;
	c.ipadx = 5;
	c.anchor = GridBagConstraints.WEST;

	JLabel typeLabel = new JLabel("Equation Column");
	c.gridwidth = 1;
	gridbag.setConstraints(typeLabel, c);
	mainPanel.add(typeLabel);

	equationColumnComboBox = new JComboBox();

	String[] numbers = {"One", "Two", "Three", "Four", "Five"};

	for(int i = 0; i < equationColumnCount; i++)
	    equationColumnComboBox.addItem(numbers[i]);

	equationColumnComboBox.addActionListener(this);

	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(equationColumnComboBox, c);
	mainPanel.add(equationColumnComboBox);

        c.fill = GridBagConstraints.HORIZONTAL;

	columnNameTextField = 
	    GridBagHelper.addTextRow(mainPanel, "Column name", "", gridbag, c, 18);

	equationComboBox =
	    GridBagHelper.addEquationRow(mainPanel, "Equation", "", gridbag, c);

	JPanel buttonPanel = new JPanel();
	okButton = new JButton("OK");
	okButton.addActionListener(this);
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(this);
	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
  
	getContentPane().add(mainPanel, BorderLayout.NORTH);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	// Open dialog in centre of window
	Dimension size = getPreferredSize();
	int x = (DesktopManager.getDesktop().getWidth() - size.width) / 2;
	int y = (DesktopManager.getDesktop().getHeight() - size.height) / 2;
	setBounds(x, y, size.width, size.height);
    }

    public boolean showDialog(EquationColumn[] equationColumns) {

	// Creat copy of equation columns to work with
	this.equationColumns = new EquationColumn[equationColumns.length];
	for(int i = 0; i < equationColumns.length; i++)
	    this.equationColumns[i] = (EquationColumn)equationColumns[i].clone();

	displayEquationColumn(0);

	DesktopManager.getDesktop().add(this);
	show();

	try {
	    while(isDone == false) {
		Thread.sleep(10);
	    }
	}
	catch(Exception e) {
	    // ignore
	}

	return OKButtonPressed;
    }

    public EquationColumn[] getEquationColumns() {
	return equationColumns;
    }

    private void saveEquationColumn(int column) {
	// Store new values the user has typed in
	equationColumns[column].setShortName(columnNameTextField.getText());
	equationColumns[column].setEquation(equationComboBox.getEquationText());
    }

    private void displayEquationColumn(int column) {
	currentEquationColumn = column;

	columnNameTextField.setText(equationColumns[column].getShortName());
	equationComboBox.setEquationText(equationColumns[column].getEquation());
    }

    // Make sure the expression field is correct in each equation column. If
    // any of the equations do not parse then display an error dialog to
    // the user.
    private boolean parseEquations() {
        boolean success = true;
        int i = 0;

        try {
            for(i = 0; i < equationColumns.length; i++) {
                String equationString = equationColumns[i].getEquation();
                
                if(equationString != null && equationString.length() > 0) 
                    equationColumns[i].setExpression(Parser.parse(equationString));
                else
                    equationColumns[i].setExpression(null);
            }
        }
        catch(ExpressionException e) {
            JOptionPane.
                showInternalMessageDialog(this, "Error parsing equation", 
                                          e.getReason(),
                                          JOptionPane.ERROR_MESSAGE);
            success = false;
        }

        return success;
    }

    public void actionPerformed(ActionEvent e) {

	if(e.getSource() == okButton) {
	    saveEquationColumn(currentEquationColumn);

            if(parseEquations()) {
                OKButtonPressed = true;
                dispose();
                isDone = true;
            }
	}
	else if(e.getSource() == cancelButton) {
	    saveEquationColumn(currentEquationColumn);

	    OKButtonPressed = false;
	    dispose();
	    isDone = true;
	}

	else if(e.getSource() == equationColumnComboBox) {
	    // Save the current values and display new ones
	    saveEquationColumn(currentEquationColumn);
	    displayEquationColumn(equationColumnComboBox.getSelectedIndex());
	}
    }	
}
