package org.mov.parser;

import org.mov.util.*;
import org.mov.quote.*;

/**
 * A representation of the concept of <code>day close</code>.
 */
public class DayCloseExpression extends TerminalExpression {

    public float evaluate(QuoteCache cache, String symbol, int day) {
	// shouldnt happen
	return 0.0F;
    }
 
    public String toString() {
	return "day_close";
    }

    public int checkType() throws TypeMismatchException {
	return QUOTE_TYPE;
    }
}
