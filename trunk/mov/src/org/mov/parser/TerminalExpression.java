package org.mov.parser;

import org.mov.util.*;

abstract public class TerminalExpression extends Expression {

    public int getNeededChildren() {
	return 0;
    }
}
