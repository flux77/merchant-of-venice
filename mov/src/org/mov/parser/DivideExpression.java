package org.mov.parser;

import org.mov.util.*;

public class DivideExpression extends ArithmeticExpression {

    public DivideExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	float right = getRight().evaluate(cache, symbol, day);

	if(right != 0.0F)
	    return getLeft().evaluate(cache, symbol, day) / right;
	else
	    return 0.0F; // whatever :)
    }

    public String toString() {
	return super.toString("/");
    }
}
