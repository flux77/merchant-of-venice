package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * Generic interface for all financial account objects. This interface
 * defines some generic properties that all accounts need to have such
 * as name, type and value.
 */
public interface Account {
    /** Account is a cash account (bank account, cash management account etc)
     */
    public static final int CASH_ACCOUNT = 0;

    /** Account is a share trading account which contains a list of shares */
    public static final int SHARE_ACCOUNT = 1;

    /**
     * Return the name of this account.
     *
     * @return	name of the account
     */
    public String getName();

    /**
     * Return the type of this account. 
     *
     * @return	type of the account, either {@link #CASH_ACCOUNT} or
     *		{@link #SHARE_ACCOUNT} 
     */
    public int getType();

    /**
     * Return the value of this account on the given day.
     *
     * @param	cache	the quote cache
     * @param	date	the date to calculate
     */
    public float getValue(QuoteCache cache, TradingDate date)
	throws EvaluationException;


    /**
     * Perform a transaction on this account.
     *
     * @param	transaction	transaction to occur
     */
    public void transaction(Transaction transaction);

    /**
     * Remove all transactions from account.
     */
    public void removeAllTransactions();
}

