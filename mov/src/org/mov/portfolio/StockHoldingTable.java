package org.mov.portfolio;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.table.*;
import org.mov.quote.*;

/**
 * Display stock holdings in a swing table for a ShareAccount. This table
 * will display a row for each stock held, givings its symbol, number of
 * shares held, current day close value, current market value and its
 * change in today's trading.
 * @see ShareAccount
 */
public class StockHoldingTable extends AbstractTable {

    class Model extends AbstractTableModel {

	private static final int SYMBOL_COLUMN = 0;
	private static final int SHARES_COLUMN = 1;
	private static final int DAY_CLOSE_COLUMN = 2;
	private static final int MARKET_VALUE_COLUMN = 3;
	private static final int CHANGE_COLUMN = 4;

	private String[] headers = {
	    "Symbol", "Shares", "Day Close", "Mkt Value", "Change"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    Change.class}; 

	private QuoteCache cache;
	private HashMap stockHoldings;
	private Object[] symbols;
	private TradingDate date;

	public Model(HashMap stockHoldings, QuoteCache cache) {
	    this.cache = cache;
	    this.stockHoldings = stockHoldings;
	    symbols = stockHoldings.keySet().toArray();

	    // Pull first date from cache
	    Iterator iterator = cache.dateIterator(0);
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
	    
	    StockHolding stockHolding = 
		(StockHolding)stockHoldings.get(symbol);

	    // Shouldnt happen
	    if(stockHolding == null) 
		return "";

	    symbol = symbol.toLowerCase();

	    try {
		switch(column) {
		case(SYMBOL_COLUMN):
		    return symbol.toUpperCase();
		    
		case(SHARES_COLUMN):
		    return new Integer(stockHolding.getShares());
		    
		case(DAY_CLOSE_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_CLOSE, date));
		    
		case(MARKET_VALUE_COLUMN):
		    return Converter.quoteToString
			(cache.getQuote(symbol, Quote.DAY_CLOSE, date) *
			 stockHolding.getShares());
		    
		case(CHANGE_COLUMN):
		    return 
			Converter.changeToChange
			(cache.getQuote(symbol, Quote.DAY_OPEN, date),
			 cache.getQuote(symbol, Quote.DAY_CLOSE, date));
		}
	    }
	    catch(EvaluationException e) {
		System.out.println("exceptioN!!!");

		// should not happen
	    }

	    return "";

	}
    }

    /**
     * Create a new stock holding table.
     *
     * @param	stockHoldings	stock holdings for ShareAccount
     * @param	cache		quote cache
     */
    public StockHoldingTable(HashMap stockHoldings, QuoteCache cache) {
	setModel(new Model(stockHoldings, cache));
    }
}
