package org.mov.portfolio;

import java.util.*;

public class Portfolio {

    private String name;

    Vector accounts = new Vector();
    Vector transactions = new Vector();

    public Portfolio(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void addAccount(Account account) {
	accounts.add(account);
    }

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

    public Vector getAccounts() {
	return accounts;
    }

    public Account findAccountByName(String name) {
	Iterator iterator = accounts.iterator();

	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();

	    if(account.getName().equals(name)) 
		return account;
	}

	return null;
    }

    public Vector getTransactions() {
	return transactions;
    }

}
