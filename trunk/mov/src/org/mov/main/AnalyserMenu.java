package org.mov.main;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.chart.*;
import org.mov.parser.*;
import org.mov.table.*;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.ui.AnalyserDesktopManager;

public class AnalyserMenu implements ActionListener, ContainerListener {

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

    public AnalyserMenu(JFrame frame, JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;
	desktop.addContainerListener(this);
	JMenuBar menuBar = new JMenuBar();
	// File 
	{	   
	    JMenu fileMenu = addMenu(menuBar, "File", 'F');
	    // File -> Preferences
	    JMenu filePreferences = addMenu(fileMenu, "Preferences", 'P');

	    // File -> Preferences -> Stock Quote Source
	    filePreferencesQuoteMenuItem = addMenuItem(filePreferences, 
						       "Quote Source");

	    fileMenu.addSeparator();

	    // File -> Exit
	    fileExitMenuItem = addMenuItem(fileMenu, "Exit", 'x');
	    fileExitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
								   KeyEvent.VK_Q, 
								   ActionEvent.CTRL_MASK));
	}

	// Table
	{
	    JMenu tableMenu = addMenu(menuBar, "Table", 'T');
	    
	    // Table -> Companies + Funds
	    JMenu tableMenuCompany = addMenu(tableMenu, "Companies + Funds", 'f');

	    // Table -> Companies + Funds -> List all
	    tableCompanyListAllMenuItem = 
		addMenuItem(tableMenuCompany, "List all", 'l');

	    // Table -> Companies + Funds -> List by rule
	    tableCompanyListRuleMenuItem = 
		addMenuItem(tableMenuCompany, "List by rule", 'r');
	    
	    // Table -> Indices
	    JMenu tableMenuIndices = addMenu(tableMenu, "Indices",'i');

	    // Table -> Indices -> List All
	    tableIndicesListAllMenuItem = 
		addMenuItem(tableMenuIndices, "List all", 'l');

	    // Table -> Indices -> List by Rule
	    tableIndicesListRuleMenuItem = 
		addMenuItem(tableMenuIndices, "List by rule",'r');
	    
	    // Table -> All Commodities
	    JMenu tableMenuCommodities = addMenu(tableMenu, "All Commodities", 'l');

	    // Table -> All Commodities -> List All
	    tableCommoditiesListAllMenuItem = 
		addMenuItem(tableMenuCommodities, "List all", 'l');
	    tableCommoditiesListAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
									     KeyEvent.VK_L, 
									     ActionEvent.CTRL_MASK));

	    // Table -> All Commodities -> List by Rule
	    tableCommoditiesListRuleMenuItem = 
		addMenuItem(tableMenuCommodities, "List by rule",'r');
	}
	
	// Graph        
	{
	    JMenu graphMenu = addMenu(menuBar, "Graph", 'r');
	    
	    // Graph -> Commodities
	    JMenu graphCommodityMenu = addMenu(graphMenu, "Commodities", 'c');
	    
	    // Graph -> Commodities -> By Codes
	    graphCommodityCodeMenuItem = 
		addMenuItem(graphCommodityMenu, "By Symbols", 's');
	    graphCommodityCodeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
									     KeyEvent.VK_G, 
									     ActionEvent.CTRL_MASK));
	    
	    // Graph -> Commodities -> By Name
	    graphCommodityNameMenuItem = 
		addMenuItem(graphCommodityMenu, "By Name",'n');
	}
     
	// Portfolio menu
	{
	    JMenu portfolioMenu = addMenu(menuBar, "Portfolio", 'o');
	}

	// Paper-trade menu
	{
	    JMenu paperTradeMenu = addMenu(menuBar, "Paper Trade", 'a');
	}

	// Genetic Algorithm menu
	{
	    JMenu geneticAlgorithmMenu = addMenu(menuBar, "GA", 'G');
	}

	// Window menu
	{
	    windowMenu = addMenu(menuBar, "Window", 'W');
	    windowTileHorizontalMenuItem = addMenuItem(windowMenu, "Tile Horizontally");
	    windowTileHorizontalMenuItem.setEnabled(false);
	    windowTileVerticalMenuItem = addMenuItem(windowMenu, "Tile Vertically");
	    windowTileVerticalMenuItem.setEnabled(false);
	    windowCascadeMenuItem = addMenuItem(windowMenu, "Cascade");
	    windowCascadeMenuItem.setEnabled(false);
	    windowGridMenuItem = addMenuItem(windowMenu, "Arrange all");
	    windowGridMenuItem.setEnabled(false);
	    windowMenu.addSeparator();
	}

	frame.setJMenuBar(menuBar);
    }

    /**
     * Creates a menu item and attaches it to a menu
     * @param parent The menu to attach the menu item to
     * @param title The title of the menu item
     */
    private JMenuItem addMenuItem(JMenuItem parent, String title) {
	return addMenuItem(parent, title, (char)0);
    }

    /**
     * Creates a menu item and attaches it to a menu
     * @param parent The menu to attach the menu item to
     * @param title The title of the menu item
     */
    private JMenuItem addMenuItem(JMenuItem parent, String title, char key) {
	JMenuItem menuItem;
	if (key != 0) {
	    menuItem = new JMenuItem(title, key);
	} else {
	    menuItem = new JMenuItem(title);
	}
	menuItem.addActionListener(this);
	parent.add(menuItem);

	return menuItem;
    } 

    /**
     * Creates a menu and attaches it to a component
     * @param parent The component to attach the menu to
     * @param title The title of the menu
     */
    private JMenu addMenu(JComponent parent, String title) {
	return addMenu(parent, title, (char)0);
    }

    /**
     * Creates a menu and attaches it to a component
     * @param parent The component to attach the menu to
     * @param title The title of the menu
     * @param key The accelerator key for the menu
     */
    private JMenu addMenu(JComponent parent, String title, char key) {
	JMenu menu = new JMenu(title);
	if (key != 0)
	    menu.setMnemonic(key);
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
				    ((AnalyserDesktopManager)(desktop.getDesktopManager())).newCentredFrame(new PreferencesModule(desktop, PreferencesModule.QUOTE_SOURCE_PAGE));
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
			AnalyserDesktopManager.tileFrames(desktop, 
							AnalyserDesktopManager.HORIZONTAL);
		    }
		    else if (menu == windowTileVerticalMenuItem) {
			AnalyserDesktopManager.tileFrames(desktop, 
							AnalyserDesktopManager.VERTICAL);
		    }
		    else if (menu == windowCascadeMenuItem) {
			AnalyserDesktopManager.tileFrames(desktop, 
							AnalyserDesktopManager.CASCADE);
		    }
		    else if (menu == windowGridMenuItem) {
			AnalyserDesktopManager.tileFrames(desktop, 
							AnalyserDesktopManager.ARRANGE);
		    }

		    // Must be a window action
		    else {
			Component c = (Component)menu_frame_hash.get(menu);
			if(c != null) {
			    JInternalFrame f = null;
			    if (menu.getText().substring(0,1).equals("(")) {
				JInternalFrame.JDesktopIcon icon = (JInternalFrame.JDesktopIcon)c;
				f = icon.getInternalFrame();
			    } else {
				f = (JInternalFrame) c;
			    }

			    try {
				f.setIcon(false);
				desktop.setSelectedFrame(f);
				f.setSelected(true);
				f.toFront();
			    } catch (PropertyVetoException e) {}
			}
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
	    ((AnalyserDesktopManager)(desktop.getDesktopManager())).newFrame(chart);
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
	    ((AnalyserDesktopManager)(desktop.getDesktopManager())).newFrame(new QuoteModule(desktop, cache, expression));
	}
    }

    private Hashtable frame_menu_hash = null;
    private Hashtable menu_frame_hash = null;

    /**
     * Called by the JDesktopPane to notify of a new JInternalFrame being added to the display
     *
     * @param e - ContainerEvent generated by JDesktopPane
     */
    public void componentAdded(ContainerEvent e) {
	Object o = e.getChild();

	if (o.getClass().getName().equals("org.mov.main.AnalyserFrame") ||
	    o.getClass().getName().equals("javax.swing.JInternalFrame$JDesktopIcon")) {
	    if (frame_menu_hash ==  null) {
		frame_menu_hash = new Hashtable();
		menu_frame_hash = new Hashtable();
	    }
	    
	    String title;
	    if (o.getClass().getName().equals("org.mov.main.AnalyserFrame"))
		title = ((AnalyserFrame)o).getTitle();
	    else if (o.getClass().getName().equals("javax.swing.JInternalFrame$JDesktopIcon"))
		title = "("+((JInternalFrame.JDesktopIcon)o).getInternalFrame().getTitle()+")";
	    else
		title = "Unknown";

	    // Store the menu item in a hash referenced by the window's name
	    JMenuItem i = addMenuItem(windowMenu, title);
	    frame_menu_hash.put(o, i);
	    menu_frame_hash.put(i, o);
	    if (frame_menu_hash.size() == 1) {
		windowTileHorizontalMenuItem.setEnabled(true);
		windowTileVerticalMenuItem.setEnabled(true);
		windowCascadeMenuItem.setEnabled(true);
		windowGridMenuItem.setEnabled(true);
	    }
	}
    }

    /**
     * Called by the JDesktopPane to notify of a JInternalFrame being removed from the display
     *
     * @param e - ContainerEvent generated by JDesktopPane
     */
    public void componentRemoved(ContainerEvent e) {
	Object o = e.getChild();
	if (o.getClass().getName().equals("org.mov.main.AnalyserFrame") ||
	    o.getClass().getName().equals("javax.swing.JInternalFrame$JDesktopIcon")) {
	    windowMenu.remove((JMenuItem)frame_menu_hash.get(o));
	    menu_frame_hash.remove(frame_menu_hash.get(o));
	    frame_menu_hash.remove(o);
	    if (frame_menu_hash.size() == 0) {
		windowTileHorizontalMenuItem.setEnabled(false);
		windowTileVerticalMenuItem.setEnabled(false);
		windowCascadeMenuItem.setEnabled(false);
		windowGridMenuItem.setEnabled(false);
	    }
	}
    }
}


