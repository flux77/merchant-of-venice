package org.mov.main;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.chart.*;
import org.mov.parser.*;
import org.mov.table.*;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.ui.InternalFrameHandler;

public class AnalyserMenu implements ActionListener, PropertyChangeListener {

    private JMenuItem filePreferencesQuoteMenuItem;
    private JMenuItem fileExitMenuItem;
    private JMenuItem graphCommodityCodeMenuItem;
    private JMenuItem graphCommodityNameMenuItem;
    private JMenuItem tableCompanyListAllMenuItem;
    private JMenuItem tableCompanyListRuleMenuItem;
    private JMenuItem tableIndicesListAllMenuItem;
    private JMenuItem tableIndicesListRuleMenuItem;
    private JMenuItem tableCommoditiesListAllMenuItem;
    private JMenuItem tableCommoditiesListRuleMenuItem;
    private JMenuItem windowTileHorizontalMenuItem;
    private JMenuItem windowTileVerticalMenuItem;
    private JMenuItem windowCascadeMenuItem;
    private JMenuItem windowGridMenuItem;

    private JMenu windowMenu;
    private JDesktopPane desktop;
    private JFrame frame;

    static private int DEFAULT_FRAME_WIDTH = 425;
    static private int DEFAULT_FRAME_HEIGHT = 350;

    public AnalyserMenu(JFrame frame, JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;

	JMenuBar menuBar = new JMenuBar();

	// File 
	{	   
	    JMenu fileMenu = addMenu(menuBar, "File");

	    // File -> Preferences
	    JMenu filePreferences = addMenu(fileMenu, "Preferences");

	    // File -> Preferences -> Stock Quote Source
	    filePreferencesQuoteMenuItem = addMenuItem(filePreferences, 
						       "Quote Source");

	    fileMenu.addSeparator();

	    // File -> Exit
	    fileExitMenuItem = addMenuItem(fileMenu, "Exit");
	}

	// Table
	{
	    JMenu tableMenu = addMenu(menuBar, "Table");
	    
	    // Table -> Companies + Funds
	    JMenu tableMenuCompany = addMenu(tableMenu, "Companies + Funds");

	    // Table -> Companies + Funds -> List all
	    tableCompanyListAllMenuItem = 
		addMenuItem(tableMenuCompany, "List all");

	    // Table -> Companies + Funds -> List by rule
	    tableCompanyListRuleMenuItem = 
		addMenuItem(tableMenuCompany, "List by rule");
	    
	    // Table -> Indices
	    JMenu tableMenuIndices = addMenu(tableMenu, "Indices");

	    // Table -> Indices -> List All
	    tableIndicesListAllMenuItem = 
		addMenuItem(tableMenuIndices, "List all");

	    // Table -> Indices -> List by Rule
	    tableIndicesListRuleMenuItem = 
		addMenuItem(tableMenuIndices, "List by rule");
	    
	    // Table -> All Commodities
	    JMenu tableMenuCommodities = addMenu(tableMenu, "All Commodities");

	    // Table -> All Commodities -> List All
	    tableCommoditiesListAllMenuItem = 
		addMenuItem(tableMenuCommodities, "List all");

	    // Table -> All Commodities -> List by Rule
	    tableCommoditiesListRuleMenuItem = 
		addMenuItem(tableMenuCommodities, "List by rule");
	}
	
	// Graph        
	{
	    JMenu graphMenu = addMenu(menuBar, "Graph");
	    
	    // Graph -> Commodities
	    JMenu graphCommodityMenu = addMenu(graphMenu, "Commodities");
	    
	    // Graph -> Commodities -> By Codes
	    graphCommodityCodeMenuItem = 
		addMenuItem(graphCommodityMenu, "By Symbols");
	    
	    // Graph -> Commodities -> By Name
	    graphCommodityNameMenuItem = 
		addMenuItem(graphCommodityMenu, "By Name");
	}
     
	// Portfolio menu
	{
	    JMenu portfolioMenu = addMenu(menuBar, "Portfolio");
	}

	// Paper-trade menu
	{
	    JMenu paperTradeMenu = addMenu(menuBar, "Paper Trade");
	}

	// Genetic Algorithm menu
	{
	    JMenu geneticAlgorithmMenu = addMenu(menuBar, "GA");
	}

	// Window menu
	{
	    windowMenu = addMenu(menuBar, "Window");
	    windowMenu.addSeparator();
	    windowTileHorizontalMenuItem = addMenuItem(windowMenu, "Tile Horizontally");
	    windowTileHorizontalMenuItem.setEnabled(false);
	    windowTileVerticalMenuItem = addMenuItem(windowMenu, "Tile Vertically");
	    windowTileVerticalMenuItem.setEnabled(false);
	    windowCascadeMenuItem = addMenuItem(windowMenu, "Cascade");
	    windowCascadeMenuItem.setEnabled(false);
	    windowGridMenuItem = addMenuItem(windowMenu, "Arrange all");
	    windowGridMenuItem.setEnabled(false);
	}

	frame.setJMenuBar(menuBar);
    }

