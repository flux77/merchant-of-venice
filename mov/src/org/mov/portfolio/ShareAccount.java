package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

public class ShareAccount implements Account {
    
    // History of trades
    private Vector trades = new Vector();

    // Current stock holdings
    private HashMap holdings = new HashMap();

    // Name of share portfolio
    private String name;

    public ShareAccount(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public float getValue(QuoteCache cache, TradingDate date) 
	throws EvaluationException { 
	Set set = getStockHoldings();
	Iterator iterator = set.iterator();
	StockHolding holding;
	float value = 0;

	while(iterator.hasNext()) {
	    holding = (StockHolding)iterator.next();
	    value += cache.getQuote(holding.getSymbol(), Token.DAY_CLOSE_TOKEN,
				    date) *
		holding.getShares();
	}
	
	return value;
    }

    public void trade(TradingDate date, int trade, String symbol, int shares, 
		      float price, float tradeCost) {
	
	// Add record of trade
	trades.add(new Trade((TradingDate)date.clone(), trade, symbol,
			     shares, price, tradeCost));

	// Get current holding in this stock
	StockHolding holding = (StockHolding)holdings.get(symbol);

	switch(trade) {
	case(Trade.BUY):

	    // Do we already own the stock? If so accumulate
	    if(holding != null)
		holding.accumulate(shares);
	    else // otherwise add new stock to portfolio
		holdings.put((Object)symbol, new StockHolding(symbol,
							      shares));

	    break;
	    
	case(Trade.SELL):

	    // ignore trying to sell stock we dont own
	    if(holding != null) {
		holding.reduce(shares);

		// do we have any left? if not remove stock holding from
		// holdings
		if(holding.getShares() <= 0)
		    holdings.remove(holding);
	    }

	    break;
	}
    }

    public StockHolding get(String symbol) {
	return (StockHolding)holdings.get(symbol);
    }

    public Set getStockHoldings() {
	return holdings.keySet();
    }

    public int size() {
	return holdings.size();
    }
}
