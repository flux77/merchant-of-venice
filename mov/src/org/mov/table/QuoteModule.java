package org.mov.table;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;
import org.mov.ui.*;

public class QuoteModule extends AbstractTable
    implements Module,
	       ActionListener {
    
    private static final int EQUATION_COLUMNS = 5;

    private static final int SYMBOL_COLUMN = 0;
    private static final int VOLUME_COLUMN = 1;
    private static final int DAY_LOW_COLUMN = 2;
    private static final int DAY_HIGH_COLUMN = 3;
    private static final int DAY_OPEN_COLUMN = 4;
    private static final int DAY_CLOSE_COLUMN = 5;
    private static final int CHANGE_COLUMN = 6;
    private static final int ACTIVITY_COLUMN = 7;
    private static final int EQUATION_SLOT_COLUMN = 8;

    private JMenuBar menuBar;
    private JCheckBoxMenuItem showSymbolsColumn;
    private JCheckBoxMenuItem showVolumeColumn;
    private JCheckBoxMenuItem showDayLowColumn;
    private JCheckBoxMenuItem showDayHighColumn;
    private JCheckBoxMenuItem showDayOpenColumn;
    private JCheckBoxMenuItem showDayCloseColumn;
    private JCheckBoxMenuItem showChangeColumn;
    private JCheckBoxMenuItem[] showEquationColumns =
	new JCheckBoxMenuItem[EQUATION_COLUMNS];

    private JMenuItem sortByMostActive;
    private JMenuItem tableClose;
    
    private PropertyChangeSupport propertySupport;
    private QuoteCache cache;
    private Object[] symbols;
    private org.mov.parser.Expression expression = null;
    
    class Model extends AbstractTableModel {
	private String[] headers = {
	    "Symbol", "Volume", "Day Low", "Day High", "Day Open", 
	    "Day Close", "Change", "Activity"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    String.class, String.class, Change.class, Float.class};

	private TradingDate date = null;
	private QuoteCache cache;
	private Object[] symbols;
    
	public Model(QuoteCache cache, Object[] symbols) {
	    this.cache = cache;
	    this.symbols = symbols;
	    
	    // Pull first date from cache
	    Iterator iterator = cache.dateIterator(0);
	    if(iterator.hasNext())
		date = (TradingDate)iterator.next();
	}
	
	public int getRowCount() {
	    return symbols.length;
	}

	public int getColumnCount() {
	    return headers.length + EQUATION_COLUMNS;
	}
	
	public String getColumnName(int c) {
	    if(c < headers.length) 
		return headers[c];
	    else
		return new String("Eqn. " + (c + 1 - headers.length));
	}

	public Class getColumnClass(int c) {
	    if(c < headers.length) 
		return columnClasses[c];
	    else
		return String.class;
	}
	
	public Object getValueAt(int row, int column) {

	    if(row >= getRowCount()) 
		return "";

	    String symbol = (String)symbols[row];

	    try {
		switch(column) {
		case(SYMBOL_COLUMN):
		    return symbol.toUpperCase();
		    
		case(VOLUME_COLUMN):
		    return new Integer
			((int)cache.getQuote(symbol, Quote.DAY_VOLUME, 
					     date));
		    
		case(DAY_LOW_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_LOW, date));
		    
		case(DAY_HIGH_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_HIGH, date));
		    
		case(DAY_OPEN_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_OPEN, date));
		    
		case(DAY_CLOSE_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_CLOSE, date));
		    
		case(CHANGE_COLUMN):
		    return 
			Converter.changeToChange
			(cache.getQuote(symbol, Quote.DAY_OPEN, date),
			 cache.getQuote(symbol, Quote.DAY_CLOSE, date));

		// This column is never visible but is used to determine
		// the most active stocks
		case(ACTIVITY_COLUMN):
		    return new Float(cache.getQuote(symbol, 
						    Quote.DAY_HIGH, date) *
				     cache.getQuote(symbol, 
						    Quote.DAY_VOLUME, date));
		}
	    }
	    catch(EvaluationException e) {
		// should not happen
	    }

	    return "";
	}
    }

    public QuoteModule(QuoteCache cache) {
	this(cache, null);
    }

    public QuoteModule(QuoteCache cache, 
		      org.mov.parser.Expression expression) {
	
	this.expression = expression;
	this.cache = cache;

	// If theres a rule, create restricted list of symbols to display
	if(expression != null)
	    symbols = extractSymbolsUsingRule(cache);
	else
	    symbols = cache.getSymbols();

	propertySupport = new PropertyChangeSupport(this);
	setModel(new Model(this.cache, symbols), ACTIVITY_COLUMN, SORT_UP);

	addMenu();

	// Set menu items to hide equation slots
	for(int i = 0; i < EQUATION_COLUMNS; i++) {
	    showColumn(EQUATION_SLOT_COLUMN + i, false);
	}
	
	// Activity column is always hidden - its just used for
	// sorting purposes
	showColumn(ACTIVITY_COLUMN, false);

	// If the user double clicks on a cell then graph the stock
	addMouseListener(new MouseAdapter() {

		public void mouseClicked(MouseEvent evt) {
		    
		    Point point = evt.getPoint();
		    if (evt.getClickCount() == 2) {
			int row = rowAtPoint(point);
			
			// Get symbol at row
			String symbol = 
			    (String)getModel().getValueAt(row, SYMBOL_COLUMN);
			
			Vector symbols = new Vector();
			symbols.add((Object)symbol);
			    
			CommandManager.getInstance().graphStockBySymbol(symbols);
		    }
		}
	    });
    }
   
    private Object[] extractSymbolsUsingRule(QuoteCache cache) {

	Object[] symbols = cache.getSymbols();

	// Add symbols to vector when expression proves true
	try {
	    Vector extractedSymbols = new Vector();
	    String symbol;

            ProgressDialog p = ProgressDialogManager.getProgressDialog();
            p.setMaximum(symbols.length);
            p.setTitle("Filtering quotes");
            
            // Traverse array
	    for(int i = 0; i < symbols.length; i++) {
		symbol = (String)symbols[i];

		// True for this stock? Then add it to the table
		if(expression.evaluate(cache, symbol, 0) >=
		   org.mov.parser.Expression.TRUE_LEVEL)
		    extractedSymbols.add(symbol);

		// Wait until weve done one evaluation since thats when the
		// quotes are loaded
                p.increment();
	    }

	    return extractedSymbols.toArray();
	}
	catch(EvaluationException e) {

	    // Tell user expression didnt evaluate properly
	    JOptionPane.
		showInternalMessageDialog(org.mov.ui.DesktopManager.getDesktop(),
					  e.getReason() + ": " +
					  expression.toString(),
					  "Error evaluating expression",
					  JOptionPane.ERROR_MESSAGE);
	    
	    // delete erroneous expression
	    expression = null;

	    // Return all cache's symbols
	    return cache.getSymbols();
	}
	finally {
	    ProgressDialogManager.closeProgressDialog();
	}
    }

    private void addMenu() {
	menuBar = new JMenuBar();

	JMenu tableMenu = MenuHelper.addMenu(menuBar, "Table");

	JMenu columnMenu = 
	    MenuHelper.addMenu(tableMenu, "Show Columns");
	{
	    showSymbolsColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Symbols");
	    showVolumeColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Volume");
	    showDayLowColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Day Low");
	    showDayHighColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Day High");
	    showDayOpenColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Day Open");
	    showDayCloseColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Day Close");
	    showChangeColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Change Column");
	    columnMenu.addSeparator();

	    // Set menu items to hide equation slots
	    for(int i = 0; i < EQUATION_COLUMNS; i++) {
		showEquationColumns[i] = 
		    MenuHelper.addCheckBoxMenuItem(this, columnMenu,
						   "Equation Slot " + (i + 1));
	    }

	    // Put ticks next to the columns that are visible
	    showSymbolsColumn.setState(true);
	    showVolumeColumn.setState(true);
	    showDayLowColumn.setState(true);
	    showDayHighColumn.setState(true);
	    showDayOpenColumn.setState(true);
	    showDayCloseColumn.setState(true);
	    showChangeColumn.setState(true);
	}
       
	tableMenu.addSeparator();

	sortByMostActive = 
	    MenuHelper.addMenuItem(this, tableMenu,
				   "Sort by Most Active");
	
	tableMenu.addSeparator();

	tableClose = MenuHelper.addMenuItem(this, tableMenu,
					    "Close");	
    }

    public void save() {

    }

    public String getTitle() {
	return "Quotes";
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
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/TableIcon.gif"));
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
	
	else if(e.getSource() == sortByMostActive) {
	    setColumnSortStatus(ACTIVITY_COLUMN, SORT_UP);
	    resort();
	    validate();
	    repaint();
	}

	else {
	    // Otherwise its a checkbox menu item
	    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)e.getSource();
	    boolean state = menuItem.getState();
	    int column = SYMBOL_COLUMN;

	    if(menuItem == showSymbolsColumn) 
		column = SYMBOL_COLUMN;

	    else if(menuItem == showVolumeColumn) 
		column = VOLUME_COLUMN;

	    else if(menuItem == showDayLowColumn) 
		column = DAY_LOW_COLUMN;

	    else if(menuItem == showDayHighColumn) 
		column = DAY_HIGH_COLUMN;

	    else if(menuItem == showDayOpenColumn) 
		column = DAY_OPEN_COLUMN;

	    else if(menuItem == showDayCloseColumn) 
		column = DAY_CLOSE_COLUMN;

	    else if(menuItem == showChangeColumn) 
		column = CHANGE_COLUMN;

	    // Otherwise its an equation slot column
	    else {
		for(int i = 0; i < EQUATION_COLUMNS; i++) {
		    if(menuItem == showEquationColumns[i]) {
			column = EQUATION_SLOT_COLUMN + i;
		    }
		}
	    }

	    showColumn(column, state);
	}
    }
    
}
