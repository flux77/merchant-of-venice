package org.mov.chart;

// Ummm can this be merged into GraphDataSource??
import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

abstract public class QuoteGraphDataSource extends GraphDataSource {

    private QuoteCache cache;
    private int quote;
    private String symbol;

    public String getSymbol() {
	return symbol;
    }

    protected void setCache(QuoteCache cache, int quote) {
	this.cache = cache;
	this.quote = quote;

	// So far only handles a single symbol - so get that symbol
	symbol = (String)cache.getSymbols()[0];
    }

    public QuoteCache getCache() {
	return cache;
    }

    public TradingDate getEndDate() {
	return cache.getEndDate();
    }

    public TradingDate getStartDate() {
	return cache.getStartDate();
    }

    public Float getValue(TradingDate date) {

	int offset = cache.dateToOffset(date);

	// negative offset indicates date is in cache
	if(offset > 0)
	    return null;
	else 
	    try {
		return new Float(cache.getQuote(symbol, quote, offset));
	    }	
	    catch(EvaluationException e) {
		// Shouldn't happen
		return null;
	    }

    }
}

