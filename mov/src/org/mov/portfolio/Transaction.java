package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;

public class Transaction implements Comparable {

    // Transaction types on any account
   
    // Withdrawl cash
    public static final int WITHDRAWAL = 0;

    // Deposit cash
    public static final int DEPOSIT = 1;

    // Credit/Debit interest
    public static final int INTEREST = 2;

    // Fee (FID, TAX etc)
    public static final int FEE = 3;

    // Accumulate (buy) shares
    public static final int ACCUMULATE = 4;

    // Reduce (sell) shares
    public static final int REDUCE = 5;

    // Dividend
    public static final int DIVIDEND = 6;

    // Dividend DRP (Dividend Reinvestment Programme)
    public static final int DIVIDEND_DRP = 7;

    // All possible fields for all possible transactions
    private TradingDate date;
    private int type;
    private float amount;
    private String symbol;
    private int shares;
    private float tradeCost;
    private CashAccount cashAccount;
    private ShareAccount shareAccount;

    public Transaction(int type, TradingDate date, float amount, 
		       String symbol, int shares, float tradeCost,
		       CashAccount cashAccount, ShareAccount shareAccount) {
	this.type = type;
	this.date = date;
	this.amount = amount;
	this.symbol = symbol;
	this.shares = shares;
	this.tradeCost = tradeCost;
	this.cashAccount = cashAccount;
	this.shareAccount = shareAccount;
    }

    public static Transaction newWithdrawal(TradingDate date, 
					    float amount,
					    CashAccount account) {
	return new Transaction(WITHDRAWAL, date, amount, null, 0, 
			       0.0F, account, null);
    }

    public static Transaction newDeposit(TradingDate date, 
					 float amount,
					 CashAccount account) {
	return new Transaction(DEPOSIT, date, amount, null, 0, 
			       0.0F, account, null);
    }

    public static Transaction newInterest(TradingDate date, 
					  float amount,
					  CashAccount account) {
	return new Transaction(INTEREST, date, amount, null, 0, 
			       0.0F, account, null);
    }

    public static Transaction newFee(TradingDate date, 
				     float amount,
				     CashAccount account) {
	return new Transaction(FEE, date, amount, null, 0, 
			       0.0F, account, null);
    }

    public static Transaction newAccumulate(TradingDate date, 
					    float amount,
					    String symbol,
					    int shares,
					    float tradeCost,
					    CashAccount cashAccount,
					    ShareAccount shareAccount) {
	return new Transaction(ACCUMULATE, date, amount, symbol, shares, 
			       tradeCost, cashAccount, shareAccount);
    }

    public static Transaction newReduce(TradingDate date, 
					float amount,
					String symbol,
					int shares,
					float tradeCost,
					CashAccount cashAccount,
					ShareAccount shareAccount) {
	return new Transaction(REDUCE, date, amount, symbol, shares, 
			       tradeCost, cashAccount, shareAccount);
    }

    public static Transaction newDividend(TradingDate date, 
					  float amount,
					  String symbol,
					  CashAccount cashAccount,
					  ShareAccount shareAccount) {
	return new Transaction(DIVIDEND, date, amount, symbol, 0, 
			       0.0F, cashAccount, shareAccount);
    }

    public static Transaction newDividendDRP(TradingDate date, 
					     float amount,
					     String symbol,
					     int shares,
					     ShareAccount shareAccount) {
	return new Transaction(DIVIDEND_DRP, date, 0.0F, symbol, shares, 
			       0.0F, null, shareAccount);
    }

    public static String typeToString(int type) {
	String[] typeNames = {"Withdrawal", "Deposit", "Interest", "Fee",
			      "Accumulate", "Reduce", "Dividend", 
			      "Dividend DRP"};

	if(type < typeNames.length) {
	    return typeNames[type];
	}
	else
	    return "Withdrawal";
    }

    public static int stringToType(String type) {
	if(type.equals("Accumulate"))
	    return Transaction.ACCUMULATE;
	else if(type.equals("Reduce")) 
	    return Transaction.REDUCE;
	else if(type.equals("Deposit")) 
	    return Transaction.DEPOSIT;
	else if(type.equals("Fee")) 
	    return Transaction.FEE;
	else if(type.equals("Interest")) 
	    return Transaction.INTEREST;
	else if(type.equals("Withdrawal"))
	    return Transaction.WITHDRAWAL;
	else if(type.equals("Dividend")) 
	    return Transaction.DIVIDEND;
	else
	    return Transaction.DIVIDEND_DRP;
    }

    public int compareTo(Object object) {
	Transaction transaction = (Transaction)object;

	// Sort transactions based on date
	return(getDate().compareTo(transaction.getDate()));
    }

    public String toString() {
	String cashAccountName = "";
	String shareAccountName = "";

	if(getCashAccount() != null) 
	    cashAccountName = getCashAccount().getName();

	if(getShareAccount() != null)
	    shareAccountName = getShareAccount().getName();
	
	// Write in CSV format
	// date, type, amount, symbol, shares, tradeCost,
	// cashAccount, shareAccount
	return new String(getDate().toString("dd/mm/yyyy")
			  + "," +
			  Transaction.typeToString(getType())
			  + "," +
			  getAmount()
			  + "," +
			  getSymbol()
			  + "," +
			  getShares()
			  + "," +
			  getTradeCost()
			  + "," +
			  cashAccountName
			  + "," +
			  shareAccountName);
    }

    public int getType() {
	return type;
    }

    public TradingDate getDate() {
	return date;
    }

    public float getAmount() {
	return amount;
    }
    
    public String getSymbol() {
	return symbol;
    }

    public int getShares() {
	return shares;
    }

    public float getTradeCost() {
	return tradeCost;
    }

    public CashAccount getCashAccount() {
	return cashAccount;
    }

    public ShareAccount getShareAccount() {
	return shareAccount;
    }
}

