package org.mov.parser;

import org.mov.util.*;

abstract public class TernaryExpression extends Expression {

    public TernaryExpression(Expression arg1,
			     Expression arg2,
			     Expression arg3) {
	add(arg1);
	add(arg2);
	add(arg3);
    }

    public int getNeededChildren() {
	return 3;
    }

    protected Expression getArg(int arg) {
	return (Expression)getChildAt(arg);
    }
}
