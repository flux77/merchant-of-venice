package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * An expression which compares the two sub-expressions for inequality.
 */
public class NotEqualExpression extends ComparisionExpression {

    public NotEqualExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) !=
	   getRight().evaluate(cache, symbol, day))
	    return TRUE;
	else
	    return FALSE;
    }

    public String toString() {
	return super.toString("!=");
    }
}



