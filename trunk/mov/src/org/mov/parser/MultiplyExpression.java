package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which multiplies two sub-expressions.
 */
public class MultiplyExpression extends ArithmeticExpression {

    public MultiplyExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	return getLeft().evaluate(cache, symbol, day) *
	    getRight().evaluate(cache, symbol, day);
    }

    public String toString() {
	return super.toString("*");
    }
}
