package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * An expression which performs boolean <code>not</code> on the 
 * sub-expressions.
 */
public class NotExpression extends UnaryExpression {

    public NotExpression(Expression sub) {
	super(sub);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getSub().evaluate(cache, symbol, day) >= Expression.TRUE_LEVEL)
	    return FALSE;
	else
	    return TRUE;
    }

    public String toString() {
	return new String("not(" + getSub().toString() + ")");
    }

    public int checkType() throws TypeMismatchException {
	// sub type must be boolean
	if(getSub().checkType() == BOOLEAN_TYPE)
	    return BOOLEAN_TYPE;
	else
	    throw new TypeMismatchException();
    }

}
