package org.mov.portfolio;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.util.*;
import org.mov.table.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * Display an account summary in a swing table for a portfolio. The table
 * will display a row for each account, giving its name and its current
 * value.
 * @see Portfolio
 */
public class AccountTable extends AbstractTable {

    class Model extends AbstractTableModel {

	private static final int ACCOUNT_COLUMN = 0;
	private static final int VALUE_COLUMN = 1;

	private String[] headers = {
	    "Account", "Value"};

	private Class[] columnClasses = {
	    String.class, String.class};

	private QuoteCache cache;
	private Portfolio portfolio;
	private Object[] accounts;
	private TradingDate date;

	public Model(Portfolio portfolio, QuoteCache cache) {
	    this.cache = cache;
	    this.portfolio = portfolio;

	    accounts = portfolio.getAccounts().toArray();

	    // Pull first date from cache
	    Iterator iterator = cache.dateIterator(0);
	    if(iterator.hasNext())
		date = (TradingDate)iterator.next();

	    // Make sure we dont get an exception when trying to
	    // work out how much the portfolio is worth on this day
	    boolean validDate = false;

	    while(!validDate) {
		try {
		    portfolio.getValue(cache, date);
		    validDate = true;
		}
		catch(EvaluationException e) {
		    // If we do its cause its a public holiday and we
		    // can't get quotes for our stocks. So go back a day
		    date = date.previous(1);
		}
	    }
	}
	
	public int getRowCount() {
	    // One row per account plus a total row
	    return accounts.length + 1;
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
	    
	    // Account
	    if(row != (getRowCount() - 1)) {
		
		Account account = (Account)accounts[row];
		
		switch(column) {
		case(ACCOUNT_COLUMN):
		    return account.getName();
		    
		case(VALUE_COLUMN):
		    try {
			return 
			    Converter.quoteToString(account.getValue(cache, date));
		    }
		    catch(EvaluationException e) {
			// should not happen
		    }
		}
	    }

	    // Total row
	    else {
		switch(column) {
		case(ACCOUNT_COLUMN):
		    return "Total";
		    
		case(VALUE_COLUMN):
		    // Sum values of all accounts
		    Vector accounts = portfolio.getAccounts();
		    Iterator iterator = accounts.iterator();
		    float value = 0;
		    
		    while(iterator.hasNext()) {
			Account account = (Account)iterator.next();

			try {
			    value += account.getValue(cache, date);
			}
			catch(EvaluationException e) {
			    // should not happen
			}
		    }

		    return 
			Converter.quoteToString(value);
		}
	    }

	    return "";
	}
    }

    /**
     * Create a new account table.
     *
     * @param	portfolio	the portfolio to create an account summary
     *				table for
     * @param	cache		quote cache
     */
    public AccountTable(Portfolio portfolio, QuoteCache cache) {
	setModel(new Model(portfolio, cache));
    }
}
