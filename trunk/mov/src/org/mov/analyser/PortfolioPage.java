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
import java.lang.Class;
import java.lang.String;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.GridBagHelper;

public class PortfolioPage extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;

    // Swing components
    private JCheckBox multipleStockCheckBox;
    private JTextField valuePerStockTextField;
    private JTextField initialCapitalTextField;
    private JTextField tradeCostTextField;

    // Parsed input
    private boolean isMultipleStockPortfolio;
    private float valuePerStock;
    private float initialCapital;
    private float tradeCost;

    public PortfolioPage(JDesktopPane desktop) {
        this.desktop = desktop;

        layoutPage();
    }

    public void load(String key) {

        // Load last GUI settings from preferences
	HashMap settings = 
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());
                          
	Iterator iterator = settings.keySet().iterator();
                              
	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

            if(setting.equals("is_multiple_stock")) 
                multipleStockCheckBox.setSelected(value.equals("1"));
            else if(setting.equals("percent_per_stock"))
                valuePerStockTextField.setText(value);
	    else if(setting.equals("initial_capital"))
		initialCapitalTextField.setText(value);
	    else if(setting.equals("trade_cost"))
		tradeCostTextField.setText(value);
            else
                assert false;
        }

        checkDisabledStatus();
    }

    public void save(String key) {
        HashMap settings = new HashMap();

        settings.put("is_multiple_stock", multipleStockCheckBox.isSelected()? "1" : "0");
	settings.put("percent_per_stock", valuePerStockTextField.getText());
	settings.put("initial_capital", initialCapitalTextField.getText());
	settings.put("trade_cost", tradeCostTextField.getText());

        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }

    public boolean parse() {
        valuePerStock = 0.0F;
	initialCapital = 0.0F;
	tradeCost = 0.0F;

        isMultipleStockPortfolio = multipleStockCheckBox.isSelected();

	try {
	    if(!valuePerStockTextField.getText().equals(""))
		valuePerStock = 
		    Float.parseFloat(valuePerStockTextField.getText());

	    if(!initialCapitalTextField.getText().equals(""))
		initialCapital = 
		    Float.parseFloat(initialCapitalTextField.getText());
	    	   
	    if(!tradeCostTextField.getText().equals(""))
		tradeCost = 
		    Float.parseFloat(tradeCostTextField.getText());
	}
	catch(NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  "Invalid number '" +
                                                  e.getMessage() + "'",
                                                  "Invalid number",
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	if(initialCapital <= 0) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  "Cannot trade without some initial capital.",
                                                  "Invalid number",
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public float getValuePerStock() {
        return valuePerStock;
    }

    public float getInitialCapital() {
        return initialCapital;
    }

    public float getTradeCost() {
        return tradeCost;
    }

    public boolean isMultipleStockPortfolio() {
        return isMultipleStockPortfolio;
    }

    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        TitledBorder portfolioTitled = new TitledBorder("Portfolio");
        JPanel panel = new JPanel();
        panel.setBorder(portfolioTitled);
        panel.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        innerPanel.setLayout(gridbag);
        
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        multipleStockCheckBox = 
            GridBagHelper.addCheckBoxRow(innerPanel, "Multiple Stock Portfolio",
                                         false, gridbag, c);
        multipleStockCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    checkDisabledStatus();
                }
            });
        valuePerStockTextField = 
            GridBagHelper.addTextRow(innerPanel, "Value Per Stock", "", gridbag, c, 
                                     8);
        initialCapitalTextField = 
            GridBagHelper.addTextRow(innerPanel, "Initial Capital", "", gridbag, c, 
                                     10);
        tradeCostTextField =
            GridBagHelper.addTextRow(innerPanel, "Trade Cost", "", gridbag, c, 5);
        
        panel.add(innerPanel, BorderLayout.NORTH);
        add(panel);
    }

    private void checkDisabledStatus() {
        boolean isMultipleStockEnabled = multipleStockCheckBox.isSelected();

        valuePerStockTextField.setEnabled(isMultipleStockEnabled);
    }

}
