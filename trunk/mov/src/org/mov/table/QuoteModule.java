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

public class QuoteModule extends AbstractAnalyserTable
    implements AnalyserModule {

    private static final int SYMBOL_COLUMN = 0;
    private static final int VOLUME_COLUMN = 1;
    private static final int DAY_LOW_COLUMN = 2;
    private static final int DAY_HIGH_COLUMN = 3;
    private static final int DAY_OPEN_COLUMN = 4;
    private static final int DAY_CLOSE_COLUMN = 5;
    private static final int CHANGE_COLUMN = 6;

    private PropertyChangeSupport propertySupport;
    private QuoteCache cache;
    private Object[] symbols;
    private org.mov.parser.Expression expression = null;
    
    class Model extends AbstractTableModel {
	private String[] headers = {
	    "Symbol", "Volume", "Day Low", "Day High", "Day Open", 
	    "Day Close", "Change"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    String.class, String.class, Change.class};

	private TradingDate date = null;
	private QuoteCache cache;
	private Object[] symbols;
    
	public Model(QuoteCache cache, Object[] symbols) {
	    this.cache = cache;
	    this.symbols = symbols;
	    
	    // Pull first date from cache
	    Iterator iterator = cache.dateIterator();
	    if(iterator.hasNext())
		date = (TradingDate)iterator.next();
	}
	
	public int getRowCount() {
	    return symbols.length;
	}

	public int getColumnCount() {
	    return headers.length;
	}
	
	public String getColumnName(int c) {
	    return headers[c];
	}

	public Class getColumnClass(int c) {
	    return columnClasses[c];
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
			((int)cache.getQuote(symbol, Token.DAY_VOLUME_TOKEN, 
					     date));
		    
		case(DAY_LOW_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Token.DAY_LOW_TOKEN, date));
		    
		case(DAY_HIGH_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Token.DAY_HIGH_TOKEN, date));
		    
		case(DAY_OPEN_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Token.DAY_OPEN_TOKEN, date));
		    
		case(DAY_CLOSE_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Token.DAY_CLOSE_TOKEN, date));
		    
		case(CHANGE_COLUMN):
		    return 
			Converter.changeToChange
			(cache.getQuote(symbol, Token.DAY_OPEN_TOKEN, date),
			 cache.getQuote(symbol, Token.DAY_CLOSE_TOKEN, date));
		}
	    }
	    catch(EvaluationException e) {
		// should not happen
	    }

	    return "";
	}
    }

    public QuoteModule(JDesktopPane desktop, QuoteCache cache) {
	newTable(desktop, cache, null);
    }

    public QuoteModule(JDesktopPane desktop, QuoteCache cache, 
		      org.mov.parser.Expression expression) {
	newTable(desktop, cache, expression);
    }

    private void newTable(JDesktopPane desktop, QuoteCache cache, 
			  org.mov.parser.Expression expression) {

	this.expression = expression;
	this.cache = cache;

	// If theres a rule, create restricted list of symbols to display
	if(expression != null)
	    symbols = extractSymbolsUsingRule(desktop, cache);
	else
	    symbols = cache.getSymbols();

	propertySupport = new PropertyChangeSupport(this);
	setModel(new Model(this.cache, symbols));

	// The analyser table allows several new properties to customise
	// the column entries
	setRedColumn(DAY_LOW_COLUMN);
	setGreenColumn(DAY_HIGH_COLUMN);
    }

    public String getTitle() {
	// Get date in cache (should only be one so get the first one)
	String dateString = "???";
	Iterator iterator = cache.dateIterator();       
	if(iterator.hasNext()) {
	    TradingDate date = (TradingDate)iterator.next();
	    dateString = date.toLongString();
	}

	String expressionString = "";
	if(expression != null) 
	    expressionString = " [" + expression + "]";

	return "Stock Quotes for " + 
	    dateString +
	    expressionString + " (" +
	    symbols.length +
	    ")";	    
    }

    public void addModuleChangeListener (PropertyChangeListener listener) {
	propertySupport.addPropertyChangeListener (listener);
    }

    public void removeModuleChangeListener (PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener (listener);
    }

    public JComponent getComponent() {
	return this;
    }

    public JMenuBar getJMenuBar() {
	return null;
    }

    public ImageIcon getFrameIcon() {
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/TableIcon.gif"));
    }
    public boolean encloseInScrollPane() {
	return true;
    }

    private Object[] extractSymbolsUsingRule(JDesktopPane desktop, 
					     QuoteCache cache) {

	// Add symbols to vector when expression proves true
	try {
	    Vector extractedSymbols = new Vector();
	    Object[] symbols = cache.getSymbols();
	    String symbol;

	    // Traverse array
	    for(int i = 0; i < symbols.length; i++) {
		symbol = (String)symbols[i];

		// True for this stock? Then add it to the table
		if(expression.evaluate(cache, symbol, 0) >=
		   LogicExpression.TRUE_LEVEL)
		    extractedSymbols.add(symbol);
	    }
	    return extractedSymbols.toArray();
	}
	catch(EvaluationException e) {

	    // Tell user expression didnt evaluate properly
	    JOptionPane.
		showInternalMessageDialog(desktop, 
					  e.getReason() + ": " +
					  expression.toString(),
					  "Error evaluating expression",
					  JOptionPane.ERROR_MESSAGE);
	    
	    // delete erroneous expression
	    expression = null;

	    // Return all cache's symbols
	    return cache.getSymbols();
	}
    }

    public void save() {

    }
}



