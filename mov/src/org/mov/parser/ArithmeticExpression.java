package org.mov.parser;

// This class is used for the boolean expressions: +, -, *, /

import org.mov.util.*;

abstract public class ArithmeticExpression extends BinaryExpression {

    public ArithmeticExpression(Expression left, Expression right) {
	super(left, right);
    }

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
