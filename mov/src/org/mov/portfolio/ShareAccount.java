package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class ShareAccount implements Account {
    
    // Current stock holdings
    private HashMap stockHoldings = new HashMap();

    // Name of share portfolio
    private String name;

    public ShareAccount(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public float getValue(QuoteCache cache, TradingDate date) {

	Set set = stockHoldings.keySet();
	Iterator iterator = set.iterator();
	float value = 0;

	while(iterator.hasNext()) {
	    String symbol = (String)iterator.next();
	    StockHolding holding = (StockHolding)stockHoldings.get(symbol);

	    try {
		value += cache.getQuote(holding.getSymbol().toLowerCase(), 
					Token.DAY_CLOSE_TOKEN,
					date) *
		    holding.getShares();
	    }
	    catch(EvaluationException e) {
		// shouldn't happen
	    }
	}
	
	return value;
    }

    public void transaction(Transaction transaction) {
	
	String symbol = transaction.getSymbol();
	int shares = transaction.getShares();
	int type = transaction.getType();

	// Get current holding in this stock
	StockHolding holding = 
	    (StockHolding)stockHoldings.get(symbol);

	if(type == Transaction.ACCUMULATE ||
	   type == Transaction.DIVIDEND_DRP) {

	    // Do we already own the stock? If so accumulate
	    if(holding != null)
		holding.accumulate(shares);
	    else // otherwise add new stock to portfolio
		stockHoldings.put((Object)symbol, new StockHolding(symbol,
								   shares));
	}
	else if(type == Transaction.REDUCE) {
	    // ignore trying to sell stock we dont own
	    if(holding != null) {
		holding.reduce(shares);

		// do we have any left? if not remove stock holding from
		// holdings
		if(holding.getShares() <= 0)
		    stockHoldings.remove((Object)symbol);
	    }
	}
    }

    public StockHolding get(String symbol) {
	return (StockHolding)stockHoldings.get(symbol);
    }

    public HashMap getStockHoldings() {
	return stockHoldings;
    }

    public int getType() {
	return Account.SHARE_ACCOUNT;
    }

    public int size() {
	return stockHoldings.size();
    }
}
