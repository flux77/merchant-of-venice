package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which compares whether the first expression is less than
 * the second expression.
 */
public class LessThanExpression extends ComparisionExpression {

    public LessThanExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) <
	   getRight().evaluate(cache, symbol, day))
	    return TRUE;
	else
	    return FALSE;
    }

    public String toString() {
	return super.toString("<");
    }
}
