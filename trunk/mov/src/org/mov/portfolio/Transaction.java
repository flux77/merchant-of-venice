package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;

/**
 * Representation of a single transaction on the portfolio. A transaction
 * is ANY kind of financial action from trading a stock to receiving
 * credit interest.
 * Transactions are used to build up a portfolio, the user does not
 * enter how many shares they own but rather their share transactions and
 * their current total is calculated from that.
 */
public class Transaction implements Comparable {

    // Transaction types on any account
   
    /** Withdrawl cash */
    public static final int WITHDRAWAL = 0;

    /** Deposit cash */
    public static final int DEPOSIT = 1;

    /** Credit/Debit interest */
    public static final int INTEREST = 2;

    /** Fee (FID, TAX etc) */
    public static final int FEE = 3;

    /** Accumulate (buy) shares */
    public static final int ACCUMULATE = 4;

    /** Reduce (sell) shares */
    public static final int REDUCE = 5;

    /** Dividend */
    public static final int DIVIDEND = 6;

    /** Dividend DRP (Dividend Reinvestment Programme) */
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

    /**
     * Create a new generic transaction.
     *
     * @param	type	type of the transaction, e.g. {@link #INTEREST}
     * @param	date	date the transaction took place
     * @param	amount	the amount/value/cost of the transaction
     * @param	symbol	for share trades and dividends, the symbol traded
     * @param	shares	for share trades and dividend DRPs, the number of
     *			symbols bought/solded/re-invested
     * @param	tradeCost	for shares trades, the cost of the trade
     *			including any GST, Stamp Duty, Taxes etc
     * @param	cashAccount	related cash account (if any)
     * @param	shareAccount	related share account (if any)
     */
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

    /**
     * Create a new withdrawal transaction. Money has been withdrawn from
     * a {@link CashAccount}. 
     *
     * @param	date	date the transaction took place
     * @param	amount	the amount withdrawn
     * @param	cashAccount	cash account 
     */
    public static Transaction newWithdrawal(TradingDate date, 
					    float amount,
					    CashAccount account) {
	return new Transaction(WITHDRAWAL, date, amount, null, 0, 
			       0.0F, account, null);
    }

    /**
     * Create a new deposit transaction. Money has been deposited into
     * a {@link CashAccount}. 
     *
     * @param	date	date the transaction took place
     * @param	amount	the amount deposited
     * @param	cashAccount	cash account 
     */
    public static Transaction newDeposit(TradingDate date, 
					 float amount,
					 CashAccount account) {
	return new Transaction(DEPOSIT, date, amount, null, 0, 
			       0.0F, account, null);
    }

    /**
     * Create a new interest transaction. {@link CashAccount} has
     * received credit interest.
     *
     * @param	date	date the transaction took place
     * @param	amount	the amount deposited
     * @param	cashAccount	cash account 
     */
    public static Transaction newInterest(TradingDate date, 
					  float amount,
					  CashAccount account) {
	return new Transaction(INTEREST, date, amount, null, 0, 
			       0.0F, account, null);
    }

    /**
     * Create a new fee transaction. {@link CashAccount} has been
     * debited with some sort of fee.
     *
     * @param	date	date the transaction took place
     * @param	amount	the amount withdrawn
     * @param	cashAccount	cash account 
     */
    public static Transaction newFee(TradingDate date, 
				     float amount,
				     CashAccount account) {
	return new Transaction(FEE, date, amount, null, 0, 
			       0.0F, account, null);
    }

    /**
     * Create a new accumulate transaction. Shares have been bought.
     *
     * @param	date	date the transaction took place
     * @param	amount	the cost of the shares (not including trade cost)
     * @param	symbol	the symbol traded
     * @param	shares	number of shares traded
     * @param	tradeCost	for shares trades, the cost of the trade
     *			including any GST, Stamp Duty, Taxes etc
     * @param	shareAccount	share account 
     * @param	cashAccount	cash account 
     */
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

