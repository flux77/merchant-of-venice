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

import org.mov.quote.MissingQuoteException;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteCache;
import org.mov.quote.WeekendDateException;
import org.mov.util.TradingDate;
import org.mov.util.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Representation of a portfolio. A portfolio object contains several
 * accounts, accounts can be either {@link CashAccount} or
 * {@link ShareAccount}.
 */
public class Portfolio implements Cloneable {

    // Name of portfolio
    private String name;

    // List of accounts
    private List accounts = new ArrayList();

    // Transaction history
    private List transactions = new ArrayList();

    // If the portfolio is transient it is just used for displaying
    // information to the user and shouldn't be saved
    private boolean isTransient;

    // We keep track of the amount of cash deposited in the Portfolio
    // so we can calculate its profit/loss.
    private Money deposits;

    /**
     * Create a new empty portfolio.
     *
     * @param	name	The name of the portfolio
     */
    public Portfolio(String name) {
        this(name, false);
    }

    /**
     * Create a new empty portfolio.
     *
     * @param	name	The name of the portfolio
     * @param   isTransient Set to <code>true</code> if the portfolio displays
     *                      working information and shouldn't be saved.
     */
    public Portfolio(String name, boolean isTransient) {
	this.name = name;
        this.isTransient = isTransient;
        this.deposits = Money.ZERO;
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
     * Set the portfolio name.
     *
     * @param name the new portfolio name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return whether the portfolio is transient or permanent.
     *
     * @return <code>true</code> if the portfolio is transient and shouldn't
     *         be saved
     */
    public boolean isTransient() {
        return isTransient;
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
     * @param	transactions	a list of transactions
     * @see	Transaction
     */
    public void addTransactions(List transactions) {

	// Sort transactions by date
	List list = new ArrayList(transactions);
	Collections.sort(list);

	// Add them in one by one
	for(Iterator iterator = list.iterator(); iterator.hasNext();) {
	    Transaction transaction = (Transaction)iterator.next();

	    addTransaction(transaction);
	}
    }

    /**
     * Record a single transaction on the portfolio.
     *
     * @param	transaction	a new transaction
     */
    public void addTransaction(Transaction transaction) {

	// If the transaction is older than an existing transaction then remove all
	// transactions. Put the new transaction with them, sort them all and then
	// add all of the transactions. I.e. we must add the transactions in chronological
	// order to prevent things like selling stock before we have bought it.

	if(countTransactions() > 0 &&
	   ((Transaction)transactions.get(transactions.size() - 1)).compareTo(transaction) > 0) {

	    List allTransactions = new ArrayList(transactions);
	    allTransactions.add(transaction);

	    removeAllTransactions();
	    addTransactions(allTransactions);
	}

	// Otherwise we can just append
	else {
	    // Record history of transactions
	    transactions.add(transaction);

	    // Now update accounts
	    for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
		Account account = (Account)iterator.next();
		
		// Is this account involved in the transaction? If it
		// it is we'll need to update it
		if(account == transaction.getCashAccount() ||
		   account == transaction.getCashAccount2() ||
		   account == transaction.getShareAccount()) {
		    account.transaction(transaction);
		}
            }

            // Update our deposit figure for profit/loss calculation
            if(transaction.getType() == Transaction.WITHDRAWAL)
                deposits = deposits.subtract(transaction.getAmount());
            else if(transaction.getType() == Transaction.DEPOSIT)
                deposits = deposits.add(transaction.getAmount());
	}
    }

    public Object clone() {

	// First clone portfolio object
	Portfolio clonedPortfolio = new Portfolio(getName());

	// Now clone accounts and insert the cloned accounts into
	// the cloned portfolio
        for(Iterator accountIterator = accounts.iterator();
            accountIterator.hasNext();) {

	    Account account = (Account)accountIterator.next();
	    Object clonedAccount;

	    if(account.getType() == Account.SHARE_ACCOUNT) {
		clonedAccount = ((ShareAccount)account).clone();
	    }
	    else {
		assert account.getType() == Account.CASH_ACCOUNT;
		clonedAccount = ((CashAccount)account).clone();
	    }

	    clonedPortfolio.addAccount((Account)clonedAccount);
	}

	// Now clone the transactions
	for (Iterator transactionIterator = transactions.iterator();
             transactionIterator.hasNext();) {

	    Transaction transaction =
		(Transaction)transactionIterator.next();
	    Transaction clonedTransaction = (Transaction)transaction.clone();

	    // Adjust share/cash account so it referes to the cloned
	    // portfolio accounts - not the old ones.
	    if(clonedTransaction.getShareAccount() != null) {

		String accountName =
		    clonedTransaction.getShareAccount().getName();
		ShareAccount shareAccount = (ShareAccount)
		    clonedPortfolio.findAccountByName(accountName);

		clonedTransaction.setShareAccount(shareAccount);
	    }
	    if(clonedTransaction.getCashAccount() != null) {

		String accountName =
		    clonedTransaction.getCashAccount().getName();
		CashAccount cashAccount = (CashAccount)
		    clonedPortfolio.findAccountByName(accountName);

		clonedTransaction.setCashAccount(cashAccount);
	    }
	    if(clonedTransaction.getCashAccount2() != null) {

		String accountName =
		    clonedTransaction.getCashAccount2().getName();
		CashAccount cashAccount2 = (CashAccount)
		    clonedPortfolio.findAccountByName(accountName);

		clonedTransaction.setCashAccount2(cashAccount2);
	    }
	
	    clonedPortfolio.addTransaction(clonedTransaction);
	}

	return clonedPortfolio;
    }

    /**
     * Return all the accounts in the portfolio
     *
     * @return	accounts
     */
    public List getAccounts() {
	return accounts;
    }

    /**
     * Return the number of accounts of the given type the portfolio has
     *
     * @param	type	account type, e.g. {@link Account#CASH_ACCOUNT}
     * @return	number of accounts of the given type
     */
    public int countAccounts(int type) {
	int count = 0;

        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
	    Account account = (Account)iterator.next();
	    if(account.getType() == type)
		count++;
	}

	return count;
    }

