package org.mov.parser;

// This class is used for the comparision expressons: >, <, =, <=, >=

abstract public class ComparisionExpression extends BinaryExpression {

    public ComparisionExpression(Expression left, Expression right) {
	super(left, right);
    }

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
