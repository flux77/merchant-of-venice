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

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.mov.analyser.GPPageInitialPopulation;
import org.mov.analyser.GPModule;
import org.mov.analyser.GPResultModule;
import org.mov.analyser.PaperTradeModule;
import org.mov.analyser.PaperTradeResultModule;
import org.mov.chart.*;
import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.help.HelpModule;
import org.mov.util.ExpressionQuery;
import org.mov.util.Locale;
import org.mov.util.TradingDate;
import org.mov.portfolio.Portfolio;
import org.mov.portfolio.PortfolioModule;
import org.mov.prefs.PreferencesModule;
import org.mov.prefs.PreferencesManager;
import org.mov.quote.Quote;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteRange;
import org.mov.quote.QuoteSourceManager;
import org.mov.quote.ScriptQuoteBundle;
import org.mov.quote.Symbol;
import org.mov.table.PortfolioTableModule;
import org.mov.table.QuoteModule;
import org.mov.table.WatchScreen;
import org.mov.table.WatchScreenModule;
import org.mov.importer.ImporterModule;
import org.mov.ui.DesktopManager;
import org.mov.ui.MainMenu;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;
import org.mov.ui.SymbolListDialog;
import org.mov.ui.TextDialog;
import org.mov.ui.TradingDateDialog;

/**
 * This class manages the tasks that can be initiated from menus and toolbars. Each
 * task is launched in a separate thread.
 *
 * @author Dan Makovec
 */
public class CommandManager {

    // Singleton instance of this class
    private static CommandManager instance = null;

    private DesktopManager desktopManager;
    private JDesktopPane desktop;

    // Keep track of dialogs/modules to make sure the user doesn't open
    // two about dialogs, two preferences etc.
    private boolean isAboutDialogUp = false;
    private JInternalFrame importModuleFrame = null;
    private JInternalFrame preferencesModuleFrame = null;

    // Locales for about box translation credits
    private java.util.Locale french = new java.util.Locale("FR");
    private java.util.Locale italian = new java.util.Locale("IT");
    private java.util.Locale swedish = new java.util.Locale("SV");
    private java.util.Locale simplifiedChinese = new java.util.Locale("ZH");    

    // Class should only be constructed once by this class
    private CommandManager() {
        // nothing to do
    }

    /**
     * Return the static CommandManager for this application
     */
    public static CommandManager getInstance() {
	if (instance == null)
	    instance = new CommandManager();

	return instance;
    }


    public void setDesktopManager(DesktopManager desktopManager) {
	this.desktopManager = desktopManager;
	this.desktop = DesktopManager.getDesktop();
    }

    /**
     * Tile all the open internal frames horizontally
     */
    public void tileFramesHorizontal() {
	DesktopManager.tileFrames(DesktopManager.HORIZONTAL);
    }

    /**
     * Tile all the open internal frames vertically
     */
    public void tileFramesVertical() {
	DesktopManager.tileFrames(DesktopManager.VERTICAL);
    }

    /**
     * Arrange all open internal frames in a cascading fashion
     */
    public void tileFramesCascade() {
	DesktopManager.tileFrames(DesktopManager.CASCADE);
    }

    /**
     * Allocate as square a shape as possible to open infternal frames
     */
    public void tileFramesArrange() {
	DesktopManager.tileFrames(DesktopManager.ARRANGE);
    }

    /**
     * Display the transactions to the user, opening portfolio window
     *
     * @param portfolio the portfolio
     * @param quoteBundle fully loaded quote bundle
     */
    public void tableTransactions(final Portfolio portfolio,
                              final QuoteBundle quoteBundle) {
        PortfolioModule porfolioModule = new PortfolioModule(desktop, portfolio, quoteBundle);
        desktopManager.newFrame(porfolioModule);
        porfolioModule.tablePortfolio();
    }

    public void tableStocks(final int type) {
        Thread thread = new Thread(new Runnable() {
                public void run() {
                    String title =
                        new String(Locale.getString("LIST_IT",
						    QuoteRange.getDescription(type)));
                    tableStocks(title, type, null, null, null);
                }
            });
        thread.start();
    }

