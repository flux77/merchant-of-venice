package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which compares whether the first expression is less than or
 * equal to the second expression.
 */
public class LessThanEqualExpression extends ComparisionExpression {

    public LessThanEqualExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) <=
	   getRight().evaluate(cache, symbol, day))
	    return TRUE;
	else
	    return FALSE;
    }

    public String toString() {
	return super.toString("<=");
    }
}
