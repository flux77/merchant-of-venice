package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/** Representation of a cash account in a portfolio.
 */
public class CashAccount implements Account, Cloneable {

    // Amount of cash available
    private float capital;

    private String name;

    /**
     * Create a new cash account.
     *
     * @param	name	the name of the new cash account
     */
    public CashAccount(String name) {
	this.name = name;
	this.capital = 0.0F;
    }

    public void transaction(Transaction transaction) {

	int type = transaction.getType();

	// Update value of account
	if(type == Transaction.WITHDRAWAL ||
	   type == Transaction.FEE) {
	    capital -= transaction.getAmount();
	}
	else if(type == Transaction.DEPOSIT ||
		type == Transaction.INTEREST ||
		type == Transaction.DIVIDEND) {
	    capital += transaction.getAmount();
	}
	else if(type == Transaction.ACCUMULATE) {
	    capital -= (transaction.getAmount() + transaction.getTradeCost());
	}
	else if(type == Transaction.REDUCE) {
	    capital += (transaction.getAmount() - transaction.getTradeCost());
	}
    }

    public Object clone() {
	CashAccount clonedCashAccount = new CashAccount(getName());

	return clonedCashAccount;
    }

    public String getName() {
	return name;
    }

    public float getValue(QuoteCache cache, TradingDate date) {
	return capital;
    }

    /**
     * Return the value of this account. Since the value does not
     * depend on any stock price, the cache and date can be
     * omitted.
     */
    public float getValue() {
	return capital;
    }

    public void removeAllTransactions() {
	capital = 0.0F;
    }

    public int getType() {
	return Account.CASH_ACCOUNT;
    }
}
