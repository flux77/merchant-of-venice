package org.mov.analyser;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;

public class PaperTrade {

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


    private static boolean sell(QuoteCache cache,
				Portfolio portfolio,
				CashAccount cashAccount,
				ShareAccount shareAccount,
				String symbol,
				float tradeCost,
				int day) 
	throws EvaluationException {

	// Make sure we have enough money for the trade
	if(cashAccount.getValue() >= tradeCost) {

	    // Get the number of shares we own - we will sell all of them
	    StockHolding stockHolding = shareAccount.get(symbol);
	    int shares = 0;
	    if(stockHolding != null) 
		shares = stockHolding.getShares();

	    // Only sell if we have any!
	    if(shares > 0) {

		// How much are they worth? We sell at the day open price
		float amount = shares * cache.getQuote(symbol, Quote.DAY_OPEN,
						       day);
		
		TradingDate date = cache.offsetToDate(day);
		Transaction sell = Transaction.newReduce(date, amount,
							 symbol, shares,
							 tradeCost,
							 cashAccount,
							 shareAccount);
		portfolio.addTransaction(sell);

		return true;
	    }
	}

	return false;
    }

    private static boolean buy(QuoteCache cache,
			       Portfolio portfolio,
			       CashAccount cashAccount,
			       ShareAccount shareAccount,
			       String symbol,
			       float amount,				 
			       float tradeCost,
			       int day) 
	throws EvaluationException {


	// Calculate maximum number of shares we can buy with
	// the given amount
	float sharePrice = cache.getQuote(symbol, Quote.DAY_OPEN, day);
	int shares = 
	    (new Double(Math.floor(amount / sharePrice))).intValue();
	
	// Now calculate the actual amount the shares will cost
	amount = sharePrice * shares;
	
	// Make sure we have enough money for the trade
	if(cashAccount.getValue() >= (tradeCost + amount)) {
	    TradingDate date = cache.offsetToDate(day);	    
	    Transaction buy = Transaction.newAccumulate(date, amount,
							symbol, shares,
							tradeCost,
							cashAccount,
							shareAccount);

	    portfolio.addTransaction(buy);

	    return true;
	}
	
	return false;
    }

    public static Portfolio paperTrade(String portfolioName, 
				       QuoteCache cache, String symbol,
				       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       float capital,
				       float tradeCost) {

	System.out.println("paper trade");

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

	System.out.println("day " + day + " to " + endDay);

	// Now iterate through each trading day and decide whether
	// to buy/sell. The last day is used for placing the previous
	// day's buy/sell orders.
	while(day < endDay) {

	    //	    System.out.println("trying date " + day);

	    try {		

		// If we own the stock should we sell?
		if(ownStock) {
		    if(sell.evaluate(cache, symbol, day) >= Expression.TRUE) {
			if(sell(cache, portfolio, cashAccount,
				shareAccount, symbol, 
				tradeCost, day + 1))
			    ownStock = false;
		    }
		}
		
		// If we don't own the stock should we buy?
		else {
		    if(buy.evaluate(cache, symbol, day) >= Expression.TRUE) {
			// Spend all our money except for enough to do
			// a buy and a later sell trade
			float amount = cashAccount.getValue() - 2 * tradeCost;

			if(buy(cache, portfolio, cashAccount, 
			       shareAccount, symbol, amount, 
			       tradeCost, day + 1))
			    ownStock = true;

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

/*

	System.out.println("loading quotes");
	QuoteCache cache = new QuoteCache("NCP");



	Parser parser = new Parser();

	try {
	    System.out.println("ncp start: " +
			       cache.getQuote("ncp", Quote.DAY_CLOSE,
					      new TradingDate(1990, 1, 3)));
	    System.out.println("ncp end: " +
			       cache.getQuote("ncp", Quote.DAY_CLOSE,
					      new TradingDate(2001, 12, 20)));


	    //	    int i = 10;
	    //int j = 30;

	    for(int i = 1; i < 100; i++) {
	    	for(int j = i + 1; j < 101; j++) {
		    
		    Expression buy = 
	    		parser.parse("avg(day_close, " + i + ", 0) > avg(day_close, " + j + ", 0)");
		    Expression sell = 
		    	parser.parse("avg(day_close, " + i + ", 0) < avg(day_close, " + j + ", 0)");

		    //Expression buy = 
		    //	parser.parse("1");
		    //Expression sell = 
		    //	parser.parse("0");
		    
		    Portfolio portfolio = 
			PaperTradeModule.paperTrade("test",
						    cache,
						    "ncp",
						    new TradingDate(1990, 1, 3),
						    new TradingDate(2001, 12, 20),
						    buy,
						    sell,
						    10000,
						    35);
	    
		    
		    System.out.println(i + " " + j + " " + portfolio.getValue(cache,
									      new TradingDate(2001, 12, 20)));
		}
	    }

	    //    CommandManager.getInstance().graphPortfolio(portfolio);
	}
	catch(ExpressionException e) {
	    System.out.println(e);
	}
	

	*/
