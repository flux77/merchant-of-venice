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
import org.mov.util.Locale;
import org.mov.portfolio.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.table.*;
import org.mov.ui.*;

/**
 * The main menu of the application.
 */
public class MainMenu implements ActionListener, ModuleListener
{

    // All the menu items
    private JMenuItem fileImportQuotesMenuItem;
    private JMenuItem filePortfolioNewMenuItem;
    private JMenuItem filePreferencesMenuItem;
    private JMenuItem fileExitMenuItem;

    private JMenuItem graphCommodityCodeMenuItem;
    private JMenuItem graphCommodityNameMenuItem;
    private JMenuItem graphMarketAdvanceDeclineMenuItem;

    private JMenuItem quoteWatchScreenNewMenuItem;
    private JMenuItem quoteCompanyListAllMenuItem;
    private JMenuItem quoteCompanyListRuleMenuItem;
    private JMenuItem quoteCompanyListDateMenuItem;
    private JMenuItem quoteIndicesListAllMenuItem;
    private JMenuItem quoteIndicesListRuleMenuItem;
    private JMenuItem quoteIndicesListDateMenuItem;
    private JMenuItem quoteCommoditiesListAllMenuItem;
    private JMenuItem quoteCommoditiesListRuleMenuItem;
    private JMenuItem quoteCommoditiesListDateMenuItem;
    private JMenuItem quoteStocksListSymbolsMenuItem;

    private JMenuItem analysisPaperTradeMenuItem;
    private JMenuItem analysisGPMenuItem;

    private JMenuItem windowTileHorizontalMenuItem;
    private JMenuItem windowTileVerticalMenuItem;
    private JMenuItem windowCascadeMenuItem;
    private JMenuItem windowGridMenuItem;

    private JMenuItem helpContentsMenuItem;
    private JMenuItem helpAboutMenuItem;

    private JMenu helpMenu;
    private JMenu windowMenu;
    private JMenu filePortfolioMenu;
    private JMenu graphPortfolioMenu;
    private JMenu quoteWatchScreenMenu;

    private org.mov.ui.DesktopManager desktopManager;
    private JDesktopPane desktop;
    private JFrame frame;

    // Mappings between menus and portfolios
    private HashMap portfolioHash = new HashMap();
    private HashMap portfolioGraphHash = new HashMap();

    // Mapping between menus and watch screens
    private HashMap watchScreenHash = new HashMap();

    // Used for window menu - keeps track of modules + menu items
    private Hashtable moduleToMenuItemHash = new Hashtable();
    private Hashtable menuItemToModuleHash = new Hashtable();

    // Singleton instance of this class
    private static MainMenu instance = null;

    /**
     * Construct a new main menu and attach it to the given frame.
     *
     * @param	frame	the window frame
     * @param	desktopManager	the desktop to lunch internal frames on
     */
    public static MainMenu getInstance(JFrame frame, 
				       org.mov.ui.DesktopManager desktopManager) {
	if(instance == null)
	    instance = new MainMenu(frame, desktopManager);
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
		     org.mov.ui.DesktopManager desktopManager) {
	this.frame = frame;
	this.desktopManager = desktopManager;
	this.desktop = DesktopManager.getDesktop();

	// Listens for modules being added, delete or having their names
	// changed
	desktopManager.addModuleListener(this);
	
	JMenuBar menuBar = new JMenuBar();
	// File 
	{	   
	    JMenu fileMenu = MenuHelper.addMenu(menuBar, Locale.getString("FILE"), 'F');

	    // File -> Portfolio
	    filePortfolioMenu = MenuHelper.addMenu(fileMenu, Locale.getString("PORTFOLIO"), 'P');

	    fileMenu.addSeparator();

	    // File -> Import
	    fileImportQuotesMenuItem = MenuHelper.addMenuItem(this, fileMenu, 
							      Locale.getString("IMPORT_QUOTES"), 
							      'I');
	    // File -> Preferences
	    filePreferencesMenuItem = MenuHelper.addMenuItem(this, fileMenu, 
							     Locale.getString("PREFERENCES"), 
							     'R');
	    fileMenu.addSeparator();

	    // File -> Exit
	    fileExitMenuItem = MenuHelper.addMenuItem(this, fileMenu, Locale.getString("EXIT"), 'Q');
	}

	// Table
	{
	    JMenu quoteMenu = MenuHelper.addMenu(menuBar, Locale.getString("TABLE"), 'T');

            // Table -> Watch screens
            quoteWatchScreenMenu = MenuHelper.addMenu(quoteMenu, Locale.getString("WATCH_SCREEN"), 'W');

            quoteMenu.addSeparator();
	    
	    // Table -> Companies + Funds
	    JMenu quoteMenuCompany = MenuHelper.addMenu(quoteMenu, 
							Locale.getString("ALL_ORDINARIES"),
					     'C');

	    // Table -> Companies + Funds -> List all
	    quoteCompanyListAllMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCompany, Locale.getString("LIST_ALL"));

	    // Table -> Companies + Funds -> List by rule
	    quoteCompanyListRuleMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCompany, Locale.getString("LIST_BY_RULE"));

