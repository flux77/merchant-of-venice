package org.mov.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.mov.chart.*;
import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.table.*;
import org.mov.ui.*;

/**
 * The main menu of the application.
 */
public class MainMenu 
    implements ActionListener, ModuleListener
{

    // All the menu items
    private JMenuItem fileImportQuotesMenuItem;
    private JMenuItem filePortfolioNewMenuItem;
    private JMenuItem filePreferencesMenuItem;
    private JMenuItem fileExitMenuItem;

    private JMenuItem graphCommodityCodeMenuItem;
    private JMenuItem graphCommodityNameMenuItem;
    private JMenuItem graphMarketAdvanceDeclineMenuItem;

    private JMenuItem tableCompanyListAllMenuItem;
    private JMenuItem tableCompanyListRuleMenuItem;
    private JMenuItem tableIndicesListAllMenuItem;
    private JMenuItem tableIndicesListRuleMenuItem;
    private JMenuItem tableCommoditiesListAllMenuItem;
    private JMenuItem tableCommoditiesListRuleMenuItem;

    private JMenuItem analysisPaperTradeMenuItem;

    private JMenuItem windowTileHorizontalMenuItem;
    private JMenuItem windowTileVerticalMenuItem;
    private JMenuItem windowCascadeMenuItem;
    private JMenuItem windowGridMenuItem;

    private JMenu windowMenu;
    private JMenu filePortfolioMenu;
    private JMenu graphPortfolioMenu;

    private org.mov.ui.DesktopManager desktopManager;
    private JDesktopPane desktop;
    private JFrame frame;

    private HashMap portfolioHash = new HashMap();
    private HashMap portfolioGraphHash = new HashMap();

    // Used for window menu - keeps track of modules + menu items
    private Hashtable moduleToMenuItemHash = new Hashtable();
    private Hashtable menuItemToModuleHash = new Hashtable();

    // Singleton instance of this class
    private static MainMenu instance = null;

    /**
     * Construct a new main menu and attach it to the given frame.
     *
     * @param	frame	the window frame
     * @param	desktop	the desktop to lunch internal frames on
     */
    public static MainMenu getInstance(JFrame frame, 
				       org.mov.ui.DesktopManager desktopManager, 
				       JDesktopPane desktop) {
	if(instance == null) {
	    instance = new MainMenu(frame, desktopManager, desktop);
	}
	return instance;
    }

    /**
     * Return the instance of the main menu. Will return null if not
     * yet created.
     * 
     * @return	the main menu instance
     */
    public static MainMenu getInstance() {
	return instance;
    }

    private MainMenu(JFrame frame, 
		     org.mov.ui.DesktopManager desktopManager, 
		     JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;
	this.desktopManager = desktopManager;

	// Listens for modules being added, delete or having their names
	// changed
	desktopManager.addModuleListener(this);
	
	JMenuBar menuBar = new JMenuBar();
	// File 
	{	   
	    JMenu fileMenu = MenuHelper.addMenu(menuBar, "File", 'F');

	    // File -> Portfolio
	    filePortfolioMenu = MenuHelper.addMenu(fileMenu, "Portfolio", 'P');

	    fileMenu.addSeparator();

	    // File -> Import
	    fileImportQuotesMenuItem = MenuHelper.addMenuItem(this, fileMenu, 
							      "Import Quotes", 
							      'I');
	    // File -> Preferences
	    filePreferencesMenuItem = MenuHelper.addMenuItem(this, fileMenu, 
							     "Preferences", 
							     'R');
	    fileMenu.addSeparator();

	    // File -> Exit
	    fileExitMenuItem = MenuHelper.addMenuItem(this, fileMenu, "Exit", 'Q');
	}

	// Table
	{
	    JMenu tableMenu = MenuHelper.addMenu(menuBar, "Table", 'T');
	    
	    // Table -> Companies + Funds
	    JMenu tableMenuCompany = MenuHelper.addMenu(tableMenu, 
							"Companies + Funds",
					     'C');

	    // Table -> Companies + Funds -> List all
	    tableCompanyListAllMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuCompany, "List all");

	    // Table -> Companies + Funds -> List by rule
	    tableCompanyListRuleMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuCompany, "List by rule");
	    
	    // Table -> Indices
	    JMenu tableMenuIndices = MenuHelper.addMenu(tableMenu, 
							"Indices", 'I');

	    // Table -> Indices -> List All
	    tableIndicesListAllMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuIndices, "List all");

	    // Table -> Indices -> List by Rule
	    tableIndicesListRuleMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuIndices, "List by rule");
	    
	    // Table -> All Commodities
	    JMenu tableMenuCommodities = MenuHelper.addMenu(tableMenu, 
							    "All Commodities",
							    'A');

	    // Table -> All Commodities -> List All
	    tableCommoditiesListAllMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuCommodities, 
				       "List all", 'L');

	    // Table -> All Commodities -> List by Rule
	    tableCommoditiesListRuleMenuItem = 
		MenuHelper.addMenuItem(this, tableMenuCommodities, 
				       "List by rule",'B');
	}
	
	// Graph        
	{
	    JMenu graphMenu = MenuHelper.addMenu(menuBar, "Graph", 'G');
	    
	    // Graph -> Commodities
	    JMenu graphCommodityMenu = MenuHelper.addMenu(graphMenu, 
							  "Commodities");
	    
	    // Graph -> Commodities -> By Codes
	    graphCommodityCodeMenuItem = 
		MenuHelper.addMenuItem(this, graphCommodityMenu, 
				       "By Symbols", 'G');
	    
	    // Graph -> Commodities -> By Name
	    graphCommodityNameMenuItem = 
		MenuHelper.addMenuItem(this, graphCommodityMenu, 
				       "By Name",'N');

	    // Graph -> Market Indicator
	    JMenu graphMarketIndicator = 
		MenuHelper.addMenu(graphMenu, "Market Indicator");

	    // Graph -> Market Indicator -> Advance/Decline
	    graphMarketAdvanceDeclineMenuItem =
		MenuHelper.addMenuItem(this, graphMarketIndicator,
				       "Advance/Decline");

	    // Graph -> Portfolio
	    graphPortfolioMenu = MenuHelper.addMenu(graphMenu, "Portfolio");
	}

	// Analysis menu
	{
	    JMenu analysisMenu = 
		MenuHelper.addMenu(menuBar, "Analysis", 'A');

	    analysisPaperTradeMenuItem = 
		MenuHelper.addMenuItem(this, analysisMenu,
				       "Paper Trade");
	}

	// Window menu
	{
	    windowMenu = MenuHelper.addMenu(menuBar, "Window", 'W');
	    windowTileHorizontalMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, "Tile Horizontally");
	    windowTileHorizontalMenuItem.setEnabled(false);
	    windowTileVerticalMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, "Tile Vertically");
	    windowTileVerticalMenuItem.setEnabled(false);
	    windowCascadeMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, "Cascade");
	    windowCascadeMenuItem.setEnabled(false);
	    windowGridMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, "Arrange all");
	    windowGridMenuItem.setEnabled(false);
	}

	// Build portfolio menus
	updatePortfolioMenu();

	frame.setJMenuBar(menuBar);
    }

    /**
     * Called when a menu item is selected.
     *
     * @param	e	an action
     */
    public void actionPerformed(final ActionEvent e) {

	// Handle all menu actions in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread menuAction = new Thread() {

		public void run() {
		    // They should all be menu actions
		    JMenuItem menu = (JMenuItem)e.getSource();
		    
		    // File Menu ********************************************************************************
		    if(menu == fileImportQuotesMenuItem) {
			CommandManager.getInstance().importQuotes();
		    }
		    else if(menu == filePortfolioNewMenuItem)
			CommandManager.getInstance().newPortfolio();

		    // Maybe its a portfolio?
		    else if(portfolioHash.get(menu) != null) {
			String portfolioName =
			    (String)portfolioHash.get(menu);

			CommandManager.getInstance().openPortfolio(portfolioName);
		    }

		    else if(menu == fileExitMenuItem) {
			// This exits the application
			frame.dispose();
			return;
		    }
		    else if(menu == filePreferencesMenuItem) {
			// Display preferences
			((DesktopManager)(desktop.getDesktopManager()))
			    .newFrame(new PreferencesModule(desktop), true, true);
		    }
		    
		    // Table Menu *******************************************************************************
		    else if(menu == tableCommoditiesListAllMenuItem)
			CommandManager.getInstance().tableListCommoditiesAll();
		    else if (menu == tableCommoditiesListRuleMenuItem)
			CommandManager.getInstance().tableListCommoditiesByRule();
		    else if(menu == tableCompanyListAllMenuItem)
			CommandManager.getInstance().tableListCompanyNamesAll();
		    else if (menu == tableCompanyListRuleMenuItem)
			CommandManager.getInstance().tableListCompanyNamesByRule();
		    else if (menu == tableIndicesListAllMenuItem)
			CommandManager.getInstance().tableListIndicesAll();
		    else if (menu == tableIndicesListRuleMenuItem)
			CommandManager.getInstance().tableListIndicesByRule();
		    
		    // Graph Menu *******************************************************************************
		    else if (menu == graphCommodityCodeMenuItem) 
			CommandManager.getInstance().graphStockBySymbol(null);
		    else if (menu == graphCommodityNameMenuItem)
			CommandManager.getInstance().graphStockByName();
		    else if (menu == graphMarketAdvanceDeclineMenuItem)
			CommandManager.getInstance().graphAdvanceDecline();

		    // Maybe its a portfolio?
		    else if(portfolioGraphHash.get(menu) != null) {
			String portfolioName =
			    (String)portfolioGraphHash.get(menu);

			Portfolio portfolio = 
			    PreferencesManager.loadPortfolio(portfolioName);

			CommandManager.getInstance().graphPortfolio(portfolio);
		    }

		    // Analysis Menu ******************************************************************************

		    else if (menu == analysisPaperTradeMenuItem)
			CommandManager.getInstance().paperTrade();

		    // Window Menu ******************************************************************************
		    else if (menu == windowTileHorizontalMenuItem)
			CommandManager.getInstance().tileFramesHorizontal();
		    else if (menu == windowTileVerticalMenuItem)
			CommandManager.getInstance().tileFramesVertical();
		    else if (menu == windowCascadeMenuItem)
			CommandManager.getInstance().tileFramesCascade();
		    else if (menu == windowGridMenuItem)
			CommandManager.getInstance().tileFramesArrange();

		    // Maybe its a window ??
		    Component c = (Component)menuItemToModuleHash.get(menu);
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
			} catch (PropertyVetoException exception) {}
		    }
		}
	    };
	
	menuAction.start();
    }

    /**
     * Called by the desktop manager to notify us when a new module is
     * added
     *
     * @param	moduleEvent	Module Event
     */
    public void moduleAdded(ModuleEvent moduleEvent) {
	Module module = (Module)moduleEvent.getSource();
	String title = module.getTitle();

	// First window? Then enable window arrange menu items and
	// add separator
	if (moduleToMenuItemHash.size() == 0) {
	    windowTileHorizontalMenuItem.setEnabled(true);
	    windowTileVerticalMenuItem.setEnabled(true);
	    windowCascadeMenuItem.setEnabled(true);
	    windowGridMenuItem.setEnabled(true);
	    
	    windowMenu.addSeparator();
	}
	
	// Store the menu item in a hash referenced by the window's name
	JMenuItem menuItem = MenuHelper.addMenuItem(this, windowMenu, title);
	moduleToMenuItemHash.put(module, menuItem);
	menuItemToModuleHash.put(menuItem, module);
    }

    /**
     * Called by the desktop manager to notify us when a module is removed
     *
     * @param	moduleEvent	Module Event
     */
    public void moduleRemoved(ModuleEvent moduleEvent) {
	Module module = (Module)moduleEvent.getSource();

	windowMenu.remove((JMenuItem)moduleToMenuItemHash.get(module));
	menuItemToModuleHash.remove(moduleToMenuItemHash.get(module));
	moduleToMenuItemHash.remove(module);

	// No more menu items? Then disable window arrange menu items
	// and remove separator
	if (moduleToMenuItemHash.size() == 0) {
	    windowTileHorizontalMenuItem.setEnabled(false);
	    windowTileVerticalMenuItem.setEnabled(false);
	    windowCascadeMenuItem.setEnabled(false);
	    windowGridMenuItem.setEnabled(false);
	    
	    // Window separator is the last menu item
	    windowMenu.remove(windowMenu.getItemCount() - 1);
	}
    }

    /**
     * Called by the desktop manager to notify us when a module is renamed
     *
     * @param	moduleEvent	Module Event
     */
    public void moduleRenamed(ModuleEvent moduleEvent) {
	// Same as removing and then adding a totally new module
	moduleRemoved(moduleEvent);
	moduleAdded(moduleEvent);
    }

    /**
     * Inform menu that the list of portfolios has changed and that
     * its menus should be redrawn
     */
    public void updatePortfolioMenu() {
	// Remove old menu items from portfolio menus (if there were any)
	filePortfolioMenu.removeAll();
	graphPortfolioMenu.removeAll();

	// Portfolio menu off of file has the ability to create a new
	// portfolio
	MenuHelper.addMenuItem(this, filePortfolioMenu, "New Portfolio");

	if(PreferencesManager.getPortfolioNames().length > 0) {
	    filePortfolioMenu.addSeparator();
	}

	// Build both portfolio menus
	portfolioHash = buildPortfolioMenu(filePortfolioMenu);
	portfolioGraphHash = buildPortfolioMenu(graphPortfolioMenu);
    }

    // Build menu with names of all portfolios. Create hashmap which
    // maps menu items back to the portfolio listed.
    private HashMap buildPortfolioMenu(JMenu portfolioMenu)
    {
	HashMap menuPortfolioMap = new HashMap();
	
	String[] portfolioNames = PreferencesManager.getPortfolioNames();
	if(portfolioNames.length > 0) {
	    for(int i = 0; i < portfolioNames.length; i++) {
		JMenuItem menu = MenuHelper.addMenuItem(this, 
							portfolioMenu,
							portfolioNames[i]);
		menuPortfolioMap.put(menu, portfolioNames[i]);
	    }
	}

	return menuPortfolioMap;
    }
}



