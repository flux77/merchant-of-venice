package org.mov.parser.expression;

import org.mov.quote.*;
import org.mov.parser.*;

/**
 * An expression which finds the minimum quote over a given trading period.
 */
public class MinExpression extends QuoteExpression {
    
    public MinExpression(Expression quote, Expression days,
			 Expression lag) {
	super(quote);

	add(quote);
	add(days);
	add(lag);
    }

    /**
     * Create a new minimum expression for the given <code>quote</code> kind,
     * for the given number of <code>days</code> starting with 
     * <code>lag</code> days away.
     *
     * @param	quote	the quote kind to find the minimum
     * @param	days	the number of days to search
     * @param	lag	the offset from the current day
     */
    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	int days = (int)getArg(1).evaluate(cache, symbol, day);
	int lastDay = day + (int)getArg(2).evaluate(cache, symbol, day);

	return min(cache, symbol, getQuoteKind(), days, lastDay);
    }

    public String toString() {
	return new String("min(" + 
			  getArg(0).toString() + ", " +
			  getArg(1).toString() + ", " +
			  getArg(2).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {

	// First type must be quote, second and third types must be value
	if(getArg(0).checkType() == QUOTE_TYPE &&
	   getArg(1).checkType() == VALUE_TYPE &&
	   getArg(2).checkType() == VALUE_TYPE)
	    return getQuoteType();
	else
	    throw new TypeMismatchException();
    }

    public int getNeededChildren() {
	return 3;
    }

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
}
