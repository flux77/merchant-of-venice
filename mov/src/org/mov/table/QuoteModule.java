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
    private JCheckBoxMenuItem[] showEquationSlots =
	new JCheckBoxMenuItem[EQUATION_COLUMNS];

    private JMenuItem applyEquations;
    private JMenuItem applyFilter;
    private JMenuItem sortByMostActive;
    private JMenuItem tableClose;
    
    private PropertyChangeSupport propertySupport;
    private QuoteCache cache;
    private Object[] symbols;

    private Model model;

    // Frame Icon
    private String frameIcon = "org/mov/images/TableIcon.gif";

    // Current equation we are filtering by
    private String filterEquationString;
    
    public class EquationSlot {
	String columnName;
	String equation;

	public Object clone() {
	    EquationSlot clone = new EquationSlot();
	    clone.columnName = new String(columnName);
	    clone.equation = new String(equation);

	    return clone;
	}
    }

    private EquationSlot[] equationSlots;

    private class Model extends AbstractTableModel {
	private String[] headers = {
	    "Symbol", "Volume", "Day Low", "Day High", "Day Open", 
	    "Day Close", "Change", "Activity"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    String.class, String.class, Change.class, Float.class};

	private TradingDate date = null;
	private QuoteCache cache;
	private Object[] symbols;
	private EquationSlot[] equationSlots;

	public Model(QuoteCache cache, Object[] symbols,
		     EquationSlot[] equationSlots) {
	    this.cache = cache;
	    this.symbols = symbols;
	    this.equationSlots = equationSlots;

	    // Pull first date from cache
	    Iterator iterator = cache.dateIterator(0);
	    if(iterator.hasNext())
		date = (TradingDate)iterator.next();
	}

	public void setSymbols(Object[] symbols) {
	    this.symbols = symbols;
	}

	public void setEquationSlots(EquationSlot[] equationSlots) {
	    this.equationSlots = equationSlots;
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
		return equationSlots[c - headers.length].columnName;
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
		      String filterEquationString) {
	
	this.filterEquationString = filterEquationString;
	this.cache = cache;

	propertySupport = new PropertyChangeSupport(this);

	// Set up equation slots
	equationSlots = new EquationSlot[EQUATION_COLUMNS];

	for(int i = 0; i < EQUATION_COLUMNS; i++) {
	    equationSlots[i] = new EquationSlot();
	    equationSlots[i].columnName = new String("Eqn. " + (i + 1));
	    equationSlots[i].equation = new String();
	}

	// Get list of symbols to display
	symbols = extractSymbolsUsingRule(filterEquationString, cache);

	model = new Model(this.cache, symbols, equationSlots);
	setModel(model, ACTIVITY_COLUMN, SORT_UP);
	addMenu();

	model.addTableModelListener(this);

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
   
    private Object[] extractSymbolsUsingRule(String filterEquation, QuoteCache cache) {

	Object[] symbols = cache.getSymbols();

	// If theres no filter string then use all symbols
	if(filterEquation == null || filterEquation.length() == 0) 
	    return symbols;

	// First parse the equation
	org.mov.parser.Expression expression = null;


	try {
	    Parser parser = new Parser();
	    expression = parser.parse(filterEquationString);
	}
	catch(ExpressionException e) {
	    // We should have already checked the string for errors before here
	    assert false;
	}

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

	JMenu tableMenu = MenuHelper.addMenu(menuBar, "Quote");

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
		showEquationSlots[i] = 
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

	applyEquations =
	    MenuHelper.addMenuItem(this, tableMenu,
				   "Apply Equations");

	applyFilter =
	    MenuHelper.addMenuItem(this, tableMenu,
				   "Apply Filter");

	sortByMostActive = 
	    MenuHelper.addMenuItem(this, tableMenu,
				   "Sort by Most Active");
	
	tableMenu.addSeparator();

	tableClose = MenuHelper.addMenuItem(this, tableMenu,
					    "Close");	
    }
    
    // Allow the user to show only stocks where the given equation is true
    private void applyFilter() {
	// Handle all action in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread thread = new Thread() {

		public void run() {

		    JDesktopPane desktop =
			org.mov.ui.DesktopManager.getDesktop();

		    String equationString = 
			ExpressionQuery.getExpression(desktop,
						      "Filter by Rule",
						      "By Rule",
						      filterEquationString);

		    if(equationString != null) {
			filterEquationString = equationString;

			// Get new list of symbols to display
			symbols = extractSymbolsUsingRule(filterEquationString, cache);

			// Update table
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

				    model.setSymbols(symbols);
				    model.fireTableDataChanged();
				}});
		    }
		}};
	
	thread.start();
    }

    // Allow the user to enter in equations which will be run on every displayed quote
    private void applyEquations() {
	// Handle all action in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread thread = new Thread() {

		public void run() {
		    
		    JDesktopPane desktop =
			org.mov.ui.DesktopManager.getDesktop();

		    final EquationsDialog dialog = 
			new EquationsDialog(desktop,
					    EQUATION_COLUMNS);

		    // Did the user modify the equation slots?
		    if(dialog.showDialog(equationSlots)) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

				    equationSlots = dialog.getEquationSlots();
				    
				    model.setEquationSlots(equationSlots);

				    // Make sure all columns with an equation
				    // are visible and all without are not. 
				    // Also update check box menus
				    for(int i = 0; i < EQUATION_COLUMNS; i++) {
					boolean containsEquation = false;
					
					if(equationSlots[i].equation.length() >
					   0)
					    containsEquation = true;
					
					showColumn(EQUATION_SLOT_COLUMN + i, 
						   containsEquation);
					
					showEquationSlots[i].setState(containsEquation);
				    }
				    
				}});
			
		    }
		}
	    };

	thread.start();
    }

    public void save() {

    }

    public String getTitle() {
	return "Quotes for " + cache.getStartDate().toString("dd/mm/yyyy");
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

	else if(e.getSource() == applyEquations) {
	    applyEquations();
	}

	else if(e.getSource() == applyFilter) {
	    applyFilter();
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
		    if(menuItem == showEquationSlots[i]) {
			column = EQUATION_SLOT_COLUMN + i;
		    }
		}
	    }

	    showColumn(column, state);
	}
    }
    
}
