package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * A representation of the concept of <code>day open</code>.
 */
public class DayOpenExpression extends TerminalExpression {

    public float evaluate(QuoteCache cache, String symbol, int day) {
	// shouldnt happen
	return 0.0F;
    }
 
    public String toString() {
	return "day_open";
    }

    public int checkType() throws TypeMismatchException {
	return QUOTE_TYPE;
    }
}
