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

    /**
     * RSI algorithm 
     * @param	cache	the quote cache to read the quotes from.
     * @param	symbol	the symbol to use.
     * @param	quote	the quote type we are interested in, e.g. DAY_OPEN.
     * @param	lastDay	fast access date offset in cache.
     * @return  the RSI value
     */
    static public float rsi(QuoteCache cache, String symbol, 
			    int quote, int days, int lastDay) 
	throws EvaluationException {
	System.err.println("Entering RSI for symbol "+symbol);

	//	Vector v = new Vector();

	// Determine the average up and down values for the days, divide by <period>
	float upvalues   = 0;
	float downvalues = 0;
	
	float last = 0;
	float current;
	for(int i = lastDay - days + 1; i <= lastDay; i++) {
	    //	for(int i = 0; i < v.size(); i++) {
	    current = cache.getQuote(symbol, quote, i);
	    System.err.println("offset "+i+", value "+current);
	    if (i < 0) {
		if (current > last)
		    upvalues += current;
		else if (current < last)
		    downvalues += current;
	    }
	    last = current;
	}

	float up_average = upvalues / days;
	float down_average = downvalues / days;
	System.out.println(" up: "+up_average+
			   " down: "+down_average);
	// RS = (up average / down average) + 1
	float strength = (up_average / down_average) + 1;
	System.err.println("s1: "+strength);
	// N = 100 / RS
	strength = 100 / strength;
	
	// RSI = 100 - N
	strength = 100 - strength;
	System.err.println("Value: "+strength);
	return strength;
    }

}

