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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.String;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GridBagHelper {

    private void GridBagHelper() {
        // this class cannot be instantiated
    }

    public static JCheckBox addCheckBoxRow(JPanel panel, String field,
                                           boolean isSelected,
                                           GridBagLayout gridbag,
                                           GridBagConstraints c) {

        JCheckBox checkBox = new JCheckBox(field, isSelected);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(checkBox, c);
        panel.add(checkBox);

        return checkBox;
    }

    // Helper method which adds a new text field in a new row to the given 
    // grid bag layout.
    public static EquationComboBox addEquationRow(JPanel panel, String field, 
                                                  String value,
                                                  GridBagLayout gridbag,
                                                  GridBagConstraints c) {
	JLabel label = new JLabel(field);
	c.gridwidth = 1;
	gridbag.setConstraints(label, c);
	panel.add(label);

	EquationComboBox comboBox = new EquationComboBox(value);
	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(comboBox, c);
	panel.add(comboBox);

	return comboBox;
    }

    // Helper method which adds a new password text field in a new row to
    // the given grid bag layout.
    public static JPasswordField addPasswordRow(JPanel panel, String field, 
                                                String value,
                                                GridBagLayout gridbag,
                                                GridBagConstraints c,
                                                int length) {
	JLabel label = new JLabel(field);
	c.gridwidth = 1;
	gridbag.setConstraints(label, c);
	panel.add(label);
	    
	JPasswordField password = new JPasswordField(value, length);
	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(password, c);
	panel.add(password);

	return password;
    }

    // Helper method which adds a new text field in a new row to the given 
    // grid bag layout.
    public static JTextField addTextRow(JPanel panel, String field, String value,
                                        GridBagLayout gridbag,
                                        GridBagConstraints c,
                                        int length) {
	JLabel label = new JLabel(field);
	c.gridwidth = 1;
	gridbag.setConstraints(label, c);
	panel.add(label);

	JTextField text = new JTextField(value, length);
	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(text, c);
	panel.add(text);

	return text;
    }
}

