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

package org.mov.table;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import org.mov.main.CommandManager;
import org.mov.main.Module;
import org.mov.main.ModuleFrame;
import org.mov.util.TradingDate;
import org.mov.prefs.PreferencesManager;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.Quote;
import org.mov.quote.QuoteBundle;
import org.mov.quote.ScriptQuoteBundle;
import org.mov.quote.Symbol;
import org.mov.ui.AbstractTable;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.ui.DesktopManager;
import org.mov.ui.MainMenu;
import org.mov.ui.MenuHelper;
import org.mov.ui.QuoteModel;
import org.mov.ui.SymbolListDialog;
import org.mov.ui.TextDialog;

public class WatchScreenModule extends AbstractTable implements Module, ActionListener {

    // Main menu items
    private JMenuBar menuBar;
    private JMenuItem addSymbols;
    private JMenuItem removeSymbols;
    private JMenuItem graphSymbols;
    private JMenuItem tableSymbols;
    private JMenuItem tableClose;
    private JMenuItem renameWatchScreen;
    private JMenuItem deleteWatchScreen;
    private JMenuItem applyEquationsMenuItem;

    // Poup menu items
    private JMenuItem popupRemoveSymbols = null;
    private JMenuItem popupGraphSymbols = null;
    private JMenuItem popupTableSymbols = null;

    private PropertyChangeSupport propertySupport;
    private ScriptQuoteBundle quoteBundle;

    private WatchScreen watchScreen;
    private QuoteModel model;

    // Frame Icon
    private String frameIcon = "org/mov/images/TableIcon.gif";

    // Set to true if weve deleted this watch screen and shouldn't try
    // to save it when we exit
    private boolean isDeleted = false;

