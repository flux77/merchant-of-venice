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

package org.mov.analyser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mov.prefs.PreferencesManager;
import org.mov.parser.Expression;
import org.mov.parser.ExpressionException;
import org.mov.parser.Parser;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class BuySellSystemPage extends JPanel implements AnalyserPage {

    private final static int MAX_CHARS_IN_TEXTBOXES = 15;
    
    private JDesktopPane desktop;

    // Swing items
    private JTextField buySystemRuleTextField;
    private JTextField sellSystemRuleTextField;
    
    public BuySellSystemPage(JDesktopPane desktop) {
	this.desktop = desktop;
        setGraphic();
        setDefaults();
    }

    public void load(String key) {

        // Load last GUI settings from preferences
	HashMap settings =
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get(setting);

            if(setting.equals("buy_system_rule")) {
                buySystemRuleTextField.setText(value);
            } else if(setting.equals("sell_system_rule")) {
                sellSystemRuleTextField.setText(value);
            } else {
                assert false;
            }
        }
   }

    public void save(String key) {
        HashMap settings = new HashMap();

        settings.put("buy_system_rule", buySystemRuleTextField.getText());
        settings.put("sell_system_rule", sellSystemRuleTextField.getText());

        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }

    public boolean parse() {
        try {
            // We need to specify the variables that are given to the expression
            // expressions so they can be parsed properly.
            Variables variables = new Variables();
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
            Expression buySystemRuleExpression = Parser.parse(variables, buySystemRuleTextField.getText());
            Expression sellSystemRuleExpression = Parser.parse(variables, sellSystemRuleTextField.getText());
        } catch(ExpressionException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ERROR_PARSING_SYSTEM_RULES"),
                                                  Locale.getString("INVALID_BUY_SELL_SYSTEM_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}
        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("BUY_SELL_SYSTEM_PAGE_SHORT_TITLE");
    }

    public String getBuySystemRule() {
        return buySystemRuleTextField.getText();
    }

    public String getSellSystemRule() {
        return sellSystemRuleTextField.getText();
    }

    public void setDefaults() {
        buySystemRuleTextField.setText("lag(open,0)");
        sellSystemRuleTextField.setText("lag(open,0)");
    }

    private void setGraphic() {

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        GridBagLayout gridbag = new GridBagLayout();
        
        // Buy Sell System Rules Panel
        TitledBorder dateTitled = new TitledBorder(Locale.getString("BUY_SELL_SYSTEM_PAGE_TITLE"));
        JPanel panel = new JPanel();
        panel.setBorder(dateTitled);
        panel.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(gridbag);

        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        buySystemRuleTextField =
            GridBagHelper.addTextRow(innerPanel, Locale.getString("BUY_SYSTEM_RULE"), "",
                                 gridbag, c,
                                 MAX_CHARS_IN_TEXTBOXES);

        sellSystemRuleTextField =
            GridBagHelper.addTextRow(innerPanel, Locale.getString("SELL_SYSTEM_RULE"), "",
                                 gridbag, c,
                                 MAX_CHARS_IN_TEXTBOXES);

        JButton defaultButton = new JButton(Locale.getString("DEFAULT"));
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // Set Default Values
                setDefaults();
            }
        });
        defaultButton.setAlignmentX(CENTER_ALIGNMENT);
        innerPanel.add(defaultButton);

        panel.add(innerPanel, BorderLayout.NORTH);
        add(panel);
    }
}
