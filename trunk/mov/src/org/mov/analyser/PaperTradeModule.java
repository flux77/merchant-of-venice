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
import javax.swing.border.*;
import javax.swing.table.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.prefs.*;
import org.mov.portfolio.*;
import org.mov.quote.*;
import org.mov.ui.*;

public class PaperTradeModule extends JPanel implements Module {

    private PropertyChangeSupport propertySupport;   
    private JDesktopPane desktop;
    private ScriptQuoteBundle quoteBundle;

    // Single result table for entire application
    private static ModuleFrame resultsFrame = null;

    private JTabbedPane tabbedPane;

    // Pages
    private QuoteRangePage quoteRangePage;
    private RulesPage rulesPage;
    private PortfolioPage portfolioPage;

    /**
     * Create a new paper trade module.
     *
     * @param	desktop	the current desktop
     */
    public PaperTradeModule(JDesktopPane desktop) {

	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);

	layoutPaperTrade();

	// Load GUI settings from preferences
	load();
    }

    private void layoutPaperTrade() {

        tabbedPane = new JTabbedPane();
        quoteRangePage = new QuoteRangePage(desktop);
        tabbedPane.addTab("Range", quoteRangePage.getComponent());

        rulesPage = new RulesPage(desktop);
        tabbedPane.addTab("Rules", rulesPage.getComponent());

        portfolioPage = new PortfolioPage(desktop);
        tabbedPane.addTab("Portfolio", portfolioPage.getComponent());

	// Run, close buttons
	JPanel buttonPanel = new JPanel();
	JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    // Run paper trade
                    run();
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

    // Load GUI settings from preferences
    private void load() {
        quoteRangePage.load(getClass().getName());
        rulesPage.load(getClass().getName());
        portfolioPage.load(getClass().getName());
    }

    // Save GUI settings to preferences
    public void save() {
        quoteRangePage.save(getClass().getName());
        rulesPage.save(getClass().getName());
        portfolioPage.save(getClass().getName());
    }

    public String getTitle() {
	return "Paper Trade";
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

    private void run() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    // Before we paper trade, save our interface results
                    // so if the programme crashes etc our stuff is still there
                    save();
                    
                    // Read data from GUI and load quote data
                    if(parse()) {
                        Vector paperTradeResults = getPaperTradeResults();

                        if(paperTradeResults != null)
                            display(paperTradeResults);
                    }
                }
            });
        
        t.start();
    }

    // Read data from interface and display if there are any errors.
    // Return true if the data is OK
    private boolean parse() {
        if(!quoteRangePage.parse()) {
            tabbedPane.setSelectedComponent(quoteRangePage.getComponent());
            return false;
        }
        else if(!rulesPage.parse()) {
            tabbedPane.setSelectedComponent(rulesPage.getComponent());
            return false;
        }
        else if(!portfolioPage.parse()) {
            tabbedPane.setSelectedComponent(portfolioPage.getComponent());
            return false;
        }
        else
            return true;
    }
   
    private PaperTradeResult paperTradeAllSymbols(ProgressDialog progress, 
                                                  ScriptQuoteBundle quoteBundle,
                                                  Variables variables)
        throws EvaluationException {


        Portfolio portfolio = 
            PaperTrade.paperTrade("Paper Trade of " + 
                                  quoteBundle.getQuoteRange().getDescription(),
                                  quoteBundle,
                                  variables,
                                  quoteRangePage.getOrderComparator(quoteBundle),
                                  quoteRangePage.getQuoteRange().getFirstDate(),
                                  quoteRangePage.getQuoteRange().getLastDate(),
                                  rulesPage.getBuyRule(),
                                  rulesPage.getSellRule(),
                                  portfolioPage.getInitialCapital(),
                                  portfolioPage.getValuePerStock(),
                                  portfolioPage.getTradeCost());

        // Running the equation means we might need to load in
        // more quotes so the note may have changed...
        progress.setNote("Paper Trading...");
        progress.increment();

        return new BasicPaperTradeResult(portfolio, 
                                         quoteBundle, 
                                         portfolioPage.getInitialCapital(),
                                         portfolioPage.getTradeCost(),
                                         rulesPage.getBuyRule().toString(), 
                                         rulesPage.getSellRule().toString(), 
                                         quoteRangePage.getQuoteRange().getFirstDate(),
                                         quoteRangePage.getQuoteRange().getLastDate());

    }
 
    private PaperTradeResult paperTradeSymbol(ProgressDialog progress, Vector symbols, 
                                              ScriptQuoteBundle quoteBundle,
                                              Variables variables)
        throws EvaluationException {

        String firstSymbol = (String)symbols.firstElement();
        Portfolio portfolio = 
            PaperTrade.paperTrade("Paper Trade of " + 
                                  firstSymbol.toLowerCase(),
                                  quoteBundle,
                                  variables,
                                  firstSymbol.toLowerCase(),
                                  quoteRangePage.getQuoteRange().getFirstDate(),
                                  quoteRangePage.getQuoteRange().getLastDate(),
                                  rulesPage.getBuyRule(),
                                  rulesPage.getSellRule(),
                                  portfolioPage.getInitialCapital(),
                                  portfolioPage.getTradeCost());

        // Running the equation means we might need to load in
        // more quotes so the note may have changed...
        progress.setNote("Paper Trading...");
        progress.increment();

        return new BasicPaperTradeResult(portfolio, 
                                         quoteBundle, 
                                         portfolioPage.getInitialCapital(),
                                         portfolioPage.getTradeCost(),
                                         rulesPage.getBuyRule().toString(), 
                                         rulesPage.getSellRule().toString(), 
                                         quoteRangePage.getQuoteRange().getFirstDate(),
                                         quoteRangePage.getQuoteRange().getLastDate());
    }
   
    private PaperTradeResult paperTradeForEachSymbol(ProgressDialog progress, Vector symbols, 
                                                     ScriptQuoteBundle quoteBundle,
                                                     Variables variables)
        throws EvaluationException {

        // These are the only values that are averaged
        int averageNumberTrades = 0;
        int averageFinalCapital = 0;
		    
        // Evaluate expression for each equation
        for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
            String symbol = (String)iterator.next();

            Portfolio portfolio = 
                PaperTrade.paperTrade("Paper Trade of " + 
                                      symbol.toLowerCase(),
                                      quoteBundle,
                                      variables,
                                      symbol.toLowerCase(),
                                      quoteRangePage.getQuoteRange().getFirstDate(),
                                      quoteRangePage.getQuoteRange().getLastDate(),
                                      rulesPage.getBuyRule(),
                                      rulesPage.getSellRule(),
                                      portfolioPage.getInitialCapital(),
                                      portfolioPage.getTradeCost());
            
            averageNumberTrades += (portfolio.countTransactions(Transaction.ACCUMULATE) +
                                    portfolio.countTransactions(Transaction.REDUCE));
            try {
                averageFinalCapital += 
                    portfolio.getValue(quoteBundle, 
                                       quoteRangePage.getQuoteRange().getLastDate());
            }
            catch(MissingQuoteException e) {
                // Already checked...
                assert false;
            }
            
            // Running the equation means we might need to load in
            // more quotes so the note may have changed...
            progress.setNote("Paper Trading...");
            progress.increment();
        }

        if(symbols.size() > 1) {
            averageNumberTrades /= symbols.size();
            averageFinalCapital /= symbols.size();
        }

        return new AveragePaperTradeResult(quoteBundle, 
                                           "Symbols",
                                           portfolioPage.getInitialCapital(),
                                           averageFinalCapital,
                                           portfolioPage.getTradeCost(),
                                           averageNumberTrades,
                                           rulesPage.getBuyRule().toString(), 
                                           rulesPage.getSellRule().toString(), 
                                           quoteRangePage.getQuoteRange().getFirstDate(),
                                           quoteRangePage.getQuoteRange().getLastDate());
    }

    private PaperTradeResult getPaperTradeResult(ProgressDialog progress, Vector symbols, 
                                                 ScriptQuoteBundle quoteBundle,
                                                 Variables variables)
        throws EvaluationException {

        PaperTradeResult paperTradeResult;
        
        // If there is only one symbol to trade - we create a
        // basic paper trade result of just that symbol. This
        // includes the portfolio so it can be graphed
        if(symbols.size() == 1) 
            paperTradeResult = 
                paperTradeSymbol(progress, symbols, quoteBundle, variables);
        
        // If there are multiple symbols, we paper trade each one
        // and create a summary paper trade result. This doesn't
        // include portfolio information (there'd be too much data!)
        else
            paperTradeResult = 
                paperTradeForEachSymbol(progress, symbols, quoteBundle, variables);

        return paperTradeResult;
    }
                                
    private Vector getPaperTradeResults() {
        ProgressDialog progress = 
            ProgressDialogManager.getProgressDialog();

        progress.setIndeterminate(true);
        progress.show("Paper Trade");

        quoteBundle = new ScriptQuoteBundle(quoteRangePage.getQuoteRange());
        Vector symbols = quoteBundle.getAllSymbols();
        
        // If we are using a rule family, how many equations are in the family?
        // Otherwise it's just a single equation.
        int numberEquations = (rulesPage.isFamilyEnabled()? 
                               rulesPage.getARange() * rulesPage.getBRange() * 
                               rulesPage.getCRange() :
                               1);
        
        // If multiple stock portfolio is enabled we only do a single run.
        // Otherwise we do a run for each symbol in the quote bundle.
        int runsPerEquation = (portfolioPage.isMultipleStockPortfolio()?
                               1 :
                               symbols.size());

        progress.setIndeterminate(false);
        progress.setMaximum(numberEquations * runsPerEquation);
        progress.setProgress(0);
        progress.setNote("Paper Trading...");
        progress.setMaster(true);

	// Iterate through all possible paper trade equations
        Vector paperTradeResults = new Vector(numberEquations);

        try {
            Variables variables = new Variables();

            // If the user has selected rule family, then iterate through
            // each combination of a, b, c
            if(rulesPage.isFamilyEnabled()) {
                variables.add("a", Expression.INTEGER_TYPE);
                variables.add("b", Expression.INTEGER_TYPE);
                variables.add("c", Expression.INTEGER_TYPE);
                
                for(int a = 1; a <= rulesPage.getARange(); a++) {
                    for(int b = 1; b <= rulesPage.getBRange(); b++) {
                        for(int c = 1; c <= rulesPage.getCRange(); c++) {
                            
                            variables.setValue("a", a);
                            variables.setValue("b", a);
                            variables.setValue("c", a);
                        
                            paperTradeResults.add(getPaperTradeResult(progress, symbols, 
                                                                      quoteBundle,
                                                                      variables));
                        }
                    }
                }
            }

            // Otherwise there is only one equation and one result.
            else {
                paperTradeResults.add(getPaperTradeResult(progress, symbols, 
                                                          quoteBundle,
                                                          variables));
            }

        } catch(EvaluationException e) {
            ProgressDialogManager.closeProgressDialog(progress);
            progress = null;

            JOptionPane.showInternalMessageDialog(desktop, 
                                                  e.getReason() + ".",
                                                  "Error executing paper trade",
                                                  JOptionPane.ERROR_MESSAGE);
            
            return null;
        }

        ProgressDialogManager.closeProgressDialog(progress);

	return paperTradeResults;
    }

    private void display(final Vector paperTradeResults) {

	// Invokes on dispatch thread
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {

		    // Dispaly results table if its not already up (or if it
		    // was closed we need to create a new one)
		    if(resultsFrame == null || resultsFrame.isClosed()) {
			resultsFrame = 
			    CommandManager.getInstance().newPaperTradeResultTable();
		    }
		    else {
			resultsFrame.toFront();
			
			try {
			    resultsFrame.setIcon(false);
			    resultsFrame.setSelected(true);
			}
			catch(java.beans.PropertyVetoException e) {
			    assert false;
			}
		    }

		    // Send result to result table for display
		    PaperTradeResultModule resultsModule = 
			(PaperTradeResultModule)resultsFrame.getModule();
		    
                    resultsModule.addResults(paperTradeResults);
		}});
    }
}
