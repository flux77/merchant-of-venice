package org.mov.main;

import java.awt.event.*;
import java.util.*;
import javax.swing.JDesktopPane;
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
    public void tableListCompanyNamesAll() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying list of all companies");
                p.show();
                displayStockList(QuoteSource.COMPANIES_AND_FUNDS, null);
                if (!Thread.currentThread().interrupted())
                    ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing stocks by company name, matching a rule that is to be input by the user */
    public void tableListCompanyNamesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Expression expr = ExpressionQuery.getExpression(desktop_instance,
				 "List Companies and Funds",
				 "By Rule");
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying list of companies by rule \""+expr+"\"");
                p.show();

                displayStockList(QuoteSource.COMPANIES_AND_FUNDS,
                                 expr);
                
                ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing all the stocks by symbol */
    public void tableListCommoditiesAll() {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying table of all commodities");
                p.show();

                displayStockList(QuoteSource.ALL_COMMODITIES, null);
                if (!Thread.currentThread().interrupted())
                    ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing stocks by symbol, matching a rule that is to be input by the user */
    public void tableListCommoditiesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Expression expr = ExpressionQuery.getExpression(desktop_instance,
                                                               "List Commodities",
                                                               "By Rule"); 
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying table commodities by rule \""+expr+"\"");
                p.show();

                displayStockList(QuoteSource.ALL_COMMODITIES,expr);
                ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing all the indices by symbol */
    public void tableListIndicesAll() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying table of all indices");
                p.show();

                displayStockList(QuoteSource.INDICES, null);
                ProgressDialogManager.closeProgressDialog();
            }
        });
        t.start();
    }

    /** Display an internal frame, listing indices by symbol, matching a rule that is to be input by the user */
    public void tableListIndicesByRule() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Expression expr = ExpressionQuery.getExpression(desktop_instance,
                                                               "List Indices",
                                                               "By Rule"); 

                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying list of companies by rule \""+expr+"\"");
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
     * @see org.mov.parser.Expression
     */
    private void displayStockList(int searchRestriction, 
				  Expression expression) {
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

    public void openPortfolio(String portfolioName) {
	Portfolio portfolio = PreferencesManager.loadPortfolio(portfolioName);

	QuoteCache cache = 
	    new QuoteCache(QuoteSourceManager.getSource().getLatestQuoteDate(),
			   QuoteSource.ALL_COMMODITIES);
	
	((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(new PortfolioModule(desktop_instance, portfolio, cache));
    }

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
	    
	    QuoteCache cache = 
		new QuoteCache(QuoteSourceManager.getSource().getLatestQuoteDate(),
			       QuoteSource.ALL_COMMODITIES);

	    ((DesktopManager)(desktop_instance.getDesktopManager())).newFrame(new PortfolioModule(desktop_instance, portfolio, cache));
	}
    }
    /** Displays a graph closing prices for stock(s), based on their code. The stock(s) is/are determined by a user prompt */
    public void graphStockByCode() {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                SortedSet s = CommodityListQuery.getCommoditiesByCode(desktop_instance, "Graph stocks by code");
                String str = s.toString();
                str = str.substring(1,str.length()-1);
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying graph of stock symbols "+str);
                p.show();
                graphStock(s);
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
                SortedSet s = CommodityListQuery.getCommodityByName(desktop_instance, "Graph stocks by name");
                String str = s.toString();
                str = str.substring(1,str.length()-1);
                ProgressDialog p = ProgressDialogManager.getProgressDialog();
                p.setTitle("Displaying graph of stocks named "+str);
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
