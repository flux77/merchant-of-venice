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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mov.prefs.PreferencesManager;
import org.mov.ui.ConfirmDialog;
import org.mov.util.Locale;

public class GPPage extends JPanel implements AnalyserPage {

    private JDesktopPane desktop;
    
    // Panels inside the tabs
    private GPPageParameters GPPageParameters;
    public GPPageInitialPopulation GPPageInitialPopulation;
    
    
    public GPPage(JDesktopPane desktop, double maxHeight) {
        this.desktop = desktop;

        Dimension preferredSize = new Dimension();
        preferredSize.setSize(this.getPreferredSize().getWidth(), maxHeight/2);
        
        this.GPPageParameters = new GPPageParameters(desktop);
        this.GPPageInitialPopulation = new GPPageInitialPopulation(desktop, Locale.getString("GP_PAGE_INITIAL_POPULATION_LONG"), preferredSize);
        
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

            GPPageParameters.load(setting, value);
            GPPageInitialPopulation.loadCommon(setting, value);
        }
       
        HashMap settingsInitPop =
                PreferencesManager.loadAnalyserPageSettings(key + getClass().getName() + "GPInitialPopulation");

        Iterator iteratorInitPop = settingsInitPop.keySet().iterator();

	while(iteratorInitPop.hasNext()) {
	    String settingInitPop = (String)iteratorInitPop.next();
	    String valueInitPop = (String)settingsInitPop.get((Object)settingInitPop);

            GPPageInitialPopulation.load(valueInitPop);
        }
        GPPageInitialPopulation.loadEmpty();

    }

    public void save(String key) {
        
        HashMap settings = new HashMap();
        HashMap settingsInitPop =
                PreferencesManager.loadAnalyserPageSettings(key + getClass().getName() + "GPInitialPopulation");
        HashMap settingsInitPopCommon = new HashMap();

        GPPageParameters.save(settings);
        GPPageInitialPopulation.save(settingsInitPopCommon, settingsInitPop, "GPInitialPopulation");

        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settings);
        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName() + "GPInitialPopulation",
                                                    settingsInitPop);
        PreferencesManager.saveAnalyserPageSettings(key + getClass().getName(),
                                                    settingsInitPopCommon);
    }

    public boolean parse() {
        if(!GPPageParameters.parse())
            return false;
        if(!GPPageInitialPopulation.parse())
            return false;
        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return Locale.getString("GP_PAGE_SHORT_TITLE");
    }
    
    public int getBreedingPopulation() {
        return GPPageParameters.getBreedingPopulation();
    }

    public int getDisplayPopulation() {
        return GPPageParameters.getDisplayPopulation();
    }

    public int getGenerations() {
        return GPPageParameters.getGenerations();
    }

    public int getPopulation() {
        return GPPageParameters.getPopulation();
    }
    
    public int getMutations() {
        return GPPageInitialPopulation.getMutations();
    }
    
    public int getIfRandom() {
        return GPPageInitialPopulation.getIfRandom();
    }
    
    public int getRandomFromInitialPopulation() {
        return GPPageInitialPopulation.getRandom();
    }
    
    public String getBuyRuleFromInitialPopulation(int row) {
        return GPPageInitialPopulation.getBuyRule(row);
    }
    
    public String getSellRuleFromInitialPopulation(int row) {
        return GPPageInitialPopulation.getSellRule(row);
    }
    
    private void layoutPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = new TitledBorder(Locale.getString("GP_PAGE_TITLE"));
        JPanel panel = new JPanel();
        panel.setBorder(titledBorder);
        panel.setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab(Locale.getString("GP_PAGE_PARAMETERS_SHORT"), GPPageParameters);
        tabbedPane.addTab(Locale.getString("GP_PAGE_INITIAL_POPULATION_SHORT"), GPPageInitialPopulation);

        // The last panel (the GP Page Selection one)
        panel.add(tabbedPane, BorderLayout.NORTH);
        this.add(panel);
    }
}
