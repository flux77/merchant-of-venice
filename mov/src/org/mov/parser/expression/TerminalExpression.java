package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;

/**
 * Abstract base class for all expressions requiring no arguments.
 */
abstract public class TerminalExpression extends Expression {

    /**
     * Create a new terminal expression.
     */
    public TerminalExpression() {
	// nothing to do
    }

    /**
     * Return the number of children required in a terminal expression.
     * This will always evaluate to <code>0</code>.
     *
     * @return	<code>0</code>
     */
    public int getNeededChildren() {
	return 0;
    }
}
