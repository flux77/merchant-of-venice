package org.mov.ui;

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
import org.mov.ui.*;

/**
 * The main menu of the application.
 */
public class MainMenu implements ActionListener, ContainerListener {

    // All the menu items
    private JMenuItem fileImportQuotesMenuItem;
    private JMenuItem filePortfolioNewMenuItem;
    private JMenuItem filePreferencesMenuItem;
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

    private JMenu filePortfolioMenu;
    private JMenu windowMenu;

    private JDesktopPane desktop;
    private JFrame frame;

    private HashMap portfolioHash = new HashMap();

    private static MainMenu instance = null;

    /**
     * Construct a new main menu and attach it to the given frame.
     *
     * @param	frame	the window frame
     * @param	desktop	the desktop to lunch internal frames on
     */
    public static MainMenu getInstance(JFrame frame, JDesktopPane desktop) {
	if(instance == null) {
	    instance = new MainMenu(frame, desktop);
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

    private MainMenu(JFrame frame, JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;
	desktop.addContainerListener(this);
	JMenuBar menuBar = new JMenuBar();
	// File 
	{	   
	    JMenu fileMenu = MenuHelper.addMenu(menuBar, "File", 'F');

	    // File -> Portfolio
	    filePortfolioMenu = MenuHelper.addMenu(fileMenu, "Portfolio", 'P');

	    // File -> Portfolio -> New
	    updatePortfolioMenu();

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
	}

	// Genetic Algorithm menu
	{
	    JMenu geneticAlgorithmMenu = 
		MenuHelper.addMenu(menuBar, "GA", 'A');
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
	    windowMenu.addSeparator();
	}

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
			CommandManager.getInstance().graphStockByCode();
		    else if (menu == graphCommodityNameMenuItem)
			CommandManager.getInstance().graphStockByName();
		    
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
	    };
	
	menuAction.start();
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

	if (o.getClass().getName().equals("org.mov.main.ModuleFrame") ||
	    o.getClass().getName().equals("javax.swing.JInternalFrame$JDesktopIcon")) {
	    if (frame_menu_hash ==  null) {
		frame_menu_hash = new Hashtable();
		menu_frame_hash = new Hashtable();
	    }
	    
	    String title;
	    if (o.getClass().getName().equals("org.mov.main.ModuleFrame"))
		title = ((ModuleFrame)o).getTitle();
	    else if (o.getClass().getName().equals("javax.swing.JInternalFrame$JDesktopIcon"))
		title = "("+((JInternalFrame.JDesktopIcon)o).getInternalFrame().getTitle()+")";
	    else
		title = "Unknown";

	    // Store the menu item in a hash referenced by the window's name
	    JMenuItem i = MenuHelper.addMenuItem(this, windowMenu, title);
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
	if (o.getClass().getName().equals("org.mov.main.ModuleFrame") ||
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

    public void updatePortfolioMenu() {
	// Remove old menu items from portfolio menu (if there were any)
	filePortfolioMenu.removeAll();
	portfolioHash.clear();

	// Now rebuild menu listing all portfolios
	filePortfolioNewMenuItem = 
	    MenuHelper.addMenuItem(this, filePortfolioMenu, "New Portfolio");
	
	String[] portfolioNames = PreferencesManager.getPortfolioNames();
	if(portfolioNames.length > 0) {
	    filePortfolioMenu.addSeparator();
	    
	    for(int i = 0; i < portfolioNames.length; i++) {
		    JMenuItem menu = MenuHelper.addMenuItem(this, 
							    filePortfolioMenu,
							    portfolioNames[i]);
		    portfolioHash.put(menu, portfolioNames[i]);
	    }
	}
    }
}



