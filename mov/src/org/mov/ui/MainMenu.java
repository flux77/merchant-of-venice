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
import org.mov.ui.DesktopManager;

public class MainMenu implements ActionListener, ContainerListener {

    private JMenuItem fileImportQuotesMenuItem;
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

    public MainMenu(JFrame frame, JDesktopPane desktop) {

	this.frame = frame;
	this.desktop = desktop;
	desktop.addContainerListener(this);
	JMenuBar menuBar = new JMenuBar();
	// File 
	{	   
	    JMenu fileMenu = addMenu(menuBar, "File", 'F');

	    // File -> Import
	    fileImportQuotesMenuItem = addMenuItem(fileMenu, "Import Quotes", 
						   'I');

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
		    
		    // File Menu ********************************************************************************
		    if(menu == fileImportQuotesMenuItem) {
			CommandManager.getInstance().importQuotes();
		    }
		    else if(menu == fileExitMenuItem)
			System.exit(0);
		    else if(menu == filePreferencesQuoteMenuItem) {
			// Display preferences
			((DesktopManager)(desktop.getDesktopManager()))
			    .newFrame(new PreferencesModule(desktop, 
							    PreferencesModule.QUOTE_SOURCE_PAGE), true);
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

		    // Must be a window selection action
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
}



