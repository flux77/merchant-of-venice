package org.mov.main;

/*

// TODO: need to optimise so if we are doing non GA-trading it doesnt load
// in all the data at once (this way we can trade all stocks over all data
// without running out of mem)

import java.util.*;

public class PaperTrade {
   
    // Variables passed to us

    private String name;		// Name of paper trade
    private TradingDate startDate;	// Paper trade from this date
    private TradingDate endDate;	// Paper trade to this date
    private Expression buy;		// Buy indicator    
    private Expression sell;		// Sell indicator
    private float tradeAmount;		// Trade in lots of
    private float tradeCost;		// Cost of trade (brokerage fees etc)
    private QuoteCache cache;		// Cache for stock quotes

    // Internal variables

    private CashAccount cashAccount;	// Our stock pile of cash
    private ShareAccount shareAccount;	// Our shares 

    public PaperTrade(String name, 
		      TradingDate startDate, 
		      TradingDate endDate,
		      Expression buy, 
		      Expression sell,
		      float capital, 
		      float tradeAmount, 
		      float tradeCost,
		      int searchRestriction) {

	newPaperTrade(name, startDate, endDate, buy, sell, capital, 
		      tradeAmount, tradeCost, 
		      new QuoteCache(endDate, searchRestriction));
    }

    public PaperTrade(String name, 
		      TradingDate startDate, 
		      TradingDate endDate,
		      Expression buy, 
		      Expression sell,
		      float capital, 
		      float tradeAmount, 
		      float tradeCost,
		      QuoteCache cache) {

	newPaperTrade(name, startDate, endDate, buy, sell, capital, 
		      tradeAmount, tradeCost, 
		      cache);
    }

    public CashAccount getCashAccount() {
	return cashAccount;
    }

    public ShareAccount getShareAccount() {
	return shareAccount;
    }

    // Calculate value of shares + capital at the latest date
    public float getValue() throws EvaluationException {
	return cashAccount.getValue(cache, endDate) + 
	    shareAccount.getValue(cache, endDate);
    }

    // Paper trade up to end date
    public void trade() throws EvaluationException {
	// Trade for days in cache going from the latest date to
	// the earliest-1. All trades go through the next day so we
	// dont bother to issue anymore orders on the last day.

	for(int today = cache.getNumberDays(); today < 0; today++)
	    dayTrade(today);
    }
	
    // Day trade on current day
    private void dayTrade(int today) throws EvaluationException {

	// Sell before buy so we can buy with the money we just made
	// selling. 
	sellStocks(today);
	buyStocks(today);
    }

    private void buyStocks(int today) throws EvaluationException {

	// Only buy if we have enough money
	if(enoughToBuy()) {
	    String symbol;
	    
	    // Iterate through all stocks seeing if we should buy anything
	    Iterator iterator = cache.dayQuotes(today).iterator();

	    while(iterator.hasNext()) {
		symbol = (String)iterator.next();

		// If we don't own the stock and the buy indicator
		// tells us to buy - then buy
		if(shareAccount.get(symbol) == null &&
		   buy.evaluate(cache, symbol, today) >=
		   LogicExpression.TRUE_LEVEL) {
		    
		    // Put in a bid to the market to buy the share at
		    // today's close price. If the next day's share low price
		    // is lower than today's close price well be able to buy
		    // otherwise we wont
		    float today_close_price = 
			cache.getQuote(symbol, Token.DAY_CLOSE_TOKEN, today);
		    float tomorrow_low_price = 
			cache.getQuote(symbol, Token.DAY_LOW_TOKEN, today+1);

		    // Buy them tomorrow at today's close price if possible
		    if(tomorrow_low_price <= today_close_price) {
			int shares = sharesToBuy(today_close_price);

			history.trade(cache.getDate(today+1), 
				      Trade.BUY, 
				      symbol, 
				      shares, 
				      today_close_price,
				      tradeCost);

			capital -= tradeCost + shares * today_close_price; 
			
			// Only continue if we have enough money
			if(!enoughToBuy())
			    break;
		    }
		}
	    }
	}
    }

    // do we have enough capital to buy?
    private boolean enoughToBuy() {
	if(cashAccount.getValue() > tradeAmount + tradeCost) 
	    return true;
	else
	    return false;
    }

    // calculate how many shares to buy (rounds down to nearest share)
    private int sharesToBuy(float sharePrice) {
	return (int)(tradeAmount / sharePrice);
    }

    private void sellStocks(int today) throws EvaluationException {

	// Assumption: we always have enough to sell our stocks - 
	// temporary debt is allowed to pay for trade cost

	// Iterate through stock holdings to see if its time to 
	Iterator iterator = shareAccount.getStockHoldings().iterator();
	String symbol;
	StockHolding stock;

	while(iterator.hasNext()) {
	    symbol = (String)iterator.next();

	    // Evaluates to true?
	    if(sell.evaluate(cache, symbol, today) >=
	       LogicExpression.TRUE_LEVEL) {

		stock = portfolio.get(symbol);
		float today_close_price = 
		    cache.getQuote(symbol, Token.DAY_CLOSE_TOKEN, today);
		float tomorrow_high_price = 
		    cache.getQuote(symbol, Token.DAY_HIGH_TOKEN, today+1);

		// Put in bid to market to sell the share at today's
		// close price. If the next day's share high price 
		// is greater than today's close price well be able to
		// sell otherwise we wont.
		if(tomorrow_high_price >= today_close_price) {

		    history.trade(cache.getDate(today+1), 
				  Trade.SELL, 
				  symbol, 
				  stock.getShares(), // sell them all
				  today_close_price,
				  tradeCost);

		    capital += (stock.getShares() * today_close_price -
				tradeCost);
		}
	    }
	}
    }

    private void newPaperTrade(String name, 
			       TradingDate startDate, TradingDate endDate,
			       Expression buy, Expression sell, float capital, 
			       float tradeAmount, float tradeCost,
			       QuoteCache cache) {

	this.name = name;
	this.startDate = startDate;
	this.endDate = endDate;
	this.buy = buy;
	this.sell = sell;	
	this.cache = cache;
	this.tradeAmount = tradeAmount;
	this.tradeCost = tradeCost;

	this.shareAccount = new ShareAccount(name);
	this.cashAccount = new CashAccount(name, capital);
    } 
}

*/
