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

package org.mov.main;

import java.awt.event.*;
import java.util.*;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import org.mov.analyser.*;
import org.mov.chart.*;
import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.Expression;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.table.QuoteModule;
import org.mov.importer.ImporterModule;
import org.mov.ui.*;

/**
 * This class manages the actions that can be initiated from menus and toolbars, usually by firing off modules.
 */

public class CommandManager {

    /** The instance of the CommandManager to be returned whenever getInstance() is called */
    private static CommandManager command_manager_instance = null;

    /** 
     * Return the static CommandManager for this application
     */
    public static CommandManager getInstance() {
	if (command_manager_instance == null)
	    command_manager_instance = new CommandManager();

	return command_manager_instance;
    }

    /** The desktop that any window operations will be performed on */
    private JDesktopPane desktop_instance;
    
    /**************************************************************************/
    /**************************************************************************/

    /**
     * Sets the desktop that any window operations will be performed on
     * @param desktop The desktop that any window operations will be performed on 
     */
    public void setDesktop(JDesktopPane desktop) {
	desktop_instance = desktop;
    }

    /**************************************************************************/
    
    /** Tiles all the open internal frames horizontally */
    public void tileFramesHorizontal() {
	DesktopManager.tileFrames(DesktopManager.HORIZONTAL);
    }

    /** Tiles all the open internal frames vertically */
    public void tileFramesVertical() {
	DesktopManager.tileFrames(DesktopManager.VERTICAL);
    }
    /** Arranges all open internal frames in a cascading fashion */
    public void tileFramesCascade() {
	DesktopManager.tileFrames(DesktopManager.CASCADE);
    }
    /** Allocates as square a shape as possible to open infternal frames*/
    public void tileFramesArrange() {
	DesktopManager.tileFrames(DesktopManager.ARRANGE);
    }

    /**************************************************************************/
    /**************************************************************************/
    
