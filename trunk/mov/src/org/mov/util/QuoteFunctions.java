package org.mov.util;

import org.mov.parser.*;

public class QuoteFunctions {
    
    // Finds the minimum quote 
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
