package org.mov.parser;

import org.mov.util.*;

public class SubtractExpression extends ArithmeticExpression {

    public SubtractExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	return getLeft().evaluate(cache, symbol, day) -
	    getRight().evaluate(cache, symbol, day);
    }

    public String toString() {
	return super.toString("-");
    }
}
