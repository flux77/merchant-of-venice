package org.mov.analyser;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;

public class PaperTradeModule {

    private final static String CASH_ACCOUNT_NAME = "Cash Account";
    private final static String SHARE_ACCOUNT_NAME = "Share Account";

    private static Portfolio createPortfolio(String portfolioName,
					    TradingDate startDate,
					    float capital) {
	Portfolio portfolio = new Portfolio(portfolioName);

	// Add a cash account and a share account
	CashAccount cashAccount = new CashAccount(CASH_ACCOUNT_NAME);
	ShareAccount shareAccount = new ShareAccount(SHARE_ACCOUNT_NAME);

	portfolio.addAccount(cashAccount);
	portfolio.addAccount(shareAccount);

	// Deposit starting capital into portfolio
	Transaction transaction = 
	    Transaction.newDeposit(startDate, capital, cashAccount);

	portfolio.addTransaction(transaction);

	return portfolio;
    }

    /*
    private static Portfolio sell(QuoteCache cache,
				  Portfolio portfolio,
				  CashAccount cashAccount,
				  ShareAccount shareAccount,
				  String symbol,
				  float tradeCost,
				  int day) {

	TradingDate date = cache.offsetToDate(day);

	// Make sure we have enough money for the trade
	if(cashAccount.getValue(cache, date) >= tradeCost) {

	}

    }
    */

    public static Portfolio paperTrade(String portfolioName, 
				       QuoteCache cache, String symbol,
				       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       float capital,
				       float tradeCost) {

	// First create a portfolio suitable for paper trading
	Portfolio portfolio = createPortfolio(portfolioName,
					      startDate,
					      capital);
	ShareAccount shareAccount = 
	    (ShareAccount)
	    portfolio.findAccountByName(SHARE_ACCOUNT_NAME);
	CashAccount cashAccount = 
	    (CashAccount)
	    portfolio.findAccountByName(CASH_ACCOUNT_NAME);

	int day = cache.dateToOffset(startDate);
	int endDay = cache.dateToOffset(endDate);

	// This is set when we own the stock
	boolean ownStock = false;

	// Now iterate through each trading day and decide whether
	// to buy/sell
	while(day <= endDay) {

	    try {		
		// If we own the stock should we sell?
		if(ownStock) {
		    if(sell.evaluate(cache, symbol, day) >= Expression.TRUE) {
			//			portfolio = sell(portfolio, cashAccount,
			//		 shareAccount, cache, symbol, 
			//		 tradeCost, day);
		    }
		}
		
		// If we don't own the stock should we buy?
		else {
		    if(buy.evaluate(cache, symbol, day) >= Expression.TRUE) {
			//			portfolio = buy(portfolio, cash, symbol, date);
		    }
		}
	    }
	    catch(EvaluationException e) {
		// we couldnt get a quote for this day - ignore
	    }

	    // Go to the next trading day
	    day++;
	}

	return portfolio;
    }


}
