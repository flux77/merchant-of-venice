package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * An expression which represents the control flow of 
 * <code>if (x) {y} else {z}</code>.
 */
public class IfExpression extends TernaryExpression {

    /**
     * Construct an <code>if</code> expression.
     *
     * @param	arg1	the expression to be tested
     * @param	arg2	the expression to be executed if the test was 
     *			{@link #TRUE}
     * @param	arg2	the expression to be executed if the test was 
     *			{@link #FALSE}
     */
    public IfExpression(Expression arg1, 
			Expression arg2,
			Expression arg3) {
	super(arg1, arg2, arg3);
    }

    public float evaluate(QuoteCache cache, String symbol, int day) 
	throws EvaluationException {

	// if(...) then
	if(getArg(0).evaluate(cache, symbol, day) 
	   >= Expression.TRUE_LEVEL)
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

    /**
     * Check the input arguments to the expression. The first argument
     * must be {@link #BOOLEAN_TYPE}, the remaining arguments can be
     * any type but must be the same.      
     *
     * @return	the type of the second and third arguments
     */
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

