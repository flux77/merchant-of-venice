package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

public class IfExpression extends TernaryExpression {

    public IfExpression(Expression arg1, 
			Expression arg2,
			Expression arg3) {
	super(arg1, arg2, arg3);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	// if(...) then
	if(getArg(0).evaluate(cache, symbol, day) 
	   >= LogicExpression.TRUE_LEVEL)
	    return getArg(1).evaluate(cache, symbol, day);
	// else
	else
	    return getArg(2).evaluate(cache, symbol, day);
    }

    public String toString() {
	return new String("if(" + getArg(0).toString() + ") {" +
			  getArg(1).toString() + "} else {" +
			  getArg(2).toString() + "} ");
    }

    public int checkType() throws TypeMismatchException {
	// if(arg0) { arg1 } else { arg2} 
	// then type of arg1 should be the same of arg2
	// arg0 must be boolean
	int arg0type = getArg(0).checkType();
	int arg1type = getArg(1).checkType();
	int arg2type = getArg(2).checkType();

	if(arg0type == BOOLEAN_TYPE &&
	   arg1type == arg2type)
	    return arg1type;
	else
	    throw new TypeMismatchException();
    }
}

