package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which performs boolean <code>or</code> on two sub-expressions.
 */
public class OrExpression extends LogicExpression {

    public OrExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getLeft().evaluate(cache, symbol, day) >= TRUE_LEVEL ||
	   getRight().evaluate(cache, symbol, day) >= TRUE_LEVEL)
	    return 1.0F;
	else
	    return 0.0F;
    }

    public String toString() {
	return super.toString("or");
    }
}

