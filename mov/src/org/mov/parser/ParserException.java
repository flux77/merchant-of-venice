package org.mov.parser;

/**
 * An exception which is thrown when there is a problem parsing an
 * expression.
 */
public class ParserException extends ExpressionException {

    /**
     * Create a new parser exception with the given parse error reason.
     *
     * @param	reason	the reason the parsing failed
     */
    public ParserException(String reason) {
	super(reason);
    }
}
