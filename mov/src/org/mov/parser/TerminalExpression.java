package org.mov.parser;

import org.mov.util.*;

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
