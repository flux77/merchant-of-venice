// Needs rewrite since trading history table is obsolete

/*

package org.mov.table;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class TradingHistoryTable extends AbstractAnalyserTable
    implements AnalyserModule {

    public static final int DATE_COLUMN = 0;
    public static final int TRADE_COLUMN = 1;
    public static final int SYMBOL_COLUMN = 2;
    public static final int SHARES_COLUMN = 3;
    public static final int PRICE_COLUMN = 4;
    public static final int VALUE_COLUMN = 5;
    
    public static JPopupMenu popup;
    private PropertyChangeSupport propertySupport;
    private TradingHistory tradingHistory;
    private Vector table;
    
    class Model extends AbstractTableModel {
	private String[] headers = {
	    "Date", "Trade", "Symbol", "Shares", "Price", "Then Value"};

	private Class[] columnClasses = {
	    Date.class, String.class, String.class, Integer.class,
	    String.class, String.class};

	private TradingHistory tradingHistory;
	private Vector table;

	public Model(TradingHistory tradingHistory, Vector table) {
	    this.tradingHistory = tradingHistory;
	    this.table = table;
	}

	public int getRowCount() {
	    return tradingHistory.size();
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

	private Stock find(String symbol) {
	    Iterator iterator = table.iterator();
	    Stock stock;

	    while(iterator.hasNext()) {

		stock = (Stock)iterator.next();
		
		if(symbol.compareTo(stock.getSymbol()) == 0)
		    return stock;
	    }
	    
	    return null;
	}
	
	public Object getValueAt(int row, int column) {

	    if(row >= getRowCount()) 
		return "";

	    Trade trade = tradingHistory.get(row);
	    Stock stock = find(trade.getSymbol());
	    Double value;

	    switch(column) {
	    case(DATE_COLUMN):
		return trade.getDate();

	    case(TRADE_COLUMN):
		int tradeType = trade.getTrade();

		if(tradeType == Trade.SELL)
		    return "Sell";
		else
		    return "Buy";

	    case(SYMBOL_COLUMN):
		return trade.getSymbol();

	    case(SHARES_COLUMN):
		return new Integer(trade.getShares());

	    case(PRICE_COLUMN):
		return Converter.quoteToString(trade.getPrice());

	    case(VALUE_COLUMN):
		return Converter.priceToString(trade.getPrice() * 
					       trade.getShares());
	    default:
		return "";
	    }
	}
    }

    public TradingHistoryTable(TradingHistory tradingHistory, Vector table) {
	propertySupport = new PropertyChangeSupport(this);
	this.tradingHistory = tradingHistory;
	this.table = table;

	setModel(new Model(tradingHistory, table));
    }

    public String getTitle() {
	return tradingHistory.getName() + " Trading History";
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener (listener);
    }

    public JComponent getComponent() {
	return this;
    }

    public JMenuBar getJMenuBar() {
	return null;
    }

    public boolean encloseInScrollPane() {
	return true;
    }
}


*/
