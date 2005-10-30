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
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.mov.prefs.PreferencesManager;
import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

public class ANNTrainingPage extends JPanel implements AnalyserPage {
        
    private JDesktopPane desktop;
    
    private JTextField learningRateTextRow;
    private JTextField momentumTextRow;
    private JTextField preLearningTextRow;
    private JTextField totCyclesTextRow;
    
    private JTextField earningPercentageTextRow;
    
    private double learningRate = 0.7D;
    private double momentum = 0.5D;
    private int preLearning = 0;
    private int totCycles = 50;
    
    private double earningPercentage = 1.0D;
    
    /**
     * Construct a new ANN training parameters page.
     * It manages:
     * the training parameters for the artificial neural network monitor,
     * the cross target parameters.
     *
     * @param desktop the desktop
     */
    public ANNTrainingPage(JDesktopPane desktop) {
        
        Dimension preferredSize = new Dimension();
        
        this.desktop = desktop;
        
        setGraphic();
        
    }
    
    /** 
     * Save the preferences
     */
    public void save(String key) {
        
        HashMap settingsInitPop = new HashMap();

        settingsInitPop.put("learning_rate", learningRateTextRow.getText());
        settingsInitPop.put("momentum", momentumTextRow.getText());
        settingsInitPop.put("pre_learning", preLearningTextRow.getText());
        settingsInitPop.put("tot_cycles", totCyclesTextRow.getText());
        
        settingsInitPop.put("earning_percentage", earningPercentageTextRow.getText());

        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settingsInitPop);
    }
    
    /** 
     * Load the preferences
     */
    public void load(String key) {
        // Load last GUI settings from preferences
	HashMap settings =
            PreferencesManager.loadAnalyserPageSettings(key + getClass().getName());

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

            if (value != null) {
                this.loadCommon(setting, value);
            }
        }
    }
    
    /** 
     * Load the values of preferences
     */
    private void loadCommon(String setting, String value) {
        if(setting.equals("learning_rate") && !value.equals("")) {
            learningRateTextRow.setText(value);
        }
        if(setting.equals("momentum") && !value.equals("")) {
            momentumTextRow.setText(value);
        }
        if(setting.equals("pre_learning") && !value.equals("")) {
            preLearningTextRow.setText(value);
        }
        if(setting.equals("tot_cycles") && !value.equals("")) {
            totCyclesTextRow.setText(value);
        }

        if(setting.equals("earning_percentage") && !value.equals("")) {
            earningPercentageTextRow.setText(value);
        }
    }
    
    /** 
     * Parse the GUI
     */
    public boolean parse() {
        boolean returnValue = true;
        
        // Check all the numbers are correct doubles or integers.
        try {
            if(!learningRateTextRow.getText().equals("")) {
                learningRate = Double.parseDouble(
                        learningRateTextRow.getText());
            }
            if(!momentumTextRow.getText().equals("")) {
                momentum = Double.parseDouble(
                        momentumTextRow.getText());
            }
            if(!preLearningTextRow.getText().equals("")) {
                preLearning = Integer.parseInt(
                        preLearningTextRow.getText());
            }
            if(!totCyclesTextRow.getText().equals("")) {
                totCycles = Integer.parseInt(
                        totCyclesTextRow.getText());
            }

            if(!earningPercentageTextRow.getText().equals("")) {
                earningPercentage = Double.parseDouble(
                        earningPercentageTextRow.getText());
            }
        } catch(NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                Locale.getString("ERROR_PARSING_NUMBER",
                e.getMessage()),
                Locale.getString("INVALID_ANN_ERROR"),
                JOptionPane.ERROR_MESSAGE);
            returnValue = false;
        }
       
        // Check the range for learning rate
        // It must be a number between 0.0 and 1.0
        if((learningRate<0.0D) || (learningRate>1.0D)) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ANN_LEARNING_RATE_RANGE_ERROR"),
                                                  Locale.getString("INVALID_ANN_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    returnValue = false;
        }
        // Check the range for momentum
        // It must be a number between 0.0 and 1.0
        if((momentum<0.0D) || (momentum>1.0D)) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ANN_MOMENTUM_RANGE_ERROR"),
                                                  Locale.getString("INVALID_ANN_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    returnValue = false;
        }
        
        // Check the range for preLearning
        // It must be a positive number
        if(preLearning<0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ANN_PRE_LEARNING_RANGE_ERROR"),
                                                  Locale.getString("INVALID_ANN_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    returnValue = false;
        }
        // Check the range for totCycles
        // It must be a positive number and not equal to zero
        if(totCycles<=0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ANN_TOT_CYCLES_RANGE_ERROR"),
                                                  Locale.getString("INVALID_ANN_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    returnValue = false;
        }
        
        return returnValue;
    }
    
    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("ANN_TRAINING_PARAMETERS_SHORT");
    }
    
    /** 
     * Get learning rate of ANN
     *
     * @return the learning rate
     */
    public double getLearningRate() {
        return learningRate;
    }
    
    /** 
     * Get momentum of ANN
     *
     * @return the momentum
     */
    public double getMomentum() {
        return momentum;
    }
    
    /** 
     * Get pre learning of ANN.
     * pre learning is the initial ignored input patterns (during the training phase)
     *
     * @return pre learning
     */
    public int getPreLearning() {
        return preLearning;
    }
    
    /** 
     * Get tot cycles of ANN.
     * tot cycles is how many times the net must be trained on the input patterns
     *
     * @return tot cycles
     */
    public int getTotCycles() {
        return totCycles;
    }
    
    /** 
     * Get the earning percentage of ANN.
     * Earning percentage is the percentage we want to gain each day trade,
     * for example if we have a capital of 100, and we set earning percentage
     * to 1, we'll train the neural network so that ANN will try to get a capital of 101
     * in the first day trade, 102 in the second and so on... 
     *
     * @return earning percentage
     */
    public double getEarningPercentage() {
        return earningPercentage;
    }
    
    /*
     * Set the GUI
     */
    private void setGraphic() {
        
        GridBagLayout gridbag = new GridBagLayout();
                
        TitledBorder titledBorder = new TitledBorder(
                Locale.getString("ANN_TRAINING_PARAMETERS_LONG"));
        this.setBorder(titledBorder);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel panelOne = new JPanel();
        JPanel panelTwo = new JPanel();
        panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.Y_AXIS));
        panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.Y_AXIS));
        TitledBorder titledBorderSectionOne = new TitledBorder(
                Locale.getString("ANN_TRAINING_PARAMETERS_SHORT"));
        TitledBorder titledBorderSectionTwo = new TitledBorder(
                Locale.getString("CROSS_TARGET"));
        panelOne.setBorder(titledBorderSectionOne);
        panelTwo.setBorder(titledBorderSectionTwo);

        JPanel innerPanelOne = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        innerPanelOne.setLayout(gridbag);
        
        // Text boxes for training parameters.
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        learningRateTextRow =
        GridBagHelper.addTextRow(innerPanelOne,
        Locale.getString("TRAINING_PARAMETER_LEARNING_RATE"), "",
        gridbag, c,
        12);
        momentumTextRow =
        GridBagHelper.addTextRow(innerPanelOne,
        Locale.getString("TRAINING_PARAMETER_MOMENTUM"), "",
        gridbag, c,
        12);
        preLearningTextRow =
        GridBagHelper.addTextRow(innerPanelOne,
        Locale.getString("TRAINING_PARAMETER_PRE_LEARNING"), "",
        gridbag, c,
        12);
        totCyclesTextRow =
        GridBagHelper.addTextRow(innerPanelOne,
        Locale.getString("TRAINING_PARAMETER_TOT_CYCLES"), "",
        gridbag, c,
        12);

        
        // Cross Target Panel
        JPanel innerPanelTwo = new JPanel();
        innerPanelTwo.setLayout(gridbag);
        
        // Text boxes for training parameters.
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        earningPercentageTextRow =
        GridBagHelper.addTextRow(innerPanelTwo,
        Locale.getString("EARNING_PERCENTAGE"), "",
        gridbag, c,
        12);
        
        panelOne.add(innerPanelOne);
        
        panelTwo.add(innerPanelTwo);
        
        this.add(panelOne);
        this.add(panelTwo);
        
        setValues();
    }
    
    /*
     * Set the values in the text boxes of the GUI
     */
    private void setValues() {
        learningRateTextRow.setText(Double.toString(learningRate));
        momentumTextRow.setText(Double.toString(momentum));
        preLearningTextRow.setText(Integer.toString(preLearning));
        totCyclesTextRow.setText(Integer.toString(totCycles));
        
        earningPercentageTextRow.setText(Double.toString(earningPercentage));
    }

}