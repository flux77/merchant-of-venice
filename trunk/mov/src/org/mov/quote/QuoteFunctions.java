package org.mov.quote;

import org.mov.parser.*;

/**
 * Provides a simple set of mathematical functions that can be performed
 * on a series of stock quotes. 
 */
public class QuoteFunctions {
    
    /** 
     * Finds the minimum stock quote for a given symbol in a given range. 
     *
     * @param	cache	the quote cache to read the quotes from.
     * @param	symbol	the symbol to use.
     * @param	quote	the quote type we are interested in, e.g. DAY_OPEN.
     * @param	lastDay	fast access date offset in cache.
     * @return	the minimum stock quote.
     */
    static public float min(QuoteCache cache, String symbol, 
			    int quote, int days, int lastDay) 
	throws EvaluationException {

	float min = Float.MAX_VALUE;
	float value;

	for(int i = lastDay - days + 1; i <= lastDay; i++) {
	    value = cache.getQuote(symbol, quote, i);

	    if(value < min)
		min = value;
	}

	return min;
    }

    /** 
     * Finds the maximum stock quote for a given symbol in a given range. 
     *
     * @param	cache	the quote cache to read the quotes from.
     * @param	symbol	the symbol to use.
     * @param	quote	the quote type we are interested in, e.g. DAY_OPEN.
     * @param	lastDay	fast access date offset in cache.
     * @return	the maximum stock quote.
     */
    static public float max(QuoteCache cache, String symbol, 
			    int quote, int days, int lastDay)
	throws EvaluationException {

	float max = Float.MIN_VALUE;
	float value;
	
	for(int i = lastDay - days + 1; i <= lastDay; i++) {
	    value = cache.getQuote(symbol, quote, i);

	    if(value > max)
		max = value;
	}

	return max;
    }

    /** 
     * Average the stock quotes for a given symbol in a given range. 
     *
     * @param	cache	the quote cache to read the quotes from.
     * @param	symbol	the symbol to use.
     * @param	quote	the quote type we are interested in, e.g. DAY_OPEN.
     * @param	lastDay	fast access date offset in cache.
     * @return	average stock quote.
     */
    static public float avg(QuoteCache cache, String symbol, 
			    int quote, int days, int lastDay) 
	throws EvaluationException {

	float avg = 0;

	// Sum quotes
	for(int i = lastDay - days + 1; i <= lastDay; i++) 
	    avg += cache.getQuote(symbol, quote, i);

	// Average
	avg /= days;

	return avg;
    }
}
