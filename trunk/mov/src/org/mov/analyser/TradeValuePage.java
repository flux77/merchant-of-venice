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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.mov.prefs.PreferencesManager;
import org.mov.parser.Expression;
import org.mov.parser.ExpressionException;
import org.mov.parser.Parser;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class TradeValuePage extends JPanel implements AnalyserPage {

    private final static int MAX_CHARS_IN_TEXTBOXES = 15;
    
    private JDesktopPane desktop;

    // Swing items
    private ButtonGroup tradeCostBuyButtonGroup;
    private JRadioButton tradeCostBuyByKeyButton;
    private JRadioButton tradeCostBuyByEquationButton;
    private JComboBox tradeCostBuyComboBox;
    private JTextField tradeCostBuyTextField;
    
    private ButtonGroup tradeCostSellButtonGroup;
    private JRadioButton tradeCostSellByKeyButton;
    private JRadioButton tradeCostSellByEquationButton;
    private JComboBox tradeCostSellComboBox;
    private JTextField tradeCostSellTextField;
    
    public TradeValuePage(JDesktopPane desktop) {
	this.desktop = desktop;
        setGraphic();
    }

    public void load(String key) {

        // Load last GUI settings from preferences
	HashMap settings =
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get(setting);

            if(setting.equals("trade_cost_buy_text_field"))
                tradeCostBuyTextField.setText(value);
            else if(setting.equals("trade_cost_buy_combo"))
                tradeCostBuyComboBox.setSelectedItem(value);
            else if(setting.equals("trade_cost_buy")) {
                if(value.equals("byKey"))
                    tradeCostBuyByKeyButton.setSelected(true);
                else
                    tradeCostBuyByEquationButton.setSelected(true);
            }
            if(setting.equals("trade_cost_sell_text_field"))
                tradeCostSellTextField.setText(value);
            else if(setting.equals("trade_cost_sell_combo"))
                tradeCostSellComboBox.setSelectedItem(value);
            else if(setting.equals("trade_cost_sell")) {
                if(value.equals("byKey"))
                    tradeCostSellByKeyButton.setSelected(true);
                else
                    tradeCostSellByEquationButton.setSelected(true);
            }
            else
                assert false;
        }
   }

    public void save(String key) {
        HashMap settings = new HashMap();

        settings.put("trade_cost_buy", tradeCostBuyByKeyButton.isSelected()? "byKey" : "byEquation");
        settings.put("trade_cost_buy_combo", tradeCostBuyComboBox.getSelectedItem());
        settings.put("trade_cost_buy_text_field", tradeCostBuyTextField.getText());
        settings.put("trade_cost_sell", tradeCostSellByKeyButton.isSelected()? "byKey" : "byEquation");
        settings.put("trade_cost_sell_combo", tradeCostSellComboBox.getSelectedItem());
        settings.put("trade_cost_sell_text_field", tradeCostSellTextField.getText());

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
            if (tradeCostBuyByEquationButton.isSelected()) {
                Expression tradeCostBuyExpression = Parser.parse(variables, tradeCostBuyTextField.getText());
            }
            if (tradeCostSellByEquationButton.isSelected()) {
                Expression tradeCostSellExpression = Parser.parse(variables, tradeCostSellTextField.getText());
            }
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
        return Locale.getString("TRADE_VALUE_PAGE_SHORT_TITLE");
    }

    public String getTradeCostBuy() {
        JRadioButton generalTradeCostByKeyButton = tradeCostBuyByKeyButton;
        JComboBox generalTradeCostComboBox = tradeCostBuyComboBox;
        JTextField generalTradeCostTextField = tradeCostBuyTextField;
        return getTradeCost(generalTradeCostByKeyButton, generalTradeCostComboBox, generalTradeCostTextField);
    }
    
    public String getTradeCostSell() {
        JRadioButton generalTradeCostByKeyButton = tradeCostSellByKeyButton;
        JComboBox generalTradeCostComboBox = tradeCostSellComboBox;
        JTextField generalTradeCostTextField = tradeCostSellTextField;
        return getTradeCost(generalTradeCostByKeyButton, generalTradeCostComboBox, generalTradeCostTextField);
    }
    
    private String getTradeCost(JRadioButton radio, JComboBox combo, JTextField text) {
        String retValue = "open";
        if (radio.isSelected()) {
            if (combo.getSelectedIndex()==0) {
                // TOMORROW_OPEN
                retValue = "open";
            } else if (combo.getSelectedIndex()==1) {
                // TODAY_CLOSE
                retValue = "lag(close,-1)";
            } else if (combo.getSelectedIndex()==2) {
                // TODAY_MIN_MAX_AVG
                retValue = "(lag(low,-1)+lag(high,-1))/2.0";
            } else if (combo.getSelectedIndex()==3) {
                // TODAY_OPEN_CLOSE_AVG
                retValue = "(lag(open,-1)+lag(close,-1))/2.0";
            } else if (combo.getSelectedIndex()==4) {
                // TODAY_MIN
                retValue = "lag(low,-1)";
            } else if (combo.getSelectedIndex()==5) {
                // TODAY_MAX
                retValue = "lag(high,-1)";
            } 
        } else {
            retValue = text.getText();
        }
        return retValue;
    }

    private void setGraphic() {

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Trade Value Panel
        TitledBorder dateTitled = new TitledBorder(Locale.getString("TRADE_VALUE_PAGE_TITLE"));
        JPanel panel = new JPanel();
        panel.setBorder(dateTitled);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        // Trade Cost Panels
        // Buy
        TitledBorder dateTitledBuy = new TitledBorder(Locale.getString("BUY_TRADE_COST"));
        JPanel panelBuy = new JPanel();
        panelBuy.setBorder(dateTitledBuy);
        panelBuy.setLayout(new BorderLayout());
        
        JPanel innerPanelBuy = new JPanel();
        innerPanelBuy.setLayout(gridbag);
        
        tradeCostBuyButtonGroup = new ButtonGroup();

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        tradeCostBuyByKeyButton = new JRadioButton(Locale.getString("BY"));
        tradeCostBuyByKeyButton.setSelected(true);
        tradeCostBuyButtonGroup.add(tradeCostBuyByKeyButton);

        c.gridwidth = 1;
        gridbag.setConstraints(tradeCostBuyByKeyButton, c);
        innerPanelBuy.add(tradeCostBuyByKeyButton);

        tradeCostBuyComboBox = new JComboBox();
        tradeCostBuyComboBox.addItem(Locale.getString("TOMORROW_OPEN"));
        tradeCostBuyComboBox.addItem(Locale.getString("TODAY_CLOSE"));
        tradeCostBuyComboBox.addItem(Locale.getString("TODAY_MIN_MAX_AVG"));
        tradeCostBuyComboBox.addItem(Locale.getString("TODAY_OPEN_CLOSE_AVG"));
        tradeCostBuyComboBox.addItem(Locale.getString("TODAY_MIN"));
        tradeCostBuyComboBox.addItem(Locale.getString("TODAY_MAX"));
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tradeCostBuyComboBox, c);
        innerPanelBuy.add(tradeCostBuyComboBox);

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        tradeCostBuyByEquationButton = new JRadioButton(Locale.getString("BY_EQUATION"));
        tradeCostBuyButtonGroup.add(tradeCostBuyByEquationButton);

        c.gridwidth = 1;
        gridbag.setConstraints(tradeCostBuyByEquationButton, c);
        innerPanelBuy.add(tradeCostBuyByEquationButton);

        tradeCostBuyTextField = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tradeCostBuyTextField, c);
        innerPanelBuy.add(tradeCostBuyTextField);

        panelBuy.add(innerPanelBuy, BorderLayout.NORTH);
        
        // Sell
        TitledBorder dateTitledSell = new TitledBorder(Locale.getString("SELL_TRADE_COST"));
        JPanel panelSell = new JPanel();
        panelSell.setBorder(dateTitledSell);
        panelSell.setLayout(new BorderLayout());
        
        JPanel innerPanelSell = new JPanel();
        innerPanelSell.setLayout(gridbag);
        
        tradeCostSellButtonGroup = new ButtonGroup();

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        tradeCostSellByKeyButton = new JRadioButton(Locale.getString("BY"));
        tradeCostSellByKeyButton.setSelected(true);
        tradeCostSellButtonGroup.add(tradeCostSellByKeyButton);

        c.gridwidth = 1;
        gridbag.setConstraints(tradeCostSellByKeyButton, c);
        innerPanelSell.add(tradeCostSellByKeyButton);

        tradeCostSellComboBox = new JComboBox();
        tradeCostSellComboBox.addItem(Locale.getString("TOMORROW_OPEN"));
        tradeCostSellComboBox.addItem(Locale.getString("TODAY_CLOSE"));
        tradeCostSellComboBox.addItem(Locale.getString("TODAY_MIN_MAX_AVG"));
        tradeCostSellComboBox.addItem(Locale.getString("TODAY_OPEN_CLOSE_AVG"));
        tradeCostSellComboBox.addItem(Locale.getString("TODAY_MIN"));
        tradeCostSellComboBox.addItem(Locale.getString("TODAY_MAX"));
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tradeCostSellComboBox, c);
        innerPanelSell.add(tradeCostSellComboBox);

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        tradeCostSellByEquationButton = new JRadioButton(Locale.getString("BY_EQUATION"));
        tradeCostSellButtonGroup.add(tradeCostSellByEquationButton);

        c.gridwidth = 1;
        gridbag.setConstraints(tradeCostSellByEquationButton, c);
        innerPanelSell.add(tradeCostSellByEquationButton);

        tradeCostSellTextField = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tradeCostSellTextField, c);
        innerPanelSell.add(tradeCostSellTextField);

        panelSell.add(innerPanelSell, BorderLayout.NORTH);
        

        panel.add(panelBuy);
        panel.add(panelSell);
        add(panel);
    }
}
