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
import org.mov.ui.InternalFrameHandler;

public class AnalyserMenu implements ActionListener, PropertyChangeListener {

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

    private TradingDate lastQuoteDay;

    private JDesktopPane desktop;
    private JFrame frame;

    public AnalyserMenu(JFrame frame, JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;

	// Get date of latest stock quotes in database
	lastQuoteDay = Database.getInstance().getLatestQuoteDate();

	JMenuBar menuBar = new JMenuBar();

	// File 
	{	   
	    JMenu fileMenu = addMenu(menuBar, "File");

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

    public void actionPerformed(ActionEvent e) {

	// They should all be menu actions
	JMenuItem menu = (JMenuItem)e.getSource();

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
	    InternalFrameHandler.tileFrames(desktop, InternalFrameHandler.HORIZONTAL);
	}
	else if (menu == windowTileVerticalMenuItem) {
	    InternalFrameHandler.tileFrames(desktop, InternalFrameHandler.VERTICAL);
	}
	else if (menu == windowCascadeMenuItem) {
	    InternalFrameHandler.tileFrames(desktop, InternalFrameHandler.CASCADE);
	}
	else if (menu == windowGridMenuItem) {
	    InternalFrameHandler.tileFrames(desktop, InternalFrameHandler.ARRANGE);
	}

	// Exit?
	else if(menu == fileExitMenuItem) 
	    System.exit(0);
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
	    while(iterator.hasNext()) {
		symbol = (String)iterator.next();
		
		QuoteCache cache = new QuoteCache(symbol);
		Graph graph = 
		    new LineGraph(new DayCloseGraphDataSource(cache));

		chart.add(graph, 0);
	    }

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
	    searchRestriction = Database.ALL_COMMODITIES;

	else if(menu == tableCompanyListAllMenuItem ||
		menu == tableCompanyListRuleMenuItem)
	    searchRestriction = Database.COMPANIES_AND_FUNDS;

	else 
	    searchRestriction = Database.INDICES;

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
	    QuoteCache cache = new QuoteCache(lastQuoteDay, searchRestriction);

	    // Display table of quotes
	    newFrame(new QuoteModule(desktop, cache, expression));
	}
    }

    private void newFrame(AnalyserModule module) {
	AnalyserFrame frame = new AnalyserFrame(module, 0, 0, 400, 300);
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

