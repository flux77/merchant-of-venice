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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.TitledBorder;

import org.mov.analyser.ga.GAIndividual;
import org.mov.parser.Expression;
import org.mov.parser.ExpressionException;
import org.mov.parser.Parser;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.prefs.PreferencesManager;
import org.mov.ui.EquationComboBox;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;


public class GARulesPage extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;

    // Swing components
    private JCheckBox ruleFamilyEnabledCheckBox;
    private EquationComboBox buyRuleEquationComboBox;
    private EquationComboBox sellRuleEquationComboBox;
    private JTextField parameterTextField;
    private JTextField minValueTextField;
    private JTextField maxValueTextField;
    private JButton addParameterButton;
    
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
    public GARulesPage(JDesktopPane desktop,
                       double maxHeight) {
                           
        Dimension preferredSize = new Dimension();
        preferredSize.setSize(this.getPreferredSize().getWidth(), maxHeight/2);
        
        this.desktop = desktop;
        this.GARulesPageModule = new GARulesPageModule(desktop);
        layoutPage(preferredSize);
        
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
            else if (setting.equals("parameter"))
                parameterTextField.setText(value);
            else if (setting.equals("min_value"))
                minValueTextField.setText(value);
            else if (setting.equals("max_value"))
                maxValueTextField.setText(value);
        }
        
        HashMap settingsParam =
                PreferencesManager.loadAnalyserPageSettings(key + idStr);

        Iterator iteratorParam = settingsParam.keySet().iterator();

	while(iteratorParam.hasNext()) {
	    String settingParam = (String)iteratorParam.next();
	    String valueParam = (String)settingsParam.get((Object)settingParam);

            GARulesPageModule.load(valueParam);
        }
        
    }
    
    public void save(String key) {
        String idStr = "Parameters";

        HashMap settingsParam =
                PreferencesManager.loadAnalyserPageSettings(key + idStr);
        HashMap settings = new HashMap();

        GARulesPageModule.save(settingsParam, idStr);
        settings.put("buy_rule", buyRuleEquationComboBox.getEquationText());
        settings.put("sell_rule", sellRuleEquationComboBox.getEquationText());
        settings.put("parameter", parameterTextField.getText());
        settings.put("min_value", minValueTextField.getText());
        settings.put("max_value", maxValueTextField.getText());

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
        
        variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        // Insert all the parameters in variables.
        // We use lowestGAIndividual, but highestGAIndividual should be the same.
        // All the GAIndividual have the same parameters during all GA Algorithm,
        // they just differ one from another because of the values.
        GAIndividual lowestGAIndividual = this.getLowestIndividual();
        for (int ii=0; ii<lowestGAIndividual.size(); ii++)
            variables.add(lowestGAIndividual.parameter(ii), lowestGAIndividual.type(ii), Variable.CONSTANT);
        
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
     * Return the GAIndividual with the lowest parameters.
     *
     * @return the individual
     */
    public GAIndividual getLowestIndividual() {
        int sizeOfIndividual = GARulesPageModule.getRowCount();
        String[] parameters = new String[sizeOfIndividual];
        double[] values = new double[sizeOfIndividual];
        int[] types = new int[sizeOfIndividual];
        for (int ii=0; ii<sizeOfIndividual; ii++) {
            parameters[ii]=(String)GARulesPageModule.getValueAt(ii,GARulesPageModule.PARAMETER_COLUMN);
            String value=(String)GARulesPageModule.getValueAt(ii,GARulesPageModule.MIN_PARAMETER_COLUMN);
            try {
                values[ii] = Double.valueOf(value.trim()).doubleValue();
             } catch (NumberFormatException nfe) {
                 // it should never happen, because the numbers are already checked in input.
                return null;
            }
            // if there is a full stop in the number string,
            // interpret it as a FLOAT number
            if (value.indexOf('.')==-1) {
                types[ii]=Expression.INTEGER_TYPE;
            } else {
                types[ii]=Expression.FLOAT_TYPE;
            } 
        }
        GAIndividual retValue = new GAIndividual(parameters, values, types);
        return retValue;
    }
    
    /**
     * Return the GAIndividual with the highest parameters.
     *
     * @return the individual
     */
    public GAIndividual getHighestIndividual() {
        int sizeOfIndividual = GARulesPageModule.getRowCount();
        String[] parameters = new String[sizeOfIndividual];
        double[] values = new double[sizeOfIndividual];
        int[] types = new int[sizeOfIndividual];
        for (int ii=0; ii<sizeOfIndividual; ii++) {
            parameters[ii]=(String)GARulesPageModule.getValueAt(ii,GARulesPageModule.PARAMETER_COLUMN);
            String value=(String)GARulesPageModule.getValueAt(ii,GARulesPageModule.MAX_PARAMETER_COLUMN);
            try {
                values[ii] = Double.valueOf(value.trim()).doubleValue();
             } catch (NumberFormatException nfe) {
                 // it should never happen, because the numbers are already checked in input.
                return null;
            }
            // if there is a full stop in the number string,
            // interpret it as a FLOAT number
            if (value.indexOf('.')==-1) {
                types[ii]=Expression.INTEGER_TYPE;
            } else {
                types[ii]=Expression.FLOAT_TYPE;
            } 
        }
        GAIndividual retValue = new GAIndividual(parameters, values, types);
        return retValue;
    }
    
    private void addRow() {
        double dbl = 0.0D;
        String str = null;
        try {
            // Control if right numbers (minimum and maximum parameters' bounds)
            // are going to be inserted
            str = minValueTextField.getText().trim();
            dbl = Double.valueOf(str).doubleValue();
            str = maxValueTextField.getText().trim();
            dbl = Double.valueOf(str).doubleValue();
            // Insert new row with new parameter
            GARulesPageModule.addRow(parameterTextField.getText(),
                    minValueTextField.getText(), maxValueTextField.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showInternalMessageDialog(desktop, Locale.getString("ERROR_PARSING_NUMBER", str),
                                                Locale.getString("ERROR_PARSING_RULES"),
                                                JOptionPane.ERROR_MESSAGE);
        }

    }
    
    private void layoutPage(Dimension preferredSize) {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Rules panel
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
        
        // Parameters panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // GARulesPageModule is already declared as global variable
        GARulesPageModule.setLayout(new BoxLayout(GARulesPageModule, BoxLayout.Y_AXIS));
        
        JScrollPane upDownScrollPane = new JScrollPane(GARulesPageModule);
        upDownScrollPane.setLayout(new ScrollPaneLayout());
        upDownScrollPane.setPreferredSize(preferredSize);
        add(upDownScrollPane);
        
        // Text Boxes to add a new parameter
        JPanel innerPanelParam = new JPanel();
        innerPanel.setLayout(gridbag);

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        parameterTextField =
            GridBagHelper.addTextRow(innerPanelParam, Locale.getString("PARAMETER"), "",
                                     gridbag, c,
                                     7);

        minValueTextField =
            GridBagHelper.addTextRow(innerPanelParam,
                                     Locale.getString("MIN_PARAMETER"), "",
                                     gridbag, c,
                                     7);
        maxValueTextField =
            GridBagHelper.addTextRow(innerPanelParam,
                                     Locale.getString("MAX_PARAMETER"), "",
                                     gridbag, c, 7);
        

        add(innerPanelParam);
        
	// button used for inserting a new parameter
	JPanel buttonPanel = new JPanel();
	JButton addParameterButton = new JButton(Locale.getString("ADD_PARAMETER"));
        addParameterButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // add a row to the table with the new parameter
                    addRow();
                }
            });
	add(addParameterButton);
    }
    
}