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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;

import org.mov.main.*;

public class GeneticProgrammeModule extends JPanel implements Module {

    private PropertyChangeSupport propertySupport;   
    private JDesktopPane desktop;

    // Pages
    private QuoteRangePage quoteRangePage;
    //    private RulesPage rulesPage;
    private PortfolioPage portfolioPage;
    private GeneticProgrammePage geneticProgrammePage;

    public GeneticProgrammeModule(JDesktopPane desktop) {
	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);

	layoutGeneticProgramme();

	// Load GUI settings from preferences
        load();
    }

    private void layoutGeneticProgramme() {

        JTabbedPane tabbedPane = new JTabbedPane();
        quoteRangePage = new QuoteRangePage(desktop);
        tabbedPane.addTab("Range", quoteRangePage.getComponent());

        //        rulesPage = new RulesPage(desktop);
        //tabbedPane.addTab("Rules", rulesPage.getComponent());

        portfolioPage = new PortfolioPage(desktop);
        tabbedPane.addTab("Portfolio", portfolioPage.getComponent());

        geneticProgrammePage = new GeneticProgrammePage(desktop);
        tabbedPane.addTab("GP", geneticProgrammePage.getComponent());
        
	// Run, close buttons
	JPanel buttonPanel = new JPanel();
	JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    System.out.println("Run!!");
                }
            });
	buttonPanel.add(runButton);

	JButton closeButton = new JButton("Close");
	closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Tell frame we want to close
                    propertySupport.
                        firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
                }
            });
	buttonPanel.add(closeButton);

        // Now layout components
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
	add(buttonPanel, BorderLayout.SOUTH);
    }

    public void load() {
        quoteRangePage.load(getClass().getName());
        //        rulesPage.load(getClass().getName());
        portfolioPage.load(getClass().getName());
        geneticProgrammePage.load(getClass().getName());
    }

    public void save() {
        quoteRangePage.save(getClass().getName());
        //        rulesPage.save(getClass().getName());
        portfolioPage.save(getClass().getName());
        geneticProgrammePage.save(getClass().getName());
    }

    public String getTitle() {
	return "Genetic Programming";
    }

    /**
     * Add a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void addModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Remove a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void removeModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Return frame icon for table module.
     *
     * @return	the frame icon.
     */
    public ImageIcon getFrameIcon() {
	return null;
    }    

    /**
     * Return displayed component for this module.
     *
     * @return the component to display.
     */
    public JComponent getComponent() {
	return this;
    }

    /**
     * Return menu bar for chart module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	return null;
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return true;
    }
}
