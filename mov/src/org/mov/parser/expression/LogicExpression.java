package org.mov.parser.expression;

import org.mov.parser.*;

/**
 * Abstract base class for the boolean expressions:
 * <code>and, or</code>
 */
abstract public class LogicExpression extends BinaryExpression {

    /**
     * Create a new logic expression with the given left and
     * right arguments.
     */
    public LogicExpression(Expression left, Expression right) {
	super(left, right);
    }

    /**
     * Check the input arguments to the expression. They must both be
     * {@link #BOOLEAN_TYPE}.
     *
     * @return	{@link #BOOLEAN_TYPE}
     */
    public int checkType() throws TypeMismatchException {
	// both types must be boolean
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();

	if(leftType == BOOLEAN_TYPE && rightType == BOOLEAN_TYPE)
	    return BOOLEAN_TYPE;
	else
	    throw new TypeMismatchException();
    }
}

