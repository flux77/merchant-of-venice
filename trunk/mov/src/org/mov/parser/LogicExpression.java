package org.mov.parser;

// This class is used for the boolean expressons: and, or

abstract public class LogicExpression extends BinaryExpression {

    // Level at which double is registered as "true"
    public final static double TRUE_LEVEL = 0.0001;

    // Levels to set true and false
    public final static double TRUE = 1.0;
    public final static double FALSE = 0.0;

    public LogicExpression(Expression left, Expression right) {
	super(left, right);
    }

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

