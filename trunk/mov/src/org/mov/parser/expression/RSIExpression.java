package org.mov.parser.expression;

import org.mov.parser.*;
import org.mov.quote.*;

/**
 * An expression which finds the RSI over a given trading period.
 */
public class RSIExpression extends Expression {
    
    private int quoteKind;

    public RSIExpression(Expression days, Expression lag) {
	add(days);
	add(lag);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {
	
	int days = (int)getArg(0).evaluate(cache, symbol, day);
	int lastDay = day + (int)getArg(1).evaluate(cache, symbol, day);
	System.err.println("calling rsi on symbol "+symbol);
	return QuoteFunctions.rsi(cache, symbol, Token.DAY_CLOSE_TOKEN, days,
				  lastDay);
    }

    public String toString() {
	return new String("rsi(" + 
			  getArg(0).toString() + ", " +
			  getArg(1).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {

	// First type must be quote, second and third types must be value
	if(getArg(0).checkType() == VALUE_TYPE &&
	   getArg(1).checkType() == VALUE_TYPE)
	    return VALUE_TYPE;
	else
	    throw new TypeMismatchException();
    }

    public int getNeededChildren() {
	return 2;
    }

    private Expression getArg(int arg) {
	return (Expression)getChildAt(arg);
    }

}

