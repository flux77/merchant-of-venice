/*

package org.mov.main;

// This code has been deprecated. The new class should just be a vector
// of accounts.

import java.util.*;

public class Portfolio {

    private HashMap stockHoldings;
    private String name;

    public Portfolio(String name) {
	stockHoldings = new HashMap();
	this.name = name;
    }

    public void trade(int trade, String symbol, int shares) {
	
	// Get current holding in this stock
	StockHolding stockHolding = (StockHolding)stockHoldings.get(symbol);

	switch(trade) {
	case(Trade.BUY):

	    // Do we already own the stock? If so accumulate
	    if(stockHolding != null)
		stockHolding.accumulate(shares);
	    else // otherwise add new stock to portfolio
		stockHoldings.put((Object)symbol, new StockHolding(symbol,
								   shares));

	    break;
	    
	case(Trade.SELL):

	    // ignore trying to sell stock we dont own
	    if(stockHolding != null) {
		stockHolding.reduce(shares);

		// do we have any left? if not remove stock holding from
		// holdings
		if(stockHolding.getShares() <= 0)
		    stockHoldings.remove(stockHolding);
	    }

	    break;
	}
    }

    public String getName() {
	return name;
    }

    public StockHolding get(String symbol) {
	return (StockHolding)stockHoldings.get(symbol);
    }

    public Set keySet() {
	return stockHoldings.keySet();
    }

    public int size() {
	return stockHoldings.size();
    }
}


*/
