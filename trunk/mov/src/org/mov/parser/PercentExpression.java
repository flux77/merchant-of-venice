package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which returns the given percent of a value.
 */
public class PercentExpression extends BinaryExpression {

    /**
     * Create a new percent expression.
     *
     * @param	left	calculate the percent of this number
     * @param	right	the percent value to calculate
     */
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

    /** 
     * The value we are calculating the percent for can be any type except
     * {@link #BOOLEAN_TYPE} or {@link #QUOTE_TYPE}. The percent value we
     * are calculating must be a {@link #VALUE_TYPE}. The type returned 
     * will be the same type as the value we are calculating the percent for.
     *
     * @return	the type of the first argument
     */
    public int checkType() throws TypeMismatchException {
	// returned type is type of first arg
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();
	
	if(leftType != BOOLEAN_TYPE && leftType != QUOTE_TYPE &&
	   rightType == VALUE_TYPE)
	    return leftType;
	else
	    throw new TypeMismatchException();
    }
}

