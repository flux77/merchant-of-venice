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
import java.util.HashMap;
import java.util.Random;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class GPGondolaSelectionPanel extends JPanel {

    private final static String format = GPModuleConstants.format;
    private final static double PERCENT_DOUBLE = GPModuleConstants.PERCENT_DOUBLE;
    private final static int PERCENT_INT = GPModuleConstants.PERCENT_INT;
    private final static int MAX_CHARS_IN_TEXTBOXES = 6;
    
    private JTextField[] percTextField;
    private int[] perc;
    private int[] defValues;
    private String[] defTextFieldValues;
    private boolean heldAndOrder = false;

    private JDesktopPane desktop;
    private Random random = new Random(System.currentTimeMillis());

    public GPGondolaSelectionPanel(int elements,
                                   JDesktopPane desktop,
                                   int[] defaultValues,
                                   String[] defaultTextFieldValues,
                                   String titledBorderText,
                                   Dimension preferredSize) {
        
        this.desktop = desktop;
        
        percTextField = new JTextField[elements];
        perc = new int[elements];
        defValues = defaultValues;
        defTextFieldValues = defaultTextFieldValues;
        
        setGraphic(titledBorderText, preferredSize);
        
    }
    
    public void setHeldAndOrder() {
        heldAndOrder = true;
    }

    public int getRandom() {
        return this.getRandom(true,true);
    }
    
    public int getRandom(boolean allowHeld, boolean allowOrder) {
        int total = 0;
        int totalLength = perc.length;
        if (!allowHeld)
            totalLength--;
        if (!allowOrder)
            totalLength--;
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
                                              Locale.getString("ERROR GENERATING RANDOM NUMBER"),
                                              Locale.getString("INVALID_GP_ERROR"),
                                              JOptionPane.ERROR_MESSAGE);
        return 0;
    }

    public void save(HashMap settings, String idStr) {
	for (int i=0; i<percTextField.length; i++)
            settings.put(idStr + (new Integer(i)).toString(), percTextField[i].getText());
    }
    
    public void load(String setting, String idStr, String value) {
	for (int i=0; i<percTextField.length; i++)
            if(setting.equals(idStr + (new Integer(i)).toString()))
                percTextField[i].setText(value);
    }
    
    // Fit the values, if they differ
    public void fit() {
        if (isAllValuesAcceptable()) {
            int total = 0;
            for (int i=0; i<perc.length; i++)
                total += perc[i];

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
    }

    // Return true if values already fit to percentage
    public boolean isFit() {
        if (isAllValuesAcceptable()) {
            int total = 0;
            for (int i=0; (i<perc.length); i++)
                total += perc[i];
            if (total==PERCENT_INT)
                return true;
        }
        return false;
    }

    public boolean isAllValuesAcceptable() {
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
    
    private void setNumericalValues() throws ParseException {
        setDefaultsValuesOnly();
    
        // decimalFormat manage the localization.
        DecimalFormat decimalFormat = new DecimalFormat(format);
        for (int i=0; i<perc.length; i++) {
            if(!percTextField[i].getText().equals("")) {
                perc[i] =
                    (int) Math.round(PERCENT_DOUBLE*(decimalFormat.parse(percTextField[i].getText()).doubleValue()));
            }
        }
    }
    
    private boolean isAllValuesPositive() {
        boolean returnValue = true;
        for (int i=0; i<perc.length; i++)
            returnValue = returnValue && (perc[i]>=0);
        return returnValue;
    }
    
    private boolean isTotalOK() {
        // We should consider the absence of held and order -> totalIntegerModified
        long total = 0;
        int totalLength = perc.length;
        if (heldAndOrder)
            totalLength -= 2;
        for (int i=0; (i<totalLength); i++)
            total += perc[i];
        // Check total == 0
        if (total==0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_TOTAL_GREATER_THAN_ZERO_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public void setDefaultsValuesOnly() {
        for (int i=0; i<perc.length; i++)
            perc[i]=defValues[i];
    }
        
    public void setDefaults() {
        for (int i=0; i<perc.length; i++)
            perc[i]=defValues[i];
        this.setTexts();
    }
        
    public void setTexts() {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        for (int i=0; i<percTextField.length; i++)
            percTextField[i].setText(decimalFormat.format(perc[i]/PERCENT_DOUBLE));
    }
        
    private void setGraphic(String titledBorderText, Dimension preferredSize) {
        
        GridBagLayout gridbag = new GridBagLayout();
        
        TitledBorder titledBorder = new TitledBorder(titledBorderText);
        
        this.setBorder(titledBorder);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel upDownPanel = new JPanel();
        upDownPanel.setLayout(new BoxLayout(upDownPanel, BoxLayout.Y_AXIS));
        
        JScrollPane upDownScrollPane = new JScrollPane(upDownPanel);
        upDownScrollPane.setLayout(new ScrollPaneLayout());
        upDownScrollPane.setPreferredSize(preferredSize);
        
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(gridbag);
        
        JButton fitButton = new JButton(Locale.getString("FIT"));
        JButton defaultButton = new JButton(Locale.getString("DEFAULT"));
        fitButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // Fit Values
                fit();
            }
        });
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // Set Default Values
                setDefaults();
            }
        });
        fitButton.setAlignmentX(CENTER_ALIGNMENT);
        defaultButton.setAlignmentX(CENTER_ALIGNMENT);
        innerPanel.setAlignmentX(CENTER_ALIGNMENT);
	upDownPanel.add(fitButton);
	upDownPanel.add(defaultButton);
	upDownPanel.add(innerPanel);
        
        GridBagConstraints c = new GridBagConstraints();
        
        // Fill the tabber
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        for (int i=0; i<percTextField.length; i++) {
            percTextField[i] =
                GridBagHelper.addTextRow(innerPanel, defTextFieldValues[i], "",
                                     gridbag, c,
                                     MAX_CHARS_IN_TEXTBOXES);
        }
        
        this.add(upDownScrollPane);
    }

}
    