	    // Table -> Companies + Funds -> List by date
	    quoteCompanyListDateMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCompany, Locale.getString("LIST_BY_DATE"));
	    
	    // Table -> Indices
	    JMenu quoteMenuIndices = MenuHelper.addMenu(quoteMenu, 
							Locale.getString("MARKET_INDICES"), 'I');

	    // Table -> Indices -> List All
	    quoteIndicesListAllMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuIndices, Locale.getString("LIST_ALL"));

	    // Table -> Indices -> List by Rule
	    quoteIndicesListRuleMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuIndices, Locale.getString("LIST_BY_RULE"));

	    // Table -> Indices -> List by Date
	    quoteIndicesListDateMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuIndices, Locale.getString("LIST_BY_DATE"));
	    
	    // Table -> All Stocks
	    JMenu quoteMenuCommodities = MenuHelper.addMenu(quoteMenu, 
							    Locale.getString("ALL_STOCKS"),
							    'A');

	    // Table -> All Stocks -> List All
	    quoteCommoditiesListAllMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCommodities, 
				       Locale.getString("LIST_ALL"), 'L');

	    // Table -> All Stocks -> List by Rule
	    quoteCommoditiesListRuleMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCommodities, 
				       Locale.getString("LIST_BY_RULE"),'B');

	    // Table -> All Stocks -> List by Date
	    quoteCommoditiesListDateMenuItem = 
		MenuHelper.addMenuItem(this, quoteMenuCommodities, 
				       Locale.getString("LIST_BY_DATE"),'D');

            // Table -> Stocks -> List by Symbols
            JMenu quoteMenuStocks = MenuHelper.addMenu(quoteMenu,
                                                       Locale.getString("STOCKS"), 'S');

            quoteStocksListSymbolsMenuItem =
                MenuHelper.addMenuItem(this, quoteMenuStocks, 
				       Locale.getString("LIST_BY_SYMBOLS"), 'B');
	}
	
	// Graph        
	{
	    JMenu graphMenu = MenuHelper.addMenu(menuBar, Locale.getString("GRAPH"), 'G');
	    
	    // Graph -> Commodities
	    JMenu graphCommodityMenu = MenuHelper.addMenu(graphMenu, 
							  Locale.getString("STOCK"));
	    
	    // Graph -> Commodities -> By Codes
	    graphCommodityCodeMenuItem = 
		MenuHelper.addMenuItem(this, graphCommodityMenu, 
				       Locale.getString("GRAPH_BY_SYMBOLS"), 'G');
	    
	    // Graph -> Commodities -> By Name
            //	    graphCommodityNameMenuItem = 
            //		MenuHelper.addMenuItem(this, graphCommodityMenu, 
            //			       "By Name",'N');

	    // Graph -> Market Indicator
	    JMenu graphMarketIndicator = 
		MenuHelper.addMenu(graphMenu, Locale.getString("MARKET_INDICATOR"));

	    // Graph -> Market Indicator -> Advance/Decline
	    graphMarketAdvanceDeclineMenuItem =
		MenuHelper.addMenuItem(this, graphMarketIndicator,
				       Locale.getString("ADVANCE_DECLINE"));

	    // Graph -> Portfolio
	    graphPortfolioMenu = MenuHelper.addMenu(graphMenu, Locale.getString("PORTFOLIO"));
	}

	// Analysis menu
	{
	    JMenu analysisMenu = 
		MenuHelper.addMenu(menuBar, Locale.getString("ANALYSIS"), 'A');

	    analysisPaperTradeMenuItem = 
		MenuHelper.addMenuItem(this, analysisMenu,
				       Locale.getString("PAPER_TRADE"));

	    analysisGPMenuItem = 
		MenuHelper.addMenuItem(this, analysisMenu,
				       Locale.getString("GP"));
	}

	// Window menu
	{
	    windowMenu = MenuHelper.addMenu(menuBar, Locale.getString("WINDOW"), 'W');
	    windowTileHorizontalMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, Locale.getString("TILE_HORIZONTALLY"));
	    windowTileHorizontalMenuItem.setEnabled(false);
	    windowTileVerticalMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, Locale.getString("TILE_VERTICALLY"));
	    windowTileVerticalMenuItem.setEnabled(false);
	    windowCascadeMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, Locale.getString("CASCADE"));
	    windowCascadeMenuItem.setEnabled(false);
	    windowGridMenuItem = 
		MenuHelper.addMenuItem(this, windowMenu, Locale.getString("ARRANGE_ALL"));
	    windowGridMenuItem.setEnabled(false);
	}

        // Help menu
        {
            helpMenu = MenuHelper.addMenu(menuBar, Locale.getString("HELP"), 'H');
            helpContentsMenuItem = MenuHelper.addMenuItem(this, helpMenu, 
							  Locale.getString("CONTENTS"));
            helpAboutMenuItem = MenuHelper.addMenuItem(this, helpMenu, 
						       Locale.getString("ABOUT"));
        }

	// Build portfolio and watchscreen menus
	updatePortfolioMenu();
        updateWatchScreenMenu();

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

		    // Perhaps user selected a module from the window menu?
		    Component c = (Component)menuItemToModuleHash.get(menu);
		    
		    // File Menu
		    if(menu == fileImportQuotesMenuItem) {
			CommandManager.getInstance().importQuotes();
		    }
		    else if(menu == filePortfolioNewMenuItem)
			CommandManager.getInstance().newPortfolio();

		    // Maybe it's a portfolio?
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
		    else if(menu == filePreferencesMenuItem)
			// Display preferences
			CommandManager.getInstance().openPreferences();
		    
		    // Table Menu
		    else if(menu == quoteWatchScreenNewMenuItem)
			CommandManager.getInstance().newWatchScreen();

		    // Maybe it's a watch screen
		    else if(watchScreenHash.get(menu) != null) {
			String watchScreenName =
			    (String)watchScreenHash.get(menu);
			CommandManager.getInstance().openWatchScreen(watchScreenName);
		    }

		    else if(menu == quoteCommoditiesListAllMenuItem)
			CommandManager.getInstance().tableStocks(QuoteRange.ALL_SYMBOLS);
		    else if (menu == quoteCommoditiesListRuleMenuItem)
			CommandManager.getInstance().tableStocksByRule(QuoteRange.ALL_SYMBOLS);
		    else if (menu == quoteCommoditiesListDateMenuItem)
			CommandManager.getInstance().tableStocksByDate(QuoteRange.ALL_SYMBOLS);

		    else if(menu == quoteCompanyListAllMenuItem)
                        CommandManager.getInstance().tableStocks(QuoteRange.ALL_ORDINARIES);
		    else if (menu == quoteCompanyListRuleMenuItem)
			CommandManager.getInstance().tableStocksByRule(QuoteRange.ALL_ORDINARIES);
		    else if (menu == quoteCompanyListDateMenuItem)
			CommandManager.getInstance().tableStocksByDate(QuoteRange.ALL_ORDINARIES);

		    else if (menu == quoteIndicesListAllMenuItem)
			CommandManager.getInstance().tableStocks(QuoteRange.MARKET_INDICES);
		    else if (menu == quoteIndicesListRuleMenuItem)
			CommandManager.getInstance().tableStocksByRule(QuoteRange.MARKET_INDICES);
		    else if (menu == quoteIndicesListDateMenuItem)
			CommandManager.getInstance().tableStocksByDate(QuoteRange.MARKET_INDICES);

                    else if (menu == quoteStocksListSymbolsMenuItem)
			CommandManager.getInstance().tableStocks(null);
		    
		    // Graph Menu
		    else if (menu == graphCommodityCodeMenuItem) 
			CommandManager.getInstance().graphStockBySymbol(null);
                    //		    else if (menu == graphCommodityNameMenuItem)
                    //	CommandManager.getInstance().graphStockByName();
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

		    // Analysis Menu
		    else if (menu == analysisPaperTradeMenuItem)
			CommandManager.getInstance().paperTrade();
		    else if (menu == analysisGPMenuItem)
			CommandManager.getInstance().gp();

		    // Window Menu
		    else if (menu == windowTileHorizontalMenuItem)
			CommandManager.getInstance().tileFramesHorizontal();
		    else if (menu == windowTileVerticalMenuItem)
			CommandManager.getInstance().tileFramesVertical();
		    else if (menu == windowCascadeMenuItem)
			CommandManager.getInstance().tileFramesCascade();
		    else if (menu == windowGridMenuItem)
			CommandManager.getInstance().tileFramesArrange();

                    // Help Menu
                    else if (menu == helpContentsMenuItem)
                        CommandManager.getInstance().openHelp();
                    else if (menu == helpAboutMenuItem)
                        CommandManager.getInstance().openAboutDialog();

                    // If the user selected a window from the Window menu, then
                    // bring that window to the front
		    else if(c != null) {

                        Module module;

                        // Get module
                        try {
                            module = (Module)c;
                        } 
                        catch(ClassCastException e) {
                            assert false;
                            return;
                        }
                            
                        // Get frame from module
                        Component component =  module.getComponent();
                        while(!(component instanceof JInternalFrame)) {
                            component = component.getParent();
                        }
			
                        JInternalFrame frame = (JInternalFrame)component;

			try {
			    frame.setIcon(false);
			    desktop.setSelectedFrame(frame);
			    frame.setSelected(true);
			    frame.toFront();
			} 
                        catch (PropertyVetoException exception) {
                            assert false;
                        }			
		    }
		    else 
			assert false;
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
	filePortfolioNewMenuItem = 
	    MenuHelper.addMenuItem(this, filePortfolioMenu, Locale.getString("NEW_PORTFOLIO"));

	// Build both portfolio menus
	String[] portfolioNames = PreferencesManager.getPortfolioNames();

	if(portfolioNames.length > 0)
	    filePortfolioMenu.addSeparator();
	else {
	    JMenuItem noPortfoliosMenuItem = new JMenuItem(Locale.getString("NO_PORTFOLIOS"));
	    noPortfoliosMenuItem.setEnabled(false);
	    graphPortfolioMenu.add(noPortfoliosMenuItem);
	}
		
	portfolioHash = buildMenu(filePortfolioMenu, portfolioNames);
	portfolioGraphHash = buildMenu(graphPortfolioMenu, portfolioNames);
    }

    /**
     * Inform menu that the list of watch screens has changed and that
     * its menus should be redrawn
     */
    public void updateWatchScreenMenu() {
	// Remove old menu items from watch screen menu (if there were any)
	quoteWatchScreenMenu.removeAll();

	quoteWatchScreenNewMenuItem = 
	    MenuHelper.addMenuItem(this, quoteWatchScreenMenu, 
				   Locale.getString("NEW_WATCH_SCREEN"));

	// Build both portfolio menus
	String[] watchScreenNames = PreferencesManager.getWatchScreenNames();

	if(watchScreenNames.length > 0)
	    quoteWatchScreenMenu.addSeparator();

	watchScreenHash = buildMenu(quoteWatchScreenMenu, watchScreenNames);
    }

    // Take the list of menu item and add them to the given menu. Return
    // a hashmap which maps each menu created with the given menu item.
    private HashMap buildMenu(JMenu menu, String[] items)
    {
	HashMap menuMap = new HashMap();
	
	if(items.length > 0) {
	    for(int i = 0; i < items.length; i++) {
		JMenuItem menuItem = MenuHelper.addMenuItem(this, menu, items[i]);
		menuMap.put(menuItem, items[i]);
	    }
	}

	return menuMap;
    }
}



