package org.mov.parser;

import org.mov.util.*;

/**
 * Abstract base class for expressions dealing with quotes:
 * <code>avg, lag, max, min</code>
 */
abstract public class QuoteExpression extends Expression {

    // Quote kind - one of: open, close, low, high
    private int quoteKind;

    // Quote type - one of: PRICE_TYPE or VOLUME_TYPE
    private int quoteType;

    /**
     * Create a new quote expression with the given quote expression.
     * This expression should be either a {@link DayOpenExpression},
     * {@link DayCloseExpression}, {@link DayLowExpression},
     * {@link DayHighExpression} or a {@link DayVolumeExpression}.
     *
     * @param	quote	the quote expression
     */
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

    /**
     * Get the quote kind. 
     *
     * @return	the quote kind, one of: {@link Token#DAY_OPEN_TOKEN}, 
     * {@link Token#DAY_CLOSE_TOKEN}, {@link Token#DAY_HIGH_TOKEN} or 
     * {@link Token#DAY_LOW_TOKEN}
     */
    protected int getQuoteKind() {
	return quoteKind;
    }

    /**
     * Get the quote type.
     *
     * @return	the quote type, either {@link #PRICE_TYPE} or
     * {@link #VOLUME_TYPE}
     */
    protected int getQuoteType() {
	return quoteType;
    }

    /**
     * Get the argument.
     *
     * @param	arg	the argument to return
     * @return	the argument
     */
    protected Expression getArg(int arg) {
	return (Expression)getChildAt(arg);
    }
}
