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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.AbstractTable;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.ConfirmDialog;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class GPPageInitialPopulation extends JPanel {
    
    private final static String format = GPModuleConstants.format;
    private final static double PERCENT_DOUBLE = GPModuleConstants.PERCENT_DOUBLE;
    private final static int PERCENT_INT = GPModuleConstants.PERCENT_INT;
    
    private final static int PERCENT_RANDOM = 0;
    private final static int PERCENT_INIT_POP = 1;
    private final static int PERCENT_MAX = 2;
    
    private JDesktopPane desktop;
    private int[] perc = new int[PERCENT_MAX];
    
    private JTextField numberMutationTextRow;
    private JTextField generateInitPopTextRow;
    private JTextField generateRandomPopTextRow;
    
    private Random random = new Random(System.currentTimeMillis());
    
    private GPPageInitialPopulationModule GPPageInitialPopulationModule;
    
    // mutations number (how many times the mutations would be applied by GP algorithm)
    private int mutations;
    
    public GPPageInitialPopulation(JDesktopPane desktop,
                                    String titledBorderText,
                                    Dimension preferredSize) {
        
        this.desktop = desktop;
        this.GPPageInitialPopulationModule = new GPPageInitialPopulationModule(desktop);
        
        setGraphic(titledBorderText, preferredSize);
        
    }
    
    public void save(HashMap settingsCommon, HashMap settings, String idStr) {
        settingsCommon.put("generations_changing_seeds", numberMutationTextRow.getText());
        settingsCommon.put("generations_random", generateRandomPopTextRow.getText());
        settingsCommon.put("generations_init_pop", generateInitPopTextRow.getText());
        GPPageInitialPopulationModule.save(settings, idStr);
    }
    
    public void loadCommon(String setting, String value) {
        if(setting.equals("generations_changing_seeds"))
            numberMutationTextRow.setText(value);
        if(setting.equals("generations_random"))
            generateRandomPopTextRow.setText(value);
        if(setting.equals("generations_init_pop"))
            generateInitPopTextRow.setText(value);
    }
    
    public void load(String value) {
        GPPageInitialPopulationModule.load(value);
    }
    
    public void loadEmpty() {
        GPPageInitialPopulationModule.loadEmpty();
    }
    
    public void addRowTable(String buyRule, String sellRule, String perc) {
        GPPageInitialPopulationModule.addRowTable(buyRule, sellRule, perc);
    }
    
    public boolean parse() {
        boolean returnValue = true;
        
        mutations = 0;
        
        if(!isAllValuesAcceptable()) {
            return false;
        } else {
            if(!isFitAll()) {
                ConfirmDialog dialog = new ConfirmDialog(desktop,
                Locale.getString("GP_FIT_PAGE"),
                Locale.getString("GP_FIT_TITLE"));
                boolean returnConfirm = dialog.showDialog();
                if (returnConfirm)
                    fitAll();
                else
                    return false;
            }
        }
        
        try {
            
            if(!numberMutationTextRow.getText().equals(""))
                mutations =
                Integer.parseInt(numberMutationTextRow.getText());
        } catch(NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop,
            Locale.getString("ERROR_PARSING_NUMBER",
            e.getMessage()),
            Locale.getString("INVALID_GP_ERROR"),
            JOptionPane.ERROR_MESSAGE);
            returnValue = false;
        }
        
        // If we have maximum percent for random population,
        // we don't have to check for the table,
        // because the values on the table are of initial population
        // and we do not need them.
        if (perc[PERCENT_RANDOM]!=PERCENT_INT)
            // Parse all the values in the GPPageInitialPopulationModule
            // so that we know if the table with initial population rules is OK or not.
            if (!GPPageInitialPopulationModule.parse())
                returnValue = false;
        
        return returnValue;
    }
    
    public int getMutations() {
        return mutations;
    }
    
    // The method can return one of the following values:
    // PERCENT_RANDOM = 0;
    // PERCENT_INIT_POP = 1;
    // so 0 means that we should get a random rule
    // while 1 means that we should get a rule from the user defined ones.
    public int getIfRandom() {
        if (isAllValuesAcceptable()) {
            int total = 0;
            int totalLength = perc.length;
            for (int i=0; i<totalLength; i++)
                total += perc[i];
            int randomValue = random.nextInt(total);
            
            int totalMin = 0;
            int totalMax = 0;
            for (int i=0; i<totalLength; i++) {
                totalMax = totalMin + perc[i];
                if ((randomValue >= totalMin) && (randomValue < totalMax)) {
                    return i;
                }
                totalMin += perc[i];
            }
            
            JOptionPane.showInternalMessageDialog(desktop,
            Locale.getString("ERROR_GENERATING_RANDOM_NUMBER"),
            Locale.getString("INVALID_GP_ERROR"),
            JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }
    
    public int getRandom() {
        return GPPageInitialPopulationModule.getRandom();
    }
    
    public String getBuyRule(int row) {
        return GPPageInitialPopulationModule.getBuyRule(row);
    }
    
    public String getSellRule(int row) {
        return GPPageInitialPopulationModule.getSellRule(row);
    }
    
    // Fit the values, if they differ
    public void fitAll() {
        if (isAllValuesAcceptable()) {
            int total = 0;
            for (int i=0; i<perc.length; i++) {
                total += perc[i];
            }
            
            // Set dummy values according to PERCENT_INT that is the maximum
            int[] dummyPerc = new int[perc.length];
            for (int i=0; i<perc.length; i++)
                dummyPerc[i] = Math.round((perc[i] * PERCENT_INT) / total);
            int dummyTotal = 0;
            for (int i=0; i<perc.length; i++)
                dummyTotal += dummyPerc[i];
            // Adjust approximations of Math.round method
            int count=0;
            while (dummyTotal!=PERCENT_INT) {
                if (dummyTotal>PERCENT_INT) {
                    dummyPerc[count]--;
                    dummyTotal--;
                } else {
                    dummyPerc[count]++;
                    dummyTotal++;
                }
                count++;
            }
            // Set new values
            for (int i=0; i<perc.length; i++)
                perc[i] = dummyPerc[i];
            // Update the text in the user interface
            setTexts();
        }
        validate();
        repaint();
    }
    
    // Return true if values already fit to percentage
    private boolean isFitAll() {
        if (isAllValuesAcceptable()) {
            int total = 0;
            for (int i=0; (i<perc.length); i++)
                total += perc[i];
            if (total==PERCENT_INT)
                return true;
        }
        return false;
    }
    
    private boolean isAllValuesAcceptable() {
        try {
            setNumericalValues();
        } catch(ParseException e) {
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
    
    private void setNumericalValues() throws ParseException {
        perc = new int[PERCENT_MAX];
        
        // decimalFormat manage the localization.
        DecimalFormat decimalFormat = new DecimalFormat(format);
        
        if(!generateRandomPopTextRow.getText().equals("")) {
            perc[PERCENT_RANDOM] =
            (int) Math.round(PERCENT_DOUBLE*(decimalFormat.parse(generateRandomPopTextRow.getText()).doubleValue()));
        } else {
            perc[PERCENT_RANDOM] = 0;
        }
        if(!generateInitPopTextRow.getText().equals("")) {
            perc[PERCENT_INIT_POP] =
            (int) Math.round(PERCENT_DOUBLE*(decimalFormat.parse(generateInitPopTextRow.getText()).doubleValue()));
        } else {
            perc[PERCENT_INIT_POP] = 0;
        }
    }
    
    private void setTexts() {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        generateRandomPopTextRow.setText(decimalFormat.format(perc[PERCENT_RANDOM]/PERCENT_DOUBLE));
        generateInitPopTextRow.setText(decimalFormat.format(perc[PERCENT_INIT_POP]/PERCENT_DOUBLE));
    }
    
    private boolean isAllValuesPositive() {
        boolean returnValue = true;
        for (int i=0; i<perc.length; i++)
            returnValue = returnValue && (perc[i]>=0);
        return returnValue;
    }
    
    private boolean isTotalOK() {
        long total = 0;
        int totalLength = perc.length;
        for (int i=0; (i<totalLength); i++)
            total += perc[i];
        // Check total == 0
        if (total==0) {
            JOptionPane.showInternalMessageDialog(desktop,
            Locale.getString("NO_TOTAL_GREATER_THAN_ZERO_PAGE_ERROR"),
            Locale.getString("INVALID_GP_ERROR"),
            JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void setDefaultValues() {
        // Default values means GP works with random initial population
        perc[PERCENT_RANDOM] = PERCENT_INT;
        perc[PERCENT_INIT_POP] = 0;
        
        setTexts();
        validate();
        repaint();
    }

    
    private void setGraphic(String titledBorderText, Dimension preferredSize) {
        
        GridBagLayout gridbag = new GridBagLayout();
        
        TitledBorder titledBorder = new TitledBorder(titledBorderText);
        
        this.setBorder(titledBorder);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // GPPageInitialPopulationModule is already declared as global variable
        GPPageInitialPopulationModule.setLayout(new BoxLayout(GPPageInitialPopulationModule, BoxLayout.Y_AXIS));
        
        JScrollPane upDownScrollPane = new JScrollPane(GPPageInitialPopulationModule);
        upDownScrollPane.setLayout(new ScrollPaneLayout());
        upDownScrollPane.setPreferredSize(preferredSize);
        
        JButton fitAllButton = new JButton(Locale.getString("FIT"));
        fitAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // Fit the percent values for each buy/sell rule
                GPPageInitialPopulationModule.fitAll();
                // Fit the percent values for random or not
                fitAll();
            }
        });
        
        JButton defaultButton = new JButton(Locale.getString("DEFAULT"));
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // Set the values to default, so the GP will work with random generation only
                setDefaultValues();
            }
        });
        
        JPanel innerPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        innerPanel.setLayout(gridbag);
        
        // Add Generation number to fix the range of mutation
        // the beginning formulas will be modified n times with mutation mechanism
        // according to the number in the combo box (user defined).
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        generateRandomPopTextRow =
        GridBagHelper.addTextRow(innerPanel,
        Locale.getString("GP_PAGE_GENERATE_RANDOM_PERC_TEXT_ROW"), "",
        gridbag, c,
        12);
        generateInitPopTextRow =
        GridBagHelper.addTextRow(innerPanel,
        Locale.getString("GP_PAGE_GENERATE_PERC_TEXT_ROW"), "",
        gridbag, c,
        12);
        numberMutationTextRow =
        GridBagHelper.addTextRow(innerPanel,
        Locale.getString("GP_PAGE_GENERATE_NUMBER_MUTATION_TEXT_ROW"), "",
        gridbag, c,
        6);
        
        
        this.add(upDownScrollPane);
        this.add(innerPanel);
        fitAllButton.setAlignmentX(CENTER_ALIGNMENT);
        defaultButton.setAlignmentX(CENTER_ALIGNMENT);
        this.add(fitAllButton);
        this.add(defaultButton);
        
        // Put the default values so that random initial population is the default behaviour
        this.setDefaultValues();
    }
    
}