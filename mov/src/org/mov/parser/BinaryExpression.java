package org.mov.parser;

import org.mov.util.*;

abstract public class BinaryExpression extends Expression {

    public BinaryExpression(Expression left,
			    Expression right) {
	add(left);
	add(right);
    }

    public int getNeededChildren() {
	return 2;
    }

    protected String toString(String operator) {
	String string = "";

	if(getLeft().getNeededChildren() < 2)
	    string += getLeft().toString();
	else
	    string += "(" + getLeft().toString() + ")";
	
	string += operator;
	
	if(getRight().getNeededChildren() < 2)
	    string += getRight().toString();
	else
	    string += "(" + getRight().toString() + ")";

	return string;
    }

    protected Expression getLeft() {
	return (Expression)getChildAt(0);
    }

    protected Expression getRight() {
	return (Expression)getChildAt(1);
    }
}