    public void tableStocks(final List symbols) {
        Thread thread = new Thread(new Runnable() {
                public void run() {
                    SortedSet symbolsCopy;
		    String description = QuoteRange.getDescription(QuoteRange.GIVEN_SYMBOLS);
                    String title =
                        new String(Locale.getString("LIST_IT", description));

                    if(symbols == null)
                        symbolsCopy = SymbolListDialog.getSymbols(desktop, title);

                    else {
                        symbolsCopy = new TreeSet(symbols);

                        for(Iterator iterator = symbolsCopy.iterator(); iterator.hasNext();) {
                            Symbol symbol = (Symbol)iterator.next();

                            if(!QuoteSourceManager.getSource().symbolExists(symbol)) {
                                JOptionPane.showInternalMessageDialog(desktop,
                                                                      Locale.getString("NO_QUOTES_SYMBOL",
                                                                                       symbol.toString()),
                                                                      Locale.getString("INVALID_SYMBOL_LIST"),
                                                                      JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    if(symbolsCopy != null && symbolsCopy.size() > 0)
                        tableStocks(title, QuoteRange.GIVEN_SYMBOLS, null, symbolsCopy, null);
                }
            });
        thread.start();
    }

    public void tableStocksByDate(final int type) {
        Thread thread = new Thread(new Runnable() {
                public void run() {
		
                    String title = new String(Locale.getString("LIST_IT_BY_DATE",
							       QuoteRange.getDescription(type)));
                    TradingDate date = TradingDateDialog.getDate(desktop,
                                                                 title,
                                                                 Locale.getString("DATE"));
                    if(date != null)
                        tableStocks(title, type, null, null, date);
                }
            });
        thread.start();
    }

    public void tableStocksByRule(final int type) {
        Thread thread = new Thread(new Runnable() {
                public void run() {
                    String title = new String(Locale.getString("LIST_IT_BY_RULE",
							       QuoteRange.getDescription(type)));
                    String rule = ExpressionQuery.getExpression(desktop,
                                                                title,
                                                                Locale.getString("RULE"));
                    if(rule != null)
                        tableStocks(title, type, rule, null, null);
                }
            });
        thread.start();
    }

    private void tableStocks(String title, int type, String rule, SortedSet symbols,
                             TradingDate date) {
	Thread thread = Thread.currentThread();
        ScriptQuoteBundle quoteBundle = null;
        QuoteRange quoteRange = null;
        QuoteModule table = null;
        ProgressDialog progressDialog = ProgressDialogManager.getProgressDialog();
        progressDialog.show(title);
        boolean singleDate = false;

        if (!thread.isInterrupted()) {

            if(type == QuoteRange.GIVEN_SYMBOLS) {
                quoteRange =
                    new QuoteRange(new ArrayList(symbols));
                singleDate = false;
            }
            else {
                // If this fails it'll throw a thread interupted to cancel the operation
                // If we were given a date use that, otherwise use the latest date
                // available. Load the last two dates - we need yesterday's quotes to
                // calculate each stocks percent change.
                if(date == null)
                    date = QuoteSourceManager.getSource().getLastDate();

                if(!thread.isInterrupted()) {
                    // If we couldn't load a date, the quote source will have interrupted
                    // the thead. So this shouldn't be null here.
                    assert date != null;

                    quoteRange = new QuoteRange(type, date.previous(1), date);
                }

                singleDate = true;
            }
        }

        if (!thread.isInterrupted())
            quoteBundle = new ScriptQuoteBundle(quoteRange);

        if (!thread.isInterrupted()) {
            table = new QuoteModule(quoteBundle, rule, singleDate);
            desktopManager.newFrame(table);
        }

        ProgressDialogManager.closeProgressDialog(progressDialog);
    }

    /**
     * Display the portfolio with the given name to the user.
     *
     * @param	portfolioName	name of portfolio to display
     */
    public void openPortfolio(String portfolioName) {

        // We don't run this in a new thread because we call openPortfolio(portfolio)
        // which will open a new thread for us.
        Portfolio portfolio =
            PreferencesManager.loadPortfolio(portfolioName);

        openPortfolio(portfolio);
    }

    /**
     * Display the portfolio to the user
     *
     * @param portfolio the portfolio
     */
    public void openPortfolio(final Portfolio portfolio) {

        final Thread thread = new Thread(new Runnable() {

            public void run() {
                Thread thread = Thread.currentThread();
                ProgressDialog progress = ProgressDialogManager.getProgressDialog();

                progress.show(Locale.getString("OPEN_PORTFOLIO", portfolio.getName()));

                QuoteBundle quoteBundle = null;
                TradingDate lastDate = QuoteSourceManager.getSource().getLastDate();

                if(lastDate != null) {
                    if(!thread.isInterrupted()) {
                        QuoteRange quoteRange =
                            new QuoteRange(QuoteRange.ALL_SYMBOLS, lastDate.previous(1), lastDate);

                        quoteBundle = new ScriptQuoteBundle(quoteRange);
                    }

                    if(!thread.isInterrupted())
                        openPortfolio(portfolio, quoteBundle);
                }

                ProgressDialogManager.closeProgressDialog(progress);
            }
            });

        thread.start();
    }

    /**
     * Display the portfolio to the user
     *
     * @param portfolio the portfolio
     * @param quoteBundle fully loaded quote bundle
     */
    public void openPortfolio(final Portfolio portfolio,
                              final QuoteBundle quoteBundle) {
        desktopManager.newFrame(new PortfolioModule(desktop,
                                                    portfolio, quoteBundle));
    }

    /**
     * Open up a new paper trade module.
     */
    public void paperTrade() {
	PaperTradeModule module = new PaperTradeModule(desktop);
	desktopManager.newFrame(module, true, true, true);
    }

    /**
     * Open up a new genetic programming module.
     */
    public void gp() {
	GPModule module = new GPModule(desktop);
	desktopManager.newFrame(module, true, true, true);
    }

    /**
     * Open up a result table that will display a summary of results
     * from paper trading.
     *
     * @return	frame containing paper trade result module
     */
    public ModuleFrame newPaperTradeResultTable() {	
	PaperTradeResultModule results = new PaperTradeResultModule();
	return desktopManager.newFrame(results);	
    }

    /**
     * Open up a result table that will display a summary of results
     * from genetic programming.
     *
     * @return	frame containing genetic programmes
     */
    public ModuleFrame newGPResultTable(GPPageInitialPopulation  GPPageInitialPopulation) {
	GPResultModule results = new GPResultModule(GPPageInitialPopulation);
	return desktopManager.newFrame(results);
    }

    /**
     * Open up a dialog to create and then display a new watch screen.
     */
    public void newWatchScreen() {
	// Get name for watch screen
	TextDialog dialog = new TextDialog(desktop,
					   Locale.getString("ENTER_WATCH_SCREEN_NAME"),
					   Locale.getString("NEW_WATCH_SCREEN"));
	String watchScreenName = dialog.showDialog();
	
        if(watchScreenName != null && watchScreenName.length() > 0) {
            WatchScreen watchScreen = new WatchScreen(watchScreenName);

	    // Save watch screen so we can update the menu
	    PreferencesManager.saveWatchScreen(watchScreen);
	    MainMenu.getInstance().updateWatchScreenMenu();
	
	    // Open as normal
            openWatchScreen(watchScreen);
	}
    }

    /**
     * Display the watch screen to the user
     *
     * @param watchScreenName the name of the watch screen
     */
    public void openWatchScreen(String watchScreenName) {
        WatchScreen watchScreen =
            PreferencesManager.loadWatchScreen(watchScreenName);
        openWatchScreen(watchScreen);
    }

    /**
     * Display the watch screen to the user
     *
     * @param watchScreen the watch screen
     */
    public void openWatchScreen(final WatchScreen watchScreen) {
        final Thread thread = new Thread(new Runnable() {

            public void run() {
                Thread thread = Thread.currentThread();
                ProgressDialog progress = ProgressDialogManager.getProgressDialog();

                progress.show(Locale.getString("OPEN_WATCH_SCREEN", watchScreen.getName()));

                ScriptQuoteBundle quoteBundle = null;
                TradingDate lastDate = QuoteSourceManager.getSource().getLastDate();

                if(lastDate != null) {
                    if(!thread.isInterrupted()) {
                        QuoteRange quoteRange =
                            new QuoteRange(QuoteRange.ALL_SYMBOLS, lastDate.previous(1), lastDate);

                        quoteBundle = new ScriptQuoteBundle(quoteRange);
                    }

                    if(!thread.isInterrupted())
                        desktopManager.newFrame(new WatchScreenModule(watchScreen,
                                                                      quoteBundle));
                }

                ProgressDialogManager.closeProgressDialog(progress);
            }
            });

        thread.start();
    }

    /**
     * Opens up an instance of the preferences module at the last visited page.
     */
    public void openPreferences() {
	// Only allow one copy of the preferences module to be displayed
	synchronized(this) {
	    if(!wakeIfPresent(preferencesModuleFrame)) {
		PreferencesModule preferencesModule = new PreferencesModule(desktop);

		preferencesModuleFrame =
                   desktopManager.newFrame(preferencesModule, true, false, true);
	    }
	}
    }

    /**
     * Opens up an instance of the preferences module at the given page.
     *
     * @param page the preference page to view.
     */
    public void openPreferences(int page ) {
	// Only allow one copy of the preferences module to be displayed
	synchronized(this) {
	    if(!wakeIfPresent(preferencesModuleFrame)) {
		PreferencesModule preferencesModule = new PreferencesModule(desktop, page);

		preferencesModuleFrame =
                   desktopManager.newFrame(preferencesModule, true, false, true);
	    }
	}
    }

    /**
     * Open up a dialog to create and then display a new portfolio.
     */
    public void newPortfolio() {
	// Get name for portfolio
	TextDialog dialog = new TextDialog(desktop,
					   Locale.getString("ENTER_PORTFOLIO_NAME"),
					   Locale.getString("NEW_PORTFOLIO"));
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
     * Graph the given portfolio.
     *
     * @param portfolio the portfolio
     * @param quoteBundle fully loaded quote bundle
     */
    public void graphPortfolio(Portfolio portfolio,
                               QuoteBundle quoteBundle) {
        graphPortfolio(portfolio, quoteBundle, null, null);
    }

    /**
     * Graph the given portfolio inbetween the given dates.
     *
     * @param	portfolio	the portfolio to graph
     * @param	quoteBundle		quote bundle
     * @param	startDate	date to graph from
     * @param	endDate		date to graph to
     */
    public void graphPortfolio(Portfolio portfolio,
			       QuoteBundle quoteBundle,
			       TradingDate startDate,
			       TradingDate endDate) {

        ChartModule chart = new ChartModule(desktop);

	Thread thread = Thread.currentThread();
	ProgressDialog progress = ProgressDialogManager.getProgressDialog();

        progress.show(Locale.getString("GRAPH_PORTFOLIO", portfolio.getName()));

        List symbolsTraded = portfolio.getSymbolsTraded();
        PortfolioGraphSource portfolioGraphSource = null;
        Graph graph = null;

        // If the portfolio has traded symbols and the caller has not supplied a
        // quote bundle, then load one now.
        if (symbolsTraded.size() > 0 && quoteBundle == null) {
            // Get default start and end date if not supplied
            if(startDate == null)
                startDate = portfolio.getStartDate();

            if(endDate == null) {
                endDate = QuoteSourceManager.getSource().getLastDate();		

                // Make sure the end date is after the start date! Otherwise the code
                // will assert later.
                if (endDate.before(startDate))
                    endDate = startDate;
            }

            quoteBundle = new ScriptQuoteBundle(new QuoteRange(symbolsTraded, startDate,
                                                               endDate));
        }

        // If the portfolio hasn't traded symbols then there is nothing to
        // graph
        if (symbolsTraded.size() == 0)
            DesktopManager.showErrorMessage(Locale.getString("NOTHING_TO_GRAPH"));

        else if (!thread.isInterrupted()) {
            portfolioGraphSource =
                new PortfolioGraphSource(portfolio, quoteBundle,
                                         PortfolioGraphSource.MARKET_VALUE);
            graph = new LineGraph(portfolioGraphSource,
                                  Locale.getString("MARKET_VALUE"),
                                  true);
            chart.add(graph, portfolio, quoteBundle, 0);
            chart.redraw();
            desktopManager.newFrame(chart);
        }

        ProgressDialogManager.closeProgressDialog(progress);
    }

    public void tablePortfolio(Portfolio portfolio) {
        tablePortfolio(portfolio, null, null, null);
    }

    public void tablePortfolio(Portfolio portfolio, QuoteBundle quoteBundle) {
        tablePortfolio(portfolio, quoteBundle, null, null);
    }

    public void tablePortfolio(Portfolio portfolio,
                               QuoteBundle quoteBundle,
                               TradingDate startDate,
                               TradingDate endDate) {

	Thread thread = Thread.currentThread();
	ProgressDialog progress = ProgressDialogManager.getProgressDialog();

        progress.show(Locale.getString("TABLE_PORTFOLIO", portfolio.getName()));

        List symbolsTraded = portfolio.getSymbolsTraded();

        // If the portfolio has traded symbols and the caller has not supplied a
        // quote bundle, then load one now.
        if (symbolsTraded.size() > 0 && quoteBundle == null) {
            // Get default start and end date if not supplied
            if(startDate == null)
                startDate = portfolio.getStartDate();

            if(endDate == null) {
                endDate = QuoteSourceManager.getSource().getLastDate();		

                // Make sure the end date is after the start date! Otherwise the code
                // will assert later.
                if (endDate.before(startDate))
                    endDate = startDate;
            }

            quoteBundle = new ScriptQuoteBundle(new QuoteRange(symbolsTraded, startDate,
                                                               endDate));
        }

        if (!thread.isInterrupted()) {
            PortfolioTableModule table = new PortfolioTableModule(portfolio, quoteBundle);
            desktopManager.newFrame(table);
        }

        ProgressDialogManager.closeProgressDialog(progress);
    }

    /**
     * Graph the advance/decline market indicator
     */
    public void graphAdvanceDecline() {

        final Thread thread = new Thread(new Runnable() {

            public void run() {
                Thread thread = Thread.currentThread();
                Graph graph = new AdvanceDeclineGraph();

                if (!thread.isInterrupted()) {
                    ChartModule chart = new ChartModule(desktop);
                    chart.addMarketIndicator(graph);
                    chart.redraw();

                    desktopManager.newFrame(chart);
                }
	    }
	    });

	thread.start();
    }

    /**
     * Displays a graph closing prices for stock(s), based on their code.
     * The stock(s) is/are determined by a user prompt if a set of symbols
     * is not supplied.
     *
     * @param	symbols	Optional. Set of symbols to graph.
     */
    public void graphStockBySymbol(final java.util.List symbols) {

        final Thread thread = new Thread(new Runnable() {
                public void run() {
                    SortedSet symbolsCopy;

                    if(symbols == null)
                        symbolsCopy =
			    SymbolListDialog.getSymbols(desktop,
							Locale.getString("GRAPH_BY_SYMBOLS"));
                    else {
                        // If we were given the list of symbols - then check each one exists
                        // before trying to graph it. Abort if any are not found.
                        symbolsCopy = new TreeSet(symbols);

                        for(Iterator iterator = symbolsCopy.iterator(); iterator.hasNext();) {
                            Symbol symbol = (Symbol)iterator.next();

                            if(!QuoteSourceManager.getSource().symbolExists(symbol)) {
                                JOptionPane.showInternalMessageDialog(desktop,
                                                                      Locale.getString("NO_QUOTES_SYMBOL",
                                                                                       symbol.toString()),
                                                                      Locale.getString("INVALID_SYMBOL_LIST"),
                                                                      JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    graphStock(symbolsCopy);
                }
            });
        thread.start();
    }

    /**
     * Displays a graph closing prices for stock(s), based on their name.
     *  The stock(s) is/are determined by a user prompt
     */
    /*
    public void graphStockByName() {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                SortedSet s = SymbolListDialog.getSymbolByName(desktop,
							       "Graph by name");
                graphStock(s);
            }
        });
        thread.start();
        }*/


    /**
     * Displays a graph of closing prices for an index, based on a list of symbols.
     * The stock(s) is/are determined by a user prompt if a set of symbols
     * is not supplied.
     *
     * @param	symbols	Optional. Set of symbols to graph.
     */
    public void graphIndexBySymbol(final java.util.List symbols) {

        final Thread thread = new Thread(new Runnable() {
                public void run() {
                    SortedSet symbolsCopy;

                    if(symbols == null)
                        symbolsCopy =
			    SymbolListDialog.getSymbols(desktop,
							Locale.getString("GRAPH_BY_SYMBOLS"));
                    else {
                        // If we were given the list of symbols - then check each one exists
                        // before trying to graph it. Abort if any are not found.
                        symbolsCopy = new TreeSet(symbols);

                        for(Iterator iterator = symbolsCopy.iterator(); iterator.hasNext();) {
                            Symbol symbol = (Symbol)iterator.next();

                            if(!QuoteSourceManager.getSource().symbolExists(symbol)) {
                                JOptionPane.showInternalMessageDialog(desktop,
                                                                      Locale.getString("NO_QUOTES_SYMBOL",
                                                                                       symbol.toString()),
                                                                      Locale.getString("INVALID_SYMBOL_LIST"),
                                                                      JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    graphIndex(symbolsCopy);
                }
            });
        thread.start();
    }

    /**
     * Internal function for generic setup of graph modules
     *
     * @param companySet the list of stock symbols to graph
     */
    private void graphStock(SortedSet symbols) {

        if(symbols != null) {
            ChartModule chart = new ChartModule(desktop);
            Thread thread = Thread.currentThread();
            ProgressDialog progress = ProgressDialogManager.getProgressDialog();

            Iterator iterator = symbols.iterator();
            QuoteBundle quoteBundle = null;
            GraphSource dayClose = null;
            Graph graph = null;

            String title = symbols.toString();
            title = title.substring(1, title.length() - 1);

            int progressValue = 0;

            if(symbols.size() > 1) {
                progress.setIndeterminate(false);
                progress.setMaximum(symbols.size());
                progress.setMaster(true);
            }
            else
                progress.setIndeterminate(true);

            progress.show(Locale.getString("GRAPH_SYMBOLS", title));

            while(iterator.hasNext() && !thread.isInterrupted()) {
                Symbol symbol = (Symbol)iterator.next();

                quoteBundle = new ScriptQuoteBundle(new QuoteRange(symbol));

                if(thread.isInterrupted())
                    break;

                dayClose =
                    new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_CLOSE);
                graph = new LineGraph(dayClose, Locale.getString("DAY_CLOSE"), true);
                chart.add(graph, symbol, quoteBundle, 0);
                chart.redraw();

                if(symbols.size() > 1)
                    progress.increment();
            }

            if (!thread.isInterrupted())
                desktopManager.newFrame(chart);

            ProgressDialogManager.closeProgressDialog(progress);
        }
    }

    /**
     * Internal function for generic setup of index graph modules
     *
     * @param companySet the list of stock symbols to graph
     */
    private void graphIndex(SortedSet symbols) {

        if(symbols != null) {
            ChartModule chart = new ChartModule(desktop);
            Thread thread = Thread.currentThread();
            ProgressDialog progress = ProgressDialogManager.getProgressDialog();
            Iterator iterator = symbols.iterator();
            QuoteBundle quoteBundle = null;
            GraphSource dayClose = null;
            Graph graph = null;

            String title = symbols.toString();
            title = title.substring(1, title.length() - 1);

            int progressValue = 0;

            if(symbols.size() > 1) {
                progress.setIndeterminate(false);
                progress.setMaximum(symbols.size());
                progress.setMaster(true);
            }
            else
                progress.setIndeterminate(true);

            progress.show(Locale.getString("GRAPH_SYMBOLS", title));

	    quoteBundle = new ScriptQuoteBundle(new QuoteRange(symbols));

	    dayClose =
		new OHLCVIndexQuoteGraphSource(quoteBundle, Quote.DAY_CLOSE);
	    graph = new LineGraph(dayClose, Locale.getString("DAY_CLOSE"), true);
            // PUT ME BACK IN!!!!
            //	    chart.add(graph, symbol, quoteBundle, 0);
	    chart.redraw();
	
	    if(symbols.size() > 1)
		progress.increment();
	

	    if (!thread.isInterrupted())
		desktopManager.newFrame(chart);
	
	
	    ProgressDialogManager.closeProgressDialog(progress);
	}

    }

    /**
     * Opens the about dialog box.
     */
    public void openAboutDialog() {
        if(!isAboutDialogUp) {
            isAboutDialogUp = true;
            String aboutMessage = (Locale.getString("VENICE_LONG") + ", " +
				   Main.LONG_VERSION + " / " +
                                   Main.RELEASE_DATE + "\n" +

				   Locale.getString("COPYRIGHT", "2003-4") + ", " +
				   "Andrew Leppard\n" +
				   Locale.getString("SEE_LICENSE") + "\n\n" +

                                   "Andrew Leppard (aleppard@picknow.com.au)\n\n" +
				
				   Locale.getString("ADDITIONAL_CODE") + "\n" +
                                   "Daniel Makovec, Quentin Bossard, Peter Fradley, Mark Hummel,\n" +
                                   "Bryan Lin, Alberto Nacher & Matthias St\366ckel.\n\n" +

                                   Locale.getString("TRANSLATORS") + "\n" +
                                   "Quentin Bossard (" + french.getDisplayName() + "), " +
                                   "Bryan Lin (" + simplifiedChinese.getDisplayName() + ")\n" +
                                   "Alberto Nacher (" + italian.getDisplayName() + "), " +
				   "Pontus Str\366mdahl (" + swedish.getDisplayName() + ")"
				   );

	    String aboutVenice = Locale.getString("ABOUT_VENICE",
						  Locale.getString("VENICE_SHORT"));
	    JOptionPane.showInternalMessageDialog(desktop, aboutMessage, aboutVenice,
                                                  JOptionPane.PLAIN_MESSAGE);
            isAboutDialogUp = false;
        }
    }

    /**
     * Opens the help module at the default page.
     */
    public void openHelp() {
	// Let the user open multiple instances of help if they wish. This
	// enables them to have multiple pages open and doesn't affect
	// correctness.
        HelpModule helpModule = new HelpModule(desktop);

        desktopManager.newFrame(helpModule, false, false, true);
    }

    /**
     * Displays the import quotes modules that allows the user to import
     * quotes into the application.
     */
    public void importQuotes() {
	// Only allow one copy of the import module to be displayed.
	synchronized(this) {
	    if(!wakeIfPresent(importModuleFrame)) {
		ImporterModule importerModule = new ImporterModule(desktop);
		
		importModuleFrame = desktopManager.newFrame(importerModule, true, true, false);
	    }
	}
    }

    /**
     * Checks to see if the current frame is open. If so it will make sure the
     * frame is visible and move it to the front of the screen. This function
     * has too purposes: (1) To re-use previously created frames (2) To prevent
     * multiple instances of the frames being displayed.
     *
     * @param frame the frame to check (may be null)
     * @returns <code>TRUE</code> if the frame is now displayed; <code>FALSE</code> otherwise
     */
    private boolean wakeIfPresent(JInternalFrame frame) {
	// If we have already opened the frame, and it hasn't been closed
	// then move it to the front, deiconify it and select it.
	if(frame != null && !frame.isClosed()) {
	    frame.toFront();

	    try {
		frame.setIcon(false);
		frame.setSelected(true);
	    }
	    catch(PropertyVetoException e) {
		// No frame should veto this action.
		assert false;
	    }

	    return true;
	}	

	return false;
    }
}
