package org.mov.parser;

import org.mov.util.*;

abstract public class UnaryExpression extends Expression {

    public UnaryExpression(Expression sub) {
	add(sub);
    }

    public int getNeededChildren() {
	return 1;
    }

    protected Expression getSub() {
	return (Expression)getChildAt(0);
    }
}
