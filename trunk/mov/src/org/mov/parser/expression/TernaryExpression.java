package org.mov.parser.expression;

import org.mov.parser.*;
import org.mov.util.*;

/**
 * Abstract base class for all expressions requiring three arguments.
 */
abstract public class TernaryExpression extends Expression {

    /**
     * Create a new ternary expression with the given three
     * arguments.
     *
     * @param	arg1	the first argument
     * @param	arg2	the second argument
     * @param	arg3	the third argument
     */
    public TernaryExpression(Expression arg1,
			     Expression arg2,
			     Expression arg3) {
	add(arg1);
	add(arg2);
	add(arg3);
    }

    /**
     * Return the number of children required in a ternary expression.
     * This will always evaluate to <code>3</code>.
     *
     * @return	<code>3</code>
     */
    public int getNeededChildren() {
	return 3;
    }

    /**
     * Return the given argument.
     *
     * @param	arg	the argument number
     * @return	the argument
     */
    protected Expression getArg(int arg) {
	return (Expression)getChildAt(arg);
    }
}
