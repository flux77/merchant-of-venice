package org.mov.parser;

import org.mov.util.*;

public class DayHighExpression extends TerminalExpression {

    public float evaluate(QuoteCache cache, String symbol, int day) {
	// shouldnt happen
	return 0.0F;
    }
 
    public String toString() {
	return "day_high";
    }

    public int checkType() throws TypeMismatchException {
	return QUOTE_TYPE;
    }
}
