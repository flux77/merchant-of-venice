package org.mov.parser;

import org.mov.util.*;

public class LagExpression extends QuoteExpression {
    
    public LagExpression(Expression quote, Expression lag) {
	super(quote);

	add(quote);
	add(lag);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	return cache.getQuote(symbol, getQuoteKind(), day +
			      (int)getArg(1).evaluate(cache, symbol, day));
    }

    public String toString() {
	return new String("lag(" + getArg(0).toString() + ", " +
			  getArg(1).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {

	// Left type must be quote and right type must be number type
	if(getArg(0).checkType() == QUOTE_TYPE &&
	   getArg(1).checkType() == VALUE_TYPE)
	    return getQuoteType();
	else
	    throw new TypeMismatchException();
    }

    public int getNeededChildren() {
	return 2;
    }
}
