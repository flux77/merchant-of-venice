/*
 * Merchant of Venice - technical analysis software for the stock market.
 * Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 *
 * @author  Alberto Nacher
 */

package org.mov.analyser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.TitledBorder;

import org.mov.parser.Expression;
import org.mov.parser.ExpressionException;
import org.mov.parser.Parser;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.prefs.PreferencesManager;
import org.mov.ui.EquationComboBox;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

/**
* An analysis tool page that lets the user enter a buy and sell rule, or
* a family of related buy and sell rules. This page is used by the
* {@link PaperTradeModule}. The page contains the following user fields:
*
* <ul><li>Buy Rule</li>
*     <li>Sell Rule</li>
*     <li>Enable Rule Families</li>
*     <ul>
*        <li>Range of A Variable</li>
*        <li>Range of B Variable</li>
*        <li>Range of C Variable</li>
*     </ul>
* </ul>
*
* The buy and sell rules determine when a stock should be bought or sold
* respectively. The rule family option allows the user to embedd variables
* in the rules. This enables them to specify a family of simillar rules. For
* example, a buy rule might be <code>avg(close, 15) > avg(close, 30)</code>.
* If the rule family is enabled, the user could enter 
* <code>avg(close, a) > avg(close, b)</code>. Then the paper trade would
* try each rule combination of [a, b].
*
* @author Andrew Leppard
* @see PaperTradeModule
*/
public class GARulesPage extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;

    // Swing components
    private JCheckBox ruleFamilyEnabledCheckBox;
    private EquationComboBox buyRuleEquationComboBox;
    private EquationComboBox sellRuleEquationComboBox;
    
    // Parsed input
    private Expression buyRule;
    private Expression sellRule;
    
    // Parameters Table
    private GARulesPageModule GARulesPageModule;

    /**
     * Construct a new rules page.
     *
     * @param desktop the desktop
     */    
    public GARulesPage(JDesktopPane desktop) {      
        this.desktop = desktop;
        this.GARulesPageModule = new GARulesPageModule(desktop);
        layoutPage();
    }
    
    public void load(String key) {
        
        String idStr = "Parameters";
        
        // Load last GUI settings from preferences
        HashMap settings = PreferencesManager.loadAnalyserPageSettings(key
                                                                       + getClass().getName());
        
        Iterator iterator = settings.keySet().iterator();
        
        while (iterator.hasNext()) {
            String setting = (String) iterator.next();
            String value = (String) settings.get((Object) setting);
            
            if (setting.equals("buy_rule"))
                buyRuleEquationComboBox.setEquationText(value);
            else if (setting.equals("sell_rule"))
                sellRuleEquationComboBox.setEquationText(value);
        }
        
        HashMap settingsParam =
                PreferencesManager.loadAnalyserPageSettings(key + idStr);

        Iterator iteratorParam = settingsParam.keySet().iterator();

	while(iteratorParam.hasNext()) {
	    String settingParam = (String)iteratorParam.next();
	    String valueParam = (String)settingsParam.get((Object)settingParam);

            GARulesPageModule.load(valueParam);
        }
        GARulesPageModule.loadEmpty();
        
    }
    
    public void save(String key) {
        String idStr = "GPInitialPopulation";

        HashMap settingsParam =
                PreferencesManager.loadAnalyserPageSettings(key + idStr);
        HashMap settings = new HashMap();

        GARulesPageModule.save(settingsParam, idStr);
        settings.put("buy_rule", buyRuleEquationComboBox.getEquationText());
        settings.put("sell_rule", sellRuleEquationComboBox.getEquationText());

        PreferencesManager.saveAnalyserPageSettings(key + idStr,
                                                    settingsParam);
        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }
    
    public boolean parse() {
        // We need to specify the variables that are given to the buy/sell rule
        // expressions so they can be parsed properly.
        Variables variables = new Variables();
        
        String buyRuleString = buyRuleEquationComboBox.getEquationText();
        String sellRuleString = sellRuleEquationComboBox.getEquationText();
        
        // Set the variables in the rules
        //for () {
        //    variables.add("xxx", Expression.INTEGER_TYPE, Variable.CONSTANT);
        //}
        
        variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        
        if (buyRuleString.length() == 0) {
            JOptionPane.showInternalMessageDialog(desktop, Locale
                                                  .getString("MISSING_BUY_RULE"), Locale
                                                  .getString("ERROR_PARSING_RULES"),
					JOptionPane.ERROR_MESSAGE);
            
            return false;
        }

        if (sellRuleString.length() == 0) {
            JOptionPane.showInternalMessageDialog(desktop, Locale
                                                  .getString("MISSING_SELL_RULE"), Locale
                                                  .getString("ERROR_PARSING_RULES"),
                                                  JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        try {
            Variables tmpVar = null;
            try {
                tmpVar = (Variables) variables.clone();
            } catch (CloneNotSupportedException e) {
            }
            buyRule = Parser.parse(tmpVar, buyRuleString);
        } catch (ExpressionException e) {
            buyRule = null;
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("ERROR_PARSING_BUY_RULE", 
                                                                   e.getReason()), 
                                                  Locale.getString("ERROR_PARSING_RULES"),
                                                  JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        try {
            Variables tmpVar = null;
            try {
                tmpVar = (Variables) variables.clone();
            } catch (CloneNotSupportedException e) {
            }
            sellRule = Parser.parse(tmpVar, sellRuleString);
        } catch (ExpressionException e) {
            sellRule = null;
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("ERROR_PARSING_SELL_RULE", 
                                                                   e.getReason()), 
                                                  Locale.getString("ERROR_PARSING_RULES"),
                                                  JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        // Now try reading the ranges
        
        try {
            //if (!aRangeTextField.getText().equals(""))
            //    aRange = Integer.parseInt(aRangeTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop, 
                                                  Locale.getString("ERROR_PARSING_NUMBER", 
                                                                   e.getMessage()), 
                                                  Locale.getString("ERROR_PARSING_RULES"),
                                                  JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Noramlise ranges
        //if (aRange <= 0)
        //    aRange = 1;
        
        return true;
    }
    
    public JComponent getComponent() {
		return this;
    }
    
    public String getTitle() {
        return Locale.getString("RULES_PAGE_TITLE");
    }

    /**
     * Return the parsed buy rule expression.
     *
     * @return the buy rule
     */
    public Expression getBuyRule() {
        return buyRule;
    }
    
    /**
     * Return the parsed sell rule expression.
     *
     * @return the sell rule
     */
    public Expression getSellRule() {
        return sellRule;
    }
    
    /**
     * Return the A range parameter.
     *
     * @return the maximum value of A
     */
    //public int getARange() {
    //    return aRange;
    //}
    
    private void layoutPage() {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Rules panel
        {
            TitledBorder equationTitled = new TitledBorder(Locale.getString("RULES_PAGE_TITLE"));
            JPanel panel = new JPanel();
            panel.setBorder(equationTitled);
            panel.setLayout(new BorderLayout());
            
            JPanel innerPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            innerPanel.setLayout(gridbag);
            
            c.weightx = 1.0;
            c.ipadx = 5;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            
            buyRuleEquationComboBox = GridBagHelper.addEquationRow(innerPanel,
                                                                   Locale.getString("BUY_RULE"), "",
                                                                   gridbag, c);
            sellRuleEquationComboBox = GridBagHelper.addEquationRow(innerPanel,
                                                                    Locale.getString("SELL_RULE"), 
                                                                    "", gridbag, c);
            
            panel.add(innerPanel, BorderLayout.NORTH);
            add(panel);
        }
        
        // Parameters panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // GARulesPageModule is already declared as global variable
        GARulesPageModule.setLayout(new BoxLayout(GARulesPageModule, BoxLayout.Y_AXIS));
        
        JScrollPane upDownScrollPane = new JScrollPane(GARulesPageModule);
        upDownScrollPane.setLayout(new ScrollPaneLayout());
        add(upDownScrollPane);
    }
    
}