    /**
     * Create a watch screen table.
     *
     * @param watchScreen the watch screen object
     * @param quoteBundle watch screen quotes
     */
    public WatchScreenModule(WatchScreen watchScreen,
                             ScriptQuoteBundle quoteBundle) {
        this.watchScreen = watchScreen;
	this.quoteBundle = quoteBundle;
	propertySupport = new PropertyChangeSupport(this);

        model = new QuoteModel(quoteBundle, getQuotes(), Column.HIDDEN, Column.VISIBLE);
	setModel(model, QuoteModel.SYMBOL_COLUMN, SORT_UP);
	showColumns(model);
	addMenu();
	model.addTableModelListener(this);
        resort();

        // If the user clicks on the table trap it.
	addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent evt) {
                    handleMouseClicked(evt);
                }
	    });
    }


    // Graph menu item is only enabled when items are selected in the table.
    private void checkMenuDisabledStatus() {
	int numberOfSelectedRows = getSelectedRowCount();
       
        removeSymbols.setEnabled(numberOfSelectedRows > 0? true : false);
        graphSymbols.setEnabled(numberOfSelectedRows > 0? true : false);
        tableSymbols.setEnabled(numberOfSelectedRows > 0? true : false);
    }

    // If the user double clicks on a stock with the LMB, graph the stock.
    // If the user right clicks over the table, open up a popup menu.
    private void handleMouseClicked(MouseEvent event) {

        Point point = event.getPoint();

        // Right click on the table - raise menu
        if(event.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu menu = new JPopupMenu();

            popupGraphSymbols =
                MenuHelper.addMenuItem(this, menu,
                                       "Graph");
            popupGraphSymbols.setEnabled(getSelectedRowCount() > 0);

            popupTableSymbols =
                MenuHelper.addMenuItem(this, menu,
                                       "Table");
            popupTableSymbols.setEnabled(getSelectedRowCount() > 0);

            menu.addSeparator();

            popupRemoveSymbols =
                MenuHelper.addMenuItem(this, menu,
                                       "Remove");
            popupRemoveSymbols.setEnabled(getSelectedRowCount() > 0);

            menu.show(this, point.x, point.y);
        }

        // Left double click on the table - graph stock
        else if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {

            int row = rowAtPoint(point);

            // Get symbol at row
            Symbol symbol =
                (Symbol)getModel().getValueAt(row, QuoteModel.SYMBOL_COLUMN);

            List symbols = new ArrayList();
            symbols.add(symbol);

            CommandManager.getInstance().graphStockBySymbol(symbols);
        }
    }

    // Create a menu
    private void addMenu() {
	menuBar = new JMenuBar();

        // Watch Screen Menu
        {
            JMenu tableMenu = MenuHelper.addMenu(menuBar, "Watch Screen");
            
            // Show columns menu
            tableMenu.add(createShowColumnMenu(model));
            
            tableMenu.addSeparator();

            applyEquationsMenuItem = MenuHelper.addMenuItem(this, tableMenu,
                                                            "Apply Equations");

            tableMenu.addSeparator();
            
            deleteWatchScreen = MenuHelper.addMenuItem(this, tableMenu,
                                                       "Delete");
            
            renameWatchScreen = MenuHelper.addMenuItem(this, tableMenu,
                                                       "Rename");
            
            tableMenu.addSeparator();
            
            tableClose = MenuHelper.addMenuItem(this, tableMenu,
					    "Close");	
        }

        // Symbol Menu
        {
            JMenu symbolsMenu = MenuHelper.addMenu(menuBar, "Symbols");

            addSymbols =
                MenuHelper.addMenuItem(this, symbolsMenu,
                                       "Add");
            removeSymbols =
                MenuHelper.addMenuItem(this, symbolsMenu,
                                       "Remove");

            symbolsMenu.addSeparator();
            
            graphSymbols =
                MenuHelper.addMenuItem(this, symbolsMenu,
                                       "Graph");
            tableSymbols =
                MenuHelper.addMenuItem(this, symbolsMenu,
                                       "Table");
        }

        // Listen for changes in selection so we can update the menus
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {		
                
                public void valueChanged(ListSelectionEvent e) {
                    checkMenuDisabledStatus();
                }
                
            });

	checkMenuDisabledStatus();
    }

    /**
     * Tell module to save any current state data / preferences data because
     * the window is being closed.
     */
    public void save() {
        // Don't save the watch screen if it was just deleted.
	if(!isDeleted)
            PreferencesManager.saveWatchScreen(watchScreen);
    }

    /**
     * Return the window title.
     *
     * @return	the window title
     */
    public String getTitle() {
        return watchScreen.getName();
    }

    /**
     * Add a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void addModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void removeModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    /**
     * Return frame icon for table module.
     *
     * @return	the frame icon.
     */
    public ImageIcon getFrameIcon() {
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(frameIcon));
    }

    /**
     * Return displayed component for this module.
     *
     * @return the component to display.
     */
    public JComponent getComponent() {
	return this;
    }

    /**
     * Return menu bar for chart module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	return menuBar;
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return true;
    }

    /**
     * Handle widget events.
     *
     * @param	e	action event
     */
    public void actionPerformed(final ActionEvent e) {
	if(e.getSource() == tableClose) {
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}

        // Graph symbols, either by the popup menu or the main menu
        else if((popupGraphSymbols != null && e.getSource() == popupGraphSymbols) ||
                e.getSource() == graphSymbols) {

            int[] selectedRows = getSelectedRows();
            List symbols = new ArrayList();

            for(int i = 0; i < selectedRows.length; i++) {
                Symbol symbol = (Symbol)model.getValueAt(selectedRows[i], QuoteModel.SYMBOL_COLUMN);

                symbols.add(symbol);
            }

            // Graph the highlighted symbols
            CommandManager.getInstance().graphStockBySymbol(symbols);
        }

        // Remove symbols, either by the popup menu or the main menu
        else if((popupRemoveSymbols != null && e.getSource() == popupRemoveSymbols) ||
                e.getSource() == removeSymbols) {

            int[] selectedRows = getSelectedRows();
            List symbols = new ArrayList();

            // Pull out symbols into separate list. We have to do this in two steps
            // because if we start to remove them in this loop the getValueAt()
            // function won't work properly.
            for(int i = 0; i < selectedRows.length; i++) {
                Symbol symbol = (Symbol)model.getValueAt(selectedRows[i], QuoteModel.SYMBOL_COLUMN);
                symbols.add(symbol);
            }

            // Now delete from watch screen
            watchScreen.removeAllSymbols(symbols);

            model.setQuotes(getQuotes());
            model.fireTableDataChanged();
        }

        // Add symbols to watch screen
        else if(e.getSource() == addSymbols)
            addSymbols();

        // Apply equations to watch screen
        else if(e.getSource() == applyEquationsMenuItem)
            applyEquations(quoteBundle, model);
        
        // Delete watch screen
        else if(e.getSource() == deleteWatchScreen)
            deleteWatchScreen();

        // Rename watch screen
        else if(e.getSource() == renameWatchScreen)
            renameWatchScreen();

        // Table symbols, either by the popup menu or the main menu
        else if((popupTableSymbols != null && e.getSource() == popupTableSymbols) ||
                e.getSource() == tableSymbols) {

            int[] selectedRows = getSelectedRows();
            List symbols = new ArrayList();

            for(int i = 0; i < selectedRows.length; i++) {
                Symbol symbol = (Symbol)model.getValueAt(selectedRows[i], QuoteModel.SYMBOL_COLUMN);

                symbols.add(symbol);
            }

            // Table the highlighted symbols
            CommandManager.getInstance().tableStocks(symbols);
        }

	else
            assert false;
    }

    // Delete this watch screen
    private void deleteWatchScreen() {
	int option =
	    JOptionPane.showInternalConfirmDialog(DesktopManager.getDesktop(),
						  "Are you sure you wish to delete this watch screen?",
						  "Delete Watch Screen",
						  JOptionPane.YES_NO_OPTION);
	if(option == JOptionPane.YES_OPTION) {
	    PreferencesManager.deleteWatchScreen(watchScreen.getName());

	    MainMenu.getInstance().updateWatchScreenMenu();

	    // Prevent save() function resurrecting watch screen
	    isDeleted = true;

	    // Close window
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
    }

    // Rename the watch screen
    private void renameWatchScreen() {
	// Handle all action in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread thread = new Thread() {

		public void run() {
                    String oldWatchScreenName = watchScreen.getName();
                    
                    // Get new name for watch screen
                        TextDialog dialog = new TextDialog(DesktopManager.getDesktop(),
                                                       "Enter new watch screen name",
                                                       "Rename Watch Screen",
                                                       oldWatchScreenName);
                    String newWatchScreenName = dialog.showDialog();
                    
                    if(newWatchScreenName != null && newWatchScreenName.length() > 0 &&
                       !newWatchScreenName.equals(oldWatchScreenName)) {
                        
                        // Save the watch screen under the new name
                        watchScreen.setName(newWatchScreenName);
                        PreferencesManager.saveWatchScreen(watchScreen);
                        
                        // Delete the old watch screen
                        PreferencesManager.deleteWatchScreen(oldWatchScreenName);
                        
                        // Update GUI
                        MainMenu.getInstance().updateWatchScreenMenu();
                        propertySupport.firePropertyChange(ModuleFrame.TITLEBAR_CHANGED_PROPERTY, 
                                                           0, 1); 
                    }
                }};

        thread.start();
    }

    // Add symbols to the watchscreen
    private void addSymbols() {
	// Handle all action in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread thread = new Thread() {

		public void run() {
                    Set symbols = SymbolListDialog.getSymbols(DesktopManager.getDesktop(),
                                                              "Add Symbols");
                    if(symbols != null) {
                        for(Iterator iterator = symbols.iterator(); iterator.hasNext();)
                            watchScreen.addSymbol((Symbol)iterator.next());

                        model.setQuotes(getQuotes());
                    }
                }};

        thread.start();
    }

    // Using the watch screen object, create a list of quotes that
    // we should display from the quote bundle
    private List getQuotes() {
        List quotes = new ArrayList();
        TradingDate lastDate = quoteBundle.getLastDate();        

        for(Iterator iterator = watchScreen.getSymbols().iterator();
            iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();
            Quote quote;

            try {
                quote =
                    new Quote(symbol,
                              lastDate,
                              (int)quoteBundle.getQuote(symbol, Quote.DAY_VOLUME, lastDate),
                              quoteBundle.getQuote(symbol, Quote.DAY_LOW, lastDate),
                              quoteBundle.getQuote(symbol, Quote.DAY_HIGH, lastDate),
                              quoteBundle.getQuote(symbol, Quote.DAY_OPEN, lastDate),
                              quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, lastDate));
            }
            catch(MissingQuoteException e) {
                quote = new Quote(symbol, lastDate, 0, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            quotes.add(quote);
        }
        
        return quotes;
    }
}