    /**
     * Find and return the account with the given name in the
     * portfolio.
     *
     * @param	name the name of the account to search for
     * @return	the account with the same name as given or <code>null</code>
     *		if it could not be found
     */
    public Account findAccountByName(String name) {
        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
	    Account account = (Account)iterator.next();

	    if(account.getName().equals(name))
		return account;
	}

	return null;
    }

    /**
     * Return the start date of this portfolio. The start date is
     * defined as the date of the first transaction.
     *
     * @return	date of the first transaction
     */
    public TradingDate getStartDate() {
	if(transactions.size() > 0) {
	    Transaction transaction = (Transaction)transactions.get(0);

	    return transaction.getDate();
	}
	else {
	    return null;
	}
    }

    /**
     * Return the date of the last transaction in this portfolio.
     *
     * @return	date of the last transaction
     */
    public TradingDate getLastDate() {
	if(transactions.size() > 0) {
	    Transaction transaction = (Transaction)transactions.get(transactions.size() - 1);

	    return transaction.getDate();
	}
	else {
	    return null;
	}
    }

    /**
     * Returns all the symbols traded in this portfolio.
     *
     * @return	symbols traded
     */
    public List getSymbolsTraded() {
	Set symbolsTraded = new HashSet();

	for (Iterator iterator = transactions.iterator(); iterator.hasNext();) {
	    Transaction transaction = (Transaction)iterator.next();
	    if(transaction.getType() == Transaction.ACCUMULATE)
		symbolsTraded.add(transaction.getSymbol());
	}

	return new ArrayList(symbolsTraded);
    }

    /**
     * Count the number of transactions.
     *
     * @return	the number of transactions
     */
    public int countTransactions() {
	return transactions.size();
    }

    /**
     * Count the number of transactions of the given type.
     *
     * @return	the number of transactions
     */
    public int countTransactions(int type) {
	int count = 0;

	for (Iterator iterator = transactions.iterator(); iterator.hasNext();) {
	    Transaction transaction = (Transaction)iterator.next();
	    if(transaction.getType() == type)
		count++;
	}
	
	return count;
    }

    /**
     * Return the transaction history.
     *
     * @return	transaction history
     * @see	Transaction
     */
    public List getTransactions() {
	return transactions;
    }

    /**
     * Remove all transactions from portfolio.
     */
    public void removeAllTransactions() {
	transactions.clear();
        deposits = Money.ZERO;

	// A portfolio with no transactions has no value or stock so
	// remove them from accounts
        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
	    Account account = (Account)iterator.next();	
	    account.removeAllTransactions();
	}
    }

    /**
     * Get the value of the portfolio on the given day. Currently
     * this function should only be called for dates after the last
     * transaction. When it calculates the value it will assume all
     * transactions have taken place.
     *
     * @param	quoteBundle	the quote bundle
     * @param	date	the date to calculate the value
     * @return	the value
     */
    public Money getValue(QuoteBundle quoteBundle, TradingDate date)
	throws MissingQuoteException {

        try {
            return getValue(quoteBundle, QuoteCache.getInstance().dateToOffset(date));
        }
        catch(WeekendDateException e) {
            throw MissingQuoteException.getInstance();
        }
    }

    /**
     * Get the value of the portfolio on the given day. Currently
     * this function should only be called for dates after the last
     * transaction. When it calculates the value it will assume all
     * transactions have taken place.
     *
     * @param	quoteBundle	the quote bundle
     * @param	dateOffset fast date offset
     * @return	the value
     */
     public Money getValue(QuoteBundle quoteBundle, int dateOffset)
 	throws MissingQuoteException {

         Money value = Money.ZERO;

         for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
 	    Account account = (Account)iterator.next();

 	    value = value.add(account.getValue(quoteBundle, dateOffset));
 	}
	
 	return value;
     }

    public List getStocksHeld() {
	Set stocksHeld = new HashSet();

        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
	    Account account = (Account)iterator.next();

            if(account.getType() == Account.SHARE_ACCOUNT) {
                ShareAccount shareAccount = (ShareAccount)account;

                stocksHeld.addAll(shareAccount.getStockHoldings().keySet());
            }
        }

        return new ArrayList(stocksHeld);
    }

    /**
     * Get the cash value of the Portfolio on the latest day. See {@link #getValue()}.
     *
     * @return	the value
     */
    public Money getCashValue() {
        Money value = Money.ZERO;

        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
	    Account account = (Account)iterator.next();

            if(account.getType() == Account.CASH_ACCOUNT) {
                CashAccount cashAccount = (CashAccount)account;
                value = value.add(cashAccount.getValue());
            }
	}
	
	return value;
    }

    /**
     * Get the share value of the Portfolio on the current day. See {@link #getValue()}.
     *
     * @param	quoteBundle	the quote bundle
     * @param	date            the date
     * @return	the value
     */
    public Money getShareValue(QuoteBundle quoteBundle, TradingDate date)
	throws MissingQuoteException {
        Money value = Money.ZERO;

        for(Iterator iterator = accounts.iterator(); iterator.hasNext();) {
            Account account = (Account)iterator.next();

            if(account.getType() == Account.SHARE_ACCOUNT)
                value = value.add(account.getValue(quoteBundle, date));
        }

	return value;
    }

    /**
     * Get the return of the Portfolio on the current day.
     *
     * @param	quoteBundle	the quote bundle
     * @param	date            the date
     * @return	the value
     */
    public Money getReturnValue(QuoteBundle quoteBundle, TradingDate date)
	throws MissingQuoteException {

        // The profit loss is calculated as the value of the Portfolio minus
        // the amount of cash deposited in it.
        Money value = getValue(quoteBundle, date);
        value = value.subtract(deposits);
	return value;
    }

    public Iterator iterator() {
        return new PortfolioIterator(this);
    }

    public Portfolio getPortfolio(TradingDate date) {
        // If the date falls after the date of the last transaction
        // then the current portoflio object is correct.
        if(getLastDate() == null || date.compareTo(getLastDate()) >= 0)
            return this;

        // Otherwise we will need to rebuild the portfolio up to the
        // given date.
        else {
            Portfolio portfolio = (Portfolio)clone();
            List transactions = new ArrayList(portfolio.getTransactions());

            for(Iterator iterator = transactions.iterator(); iterator.hasNext();) {
                Transaction transaction = (Transaction)iterator.next();

                // Should we include this transaction?
                if(transaction.getDate().compareTo(date) <= 0)
                    portfolio.addTransaction(transaction);

                // Otherwise we've added all the transactions and can return.
                else
                    return portfolio;
            }

            // If there is no more transactions, the given date must be before the
            // last transaction...
            assert false;
            return this;
        }
    }

    private class PortfolioIterator implements Iterator {

        private Portfolio iteratorPortfolio;
        private ListIterator transactionIterator;
        private TradingDate currentDate;

        public PortfolioIterator(Portfolio referencePortfolio) {
            // Create a copy of the portfolio and extract the list of
            // transactions.
            iteratorPortfolio =
                (Portfolio)referencePortfolio.clone();

            // Extract the transactions and get the iterator pointing to the
            // first transaction. The transaction list will be in order.
            List transactions =
                new ArrayList(iteratorPortfolio.getTransactions());
            iteratorPortfolio.removeAllTransactions();
            transactionIterator = transactions.listIterator();

            // Work out the point we iterate from
            currentDate = referencePortfolio.getStartDate();
        }

        public boolean hasNext() {
            return true;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Object next() {
            while(transactionIterator.hasNext()) {
                Transaction transaction = (Transaction)transactionIterator.next();

                // Has this transaction happened on our given date?
                if(transaction.getDate().compareTo(currentDate) <= 0)
                    iteratorPortfolio.addTransaction(transaction);

                // If it's happened after, we've gone too far! Put it back
                else {
                    transactionIterator.previous();
                    break;
                }
            }

            currentDate = currentDate.next(1);

            return (Object)iteratorPortfolio;
        }
    }

}
