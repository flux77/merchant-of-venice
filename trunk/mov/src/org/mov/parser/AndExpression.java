package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which performs boolean <code>and</code> on two 
 * sub-expressions.
 */
public class AndExpression extends LogicExpression {

    public AndExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) >= TRUE_LEVEL &&
	   getRight().evaluate(cache, symbol, day) >= TRUE_LEVEL)
	    return TRUE;
	else
	    return FALSE;
    }

    public String toString() {
	return super.toString("and");
    }
}

