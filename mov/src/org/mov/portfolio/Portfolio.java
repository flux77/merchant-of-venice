package org.mov.portfolio;

import org.mov.util.*;
import org.mov.quote.*;

import java.util.*;

/** 
 * Representation of a portfolio. A portfolio object contains several
 * accounts, accounts can be either {@link CashAccount} or
 * {@link ShareAccount}. 
 */
public class Portfolio {

    // Name of portfolio
    private String name;

    // List of accounts
    Vector accounts = new Vector();

    // Transaction history
    Vector transactions = new Vector();

    /**
     * Create a new empty portfolio.
     *
     * @param	name	The name of the portfolio
     */
    public Portfolio(String name) {
	this.name = name;
    }

    /**
     * Return the portfolio name.
     *
     * @return	the name of the portfolio
     */
    public String getName() {
	return name;
    }

    /**
     * Add an account to the portfolio.
     *
     * @param	account	the new account to add
     */
    public void addAccount(Account account) {
	accounts.add(account);
    }

    /**
     * Record multiple transactions on the portfolio. 
     *
     * @param	transactions	a vector of transactions
     * @see	Transaction
     */
    public void addTransactions(Vector transactions) {

	// Sort transactions by date
	ArrayList list = new ArrayList((Collection)transactions);
	Collections.sort(list);

	// Add them in one by one
	Iterator iterator = list.iterator();

	while(iterator.hasNext()) {
	    Transaction transaction = (Transaction)iterator.next();

	    addTransaction(transaction);
	}
    }

    /**
     * Record a single transaction on the portfolio. 
     *
     * @param	transactions	a new transaction
     */
    public void addTransaction(Transaction transaction) {

	// Record history of transactions
	transactions.add(transaction);

	// Now update accounts
	Iterator iterator = accounts.iterator();

	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();

	    // Is this account involved in the transaction? If it
	    // it is we'll need to update it
	    if(account == transaction.getCashAccount() ||
	       account == transaction.getShareAccount()) {
		account.transaction(transaction);
	    }
	}
    }

    /**
     * Return all the accounts in the portfolio
     *
     * @return	accounts
     */
    public Vector getAccounts() {
	return accounts;
    }

    /**
     * Return if this portfolio has the following account type.
     *
     * @param	type	account type, e.g. {@link Account#CASH_ACCOUNT}
     * @return	<code>1</code> if the portfolio has the account type;
     *		<code>0</code> otherwise
     */
    public boolean hasAccount(int type) {
	Iterator iterator = accounts.iterator();

	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();
	    if(account.getType() == type)
		return true;
	}

	return false;
    }

    /**
     * Find and return the account with the given name in the
     * portfolio.
     *
     * @param	the name of the account to search for
     * @return	the account with the same name as given or <code>null</code>
     *		if it could not be found
     */
    public Account findAccountByName(String name) {
	Iterator iterator = accounts.iterator();

	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();

	    if(account.getName().equals(name)) 
		return account;
	}

	return null;
    }

    /**
     * Return the transaction history.
     *
     * @param	transaction history
     * @see	Transaction
     */
    public Vector getTransactions() {
	return transactions;
    }

    /**
     * Get the value of the portfolio on the given day. Currently
     * this function should only be called for dates after the last
     * transaction. When it calculates the value it will assume all
     * transactions have taken place.
     *
     * @param	cache	the quote cache
     * @param	date	the date to calculate the value
     * @return	the value
     */
    public float getValue(QuoteCache cache, TradingDate date) {
	Iterator iterator = accounts.iterator();
	float value = 0.0F;
	
	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();

	    value += account.getValue(cache, date);
	}
	
	return value;
    }

}
