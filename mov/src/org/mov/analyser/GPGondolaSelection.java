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
import java.lang.Float;
import java.lang.String;
import java.lang.System;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.ConfirmDialog;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class GPGondolaSelection extends JPanel implements AnalyserPage {

    private final static String fmt = "0.00#";
    private final static int MAX_CHARS_IN_TEXTBOXES = 6;
    private final static double PERCENT_DOUBLE = 100.0;
    private final static int PERCENT_INT = 10000;

    private JDesktopPane desktop;
    private Random random;
    
    // Swing components
    // Integer
    private JTextField percOrdinaryNumberTextField; //ordinary number has 50% probabilty as default
    // As default we don't generate DayOfYearExpression() or MonthExpression()
    // because it would make it easy for the GP to hook onto specific dates
    // where the market is low. By removing these it forces the GP
    // to use the stock data to generate buy/sell decisions.
    private JTextField percDayOfYearExpressionTextField;
    private JTextField percMonthExpressionTextField;
    private JTextField percDayExpressionTextField;
    private JTextField percDayOfWeekExpressionTextField;
    private JTextField percHeldTextField;
    private JTextField percOrderTextField;
    // Float or Integer
    private JTextField percFloatTextField;
    private JTextField percIntegerTextField;
    // Float Quote
    private JTextField percOpenTextField;
    private JTextField percLowTextField;
    private JTextField percHighTextField;
    private JTextField percCloseTextField;
    // Boolean
    private JTextField percNotExpressionTextField;
    private JTextField percEqualThanExpressionTextField;
    private JTextField percGreaterThanEqualExpressionTextField;
    private JTextField percGreaterThanExpressionTextField;
    private JTextField percLessThanEqualExpressionTextField;
    private JTextField percLessThanExpressionTextField;
    private JTextField percNotEqualExpressionTextField;
    private JTextField percAndExpressionTextField;
    private JTextField percOrExpressionTextField;
    // Expression
    private JTextField percCreateRandomTerminalTextField;
    private JTextField percAddExpressionTextField;
    private JTextField percSubtractExpressionTextField;
    private JTextField percMultiplyExpressionTextField;
    private JTextField percDivideExpressionTextField;
    private JTextField percPercentExpressionTextField;
    private JTextField percIfExpressionTextField;
    private JTextField percLagExpressionTextField;
    private JTextField percMinExpressionTextField;
    private JTextField percMaxExpressionTextField;
    private JTextField percSumExpressionTextField;
    private JTextField percSqrtExpressionTextField;
    private JTextField percAbsExpressionTextField;
    private JTextField percAvgExpressionTextField;

    // Parsed input - To set different default values you have to modify ONLY the following variables
    // Integer
    private int percOrdinaryNumber = 5000; //ordinary number has 50% probabilty as default
    // As default we don't generate DayOfYearExpression() or MonthExpression()
    // because it would make it easy for the GP to hook onto specific dates
    // where the market is low. By removing these it forces the GP
    // to use the stock data to generate buy/sell decisions.
    private int percDayOfYearExpression = 0;
    private int percMonthExpression = 0;
    private int percDayExpression = 1250;
    private int percDayOfWeekExpression = 1250;
    private int percHeld = 1250;
    private int percOrder = 1250;
    // Float or Integer
    private int percFloat = 5000;
    private int percInteger = 5000;
    // Float Quote
    private int percOpen = 2500;
    private int percLow = 2500;
    private int percHigh = 2500;
    private int percClose = 2500;
    // Boolean
    private int percNotExpression = 1112;
    private int percEqualThanExpression = 1111;
    private int percGreaterThanEqualExpression = 1111;
    private int percGreaterThanExpression = 1111;
    private int percLessThanEqualExpression = 1111;
    private int percLessThanExpression = 1111;
    private int percNotEqualExpression = 1111;
    private int percAndExpression = 1111;
    private int percOrExpression = 1111;
    // Expression
    private int percCreateRandomTerminal = 715;
    private int percAddExpression = 714;
    private int percSubtractExpression = 714;
    private int percMultiplyExpression = 714;
    private int percDivideExpression = 714;
    private int percPercentExpression = 714;
    private int percIfExpression = 714;
    private int percLagExpression = 714;
    private int percMinExpression = 715;
    private int percMaxExpression = 715;
    private int percSumExpression = 714;
    private int percSqrtExpression = 714;
    private int percAbsExpression = 714;
    private int percAvgExpression = 715;
    

    public GPGondolaSelection(JDesktopPane desktop) {
        this.desktop = desktop;
        random = new Random(System.currentTimeMillis());
        
        layoutPage();
        
        setTexts();
    }
    
    public int getRandomToGenerateInteger(boolean allowHeld, boolean allowOrder) {
        int[][] borders = {
                              { 0, percOrdinaryNumber },
                              { percOrdinaryNumber, percOrdinaryNumber+percDayOfYearExpression },
                              { percOrdinaryNumber+percDayOfYearExpression, percOrdinaryNumber+percDayOfYearExpression+percMonthExpression },
                              { percOrdinaryNumber+percDayOfYearExpression+percMonthExpression, percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression },
                              { percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression, percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression+percDayOfWeekExpression },
                              { percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression+percDayOfWeekExpression, percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression+percDayOfWeekExpression+percHeld },
                              { percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression+percDayOfWeekExpression+percHeld, percOrdinaryNumber+percDayOfYearExpression+percMonthExpression+percDayExpression+percDayOfWeekExpression+percHeld+percOrder },
                          };
        int total = borders[borders.length-1][1];
        if(!allowHeld)
            total -= percHeld;
        if(!allowOrder)
            total -= percOrder;
        int randomValue = random.nextInt(total);
        
        for (int i=0; i<borders.length; i++) {
            if ((randomValue >= borders[i][0]) && (randomValue < borders[i][1])) {
                return i;
            } 
        }
        return 0;
    }

    public int getRandomToGenerateFloatInteger() {
        int[][] borders = {
                              { 0, percFloat },
                              { percFloat, percFloat+percInteger }
                          };
        int total = borders[borders.length-1][1];
        int randomValue = random.nextInt(total);
        
        for (int i=0; i<borders.length; i++) {
            if ((randomValue >= borders[i][0]) && (randomValue < borders[i][1])) {
                return i;
            } 
        }
        return 0;
    }

    public int getRandomToGenerateFloatQuote() {
        int[][] borders = {
                              { 0, percOpen },
                              { percOpen, percOpen+percLow },
                              { percOpen+percLow, percOpen+percLow+percHigh },
                              { percOpen+percLow+percHigh, percOpen+percLow+percHigh+percClose }
                          };
        int total = borders[borders.length-1][1];
        int randomValue = random.nextInt(total);
        
        for (int i=0; i<borders.length; i++) {
            if ((randomValue >= borders[i][0]) && (randomValue < borders[i][1])) {
                return i;
            } 
        }
        return 0;
    }

    public int getRandomToGenerateBoolean() {
        int[][] borders = {
                              { 0, percNotExpression },
                              { percNotExpression, percNotExpression+percEqualThanExpression },
                              { percNotExpression+percEqualThanExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression+percNotEqualExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression+percNotEqualExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression+percNotEqualExpression+percAndExpression },
                              { percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression+percNotEqualExpression+percAndExpression, percNotExpression+percEqualThanExpression+percGreaterThanEqualExpression+percGreaterThanExpression+percLessThanEqualExpression+percLessThanExpression+percNotEqualExpression+percAndExpression+percOrExpression }
                          };
        int total = borders[borders.length-1][1];
        int randomValue = random.nextInt(total);
        
        for (int i=0; i<borders.length; i++) {
            if ((randomValue >= borders[i][0]) && (randomValue < borders[i][1])) {
                return i;
            } 
        }
        return 0;
    }

    public int getRandomToGenerateExpression() {
        int[][] borders = {
                              { 0, percCreateRandomTerminal },
                              { percCreateRandomTerminal, percCreateRandomTerminal+percAddExpression },
                              { percCreateRandomTerminal+percAddExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression+percSqrtExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression+percSqrtExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression+percSqrtExpression+percAbsExpression },
                              { percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression+percSqrtExpression+percAbsExpression, percCreateRandomTerminal+percAddExpression+percSubtractExpression+percMultiplyExpression+percDivideExpression+percPercentExpression+percIfExpression+percLagExpression+percMinExpression+percMaxExpression+percSumExpression+percSqrtExpression+percAbsExpression+percAvgExpression }
                          };
        int total = borders[borders.length-1][1];
        int randomValue = random.nextInt(total);
        
        for (int i=0; i<borders.length; i++) {
            if ((randomValue >= borders[i][0]) && (randomValue < borders[i][1])) {
                return i;
            } 
        }
        return 0;
    }

    public void load(String key) {
        // Load last GUI settings from preferences
	HashMap settings =
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

            // Integer
	    if(setting.equals("perc_ordinary_number"))
                percOrdinaryNumberTextField.setText(value);
	    else if(setting.equals("perc_day_of_year_expression"))
                percDayOfYearExpressionTextField.setText(value);
	    else if(setting.equals("perc_month_expression"))
		percMonthExpressionTextField.setText(value);
	    else if(setting.equals("perc_day_expression"))
		percDayExpressionTextField.setText(value);
	    else if(setting.equals("perc_day_of_week_expression"))
		percDayOfWeekExpressionTextField.setText(value);
	    else if(setting.equals("perc_held"))
		percHeldTextField.setText(value);
	    else if(setting.equals("perc_order"))
		percOrderTextField.setText(value);
            // Float Quote
	    else if(setting.equals("perc_open"))
		percOpenTextField.setText(value);
	    else if(setting.equals("perc_low"))
		percLowTextField.setText(value);
	    else if(setting.equals("perc_high"))
		percHighTextField.setText(value);
	    else if(setting.equals("perc_close"))
		percCloseTextField.setText(value);
            // Boolean
	    else if(setting.equals("perc_not_expression"))
		percNotExpressionTextField.setText(value);
	    else if(setting.equals("perc_equal_than_expression"))
		percEqualThanExpressionTextField.setText(value);
	    else if(setting.equals("perc_greater_than_equal_expression"))
		percGreaterThanEqualExpressionTextField.setText(value);
	    else if(setting.equals("perc_greater_than_expression"))
		percGreaterThanExpressionTextField.setText(value);
	    else if(setting.equals("perc_less_than_equal_expression"))
		percLessThanEqualExpressionTextField.setText(value);
	    else if(setting.equals("perc_less_than_expression"))
		percLessThanExpressionTextField.setText(value);
	    else if(setting.equals("perc_not_equal_expression"))
		percNotEqualExpressionTextField.setText(value);
	    else if(setting.equals("perc_and_expression"))
		percAndExpressionTextField.setText(value);
	    else if(setting.equals("perc_or_expression"))
		percOrExpressionTextField.setText(value);
            // Expression
	    else if(setting.equals("perc_create_random_terminal"))
		percCreateRandomTerminalTextField.setText(value);
	    else if(setting.equals("perc_add_expression"))
		percAddExpressionTextField.setText(value);
	    else if(setting.equals("perc_subtract_expression"))
		percSubtractExpressionTextField.setText(value);
	    else if(setting.equals("perc_multiply_expression"))
		percMultiplyExpressionTextField.setText(value);
	    else if(setting.equals("perc_divide_expression"))
		percDivideExpressionTextField.setText(value);
	    else if(setting.equals("perc_percent_expression"))
		percPercentExpressionTextField.setText(value);
	    else if(setting.equals("perc_if_expression"))
		percIfExpressionTextField.setText(value);
	    else if(setting.equals("perc_lag_expression"))
		percLagExpressionTextField.setText(value);
	    else if(setting.equals("perc_min_expression"))
		percMinExpressionTextField.setText(value);
	    else if(setting.equals("perc_max_expression"))
		percMaxExpressionTextField.setText(value);
	    else if(setting.equals("perc_sum_expression"))
		percSumExpressionTextField.setText(value);
	    else if(setting.equals("perc_sqrt_expression"))
		percSqrtExpressionTextField.setText(value);
	    else if(setting.equals("perc_abs_expression"))
		percAbsExpressionTextField.setText(value);
	    else if(setting.equals("perc_avg_expression"))
		percAvgExpressionTextField.setText(value);
            // Float or Integer
	    else if(setting.equals("perc_float"))
		percFloatTextField.setText(value);
	    else if(setting.equals("perc_integer"))
		percIntegerTextField.setText(value);
            else
                assert false;
        }
    }

    public void save(String key) {
        HashMap settings = new HashMap();

        // Integer
	settings.put("perc_ordinary_number", percOrdinaryNumberTextField.getText());
	settings.put("perc_day_of_year_expression", percDayOfYearExpressionTextField.getText());
	settings.put("perc_month_expression", percMonthExpressionTextField.getText());
	settings.put("perc_day_expression", percDayExpressionTextField.getText());
	settings.put("perc_day_of_week_expression", percDayOfWeekExpressionTextField.getText());
	settings.put("perc_held", percHeldTextField.getText());
	settings.put("perc_order", percOrderTextField.getText());
        // Float Quote
	settings.put("perc_open", percOpenTextField.getText());
	settings.put("perc_low", percLowTextField.getText());
	settings.put("perc_high", percHighTextField.getText());
	settings.put("perc_close", percCloseTextField.getText());
        // Boolean
	settings.put("perc_not_expression", percNotExpressionTextField.getText());
	settings.put("perc_equal_than_expression", percEqualThanExpressionTextField.getText());
	settings.put("perc_greater_than_equal_expression", percGreaterThanEqualExpressionTextField.getText());
	settings.put("perc_greater_than_expression", percGreaterThanExpressionTextField.getText());
	settings.put("perc_less_than_equal_expression", percLessThanEqualExpressionTextField.getText());
	settings.put("perc_less_than_expression", percLessThanExpressionTextField.getText());
	settings.put("perc_not_equal_expression", percNotEqualExpressionTextField.getText());
	settings.put("perc_and_expression", percAndExpressionTextField.getText());
	settings.put("perc_or_expression", percOrExpressionTextField.getText());
        // Expression
	settings.put("perc_create_random_terminal", percCreateRandomTerminalTextField.getText());
	settings.put("perc_add_expression", percAddExpressionTextField.getText());
	settings.put("perc_subtract_expression", percSubtractExpressionTextField.getText());
	settings.put("perc_multiply_expression", percMultiplyExpressionTextField.getText());
	settings.put("perc_divide_expression", percDivideExpressionTextField.getText());
	settings.put("perc_percent_expression", percPercentExpressionTextField.getText());
	settings.put("perc_if_expression", percIfExpressionTextField.getText());
	settings.put("perc_lag_expression", percLagExpressionTextField.getText());
	settings.put("perc_min_expression", percMinExpressionTextField.getText());
	settings.put("perc_max_expression", percMaxExpressionTextField.getText());
	settings.put("perc_sum_expression", percSumExpressionTextField.getText());
	settings.put("perc_sqrt_expression", percSqrtExpressionTextField.getText());
	settings.put("perc_abs_expression", percAbsExpressionTextField.getText());
	settings.put("perc_avg_expression", percAvgExpressionTextField.getText());
        // Float or Integer
	settings.put("perc_float", percFloatTextField.getText());
	settings.put("perc_integer", percIntegerTextField.getText());
        
        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }

    public boolean parse() {
        try {
            setNumericalValues();
        }
	catch(ParseException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ERROR_PARSING_NUMBER",
                                                                   e.getMessage()),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

        if(!isAllValuesPositive()) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_POSITIVE_VALUES_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(!isTotalOK()) {
            // Messages inside the isTotalOK method
	    return false;
        }

        if(!isFitAll()) {
            ConfirmDialog dialog = new ConfirmDialog(desktop,
                                                     Locale.getString("GP_FIT"),
                                                     Locale.getString("GP_FIT_TITLE"));
            boolean returnConfirm = dialog.showDialog();
            if (returnConfirm)
                fitAll();
            else
                return false;
        }

        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("GP_GONDOLA_SELECTION_SHORT_TITLE");
    }

    private void setTexts() {
        DecimalFormat df = new DecimalFormat(fmt);
        // Integer
        percOrdinaryNumberTextField.setText(df.format(percOrdinaryNumber/PERCENT_DOUBLE));
        percDayOfYearExpressionTextField.setText(df.format(percDayOfYearExpression/PERCENT_DOUBLE));
        percMonthExpressionTextField.setText(df.format(percMonthExpression/PERCENT_DOUBLE));
        percDayExpressionTextField.setText(df.format(percDayExpression/PERCENT_DOUBLE));
        percDayOfWeekExpressionTextField.setText(df.format(percDayOfWeekExpression/PERCENT_DOUBLE));
        percHeldTextField.setText(df.format(percHeld/PERCENT_DOUBLE));
        percOrderTextField.setText(df.format(percOrder/PERCENT_DOUBLE));
        // Float Quote
        percOpenTextField.setText(df.format(percOpen/PERCENT_DOUBLE));
        percLowTextField.setText(df.format(percLow/PERCENT_DOUBLE));
        percHighTextField.setText(df.format(percHigh/PERCENT_DOUBLE));
        percCloseTextField.setText(df.format(percClose/PERCENT_DOUBLE));
        // Boolean
        percNotExpressionTextField.setText(df.format(percNotExpression/PERCENT_DOUBLE));
        percEqualThanExpressionTextField.setText(df.format(percEqualThanExpression/PERCENT_DOUBLE));
        percGreaterThanEqualExpressionTextField.setText(df.format(percGreaterThanEqualExpression/PERCENT_DOUBLE));
        percGreaterThanExpressionTextField.setText(df.format(percGreaterThanExpression/PERCENT_DOUBLE));
        percLessThanEqualExpressionTextField.setText(df.format(percLessThanEqualExpression/PERCENT_DOUBLE));
        percLessThanExpressionTextField.setText(df.format(percLessThanExpression/PERCENT_DOUBLE));
        percNotEqualExpressionTextField.setText(df.format(percNotEqualExpression/PERCENT_DOUBLE));
        percAndExpressionTextField.setText(df.format(percAndExpression/PERCENT_DOUBLE));
        percOrExpressionTextField.setText(df.format(percOrExpression/PERCENT_DOUBLE));
        // Expression
        percCreateRandomTerminalTextField.setText(df.format(percCreateRandomTerminal/PERCENT_DOUBLE));
        percAddExpressionTextField.setText(df.format(percAddExpression/PERCENT_DOUBLE));
        percSubtractExpressionTextField.setText(df.format(percSubtractExpression/PERCENT_DOUBLE));
        percMultiplyExpressionTextField.setText(df.format(percMultiplyExpression/PERCENT_DOUBLE));
        percDivideExpressionTextField.setText(df.format(percDivideExpression/PERCENT_DOUBLE));
        percPercentExpressionTextField.setText(df.format(percPercentExpression/PERCENT_DOUBLE));
        percIfExpressionTextField.setText(df.format(percIfExpression/PERCENT_DOUBLE));
        percLagExpressionTextField.setText(df.format(percLagExpression/PERCENT_DOUBLE));
        percMinExpressionTextField.setText(df.format(percMinExpression/PERCENT_DOUBLE));
        percMaxExpressionTextField.setText(df.format(percMaxExpression/PERCENT_DOUBLE));
        percSumExpressionTextField.setText(df.format(percSumExpression/PERCENT_DOUBLE));
        percSqrtExpressionTextField.setText(df.format(percSqrtExpression/PERCENT_DOUBLE));
        percAbsExpressionTextField.setText(df.format(percAbsExpression/PERCENT_DOUBLE));
        percAvgExpressionTextField.setText(df.format(percAvgExpression/PERCENT_DOUBLE));
        // Float or Integer
        percFloatTextField.setText(df.format(percFloat/PERCENT_DOUBLE));
        percIntegerTextField.setText(df.format(percInteger/PERCENT_DOUBLE));
    }
    
    private void setNumericalValues() throws ParseException {
        // Integer
        percOrdinaryNumber = 0;
        percDayOfYearExpression = 0;
        percMonthExpression = 0;
        percDayExpression = 0;
        percDayOfWeekExpression = 0;
        percHeld = 0;
        percOrder = 0;
        // Float Quote
        percOpen = 0;
        percLow = 0;
        percHigh = 0;
        percClose = 0;
        // Boolean
        percNotExpression = 0;
        percEqualThanExpression = 0;
        percGreaterThanEqualExpression = 0;
        percGreaterThanExpression = 0;
        percLessThanEqualExpression = 0;
        percLessThanExpression = 0;
        percNotEqualExpression = 0;
        percAndExpression = 0;
        percOrExpression = 0;
        // Expression
        percCreateRandomTerminal = 0;
        percAddExpression = 0;
        percSubtractExpression = 0;
        percMultiplyExpression = 0;
        percDivideExpression = 0;
        percPercentExpression = 0;
        percIfExpression = 0;
        percLagExpression = 0;
        percMinExpression = 0;
        percMaxExpression = 0;
        percSumExpression = 0;
        percSqrtExpression = 0;
        percAbsExpression = 0;
        percAvgExpression = 0;
        // Float or Integer
        percFloat = 0;
        percInteger = 0;
    
        // df manage the localization.
        DecimalFormat df = new DecimalFormat(fmt);
        // Integer
        if(!percOrdinaryNumberTextField.getText().equals("")) {
            percOrdinaryNumber =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percOrdinaryNumberTextField.getText()).doubleValue()));
        }
        if(!percDayOfYearExpressionTextField.getText().equals(""))
            percDayOfYearExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percDayOfYearExpressionTextField.getText()).doubleValue()));
        if(!percMonthExpressionTextField.getText().equals(""))
            percMonthExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percMonthExpressionTextField.getText()).doubleValue()));
        if(!percDayExpressionTextField.getText().equals(""))
            percDayExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percDayExpressionTextField.getText()).doubleValue()));
        if(!percDayOfWeekExpressionTextField.getText().equals(""))
            percDayOfWeekExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percDayOfWeekExpressionTextField.getText()).doubleValue()));
        if(!percHeldTextField.getText().equals(""))
            percHeld =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percHeldTextField.getText()).doubleValue()));
        if(!percOrderTextField.getText().equals(""))
            percOrder =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percOrderTextField.getText()).doubleValue()));

        // Float Quote
        if(!percOpenTextField.getText().equals(""))
            percOpen =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percOpenTextField.getText()).doubleValue()));
        if(!percLowTextField.getText().equals(""))
            percLow =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percLowTextField.getText()).doubleValue()));
        if(!percHighTextField.getText().equals(""))
            percHigh =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percHighTextField.getText()).doubleValue()));
        if(!percCloseTextField.getText().equals(""))
            percClose =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percCloseTextField.getText()).doubleValue()));

        // Boolean
        if(!percNotExpressionTextField.getText().equals(""))
            percNotExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percNotExpressionTextField.getText()).doubleValue()));
        if(!percEqualThanExpressionTextField.getText().equals(""))
            percEqualThanExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percEqualThanExpressionTextField.getText()).doubleValue()));
        if(!percGreaterThanEqualExpressionTextField.getText().equals(""))
            percGreaterThanEqualExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percGreaterThanEqualExpressionTextField.getText()).doubleValue()));
        if(!percGreaterThanExpressionTextField.getText().equals(""))
            percGreaterThanExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percGreaterThanExpressionTextField.getText()).doubleValue()));
        if(!percLessThanEqualExpressionTextField.getText().equals(""))
            percLessThanEqualExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percLessThanEqualExpressionTextField.getText()).doubleValue()));
        if(!percLessThanExpressionTextField.getText().equals(""))
            percLessThanExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percLessThanExpressionTextField.getText()).doubleValue()));
        if(!percNotEqualExpressionTextField.getText().equals(""))
            percNotEqualExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percNotEqualExpressionTextField.getText()).doubleValue()));
        if(!percAndExpressionTextField.getText().equals(""))
            percAndExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percAndExpressionTextField.getText()).doubleValue()));
        if(!percOrExpressionTextField.getText().equals(""))
            percOrExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percOrExpressionTextField.getText()).doubleValue()));

        // Expression
        if(!percCreateRandomTerminalTextField.getText().equals(""))
            percCreateRandomTerminal =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percCreateRandomTerminalTextField.getText()).doubleValue()));
        if(!percAddExpressionTextField.getText().equals(""))
            percAddExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percAddExpressionTextField.getText()).doubleValue()));
        if(!percSubtractExpressionTextField.getText().equals(""))
            percSubtractExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percSubtractExpressionTextField.getText()).doubleValue()));
        if(!percMultiplyExpressionTextField.getText().equals(""))
            percMultiplyExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percMultiplyExpressionTextField.getText()).doubleValue()));
        if(!percDivideExpressionTextField.getText().equals(""))
            percDivideExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percDivideExpressionTextField.getText()).doubleValue()));
        if(!percPercentExpressionTextField.getText().equals(""))
            percPercentExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percPercentExpressionTextField.getText()).doubleValue()));
        if(!percIfExpressionTextField.getText().equals(""))
            percIfExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percIfExpressionTextField.getText()).doubleValue()));
        if(!percLagExpressionTextField.getText().equals(""))
            percLagExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percLagExpressionTextField.getText()).doubleValue()));
        if(!percMinExpressionTextField.getText().equals(""))
            percMinExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percMinExpressionTextField.getText()).doubleValue()));
        if(!percMaxExpressionTextField.getText().equals(""))
            percMaxExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percMaxExpressionTextField.getText()).doubleValue()));
        if(!percSumExpressionTextField.getText().equals(""))
            percSumExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percSumExpressionTextField.getText()).doubleValue()));
        if(!percSqrtExpressionTextField.getText().equals(""))
            percSqrtExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percSqrtExpressionTextField.getText()).doubleValue()));
        if(!percAbsExpressionTextField.getText().equals(""))
            percAbsExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percAbsExpressionTextField.getText()).doubleValue()));
        if(!percAvgExpressionTextField.getText().equals(""))
            percAvgExpression =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percAvgExpressionTextField.getText()).doubleValue()));

        // Float or Integer
        if(!percFloatTextField.getText().equals(""))
            percFloat =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percFloatTextField.getText()).doubleValue()));
        if(!percIntegerTextField.getText().equals(""))
            percInteger =
                (int) Math.round(PERCENT_DOUBLE*(df.parse(percIntegerTextField.getText()).doubleValue()));
    }

    private boolean isAllValuesPositive() {
        boolean returnValue = true;
        returnValue = returnValue
                      && (percOrdinaryNumber>=0)
                      && (percDayOfYearExpression>=0)
                      && (percMonthExpression>=0)
                      && (percDayExpression>=0)
                      && (percDayOfWeekExpression>=0)
                      && (percHeld>=0)
                      && (percOrder>=0)
                      && (percFloat>=0)
                      && (percInteger>=0)
                      && (percOpen>=0)
                      && (percLow>=0)
                      && (percHigh>=0)
                      && (percClose>=0)
                      && (percNotExpression>=0)
                      && (percEqualThanExpression>=0)
                      && (percGreaterThanEqualExpression>=0)
                      && (percGreaterThanExpression>=0)
                      && (percLessThanEqualExpression>=0)
                      && (percLessThanExpression>=0)
                      && (percNotEqualExpression>=0)
                      && (percAndExpression>=0)
                      && (percOrExpression>=0)
                      && (percCreateRandomTerminal>=0)
                      && (percAddExpression>=0)
                      && (percSubtractExpression>=0)
                      && (percMultiplyExpression>=0)
                      && (percDivideExpression>=0)
                      && (percPercentExpression>=0)
                      && (percIfExpression>=0)
                      && (percLagExpression>=0)
                      && (percMinExpression>=0)
                      && (percMaxExpression>=0)
                      && (percSumExpression>=0)
                      && (percSqrtExpression>=0)
                      && (percAbsExpression>=0)
                      && (percAvgExpression>=0);
        return returnValue;
    }

    private boolean isTotalOK() {
        // We should consider the absence of held and order -> totalIntegerModified
        long totalIntegerModified = new Long(percOrdinaryNumber
                                + percDayOfYearExpression
                                + percMonthExpression
                                + percDayExpression
                                + percDayOfWeekExpression).longValue();
        long totalInteger = new Long(percOrdinaryNumber
                                + percDayOfYearExpression
                                + percMonthExpression
                                + percDayExpression
                                + percDayOfWeekExpression
                                + percHeld
                                + percOrder).longValue();
        long totalFloatInteger = new Long(percFloat
                                + percInteger).longValue();
        long totalFloatQuote = new Long(percOpen
                                + percLow
                                + percHigh
                                + percClose).longValue();
        long totalBoolean = new Long(percNotExpression
                                + percEqualThanExpression
                                + percGreaterThanEqualExpression
                                + percGreaterThanExpression
                                + percLessThanEqualExpression
                                + percLessThanExpression
                                + percNotEqualExpression
                                + percAndExpression
                                + percOrExpression).longValue();
        long totalExpression = new Long(percCreateRandomTerminal
                                + percAddExpression
                                + percSubtractExpression
                                + percMultiplyExpression
                                + percDivideExpression
                                + percPercentExpression
                                + percIfExpression
                                + percLagExpression
                                + percMinExpression
                                + percMaxExpression
                                + percSumExpression
                                + percSqrtExpression
                                + percAbsExpression
                                + percAvgExpression).longValue();
        long[] total = { totalIntegerModified,
                         totalInteger,
                         totalFloatInteger,
                         totalFloatQuote,
                         totalBoolean,
                         totalExpression,
                       };
        // Check total == 0
        for (int i=0; i<total.length; i++) {
            if (total[i]==0) {
                JOptionPane.showInternalMessageDialog(desktop,
                                                      Locale.getString("NO_TOTAL_GREATER_THAN_ZERO_ERROR"),
                                                      Locale.getString("INVALID_GP_ERROR"),
                                                      JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // Check total < MaxInt
        for (int i=0; i<total.length; i++) {
            if (total[i]>new Long(Integer.MAX_VALUE).longValue()) {
                JOptionPane.showInternalMessageDialog(desktop,
                                                      Locale.getString("NO_TOTAL_LOWER_THAN_MAX_INT_ERROR"),
                                                      Locale.getString("INVALID_GP_ERROR"),
                                                      JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private boolean isFitAll() {
        int totalInteger = percOrdinaryNumber
                                + percDayOfYearExpression
                                + percMonthExpression
                                + percDayExpression
                                + percDayOfWeekExpression
                                + percHeld
                                + percOrder;
        int totalFloatInteger = percFloat
                                + percInteger;
        int totalFloatQuote = percOpen
                                + percLow
                                + percHigh
                                + percClose;
        int totalBoolean = percNotExpression
                                + percEqualThanExpression
                                + percGreaterThanEqualExpression
                                + percGreaterThanExpression
                                + percLessThanEqualExpression
                                + percLessThanExpression
                                + percNotEqualExpression
                                + percAndExpression
                                + percOrExpression;
        int totalExpression = percCreateRandomTerminal
                                + percAddExpression
                                + percSubtractExpression
                                + percMultiplyExpression
                                + percDivideExpression
                                + percPercentExpression
                                + percIfExpression
                                + percLagExpression
                                + percMinExpression
                                + percMaxExpression
                                + percSumExpression
                                + percSqrtExpression
                                + percAbsExpression
                                + percAvgExpression;
        int[] total = { totalInteger,
                        totalFloatInteger,
                        totalFloatQuote,
                        totalBoolean,
                        totalExpression,
                      };
        // Check total != PERCENT_INT
        for (int i=0; i<total.length; i++) {
            if (total[i]!=PERCENT_INT) {
                return false;
            }
        }
        return true;
    }

    private void fitAll() {
        if (isAllValuesAcceptable()) {
            fitInteger();
            fitFloatInteger();
            fitFloatQuote();
            fitBoolean();
            fitExpression();
        }
    }

    // Fit the values to PERCENT_INT, if they differ
    private void fitInteger() {
        if (isAllValuesAcceptable()) {
            int total = percOrdinaryNumber
                        + percDayOfYearExpression
                        + percMonthExpression
                        + percDayExpression
                        + percDayOfWeekExpression
                        + percHeld
                        + percOrder;
            // Set dummy values according to PERCENT_INT that is the maximum
            int dummyPercOrdinaryNumber = Math.round((percOrdinaryNumber * PERCENT_INT) / total);
            int dummyPercDayOfYearExpression = Math.round((percDayOfYearExpression * PERCENT_INT) / total);
            int dummyPercMonthExpression = Math.round((percMonthExpression * PERCENT_INT) / total);
            int dummyPercDayExpression = Math.round((percDayExpression * PERCENT_INT) / total);
            int dummyPercDayOfWeekExpression = Math.round((percDayOfWeekExpression * PERCENT_INT) / total);
            int dummyPercHeld = Math.round((percHeld * PERCENT_INT) / total);
            int dummyPercOrder = Math.round((percOrder * PERCENT_INT) / total);
            int dummyTotal = dummyPercOrdinaryNumber
                        + dummyPercDayOfYearExpression
                        + dummyPercMonthExpression
                        + dummyPercDayExpression
                        + dummyPercDayOfWeekExpression
                        + dummyPercHeld
                        + dummyPercOrder;
            // Adjust approximations of Math.round method
            if (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPercOrdinaryNumber -= (dummyTotal-PERCENT_INT);
                } else {
                    dummyPercOrdinaryNumber += (PERCENT_INT-dummyTotal);
                }
            }
            // Set new values
            percOrdinaryNumber = dummyPercOrdinaryNumber;
            percDayOfYearExpression = dummyPercDayOfYearExpression;
            percMonthExpression = dummyPercMonthExpression;
            percDayExpression = dummyPercDayExpression;
            percDayOfWeekExpression = dummyPercDayOfWeekExpression;
            percHeld = dummyPercHeld;
            percOrder = dummyPercOrder;
            // Update the text in the user interface
            setTexts();
        }
    }

    // Fit the values to PERCENT_INT, if they differ
    private void fitFloatInteger() {
        if (isAllValuesAcceptable()) {
            int total = percFloat
                        + percInteger;
            // Set dummy values according to PERCENT_INT that is the maximum
            int dummyPercFloat = Math.round((percFloat * PERCENT_INT) / total);
            int dummyPercInteger = Math.round((percInteger * PERCENT_INT) / total);
            int dummyTotal = dummyPercFloat
                             + dummyPercInteger;
            // Adjust approximations of Math.round method
            if (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPercFloat -= (dummyTotal-PERCENT_INT);
                } else {
                    dummyPercFloat += (PERCENT_INT-dummyTotal);
                }
            }
            // Set new values
            percFloat = dummyPercFloat;
            percInteger = dummyPercInteger;
            // Update the text in the user interface
            setTexts();
        }
    }

    // Fit the values to PERCENT_INT, if they differ
    private void fitFloatQuote() {
        if (isAllValuesAcceptable()) {
            int total = percOpen
                        + percLow
                        + percHigh
                        + percClose;
            // Set dummy values according to PERCENT_INT that is the maximum
            int dummyPercOpen = Math.round((percOpen * PERCENT_INT) / total);
            int dummyPercLow = Math.round((percLow * PERCENT_INT) / total);
            int dummyPercHigh = Math.round((percHigh * PERCENT_INT) / total);
            int dummyPercClose = Math.round((percClose * PERCENT_INT) / total);
            int dummyTotal = dummyPercOpen
                        + dummyPercLow
                        + dummyPercHigh
                        + dummyPercClose;
            // Adjust approximations of Math.round method
            if (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPercOpen -= (dummyTotal-PERCENT_INT);
                } else {
                    dummyPercOpen += (PERCENT_INT-dummyTotal);
                }
            }
            // Set new values
            percOpen = dummyPercOpen;
            percLow = dummyPercLow;
            percHigh = dummyPercHigh;
            percClose = dummyPercClose;
            // Update the text in the user interface
            setTexts();
        }
    }

    // Fit the values to PERCENT_INT, if they differ
    private void fitBoolean() {
        if (isAllValuesAcceptable()) {
            int total = percNotExpression
                        + percEqualThanExpression
                        + percGreaterThanEqualExpression
                        + percGreaterThanExpression
                        + percLessThanEqualExpression
                        + percLessThanExpression
                        + percNotEqualExpression
                        + percAndExpression
                        + percOrExpression;
            // Set dummy values according to PERCENT_INT that is the maximum
            int dummyPercNotExpression = Math.round((percNotExpression * PERCENT_INT) / total);
            int dummyPercEqualThanExpression = Math.round((percEqualThanExpression * PERCENT_INT) / total);
            int dummyPercGreaterThanEqualExpression = Math.round((percGreaterThanEqualExpression * PERCENT_INT) / total);
            int dummyPercGreaterThanExpression = Math.round((percGreaterThanExpression * PERCENT_INT) / total);
            int dummyPercLessThanEqualExpression = Math.round((percLessThanEqualExpression * PERCENT_INT) / total);
            int dummyPercLessThanExpression = Math.round((percLessThanExpression * PERCENT_INT) / total);
            int dummyPercNotEqualExpression = Math.round((percNotEqualExpression * PERCENT_INT) / total);
            int dummyPercAndExpression = Math.round((percAndExpression * PERCENT_INT) / total);
            int dummyPercOrExpression = Math.round((percOrExpression * PERCENT_INT) / total);
            int dummyTotal = dummyPercNotExpression
                        + dummyPercEqualThanExpression
                        + dummyPercGreaterThanEqualExpression
                        + dummyPercGreaterThanExpression
                        + dummyPercLessThanEqualExpression
                        + dummyPercLessThanExpression
                        + dummyPercNotEqualExpression
                        + dummyPercAndExpression
                        + dummyPercOrExpression;
            // Adjust approximations of Math.round method
            if (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPercNotExpression -= (dummyTotal-PERCENT_INT);
                } else {
                    dummyPercNotExpression += (PERCENT_INT-dummyTotal);
                }
            }
            // Set new values
            percNotExpression = dummyPercNotExpression;
            percEqualThanExpression = dummyPercEqualThanExpression;
            percGreaterThanEqualExpression = dummyPercGreaterThanEqualExpression;
            percGreaterThanExpression = dummyPercGreaterThanExpression;
            percLessThanEqualExpression = dummyPercLessThanEqualExpression;
            percLessThanExpression = dummyPercLessThanExpression;
            percNotEqualExpression = dummyPercNotEqualExpression;
            percAndExpression = dummyPercAndExpression;
            percOrExpression = dummyPercOrExpression;
            // Update the text in the user interface
            setTexts();
        }
    }

    // Fit the values to PERCENT_INT, if they differ
    private void fitExpression() {
        if (isAllValuesAcceptable()) {
            int total = percCreateRandomTerminal
                        + percAddExpression
                        + percSubtractExpression
                        + percMultiplyExpression
                        + percDivideExpression
                        + percPercentExpression
                        + percIfExpression
                        + percLagExpression
                        + percMinExpression
                        + percMaxExpression
                        + percSumExpression
                        + percSqrtExpression
                        + percAbsExpression
                        + percAvgExpression;
            // Set dummy values according to PERCENT_INT that is the maximum
            int dummyPercCreateRandomTerminal = Math.round((percCreateRandomTerminal * PERCENT_INT) / total);
            int dummyPercAddExpression = Math.round((percAddExpression * PERCENT_INT) / total);
            int dummyPercSubtractExpression = Math.round((percSubtractExpression * PERCENT_INT) / total);
            int dummyPercMultiplyExpression = Math.round((percMultiplyExpression * PERCENT_INT) / total);
            int dummyPercDivideExpression = Math.round((percDivideExpression * PERCENT_INT) / total);
            int dummyPercPercentExpression = Math.round((percPercentExpression * PERCENT_INT) / total);
            int dummyPercIfExpression = Math.round((percIfExpression * PERCENT_INT) / total);
            int dummyPercLagExpression = Math.round((percLagExpression * PERCENT_INT) / total);
            int dummyPercMinExpression = Math.round((percMinExpression * PERCENT_INT) / total);
            int dummyPercMaxExpression = Math.round((percMaxExpression * PERCENT_INT) / total);
            int dummyPercSumExpression = Math.round((percSumExpression * PERCENT_INT) / total);
            int dummyPercSqrtExpression = Math.round((percSqrtExpression * PERCENT_INT) / total);
            int dummyPercAbsExpression = Math.round((percAbsExpression * PERCENT_INT) / total);
            int dummyPercAvgExpression = Math.round((percAvgExpression * PERCENT_INT) / total);
            int dummyTotal = dummyPercCreateRandomTerminal
                        + dummyPercAddExpression
                        + dummyPercSubtractExpression
                        + dummyPercMultiplyExpression
                        + dummyPercDivideExpression
                        + dummyPercPercentExpression
                        + dummyPercIfExpression
                        + dummyPercLagExpression
                        + dummyPercMinExpression
                        + dummyPercMaxExpression
                        + dummyPercSumExpression
                        + dummyPercSqrtExpression
                        + dummyPercAbsExpression
                        + dummyPercAvgExpression;
            // Adjust approximations of Math.round method
            if (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPercCreateRandomTerminal -= (dummyTotal-PERCENT_INT);
                } else {
                    dummyPercCreateRandomTerminal += (PERCENT_INT-dummyTotal);
                }
            }
            // Set new values
            percCreateRandomTerminal = dummyPercCreateRandomTerminal;
            percAddExpression = dummyPercAddExpression;
            percSubtractExpression = dummyPercSubtractExpression;
            percMultiplyExpression = dummyPercMultiplyExpression;
            percDivideExpression = dummyPercDivideExpression;
            percPercentExpression = dummyPercPercentExpression;
            percIfExpression = dummyPercIfExpression;
            percLagExpression = dummyPercLagExpression;
            percMinExpression = dummyPercMinExpression;
            percMaxExpression = dummyPercMaxExpression;
            percSumExpression = dummyPercSumExpression;
            percSqrtExpression = dummyPercSqrtExpression;
            percAbsExpression = dummyPercAbsExpression;
            percAvgExpression = dummyPercAvgExpression;
            // Update the text in the user interface
            setTexts();
        }
    }

    private boolean isAllValuesAcceptable() {
        try {
            setNumericalValues();
        }
	catch(ParseException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ERROR_PARSING_NUMBER",
                                                                   e.getMessage()),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

        if(!isAllValuesPositive()) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_POSITIVE_VALUES_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(!isTotalOK()) {
            // Messages inside the isTotalOK method
	    return false;
        }

        return true;
    }

    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE"));
        JPanel panel = new JPanel();
        panel.setBorder(titledBorder);
        panel.setLayout(new BorderLayout());
        
        
        JTabbedPane tabbedPane = new JTabbedPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        // Integer TabbedPane
        TitledBorder titledBorderInteger = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE_INTEGER_LONG"));
        JPanel panelInteger = new JPanel();
        panelInteger.setBorder(titledBorderInteger);
        panelInteger.setLayout(new BorderLayout());
        
        JPanel upDownPanelInteger = new JPanel();
        upDownPanelInteger.setLayout(new BorderLayout());
        
        JPanel innerPanelInteger = new JPanel();
        innerPanelInteger.setLayout(gridbag);
        
	JPanel panelFitButtonInteger = new JPanel();
        JButton fitButtonInteger = new JButton(Locale.getString("FIT"));
        fitButtonInteger.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Fit Integer Values
                    fitInteger();
                }
            });
        panelFitButtonInteger.add(fitButtonInteger);
	upDownPanelInteger.add(panelFitButtonInteger, BorderLayout.NORTH);
	upDownPanelInteger.add(innerPanelInteger, BorderLayout.CENTER);

        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_INTEGER_SHORT"), panelInteger);

        // Float or Integer TabbedPane
        TitledBorder titledBorderFloatInteger = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_INTEGER_LONG"));
        JPanel panelFloatInteger = new JPanel();
        panelFloatInteger.setBorder(titledBorderFloatInteger);
        panelFloatInteger.setLayout(new BorderLayout());
        
        JPanel upDownPanelFloatInteger = new JPanel();
        upDownPanelFloatInteger.setLayout(new BorderLayout());
        
        JPanel innerPanelFloatInteger = new JPanel();
        innerPanelFloatInteger.setLayout(gridbag);
        
	JPanel panelFitButtonFloatInteger = new JPanel();
        JButton fitButtonFloatInteger = new JButton(Locale.getString("FIT"));
        fitButtonFloatInteger.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Fit FloatInteger Values
                    fitFloatInteger();
                }
            });
        panelFitButtonFloatInteger.add(fitButtonFloatInteger);
	upDownPanelFloatInteger.add(panelFitButtonFloatInteger, BorderLayout.NORTH);
	upDownPanelFloatInteger.add(innerPanelFloatInteger, BorderLayout.CENTER);
        
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_INTEGER_SHORT"), panelFloatInteger);

        // Float Quote TabbedPane
        TitledBorder titledBorderFloatQuote = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_QUOTE_LONG"));
        JPanel panelFloatQuote = new JPanel();
        panelFloatQuote.setBorder(titledBorderFloatQuote);
        panelFloatQuote.setLayout(new BorderLayout());
        
        JPanel upDownPanelFloatQuote = new JPanel();
        upDownPanelFloatQuote.setLayout(new BorderLayout());
        
        JPanel innerPanelFloatQuote = new JPanel();
        innerPanelFloatQuote.setLayout(gridbag);
        
	JPanel panelFitButtonFloatQuote = new JPanel();
        JButton fitButtonFloatQuote = new JButton(Locale.getString("FIT"));
        fitButtonFloatQuote.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Fit FloatQuote Values
                    fitFloatQuote();
                }
            });
        panelFitButtonFloatQuote.add(fitButtonFloatQuote);
	upDownPanelFloatQuote.add(panelFitButtonFloatQuote, BorderLayout.NORTH);
	upDownPanelFloatQuote.add(innerPanelFloatQuote, BorderLayout.CENTER);
        
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_QUOTE_SHORT"), panelFloatQuote);

        // Boolean TabbedPane
        TitledBorder titledBorderBoolean = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE_BOOLEAN_LONG"));
        JPanel panelBoolean = new JPanel();
        panelBoolean.setBorder(titledBorderBoolean);
        panelBoolean.setLayout(new BorderLayout());
        
        JPanel upDownPanelBoolean = new JPanel();
        upDownPanelBoolean.setLayout(new BorderLayout());
        
        JPanel innerPanelBoolean = new JPanel();
        innerPanelBoolean.setLayout(gridbag);
        
	JPanel panelFitButtonBoolean = new JPanel();
        JButton fitButtonBoolean = new JButton(Locale.getString("FIT"));
        fitButtonBoolean.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Fit Boolean Values
                    fitBoolean();
                }
            });
        panelFitButtonBoolean.add(fitButtonBoolean);
	upDownPanelBoolean.add(panelFitButtonBoolean, BorderLayout.NORTH);
	upDownPanelBoolean.add(innerPanelBoolean, BorderLayout.CENTER);
        
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_BOOLEAN_SHORT"), panelBoolean);
        
        // Expression TabbedPane
        TitledBorder titledBorderExpression = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE_EXPRESSION_LONG"));
        JPanel panelExpression = new JPanel();
        panelExpression.setBorder(titledBorderExpression);
        panelExpression.setLayout(new BorderLayout());
        
        JPanel upDownPanelExpression = new JPanel();
        upDownPanelExpression.setLayout(new BorderLayout());
        
        JPanel innerPanelExpression = new JPanel();
        innerPanelExpression.setLayout(gridbag);
        
	JPanel panelFitButtonExpression = new JPanel();
        JButton fitButtonExpression = new JButton(Locale.getString("FIT"));
        fitButtonExpression.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Fit Expression Values
                    fitExpression();
                }
            });
        panelFitButtonExpression.add(fitButtonExpression);
	upDownPanelExpression.add(panelFitButtonExpression, BorderLayout.NORTH);
	upDownPanelExpression.add(innerPanelExpression, BorderLayout.CENTER);
        
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_EXPRESSION_SHORT"), panelExpression);


        // Fill the tabber
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        // Integer
        percOrdinaryNumberTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_ORDINARY_NUMBER_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percDayOfYearExpressionTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_DAY_OF_YEAR_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percMonthExpressionTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_MONTH_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percDayExpressionTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_DAY_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percDayOfWeekExpressionTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_DAY_OF_WEEK_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percHeldTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_HELD_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percOrderTextField =
            GridBagHelper.addTextRow(innerPanelInteger, Locale.getString("PERC_ORDER_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        // Float or Integer
        percFloatTextField =
            GridBagHelper.addTextRow(innerPanelFloatInteger, Locale.getString("PERC_FLOAT_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percIntegerTextField =
            GridBagHelper.addTextRow(innerPanelFloatInteger, Locale.getString("PERC_INTEGER_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        // Float Quote
        percOpenTextField =
            GridBagHelper.addTextRow(innerPanelFloatQuote, Locale.getString("PERC_OPEN_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percLowTextField =
            GridBagHelper.addTextRow(innerPanelFloatQuote, Locale.getString("PERC_LOW_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percHighTextField =
            GridBagHelper.addTextRow(innerPanelFloatQuote, Locale.getString("PERC_HIGH_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percCloseTextField =
            GridBagHelper.addTextRow(innerPanelFloatQuote, Locale.getString("PERC_CLOSE_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        // Boolean
        percNotExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_NOT_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percEqualThanExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_EQUAL_THAN_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percGreaterThanEqualExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_GREATER_THAN_EQUAL_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percGreaterThanExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_GREATER_THAN_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percLessThanEqualExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_LESS_THAN_EQUAL_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percLessThanExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_LESS_THAN_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percNotEqualExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_NOT_EQUAL_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percAndExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_AND_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percOrExpressionTextField =
            GridBagHelper.addTextRow(innerPanelBoolean, Locale.getString("PERC_OR_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        // Expression
        percCreateRandomTerminalTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_CREATE_RANDOM_TERMINAL_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percAddExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_ADD_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percSubtractExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_SUBTRACT_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percMultiplyExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_MULTIPLY_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percDivideExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_DIVIDE_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percPercentExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_PERCENT_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percIfExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_IF_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percLagExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_LAG_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percMinExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_MIN_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percMaxExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_MAX_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percSumExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_SUM_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percSqrtExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_SQRT_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percAbsExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_ABS_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        percAvgExpressionTextField =
            GridBagHelper.addTextRow(innerPanelExpression, Locale.getString("PERC_AVG_EXPRESSION_TEXT_FIELD"), "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);

        // Fill the panels
        panelInteger.add(upDownPanelInteger, BorderLayout.NORTH);
        panelFloatInteger.add(upDownPanelFloatInteger, BorderLayout.NORTH);
        panelFloatQuote.add(upDownPanelFloatQuote, BorderLayout.NORTH);
        panelBoolean.add(upDownPanelBoolean, BorderLayout.NORTH);
        panelExpression.add(upDownPanelExpression, BorderLayout.NORTH);
        
        // The last panel (the GP Gondola Selection one)
        panel.add(tabbedPane, BorderLayout.NORTH);
        add(panel);
    }
}
