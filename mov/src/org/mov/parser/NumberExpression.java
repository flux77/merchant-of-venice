package org.mov.parser;

import javax.swing.tree.*;

import org.mov.util.*;
import org.mov.quote.*;

public class NumberExpression extends TerminalExpression {

    int value;

    public NumberExpression(int value) {
	this.value = value;
    }

    public float evaluate(QuoteCache cache, String symbol, int day) {
	return (float)value;
    }

    public String toString() {
	return Integer.toString(value);
    }

    public int checkType() throws TypeMismatchException {
	return VALUE_TYPE;
    }
}