    /**
     * Create a new reduce transaction. Shares have been sold.
     *
     * @param	date	date the transaction took place
     * @param	amount	the value of the shares (not including trade cost)
     * @param	symbol	the symbol traded
     * @param	shares	number of shares traded
     * @param	tradeCost	for shares trades, the cost of the trade
     *			including any GST, Stamp Duty, Taxes etc
     * @param	shareAccount	share account 
     * @param	cashAccount	cash account 
     */
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

    /**
     * Create a new dividend transaction. A share has paid a dividend.
     *
     * @param	date	date the transaction took place
     * @param	amount	the value of the dividend
     * @param	symbol	the stock paying the dividend
     * @param	shareAccount	share account 
     * @param	cashAccount	cash account 
     */
    public static Transaction newDividend(TradingDate date, 
					  float amount,
					  String symbol,
					  CashAccount cashAccount,
					  ShareAccount shareAccount) {
	return new Transaction(DIVIDEND, date, amount, symbol, 0, 
			       0.0F, cashAccount, shareAccount);
    }

    /**
     * Create a new dividend transaction. A share has paid a dividend and
     * the dividend has been re-invested back into the holding, increasing
     * the holding by several shares.
     *
     * @param	date	date the transaction took place
     * @param	amount	the value of the dividend
     * @param	symbol	the stock paying the dividend
     * @param	shares	the number of shares gained
     * @param	shareAccount	share account 
     * @param	cashAccount	cash account 
     */
    public static Transaction newDividendDRP(TradingDate date, 
					     float amount,
					     String symbol,
					     int shares,
					     ShareAccount shareAccount) {
	return new Transaction(DIVIDEND_DRP, date, 0.0F, symbol, shares, 
			       0.0F, null, shareAccount);
    }

    /** 
     * Convert between a given transaction type and a text string.
     *
     * @param	type	the transaction type
     * @return	string representation of transaction
     */
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

    /**
     * Convert between a transaction type string and its transaction type.
     *
     * @param	type	string representation of the transaction 
     * @return	the transaction type
     */
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

    /**
     * Compare this transaction to another one. Transactions are compared
     * by date only.
     *
     * @param	object	other transaction object
     * @return	<code>-1</code> if this transaction is before; 
     *		<code>0</code> if they fall on the same date;
     *		<code>1</code> if this transaction is after
     */
    public int compareTo(Object object) {
	Transaction transaction = (Transaction)object;

	// Sort transactions based on date
	return(getDate().compareTo(transaction.getDate()));
    }

    /**
     * Convert this transaction to a CSV string.
     * 
     * @return	CSV representation of transaction
     */
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

    /**
     * Return the type of this transaction.
     * 
     * @return	type
     */
    public int getType() {
	return type;
    }

    /** 
     * Return the date this transaction occured on.
     *
     * @return	date
     */
    public TradingDate getDate() {
	return date;
    }

    /**
     * Return the amount of this transaction if applicable.
     *
     * @return	amount
     */
    public float getAmount() {
	return amount;
    }
    
    /** 
     * Return the symbol traded if this transaction was a share transaction.
     *
     * @return	symbol or <code>null</code> if this transaction was not a
     *		share transaction
     */
    public String getSymbol() {
	return symbol;
    }

    /** 
     * Return the shares traded if this transaction was a share transaction.
     *
     * @return	number of shares or <code>0</code> if this transaction was 
     *		not a share transaction
     */
    public int getShares() {
	return shares;
    }

    /**
     * Return the cost of the trade if this transaction was a share
     * transaction.
     * 
     * @return cost of trade or <code>0</code> if this transaction was 
     *		not a share transaction
     */
    public float getTradeCost() {
	return tradeCost;
    }

    /**
     * Return the associated cash account if any.
     *
     * @return	cash account or <code>null</code> if no cash account
     *		associated with this transaction
     * @see	CashAccount
     */
    public CashAccount getCashAccount() {
	return cashAccount;
    }

    /**
     * Return the associated share account if any.
     *
     * @return	share account or <code>null</code> if no cash account
     *		associated with this transaction
     * @see	ShareAccount
     */
    public ShareAccount getShareAccount() {
	return shareAccount;
    }
}

