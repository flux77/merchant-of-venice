package org.mov.parser;

import org.mov.util.*;

public class AvgExpression extends QuoteExpression {
    
    public AvgExpression(Expression quote, Expression days,
			 Expression lag) {
	super(quote);

	add(quote);
	add(days);
	add(lag);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {
	
	int days = (int)getArg(1).evaluate(cache, symbol, day);
	int lastDay = day + (int)getArg(2).evaluate(cache, symbol, day);

	return QuoteFunctions.avg(cache, symbol, getQuoteKind(), days,
				  lastDay);
    }

    public String toString() {
	return new String("avg(" + 
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
}

