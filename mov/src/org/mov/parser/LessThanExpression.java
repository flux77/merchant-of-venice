package org.mov.parser;

import org.mov.util.*;

public class LessThanExpression extends ComparisionExpression {

    public LessThanExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) <
	   getRight().evaluate(cache, symbol, day))
	    return 1.0F;
	else
	    return 0.0F;
    }

    public String toString() {
	return super.toString("<");
    }
}
