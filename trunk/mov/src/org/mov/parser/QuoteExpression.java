package org.mov.parser;

import org.mov.util.*;

abstract public class QuoteExpression extends Expression {

    private int quoteKind;
    private int quoteType;

    public QuoteExpression(Expression quote) {
	// All the quotes are "PRICE_TYPE" except for day volume which is
	// volume type
	quoteType = PRICE_TYPE;

	if(quote instanceof DayOpenExpression)
	    quoteKind = Token.DAY_OPEN_TOKEN;
	else if(quote instanceof DayCloseExpression)
	    quoteKind = Token.DAY_CLOSE_TOKEN;
	else if(quote instanceof DayLowExpression)
	    quoteKind = Token.DAY_LOW_TOKEN;
	else if(quote instanceof DayHighExpression)
	    quoteKind = Token.DAY_HIGH_TOKEN;
	else {
	    quoteKind = Token.DAY_VOLUME_TOKEN;
	    quoteType = VOLUME_TYPE;
	}
    }

    protected int getQuoteKind() {
	return quoteKind;
    }

    protected int getQuoteType() {
	return quoteType;
    }

    protected Expression getArg(int arg) {
	return (Expression)getChildAt(arg);
    }
}
