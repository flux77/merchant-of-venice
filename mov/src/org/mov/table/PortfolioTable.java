/*

// Needs rewriting as Portfolio class has been rewritten

package org.mov.table;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class PortfolioTable extends AbstractTable
    implements Module {

    public static final int SYMBOL_COLUMN = 0;
    public static final int SHARES_COLUMN = 1;
    public static final int DAY_OPEN_COLUMN = 2;
    public static final int DAY_CLOSE_COLUMN = 3;
    public static final int VALUE = 4;
    public static final int CHANGE_COLUMN = 5;

    public static JPopupMenu popup;
    private PropertyChangeSupport propertySupport;
    private Portfolio portfolio;
    private Vector table;
    
    class Model extends AbstractTableModel {
	private String[] headers = {
	    "Symbol", "Shares", "Day Open", "Day Close", "Value", "Change"};

	private Class[] columnClasses = {
	    String.class, Integer.class, String.class, String.class,
	    String.class, Change.class};

	private Portfolio portfolio;
	private Object[] symbols;
	private Vector table;

	public Model(Portfolio portfolio, Vector table) {
	    this.portfolio = portfolio;
	    this.table = table;
	    symbols = portfolio.keySet().toArray();
	}

	public int getRowCount() {
	    return portfolio.size();
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

	    StockHolding stockHolding = 
		portfolio.get((String)symbols[row]);

	    Stock stock = find(stockHolding.getSymbol());

	    switch(column) {
	    case(SYMBOL_COLUMN):
		return stockHolding.getSymbol();

	    case(SHARES_COLUMN):
		return new Integer(stockHolding.getShares());

	    case(VALUE):
		return Converter.priceToString(stock.getDayClose() * 
					       stockHolding.getShares());

	    case(DAY_OPEN_COLUMN):
		return Converter.quoteToString(stock.getDayOpen());

	    case(DAY_CLOSE_COLUMN):
		return Converter.quoteToString(stock.getDayClose());

	    case(CHANGE_COLUMN):
		return Converter.changeToChange(stock.getDayOpen(),
						stock.getDayClose());
	    default:
		return "";
	    }
	}
    }

    public PortfolioTable(Portfolio portfolio, Vector table) {
	propertySupport = new PropertyChangeSupport(this);
	this.portfolio = portfolio;
	this.table = table;

	setModel(new Model(portfolio, table));
    }

    public String getTitle() {
	return portfolio.getName();
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
