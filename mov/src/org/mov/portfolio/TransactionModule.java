package org.mov.portfolio;

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
import org.mov.table.*;

/** 
 * Venice module for displaying a portfolio's transaction history to
 * the user.
 */
public class TransactionModule extends AbstractTable
    implements Module {
    
    class Model extends AbstractTableModel {

	private static final int DATE_COLUMN = 0;
	private static final int TRANSACTION_COLUMN = 1;
	private static final int CREDIT_COLUMN = 2;
	private static final int DEBIT_COLUMN = 3;

	private String[] headers = {
	    "Date", "Transaction", "Credit", "Debit"
	};

	private Class[] columnClasses = {
	    TradingDate.class, String.class, Float.class, Float.class
	};

	private Vector transactions;

	public Model(Vector transactions) {
	    this.transactions = transactions;
	}

	public int getRowCount() {
	    return transactions.size();
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
	    
	    Transaction transaction = (Transaction)transactions.elementAt(row);
	    int type = transaction.getType();
	    
	    switch(column) {
		
	    case(DATE_COLUMN):
		return transaction.getDate();

	    case(TRANSACTION_COLUMN):
		return getTransactionString(transaction);
		
	    case(CREDIT_COLUMN):
		// Portfolio gains money		
		Float credit = new Float(0.0F);
		
		switch(type) {
		case(Transaction.DEPOSIT):
		case(Transaction.DIVIDEND):
		case(Transaction.INTEREST):
		    credit = new Float(transaction.getAmount());
		    break;
		}
		return credit;

	    case(DEBIT_COLUMN):
		// Portfolio loses money
		Float debit = new Float(0.0F);
		
		switch(type) {
		case(Transaction.WITHDRAWAL):
		case(Transaction.FEE):
		    debit = new Float(transaction.getAmount());
		    break;
		case(Transaction.ACCUMULATE):
		case(Transaction.REDUCE):
		    debit = new Float(transaction.getTradeCost());
		    break;
		}

		return debit;
	    }

	    return "";
	}
    }

    // Get the string to display in the transaction column
    private String getTransactionString(Transaction transaction) {
	int type = transaction.getType();
	
	String transactionString = Transaction.typeToString(type);
	
	// Add additional information here 
	switch(type) {
	case(Transaction.ACCUMULATE):
	case(Transaction.REDUCE):
	    String pricePerShare =
		Converter.priceToString(transaction.getAmount() /
					transaction.getShares());
	    
	    transactionString = 
		transactionString.concat(" " +
					 transaction.getShares() +
					 " " + 
					 transaction.getSymbol() +
					 " @ " +
					 pricePerShare);
	    break;
	case(Transaction.DIVIDEND):
	    transactionString = 
		transactionString.concat(" " +
					 transaction.getSymbol());
	    break;
	    
	case(Transaction.DIVIDEND_DRP):
	    transactionString = 
		transactionString.concat(" " +
					 transaction.getShares() +
					 " " +
					 transaction.getSymbol());
	    break;
	}
	
	return transactionString;
    }

    private PropertyChangeSupport propertySupport;
    private Portfolio portfolio;

    /**
     * Create a new transaction module from the given portfolio.
     *
     * @param	portfolio	portfolio to display transaction history
     */
    public TransactionModule(Portfolio portfolio) {
	this.portfolio = portfolio;

	propertySupport = new PropertyChangeSupport(this);

	setModel(new Model(portfolio.getTransactions()));
    }

    public void save() {
	// nothing to save
    }

    public String getTitle() {
	return portfolio.getName() + " Transactions";
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
	return null;
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return true;
    }
}
