package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

public class NotExpression extends UnaryExpression {

    public NotExpression(Expression sub) {
	super(sub);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	if(getSub().evaluate(cache, symbol, day) >= LogicExpression.TRUE_LEVEL)
	    return 0.0F;
	else
	    return 1.0F;
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
