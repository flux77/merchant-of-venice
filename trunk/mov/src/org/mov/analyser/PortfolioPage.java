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
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;
import org.mov.util.Money;

public class PortfolioPage extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;

    // Swing components
    private JRadioButton numberStocksButton;
    private JTextField numberStocksTextField;
    private JRadioButton stockValueButton;
    private JTextField stockValueTextField;
    private JTextField initialCapitalTextField;
    private JTextField tradeCostTextField;

    // Parsed input
    private Money initialCapital;
    private Money tradeCost;
    private int mode;
    private int numberStocks;
    private Money stockValue;

    public final static int NUMBER_STOCKS_MODE = 0;
    public final static int STOCK_VALUE_MODE = 1;

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

            if(setting.equals("number_stocks")) {
                numberStocksTextField.setText(value);
            }
            else if(setting.equals("stock_value"))
                stockValueTextField.setText(value);            
            else if(setting.equals("mode")) {
                if(value.equals("number_stocks"))
                    numberStocksButton.setSelected(true);
                else
                    stockValueButton.setSelected(true);
            }
	    else if(setting.equals("initial_capital"))
		initialCapitalTextField.setText(value);
	    else if(setting.equals("trade_cost"))
		tradeCostTextField.setText(value);
        }

        checkDisabledStatus();
    }

    public void save(String key) {
        HashMap settings = new HashMap();
        settings.put("mode", (numberStocksButton.isSelected()?
                              "number_stocks" : "stock_value"));
        settings.put("number_stocks", numberStocksTextField.getText());
        settings.put("stock_value", stockValueTextField.getText());
	settings.put("initial_capital", initialCapitalTextField.getText());
	settings.put("trade_cost", tradeCostTextField.getText());
        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }

    public boolean parse() {
        numberStocks = 0;
        stockValue = Money.ZERO;
	initialCapital = Money.ZERO;
	tradeCost = Money.ZERO;

        if(numberStocksButton.isSelected())
            mode = NUMBER_STOCKS_MODE;
        else {
            assert stockValueButton.isSelected();
            mode = STOCK_VALUE_MODE;
        }

	try {
            if(!stockValueTextField.getText().equals(""))
		stockValue = 
		    new Money(Float.parseFloat(stockValueTextField.getText()));

            if(!numberStocksTextField.getText().equals(""))
		numberStocks = 
		    Integer.parseInt(numberStocksTextField.getText());

	    if(!initialCapitalTextField.getText().equals(""))
		initialCapital = 
		    new Money(Float.parseFloat(initialCapitalTextField.getText()));
	    	   
	    if(!tradeCostTextField.getText().equals(""))
		tradeCost = 
		    new Money(Float.parseFloat(tradeCostTextField.getText()));
	}
	catch(NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("ERROR_PARSING_NUMBER",
                                                                   e.getMessage()),
                                                  Locale.getString("INVALID_PORTFOLIO_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	if(initialCapital.isLessThanEqual(Money.ZERO)) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("NO_CAPITAL_ERROR"),
                                                  Locale.getString("INVALID_PORTFOLIO_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

        if(mode == NUMBER_STOCKS_MODE && numberStocks <= 0) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("EMPTY_PORTFOLIO_ERROR"),
                                                  Locale.getString("INVALID_PORTFOLIO_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(mode == STOCK_VALUE_MODE && stockValue.isLessThanEqual(Money.ZERO)) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("STOCK_VALUE_ERROR"),
                                                  Locale.getString("INVALID_PORTFOLIO_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(tradeCost.isLessThan(Money.ZERO)) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("INVALID_TRADE_COST_ERROR"),
                                                  Locale.getString("INVALID_PORTFOLIO_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

	return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("PORTFOLIO_PAGE_TITLE");
    }

    public Money getInitialCapital() {
        return initialCapital;
    }

    public Money getTradeCost() {
        return tradeCost;
    }

    public int getMode() {
        assert mode == NUMBER_STOCKS_MODE || mode == STOCK_VALUE_MODE;

        return mode;
    }

    public Money getStockValue() {
        if (mode == STOCK_VALUE_MODE)
            return stockValue;
        else {
            return null;
        }
    }

    public int getNumberStocks() {
        if(mode == NUMBER_STOCKS_MODE)
            return numberStocks;
        else {
            return 0;
        }
    }

    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Portfolio panel
        {
            TitledBorder portfolioTitled = new TitledBorder(Locale.getString("PORTFOLIO"));
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
            
            initialCapitalTextField = 
                GridBagHelper.addTextRow(innerPanel, Locale.getString("INITIAL_CAPITAL"), "", 
                                         gridbag, c, 
                                         10);
            tradeCostTextField =
                GridBagHelper.addTextRow(innerPanel, Locale.getString("TRADE_COST"), "", 
                                         gridbag, c, 5);

            panel.add(innerPanel, BorderLayout.NORTH);
            add(panel);
        }

        // How many stocks
        {
            TitledBorder portfolioTitled = new TitledBorder(Locale.getString("HOW_MANY_STOCKS"));
            JPanel panel = new JPanel();
            panel.setBorder(portfolioTitled);
            panel.setLayout(new BorderLayout());
            
            JPanel innerPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            innerPanel.setLayout(gridbag);
            
            ButtonGroup buttonGroup = new ButtonGroup();
            c.weightx = 1.0;
            c.ipadx = 5;
            c.anchor = GridBagConstraints.WEST;
            
            numberStocksButton = new JRadioButton(Locale.getString("NUMBER_OF_STOCKS"));
            numberStocksButton.setSelected(true);
            numberStocksButton.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        checkDisabledStatus();
                    }
                });
            buttonGroup.add(numberStocksButton);
            
            c.gridwidth = 1;
            gridbag.setConstraints(numberStocksButton, c);
            innerPanel.add(numberStocksButton);
            
            numberStocksTextField = new JTextField("", 5);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(numberStocksTextField, c);
            innerPanel.add(numberStocksTextField);
            
            c.weightx = 1.0;
            c.ipadx = 5;
            c.anchor = GridBagConstraints.WEST;
            
            stockValueButton = new JRadioButton(Locale.getString("STOCK_VALUE"));
            stockValueButton.setSelected(true);
            stockValueButton.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        checkDisabledStatus();
                    }
                });
            buttonGroup.add(stockValueButton);
            
            c.gridwidth = 1;
            gridbag.setConstraints(stockValueButton, c);
            innerPanel.add(stockValueButton);
            
            stockValueTextField = new JTextField("", 10);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(stockValueTextField, c);
            innerPanel.add(stockValueTextField);
            
            panel.add(innerPanel, BorderLayout.NORTH);
            add(panel);
        }
    }

    private void checkDisabledStatus() {
        numberStocksTextField.setEnabled(numberStocksButton.isSelected());
        stockValueTextField.setEnabled(stockValueButton.isSelected());
    }
}
