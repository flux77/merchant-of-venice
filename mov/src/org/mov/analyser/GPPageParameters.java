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
import org.mov.util.Locale;

public class GPPageParameters extends JPanel {

    private JDesktopPane desktop;

    // Swing components
    private JTextField generationsTextField;
    private JTextField populationTextField;
    private JTextField breedingPopulationTextField;
    private JTextField displayPopulationTextField;

    // Parsed input
    private int generations;
    private int population;
    private int breedingPopulation;
    private int displayPopulation;

    public GPPageParameters(JDesktopPane desktop) {
        this.desktop = desktop;

        layoutPage();
    }

    public void load(String setting, String value) {
        if(setting.equals("generations"))
            generationsTextField.setText(value);
        else if(setting.equals("population"))
            populationTextField.setText(value);
        else if(setting.equals("breeding_population"))
            breedingPopulationTextField.setText(value);
        else if(setting.equals("display_population"))
            displayPopulationTextField.setText(value);
    }

    public void save(HashMap settings) {
        
	settings.put("generations", generationsTextField.getText());
	settings.put("population", populationTextField.getText());
	settings.put("breeding_population", breedingPopulationTextField.getText());
	settings.put("display_population", displayPopulationTextField.getText());

    }

    public boolean parse() {
        generations = 0;
        population = 0;
        breedingPopulation = 0;
        displayPopulation = 0;

        try {
	    if(!generationsTextField.getText().equals(""))
		generations =
		    Integer.parseInt(generationsTextField.getText());

	    if(!populationTextField.getText().equals(""))
		population =
		    Integer.parseInt(populationTextField.getText());
	    	
	    if(!breedingPopulationTextField.getText().equals(""))
		breedingPopulation =
		    Integer.parseInt(breedingPopulationTextField.getText());

	    if(!displayPopulationTextField.getText().equals(""))
		displayPopulation =
		    Integer.parseInt(displayPopulationTextField.getText());
	}
	catch(NumberFormatException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ERROR_PARSING_NUMBER",
                                                                   e.getMessage()),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

        if(displayPopulation > breedingPopulation) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("DISPLAY_POPULATION_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(generations <= 0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_GENERATION_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(population <= 0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_INDIVIDUAL_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(breedingPopulation <= 0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_BREEDING_INDIVIDUAL_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        if(displayPopulation <= 0) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("NO_DISPLAY_INDIVIDUAL_ERROR"),
                                                  Locale.getString("INVALID_GP_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
        }

        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("GP_PAGE_PARAMETERS_SHORT");
    }

    public int getGenerations() {
        return generations;
    }

    public int getPopulation() {
        return population;
    }

    public int getBreedingPopulation() {
        return breedingPopulation;
    }

    public int getDisplayPopulation() {
        return displayPopulation;
    }

    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = new TitledBorder(Locale.getString("GP_PAGE_PARAMETERS_LONG"));
        JPanel panel = new JPanel();
        panel.setBorder(titledBorder);
        panel.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        innerPanel.setLayout(gridbag);

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        generationsTextField =
            GridBagHelper.addTextRow(innerPanel, Locale.getString("GENERATIONS"), "",
                                     gridbag, c,
                                     5);
        populationTextField =
            GridBagHelper.addTextRow(innerPanel,
                                     Locale.getString("POPULATION"), "",
                                     gridbag, c,
                                     10);
        breedingPopulationTextField =
            GridBagHelper.addTextRow(innerPanel,
                                     Locale.getString("BREEDING_POPULATION"), "",
                                     gridbag, c, 7);

        displayPopulationTextField =
            GridBagHelper.addTextRow(innerPanel,
                                     Locale.getString("DISPLAY_POPULATION"), "",
                                     gridbag, c, 7);

        panel.add(innerPanel, BorderLayout.NORTH);
        add(panel);
    }
}