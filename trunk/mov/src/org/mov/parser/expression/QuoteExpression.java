package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

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
	    quoteKind = Quote.DAY_OPEN;
	else if(quote instanceof DayCloseExpression)
	    quoteKind = Quote.DAY_CLOSE;
	else if(quote instanceof DayLowExpression)
	    quoteKind = Quote.DAY_LOW;
	else if(quote instanceof DayHighExpression)
	    quoteKind = Quote.DAY_HIGH;
	else {
	    quoteKind = Quote.DAY_VOLUME;
	    quoteType = VOLUME_TYPE;
	}
    }

    /**
     * Get the quote kind. 
     *
     * @return	the quote kind, one of: {@link Quote#DAY_OPEN}, 
     * {@link Quote#DAY_CLOSE}, {@link Quote#DAY_HIGH} or 
     * {@link Quote#DAY_LOW}
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
