package org.mov.main;

import java.util.*;
import javax.swing.*;
import org.mov.chart.*;
import org.mov.util.CommodityListQuery;
import org.mov.util.ExpressionQuery;
import org.mov.util.Progress;
import org.mov.parser.Expression;
import org.mov.quote.Quote;
import org.mov.quote.QuoteCache;
import org.mov.quote.QuoteSource;
import org.mov.table.QuoteModule;
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

    /**
     * Sets the desktop that any window operations will be performed on
     * @param desktop The desktop that any window operations will be performed on 
     */
    public void setDesktop(JDesktopPane desktop) {
	desktop_instance = desktop;
    }

    /** Tiles all the open internal frames horizontally */
    public void tileFramesHorizontal() {
	AnalyserDesktopManager.tileFrames(AnalyserDesktopManager.HORIZONTAL);
    }

    /** Tiles all the open internal frames vertically */
    public void tileFramesVertical() {
	AnalyserDesktopManager.tileFrames(AnalyserDesktopManager.VERTICAL);
    }
    /** Arranges all open internal frames in a cascading fashion */
    public void tileFramesCascade() {
	AnalyserDesktopManager.tileFrames(AnalyserDesktopManager.CASCADE);
    }
    /** Allocates as square a shape as possible to open infternal frames*/
    public void tileFramesArrange() {
	AnalyserDesktopManager.tileFrames(AnalyserDesktopManager.ARRANGE);
    }

    /** Display an internal frame, listing all the stocks by company name */
    public void tableListCompanyNamesAll() {
	displayStockList(QuoteSource.COMPANIES_AND_FUNDS,
			 null);
    }

    /** Display an internal frame, listing stocks by company name, matching a rule that is to be input by the user */
    public void tableListCompanyNamesByRule() {
	displayStockList(QuoteSource.COMPANIES_AND_FUNDS,
			 ExpressionQuery.getExpression(desktop_instance,
						       "List Companies and Funds",
						       "By Rule"));
    }

    /** Display an internal frame, listing all the stocks by symbol */
    public void tableListCommoditiesAll() {
	displayStockList(QuoteSource.ALL_COMMODITIES,
			 null);
    }

    /** Display an internal frame, listing stocks by symbol, matching a rule that is to be input by the user */
    public void tableListCommoditiesByRule() {
	displayStockList(QuoteSource.ALL_COMMODITIES,
			 ExpressionQuery.getExpression(desktop_instance,
						       "List Commodities",
						       "By Rule"));
    }

    /** Display an internal frame, listing all the indices by symbol */
    public void tableListIndicesAll() {
	displayStockList(QuoteSource.INDICES,
			 null);
    }

    /** Display an internal frame, listing indices by symbol, matching a rule that is to be input by the user */
    public void tableListIndicesByRule() {
	displayStockList(QuoteSource.INDICES,
			 ExpressionQuery.getExpression(desktop_instance,
						       "List Indices",
						       "By Rule"));
    }

    /** 
     * Internal function for retrieving the required data displaying a table showing the results
     *
     * @param searchRestriction as defined by QuoteSource
     * @param expression as defined by Expression
     * @see org.mov.quote.QouteSource
     * @see org.mov.parser.Expression
     */
    private void displayStockList(int searchRestriction, Expression expression) {
	QuoteCache cache = new QuoteCache(Quote.getSource().getLatestQuoteDate(),
					  searchRestriction);
	((AnalyserDesktopManager)(desktop_instance.getDesktopManager())).newFrame(new QuoteModule(desktop_instance, 
												 cache, 
												 expression));
    }

    /** Displays a graph closing prices for stock(s), based on their code. The stock(s) is/are determined by a user prompt */
    public void graphStockByCode() {
	graphStock(CommodityListQuery.getCommoditiesByCode(desktop_instance, "Graph stocks by code"));
    }

    /** Displays a graph closing prices for stock(s), based on their name. The stock(s) is/are determined by a user prompt */
    public void graphStockByName() {
	graphStock(CommodityListQuery.getCommodityByName(desktop_instance, "Graph stocks by name"));
    }

    private void graphStock(SortedSet companySet) {
	if(companySet != null) {
	    Iterator iterator = companySet.iterator();
	    String symbol;
	    AnalyserChart chart = new AnalyserChart(desktop_instance);
	    
	    // Iterate through companies adding them to the graph
	    boolean owner =
		Progress.getInstance().open();
	    
	    while(iterator.hasNext()) {
		symbol = (String)iterator.next();
		
		QuoteCache cache = new QuoteCache(symbol);
		Graph graph = 
		    new LineGraph(new DayCloseGraphDataSource(cache));
		
		chart.add(graph, 0);
	    }
	    
	    Progress.getInstance().close(owner);
	    
	    chart.redraw();
	    ((AnalyserDesktopManager)(desktop_instance.getDesktopManager())).newFrame(chart);
	}
    }
}

