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

package org.mov.portfolio;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.ui.*;
import org.mov.util.*;
import org.mov.main.*;
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

    private static final int SYMBOL_COLUMN = 0;
    private static final int SHARES_COLUMN = 1;
    private static final int DAY_CLOSE_COLUMN = 2;
    private static final int MARKET_VALUE_COLUMN = 3;
    private static final int CHANGE_COLUMN = 4;

    class Model extends AbstractTableModel {

	private String[] headers = {
	    "Symbol", "Shares", "Day Close", "Mkt Value", "Change"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    Change.class}; 

	private QuoteBundle quoteBundle;
	private HashMap stockHoldings;
	private Object[] symbols;
	private TradingDate date;

	public Model(HashMap stockHoldings, QuoteBundle quoteBundle) {
	    this.quoteBundle = quoteBundle;
	    this.stockHoldings = stockHoldings;
	    symbols = stockHoldings.keySet().toArray();

	    // Pull first date from quoteBundle
	    date = quoteBundle.getFirstDate();
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
			(quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, date));
		    
		case(MARKET_VALUE_COLUMN):
		    return Converter.quoteToString
			(quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, date) *
			 stockHolding.getShares());
		    
		case(CHANGE_COLUMN):
		    return 
			Converter.changeToChange
			(quoteBundle.getQuote(symbol, Quote.DAY_OPEN, date),
			 quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, date));
		}
	    }
	    catch(MissingQuoteException e) {
		assert false;
	    }

	    return "";

	}
    }

    /**
     * Create a new stock holding table.
     *
     * @param	stockHoldings	stock holdings for ShareAccount
     * @param	quoteBundle	the quote bundle
     */
    public StockHoldingTable(HashMap stockHoldings, QuoteBundle quoteBundle) {
	setModel(new Model(stockHoldings, quoteBundle));

	// If the user double clicks on a row then graph the stock
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
}
