package org.mov.parser.expression;

import org.mov.parser.*;
import org.mov.util.*;

/**
 * Abstract base class for all expressions requiring a single argument.
 */
abstract public class UnaryExpression extends Expression {

    /**
     * Create a new unary expression with the given argument.
     *
     * @param	sub	the sub argument
     */
    public UnaryExpression(Expression sub) {
	add(sub);
    }

    /**
     * Return the number of children required in a unary expression.
     * This will always evaluate to <code>1</code>.
     *
     * @return	<code>1</code>
     */
    public int getNeededChildren() {
	return 1;
    }

    /**
     * Return the sub expression.
     *
     * @return	the single argument
     */
    protected Expression getSub() {
	return (Expression)getChildAt(0);
    }
}
