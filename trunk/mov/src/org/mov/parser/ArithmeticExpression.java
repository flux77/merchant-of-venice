package org.mov.parser;

import org.mov.util.*;

/**
 * Abstract base class for the arithmetic expressions:
 * <code>+, /, *, -</code>
 */
abstract public class ArithmeticExpression extends BinaryExpression {

    /**
     * Create a new arithmetic expression with the given left and
     * right arguments.
     */
    public ArithmeticExpression(Expression left, Expression right) {
	super(left, right);
    }

    /**
     * Check the input arguments to the expression. They can be any type
     * except {@link #BOOLEAN_TYPE} and {@link #QUOTE_TYPE} and
     * they must match.
     *
     * @return	the type of the left expression
     */
    public int checkType() throws TypeMismatchException {
	// Types must be the same and not boolean or quote
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();

	if(equivelantTypes(leftType, rightType) && leftType != BOOLEAN_TYPE
	   && leftType != QUOTE_TYPE) 
	    return leftType;
	else
	    throw new TypeMismatchException();
    }

}
