package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class CashAccount implements Account {

    // Types of transactions to a cash account

    // transfer to another account (e.g. to buy shares)
    private static final int TRANSFER = 0; 
   
    // withdrawl or deposit to external account
    private static final int WITHDRAWL_DEPOSIT = 1;

    // interest
    private static final int INTEREST = 2;

    // fee
    private static final int FEE = 3;

    // History of transactions
    private Vector transactions = new Vector();

    // Amount of cash available
    private float capital;

    private String name;

    public CashAccount(String name, float capital) {
	this.name = name;
	this.capital = capital;
    }

    public void transaction(TradingDate date, int transaction, int amount) {

	// Add record of transaction
	transactions.add(new Transaction((TradingDate)date.clone(),
					 transaction, amount));

	// Update value of account
	capital += amount;
    }

    public String getName() {
	return name;
    }

    public float getValue(QuoteCache cache, TradingDate date) {
	return capital;
    }

}