    /** Display an internal frame, listing all the stocks by company name */
    public void quoteListCompanyNamesAll() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying list of all companies");
                p.show();
                displayStockList(QuoteSource.COMPANIES_AND_FUNDS, null);

		ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing stocks by company name, matching a rule that is to be input by the user */
    public void quoteListCompanyNamesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                String expr = ExpressionQuery.getExpression(desktop_instance,
							    "List Companies and Funds",
							    "By Rule");
		if(expr != null) {
		    ProgressDialog p = 
			ProgressDialogManager.getProgressDialog();
		    p.setTitle("Displaying quotes of companies by rule \""+expr+"\"");
		    p.show();

		    displayStockList(QuoteSource.COMPANIES_AND_FUNDS,
				     expr);
                
		    ProgressDialogManager.closeProgressDialog();
		}
            }
        });
        t.start();
    }

    /** Display an internal frame, listing all the stocks by symbol */
    public void quoteListCommoditiesAll() {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying quotes of all commodities");
                p.show();

                displayStockList(QuoteSource.ALL_COMMODITIES, null);

		ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing stocks by symbol, 
	matching a rule that is to be input by the user */
    public void quoteListCommoditiesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                String expr = ExpressionQuery.getExpression(desktop_instance,
							    "List Commodities",
							    "By Rule"); 
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying quotes of commodities by rule \""+expr+"\"");
                p.show();

                displayStockList(QuoteSource.ALL_COMMODITIES,expr);
                ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing all the indices by symbol */
    public void quoteListIndicesAll() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying quotes of all indices");
                p.show();

                displayStockList(QuoteSource.INDICES, null);
                ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing indices by symbol, matching a rule that is to be input by the user */
    public void quoteListIndicesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                String expr = ExpressionQuery.getExpression(desktop_instance,
							    "List Indices",
							    "By Rule"); 

                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying quotes of companies by rule \""+expr+"\"");
                p.show();
                displayStockList(QuoteSource.INDICES, expr);
            }
        });
        t.start();
    }

    
    /** 
     * Internal function for retrieving the required data displaying a table showing the results
     *
     * @param searchRestriction as defined by QuoteSource
     * @param expression as defined by Expression
     * @see org.mov.quote.QouteSource
     */
    private void displayStockList(int searchRestriction, 
				  String expression) {
        try {
            final Thread thread = Thread.currentThread();
            ProgressDialogManager.getProgressDialog().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {thread.interrupt();}
		});
            QuoteCache cache = null;
            QuoteModule table = null;

            if (!thread.isInterrupted())
                cache = new QuoteCache(QuoteSourceManager.getSource().getLatestQuoteDate(),
				       searchRestriction);

            if (!thread.isInterrupted())
                table = new QuoteModule(cache, expression);

            if (!thread.isInterrupted())
                ((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(table);//, false);
        } catch (Exception e) {
            ProgressDialogManager.closeProgressDialog();
        }
    }

    /**
     * Display the portfolio with the given name to the user.
     *
     * @param	portfolioName	name of portfolio to display
     */
    public void openPortfolio(String portfolioName) {
	final Thread thread = Thread.currentThread();
	ProgressDialog progress = ProgressDialogManager.getProgressDialog();
	progress.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    thread.interrupt();
		}
	    });

	try {
	    QuoteCache cache = null;
	    Portfolio portfolio = 
		PreferencesManager.loadPortfolio(portfolioName);

	    progress.setTitle("Loading quotes for portfolio");
	    progress.show();

            if (!thread.isInterrupted()) 
		cache = 
		    new QuoteCache(QuoteSourceManager.getSource().getLatestQuoteDate(),
				   QuoteSource.ALL_COMMODITIES);

	    if (!thread.isInterrupted()) 
		((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(new PortfolioModule(desktop_instance, portfolio, cache));

	    if (!Thread.currentThread().interrupted())
		ProgressDialogManager.closeProgressDialog();
	    	
	} catch (Exception e) {
	    e.printStackTrace();
            ProgressDialogManager.closeProgressDialog();
        }
    }

    /**
     * Open up a new paper trade module. 
     */
    public void paperTrade() {

	PaperTradeModule paperTrade = new PaperTradeModule(desktop_instance);

	((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(paperTrade, true, true);
    }

    /**
     * Open up a result table that will display a summary of results
     * from paper trading.
     *
     * @return	frame containing paper trade result module
     */
    public ModuleFrame newPaperTradeResultTable() {
	
	PaperTradeResultModule results =
	    new PaperTradeResultModule();

	return ((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(results);	
    }

    /**
     * Open up a dialog to create and then display a new portfolio.
     */
    public void newPortfolio() {
	// Get name for portfolio
	TextDialog dialog = new TextDialog(desktop_instance, 
					   "Enter portfolio name",
					   "New Portfolio");
	String portfolioName = dialog.showDialog();

	if(portfolioName != null && portfolioName.length() > 0) {
	    Portfolio portfolio = new Portfolio(portfolioName);
	    
	    // Save portfolio so we can update the menu
	    PreferencesManager.savePortfolio(portfolio);
	    MainMenu.getInstance().updatePortfolioMenu();
	    
	    // Open as normal
	    openPortfolio(portfolioName);
	}
    }

    /**
     * Graph the given portfolio.
     *
     * @param	portfolio	the portfolio to graph
     */
    public void graphPortfolio(Portfolio portfolio) {

	// Set the start and end dates to null - the other graph
	// function will determine appropriate start and end dates
	graphPortfolio(portfolio, null, null, null);
    }

    /**
     * Graph the given portfolio inbetween the given dates.
     *
     * @param	portfolio	the portfolio to graph
     * @param	cache		quote cache
     * @param	startDate	date to graph from
     * @param	endDate		date to graph to
     */
    public void graphPortfolio(Portfolio portfolio, 
			       QuoteCache cache,
			       TradingDate startDate,
			       TradingDate endDate) {

        final ChartModule chart = new ChartModule(desktop_instance);

	final Thread thread = Thread.currentThread();
	ProgressDialog progress = ProgressDialogManager.getProgressDialog();
	progress.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    thread.interrupt();
		}
	    });

	try {
	    progress.setTitle("Loading quotes for portfolio");
	    progress.show();

	    PortfolioGraphSource portfolioGraphSource = null;
	    Graph graph = null;

            if (!thread.isInterrupted()) {
		// Get default start and end date if not supplied
		if(startDate == null) 
		    startDate = portfolio.getStartDate();

		if(endDate == null)
		    endDate = QuoteSourceManager.getSource().getLatestQuoteDate();		
		Vector symbols = portfolio.getSymbolsTraded();

		// Only need to load from cache if there are any stocks
		// in the portfolio
		if(cache == null && symbols.size() > 0) {
		    cache = new QuoteCache(symbols, startDate, endDate);
		}
	    }

            if (!thread.isInterrupted()) {
		portfolioGraphSource =
		    new PortfolioGraphSource(portfolio, cache, 
					     PortfolioGraphSource.MARKET_VALUE);
	    }

            if (!thread.isInterrupted()) {	 
		graph = new LineGraph(portfolioGraphSource);
	    }

            if (!thread.isInterrupted()) {	       
		chart.add(graph, portfolio, cache, 0);

		chart.redraw();
		((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(chart);
	    }

	    if (!Thread.currentThread().interrupted())
		ProgressDialogManager.closeProgressDialog();
	    
	} catch (Exception e) {
	    e.printStackTrace();

	    ProgressDialogManager.closeProgressDialog();
	}
    }

    /**
     * Graph the advance/decline market indicator
     */
    public void graphAdvanceDecline() {

        final Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Calculating advance/decline");
                p.show();

		final Thread thread = Thread.currentThread();

		p.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    thread.interrupt();
			}
		    });
		
		try {
		    ChartModule chart = new ChartModule(desktop_instance);
		    Graph graph = null;

                    if (!thread.isInterrupted())
			graph = new AdvanceDeclineGraph();

                    if (!thread.isInterrupted())
                        chart.add(graph, null, 0);

                    if (!thread.isInterrupted())
			ProgressDialogManager.closeProgressDialog();

		    if (!thread.isInterrupted())
			((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(chart);
		}
		catch (Exception e) {
		    ProgressDialogManager.closeProgressDialog();
		}
	    }
	    });

	t.start();
    }

    /** 
     * Displays a graph closing prices for stock(s), based on their code. 
     * The stock(s) is/are determined by a user prompt if a set of symbols
     * is not supplied.
     *
     * @param	symbols	Optional. Set of symbols to graph.
     */
    public void graphStockBySymbol(final Vector symbols) {

        final Thread t = new Thread(new Runnable() {
            public void run() {
		SortedSet symbolsCopy;

		if(symbols == null) {
		    symbolsCopy = SymbolListDialog.getSymbols(desktop_instance, 
							      "Graph stocks by code");
		}
		else {
		    symbolsCopy = new TreeSet(symbols);
		}

                String str = symbolsCopy.toString();
                str = str.substring(1,str.length()-1);
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying graph of stock symbols "+str);
                p.show();
                graphStock(symbolsCopy);
                if (!Thread.currentThread().interrupted())
                    ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** 
     * Displays a graph closing prices for stock(s), based on their name. 
     *  The stock(s) is/are determined by a user prompt 
     */
    public void graphStockByName() {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                SortedSet s = SymbolListDialog.getSymbolByName(desktop_instance, 
							       "Graph stock by name");
                String str = s.toString();
                str = str.substring(1,str.length()-1);
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying graph of stock named "+str);
                p.show();
                graphStock(s);
                if (!Thread.currentThread().interrupted())
                    ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /**
     * Internal function for generic setup of graph modules
     *
     * @param companySet the list of stock symbols to graph 
     */
    private void graphStock(final SortedSet companySet) {
        final ChartModule chart = new ChartModule(desktop_instance);

        final Thread thread = Thread.currentThread();
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {thread.interrupt();}
        });

        try {
            if(companySet != null) {
                Iterator iterator = companySet.iterator();
                String symbol = null;
                QuoteCache cache = null;
                GraphSource dayClose = null;
                Graph graph = null;

                while(iterator.hasNext() && !thread.isInterrupted()) {
                    symbol = (String)iterator.next();
                    progress.setTitle("Loading quotes for "+symbol);

                    progress.show();
                    if (!thread.isInterrupted())
                        cache = new QuoteCache(symbol);

                    if (!thread.isInterrupted())
                        dayClose = 
                            new OHLCVQuoteGraphSource(cache, Quote.DAY_CLOSE);

                    if (!thread.isInterrupted())
                        graph = new LineGraph(dayClose);

                    if (!thread.isInterrupted())
                        chart.add(graph, cache, 0);

                    chart.redraw();
                }
                if (!thread.isInterrupted())
                    ((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(chart);

            }
        } catch (Exception e) {
            ProgressDialogManager.closeProgressDialog();
        }
    }

    /** Shows a dialog and imports quotes into Venice */
    public void importQuotes() {
	((DesktopManager)(desktop_instance.getDesktopManager()))
	    .newFrame(new ImporterModule(desktop_instance), true, true);
    }
}
