package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * A representation of a value. 
 */
public class NumberExpression extends TerminalExpression {

    // The number we represent
    private float value;

    public NumberExpression(float value) {
	this.value = value;
    }

    public float evaluate(QuoteCache cache, String symbol, int day) {
	return value;
    }

    public String toString() {
	return Float.toString(value);
    }

    /**
     * A number expression is always a {@link #VALUE_TYPE}. 
     *
     * @return	{@link #VALUE_TYPE}
     */
    public int checkType() throws TypeMismatchException {
	return VALUE_TYPE;
    }
}
