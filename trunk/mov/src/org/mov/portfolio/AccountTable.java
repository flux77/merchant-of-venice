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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mov.ui.AbstractTable;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.util.Money;
import org.mov.util.TradingDate;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.QuoteBundle;

/**
 * Display an account summary in a swing table for a portfolio. The table
 * will display a row for each account, giving its name and its current
 * value.
 * @see Portfolio
 */
public class AccountTable extends AbstractTable {

    private static final int ACCOUNT_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    class Model extends AbstractTableModel {

	private QuoteBundle quoteBundle;
	private Portfolio portfolio;
	private Object[] accounts;
	private int dateOffset;

	public Model(List columns, Portfolio portfolio, QuoteBundle quoteBundle) {
            super(columns);

	    this.quoteBundle = quoteBundle;
	    this.portfolio = portfolio;

	    accounts = portfolio.getAccounts().toArray();

            // Use the latest date in the quote bundle
	    dateOffset = quoteBundle.getLastDateOffset();
	}
	
	public int getRowCount() {
	    // One row per account plus a total row
	    return accounts.length + 1;
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
			return account.getValue(quoteBundle, dateOffset);
		    }
		    catch(MissingQuoteException e) {
			return Money.ZERO;
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
		    List accounts = portfolio.getAccounts();
		    Iterator iterator = accounts.iterator();
		    Money value = Money.ZERO;
		    
		    while(iterator.hasNext()) {
			Account account = (Account)iterator.next();

			try {
			    value = value.add(account.getValue(quoteBundle, dateOffset));
			}
			catch(MissingQuoteException e) {
			    // nothing to do 
			}
		    }

		    return value;
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
     * @param	quoteBundle	the quote bundle
     */
    public AccountTable(Portfolio portfolio, QuoteBundle quoteBundle) {
        List columns = new ArrayList();
        columns.add(new Column(ACCOUNT_COLUMN, "Account", "Account", String.class, Column.VISIBLE));
        columns.add(new Column(VALUE_COLUMN, "Value", "Value", Money.class, Column.VISIBLE));

	setModel(new Model(columns, portfolio, quoteBundle));
    }
}
