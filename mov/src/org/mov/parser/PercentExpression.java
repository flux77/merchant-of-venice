package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

public class PercentExpression extends BinaryExpression {

    public PercentExpression(Expression left, Expression right) {
	super(left, right);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	return getLeft().evaluate(cache, symbol, day) *
	    (getRight().evaluate(cache, symbol, day) / 100);
    }

    public String toString() {
	return new String("percent(" + getLeft().toString() + ", " +
			  getRight().toString() + ")");
    }

    public int checkType() throws TypeMismatchException {
	// returned type is type of first arg
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();
	
	// types may be anything but boolean and do not need to match
	if(leftType != BOOLEAN_TYPE && rightType != BOOLEAN_TYPE)
	    return leftType;
	else
	    throw new TypeMismatchException();
    }
}