    private JMenuItem addMenuItem(JMenuItem parent, String text) {
	JMenuItem menuItem = new JMenuItem(text);
	menuItem.addActionListener(this);
	parent.add(menuItem);

	return menuItem;
    } 

    private JMenu addMenu(JMenuBar parent, String text) {
	JMenu menu = new JMenu(text);
	parent.add(menu);
	
	return menu;
    }

    private JMenu addMenu(JMenu parent, String text) {
	JMenu menu = new JMenu(text);
	parent.add(menu);
	
	return menu;
    }

    public void actionPerformed(final ActionEvent e) {

	// Handle all menu actions in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread menuAction = new Thread() {

		public void run() {

		    // They should all be menu actions
		    JMenuItem menu = (JMenuItem)e.getSource();
		    
		    // Is it the file menu?
		    if(menu == fileExitMenuItem)
			System.exit(0);
		    else if(menu == filePreferencesQuoteMenuItem) {
			// Display preferences
			newCentredFrame(new PreferencesModule(desktop, PreferencesModule.QUOTE_SOURCE_PAGE));
		    }
		    
		    // Is it a table menu?
		    if(menu == tableCommoditiesListAllMenuItem ||
		       menu == tableCompanyListRuleMenuItem ||
		       menu == tableCompanyListAllMenuItem ||
		       menu == tableIndicesListAllMenuItem) {
			
			handleTableMenuAction(menu);
		    }
		    
		    // Is it a graph menu?
		    else if(menu == graphCommodityCodeMenuItem ||
			    menu == graphCommodityNameMenuItem) {
			
			handleGraphMenuAction(menu);
		    }
		    
		    
		    // Is it a window handling function?
		    else if (menu == windowTileHorizontalMenuItem) {
			InternalFrameHandler.tileFrames(desktop, 
							InternalFrameHandler.HORIZONTAL);
		    }
		    else if (menu == windowTileVerticalMenuItem) {
			InternalFrameHandler.tileFrames(desktop, 
							InternalFrameHandler.VERTICAL);
		    }
		    else if (menu == windowCascadeMenuItem) {
			InternalFrameHandler.tileFrames(desktop, 
							InternalFrameHandler.CASCADE);
		    }
		    else if (menu == windowGridMenuItem) {
			InternalFrameHandler.tileFrames(desktop, 
							InternalFrameHandler.ARRANGE);
		    }
		}
	    };
	
