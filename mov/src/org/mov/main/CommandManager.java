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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import org.mov.analyser.GPModule;
import org.mov.analyser.GPResultModule;
import org.mov.analyser.PaperTradeModule;
import org.mov.analyser.PaperTradeResultModule;
import org.mov.chart.*;
import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.help.*;
import org.mov.util.ExpressionQuery;
import org.mov.util.Locale;
import org.mov.util.TradingDate;
import org.mov.parser.Expression;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.table.QuoteModule;
import org.mov.table.WatchScreen;
import org.mov.table.WatchScreenModule;
import org.mov.importer.ImporterModule;
import org.mov.ui.*;

/**
 * This class manages the tasks that can be initiated from menus and toolbars. Each
 * task is launched in a separate thread. */
public class CommandManager {

    // Singleton instance of this class
    private static CommandManager instance = null;

    private DesktopManager desktopManager;
    private JDesktopPane desktop;

    // Is the about dialog showing?
    private boolean isAboutDialogUp;

    // Class should only be constructed once by this class
    private CommandManager() {
        isAboutDialogUp = false;
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
	desktopManager.newFrame(module, true, true);
    }

    /**
     * Open up a new genetic programming module.
     */
    public void gp() {
	GPModule module = new GPModule(desktop);
	desktopManager.newFrame(module, true, true);
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
    public ModuleFrame newGPResultTable() {
	GPResultModule results = new GPResultModule();
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
	desktopManager.newFrame(new PreferencesModule(desktop), true, false);
    }

    /**
     * Opens up an instance of the preferences module at the given page.
     *
     * @param page the preference page to view.
     */
    public void openPreferences(int page ) {
	desktopManager.newFrame(new PreferencesModule(desktop, page), true, false);
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

        PortfolioGraphSource portfolioGraphSource = null;
        Graph graph = null;

        // Get default start and end date if not supplied
        if(startDate == null)
            startDate = portfolio.getStartDate();

        if(endDate == null)
            endDate = QuoteSourceManager.getSource().getLastDate();		
        List symbols = portfolio.getSymbolsTraded();

        // Only need to load from quote bundle if there are any stocks
        // in the portfolio
        if(quoteBundle == null && symbols.size() > 0) {
            quoteBundle = new ScriptQuoteBundle(new QuoteRange(symbols, startDate, endDate));
        }

        if (!thread.isInterrupted()) {
            portfolioGraphSource =
                new PortfolioGraphSource(portfolio, quoteBundle,
                                         PortfolioGraphSource.MARKET_VALUE);
            graph = new LineGraph(portfolioGraphSource);
            chart.add(graph, portfolio, quoteBundle, 0);
            chart.redraw();
            desktopManager.newFrame(chart);
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
                    chart.add(graph, null, 0);
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
                graph = new LineGraph(dayClose);
                chart.add(graph, quoteBundle, 0);
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
     * Opens the about dialog box.
     */
    public void openAboutDialog() {
        if(!isAboutDialogUp) {
            isAboutDialogUp = true;
            String aboutMessage = (Locale.getString("VENICE_LONG") + ", " +
				   Main.LONG_VERSION + " / " +
                                   Main.RELEASE_DATE + "\n\n" +

				   Locale.getString("COPYRIGHT", "2003") + ", " +
				   "Andrew Leppard\n" +
				   Locale.getString("SEE_LICENSE") + "\n\n" +

                                   "Andrew Leppaprd (aleppard@picknow.com.au)\n" +
                                   "Daniel Makovec\n\n" +
				
				   Locale.getString("ADDITIONAL_CODE") + "\n\n" +

                                   "Peter Fradley, Bryan Lin & Matthias St\366ckel."
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
        HelpModule helpModule = new HelpModule(desktop);

        desktopManager.newFrame(helpModule, false, false);
    }

    /**
     * Shows a dialog and imports quotes into Venice
     */
    public void importQuotes() {
        desktopManager.newFrame(new ImporterModule(desktop), true, true);
    }
}
