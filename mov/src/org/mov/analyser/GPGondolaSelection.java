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
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.ConfirmDialog;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class GPGondolaSelection extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;
    private Random random;
    
    // Panel inside the tabs
    GPGondolaSelectionPanel[] GPGondolaSelectionPanel = new GPGondolaSelectionPanel[5];

    public GPGondolaSelection(JDesktopPane desktop, double maxHeight) {
        this.desktop = desktop;
        random = new Random(System.currentTimeMillis());

        Dimension preferredSize = new Dimension();
        preferredSize.setSize(this.getPreferredSize().getWidth(), maxHeight/2);
        
        // Integers
        int[] defaultValuesIntegers = {5000,    //ordinary number has 50% probabilty as default
                                        // As default we don't generate DayOfYearExpression() or MonthExpression()
                                        // because it would make it easy for the GP to hook onto specific dates
                                        // where the market is low. By removing these it forces the GP
                                        // to use the stock data to generate buy/sell decisions.
                               0, 0, 1250, 1250, 1250, 1250};
        String[] defaultTextFieldValuesIntegers = {Locale.getString("PERC_ORDINARY_NUMBER_TEXT_FIELD"),
                                           Locale.getString("PERC_DAY_OF_YEAR_EXPRESSION_TEXT_FIELD"),
                                           Locale.getString("PERC_MONTH_EXPRESSION_TEXT_FIELD"),
                                           Locale.getString("PERC_DAY_EXPRESSION_TEXT_FIELD"),
                                           Locale.getString("PERC_DAY_OF_WEEK_EXPRESSION_TEXT_FIELD"),
                                           Locale.getString("PERC_HELD_TEXT_FIELD"),
                                           Locale.getString("PERC_ORDER_TEXT_FIELD")
        };
        GPGondolaSelectionPanel[0] = new GPGondolaSelectionPanel(7,
                                desktop,
                                defaultValuesIntegers,
                                defaultTextFieldValuesIntegers,
                                Locale.getString("GP_GONDOLA_SELECTION_TITLE_INTEGER_LONG"),
                                preferredSize);
        GPGondolaSelectionPanel[0].setHeldAndOrder();

        // Float or Integer
        int[] defaultValuesFloatInteger = {5000, 5000};
        String[] defaultTextFieldValuesFloatInteger = {Locale.getString("PERC_FLOAT_TEXT_FIELD"),
                                  Locale.getString("PERC_INTEGER_TEXT_FIELD")
        };
        GPGondolaSelectionPanel[1] = new GPGondolaSelectionPanel(2,
                                desktop,
                                defaultValuesFloatInteger,
                                defaultTextFieldValuesFloatInteger,
                                Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_INTEGER_LONG"),
                                preferredSize);

        // Float Quote
        int[] defaultValuesFloatQuote = {2500, 2500, 2500, 2500};
        String[] defaultTextFieldValuesFloatQuote = {Locale.getString("PERC_OPEN_TEXT_FIELD"),
                                  Locale.getString("PERC_LOW_TEXT_FIELD"),
                                  Locale.getString("PERC_HIGH_TEXT_FIELD"),
                                  Locale.getString("PERC_CLOSE_TEXT_FIELD")
        };
        GPGondolaSelectionPanel[2] = new GPGondolaSelectionPanel(4,
                                desktop,
                                defaultValuesFloatQuote,
                                defaultTextFieldValuesFloatQuote,
                                Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_QUOTE_LONG"),
                                preferredSize);

        // Boolean
        int[] defaultValuesBoolean = {1112, 1111, 1111, 1111, 1111, 1111, 1111, 1111, 1111};
        String[] defaultTextFieldValuesBoolean = {Locale.getString("PERC_NOT_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_EQUAL_THAN_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_GREATER_THAN_EQUAL_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_GREATER_THAN_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_LESS_THAN_EQUAL_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_LESS_THAN_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_NOT_EQUAL_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_AND_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_OR_EXPRESSION_TEXT_FIELD")
        };
        GPGondolaSelectionPanel[3] = new GPGondolaSelectionPanel(9,
                                desktop,
                                defaultValuesBoolean,
                                defaultTextFieldValuesBoolean,
                                Locale.getString("GP_GONDOLA_SELECTION_TITLE_BOOLEAN_LONG"),
                                preferredSize);
        // Expression
        int[] defaultValuesExpression = {715, 714, 714, 714, 714, 714, 714, 714, 715, 715, 714, 714, 714, 715};
        String[] defaultTextFieldValuesExpression = {Locale.getString("PERC_CREATE_RANDOM_TERMINAL_TEXT_FIELD"),
                                  Locale.getString("PERC_ADD_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_SUBTRACT_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_MULTIPLY_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_DIVIDE_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_PERCENT_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_IF_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_LAG_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_MIN_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_MAX_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_SUM_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_SQRT_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_ABS_EXPRESSION_TEXT_FIELD"),
                                  Locale.getString("PERC_AVG_EXPRESSION_TEXT_FIELD")
        };
        GPGondolaSelectionPanel[4] = new GPGondolaSelectionPanel(14,
                                desktop,
                                defaultValuesExpression,
                                defaultTextFieldValuesExpression,
                                Locale.getString("GP_GONDOLA_SELECTION_TITLE_EXPRESSION_LONG"),
                                preferredSize);

        layoutPage();
        
        setDefaults();
    }
    
    public int getRandomToGenerateInteger(boolean allowHeld, boolean allowOrder) {
        return GPGondolaSelectionPanel[0].getRandom(allowHeld, allowOrder);
    }

    public int getRandomToGenerateFloatInteger() {
        return GPGondolaSelectionPanel[1].getRandom();
    }

    public int getRandomToGenerateFloatQuote() {
        return GPGondolaSelectionPanel[2].getRandom();
    }

    public int getRandomToGenerateBoolean() {
        return GPGondolaSelectionPanel[3].getRandom();
    }

    public int getRandomToGenerateExpression() {
        return GPGondolaSelectionPanel[4].getRandom();
    }

    public void load(String key) {
        // Load last GUI settings from preferences
	HashMap settings =
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

            GPGondolaSelectionPanel[0].load(setting, "gp_integer", value);
            GPGondolaSelectionPanel[1].load(setting, "gp_float_integer", value);
            GPGondolaSelectionPanel[2].load(setting, "gp_float_quote", value);
            GPGondolaSelectionPanel[3].load(setting, "gp_boolean", value);
            GPGondolaSelectionPanel[4].load(setting, "gp_expression", value);
        }
    }

    public void save(String key) {
        
        HashMap settings = new HashMap();

	GPGondolaSelectionPanel[0].save(settings, "gp_integer");
	GPGondolaSelectionPanel[1].save(settings, "gp_float_integer");
	GPGondolaSelectionPanel[2].save(settings, "gp_float_quote");
	GPGondolaSelectionPanel[3].save(settings, "gp_boolean");
	GPGondolaSelectionPanel[4].save(settings, "gp_expression");

        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
    }

    public boolean parse() {
        if(!isAllValuesAcceptable()) {
            return false;
        } else {
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
        }
        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("GP_GONDOLA_SELECTION_SHORT_TITLE");
    }

    private void setDefaults() {
        GPGondolaSelectionPanel[0].setDefaults();
        GPGondolaSelectionPanel[1].setDefaults();
        GPGondolaSelectionPanel[2].setDefaults();
        GPGondolaSelectionPanel[3].setDefaults();
        GPGondolaSelectionPanel[4].setDefaults();
    }
    
    private void fitAll() {
        // Fit all values one after another (only if previous one was fitted without error)
        GPGondolaSelectionPanel[0].fit();
        GPGondolaSelectionPanel[1].fit();
        GPGondolaSelectionPanel[2].fit();
        GPGondolaSelectionPanel[3].fit();
        GPGondolaSelectionPanel[4].fit();
    }

    private boolean isFitAll() {
        // Fit all values one after another (only if previous one was fitted without error)
        if (GPGondolaSelectionPanel[0].isFit()) {
            if (GPGondolaSelectionPanel[1].isFit()) {
                if (GPGondolaSelectionPanel[2].isFit()) {
                    if (GPGondolaSelectionPanel[3].isFit()) {
                        if (GPGondolaSelectionPanel[4].isFit())
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAllValuesAcceptable() {
        // Fit all values one after another (only if previous one was fitted without error)
        if (GPGondolaSelectionPanel[0].isAllValuesAcceptable()) {
            if (GPGondolaSelectionPanel[1].isAllValuesAcceptable()) {
                if (GPGondolaSelectionPanel[2].isAllValuesAcceptable()) {
                    if (GPGondolaSelectionPanel[3].isAllValuesAcceptable()) {
                        if (GPGondolaSelectionPanel[4].isAllValuesAcceptable())
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = new TitledBorder(Locale.getString("GP_GONDOLA_SELECTION_TITLE"));
        JPanel panel = new JPanel();
        panel.setBorder(titledBorder);
        panel.setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_INTEGER_SHORT"), GPGondolaSelectionPanel[0]);
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_INTEGER_SHORT"), GPGondolaSelectionPanel[1]);
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_FLOAT_QUOTE_SHORT"), GPGondolaSelectionPanel[2]);
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_BOOLEAN_SHORT"), GPGondolaSelectionPanel[3]);
        tabbedPane.addTab(Locale.getString("GP_GONDOLA_SELECTION_TITLE_EXPRESSION_SHORT"), GPGondolaSelectionPanel[4]);

        // The last panel (the GP Gondola Selection one)
        panel.add(tabbedPane, BorderLayout.NORTH);
        this.add(panel);
    }
}