	menuAction.start();
    }

    public void handleGraphMenuAction(JMenuItem menu) {

	SortedSet companySet;

	// Get graph either by code or name
	if(menu == graphCommodityCodeMenuItem)
	    companySet = 
		CommodityListQuery.getCommoditiesByCode(desktop, "New graph");
	else
	    companySet = 
		CommodityListQuery.getCommodityByName(desktop, "New graph");

	if(companySet != null) {
	    
	    Iterator iterator = companySet.iterator();
	    String symbol;
	    AnalyserChart chart = new AnalyserChart(desktop);
	    
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
	    newFrame(chart);		
	}
    }	

    public void handleTableMenuAction(JMenuItem menu) {

	// Get search restriction, i.e. is it indices, all commodities or
	// just funds & companies?
	int searchRestriction;       
	if(menu == tableCommoditiesListAllMenuItem ||
	   menu == tableCommoditiesListRuleMenuItem)
	    searchRestriction = QuoteSource.ALL_COMMODITIES;

	else if(menu == tableCompanyListAllMenuItem ||
		menu == tableCompanyListRuleMenuItem)
	    searchRestriction = QuoteSource.COMPANIES_AND_FUNDS;

	else 
	    searchRestriction = QuoteSource.INDICES;

	// Get restriction expression if necessary
	org.mov.parser.Expression expression = null;
	boolean askedForExpression = false;

	if(menu == tableCommoditiesListRuleMenuItem) {
	    expression = 
		ExpressionQuery.getExpression(desktop,
					      "List All Commodities", 
					      "By Rule");
	    askedForExpression = true;
	}

	else if(menu == tableCompanyListRuleMenuItem) {
	    expression = 
		ExpressionQuery.getExpression(desktop,
					      "List Companies + Funds", 
					      "By Rule");
	    askedForExpression = true;
	}

	else if(menu == tableIndicesListRuleMenuItem) {
	    expression = 
		ExpressionQuery.getExpression(desktop,
					      "List Indices",
					      "By Rule");
	    askedForExpression = true;
	}

	// Only continue if user didnt ask for expression then pressed
	// cancel
	if(!askedForExpression || expression != null) {
	    // Create cache with stock quotes for this day
	    QuoteCache cache =
		new QuoteCache(Quote.getSource().getLatestQuoteDate(),
			       searchRestriction);

	    // Display table of quotes
	    newFrame(new QuoteModule(desktop, cache, expression));
	}
    }

    private void newFrame(AnalyserModule module) {
	newFrame(module, 0, 0, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
    }
    
    private void newCentredFrame(AnalyserModule module) {
	int xOffset = (desktop.getWidth() - DEFAULT_FRAME_WIDTH) / 2;
	int yOffset = (desktop.getHeight() - DEFAULT_FRAME_HEIGHT) / 2;

	newFrame(module, xOffset, yOffset, DEFAULT_FRAME_WIDTH,
		 DEFAULT_FRAME_HEIGHT);
    }

    private void newFrame(AnalyserModule module, int x, int y,
			  int width, int height) {

	// Make sure new frame is within window bounds

	// ORDER IMPORTANT
	{
	    if(x > width)
		x = desktop.getWidth() - width;
	    if(y > height)
		y = desktop.getHeight() - height;
	    if(x < 0) 
		x = 0;
	    if(y < 0)
		y = 0;
	    
	    if(x + width > desktop.getWidth())
		width = desktop.getWidth() - x;
	    if(y + height > desktop.getHeight())
		height = desktop.getHeight() - y;
	}
	
	AnalyserFrame frame = new AnalyserFrame(module, x, y, width, height);
	desktop.add(frame);

	int numframes = (new Integer(System.getProperty("number_of_frames", "0"))).intValue();
	//	System.setProperty();

	try {
	    frame.setSelected(true);
	}
	catch(PropertyVetoException v) {
	    // ignore
	}
	
	frame.moveToFront();		    
    }

    public void propertyChange(PropertyChangeEvent evt) {
	int numframes = ((Integer)evt.getOldValue()).intValue();

	// Remove the old window menus
	if (numframes > 0) {
	    for(int i = 0; i < numframes; i++) {
		windowMenu.remove(0);
	    }
	}
	

	// Put the new ones up
	numframes = ((Integer)evt.getNewValue()).intValue();

	for(int i = numframes - 1; i >= 0; i--) {
	    windowMenu.add(new JMenuItem(Toolkit.getProperty("windowname_"+i, "")), 0);
	}
    }
}

