package org.mov.parser.expression;

import org.mov.parser.*;

/**
 * Abstract base class for the comparision expressions:
 * <code>>, <, ==, !=, <=, >=</code>
 */
abstract public class ComparisionExpression extends BinaryExpression {

    /**
     * Create a new comparision expression with the given left and
     * right arguments.
     */
    public ComparisionExpression(Expression left, Expression right) {
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
	// left & right types must be the same and not boolean or quote
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();

	if(equivelantTypes(leftType, rightType) && leftType != BOOLEAN_TYPE 
	   && leftType != QUOTE_TYPE)
	    return BOOLEAN_TYPE;
	else
	    throw new TypeMismatchException();
    }
}
