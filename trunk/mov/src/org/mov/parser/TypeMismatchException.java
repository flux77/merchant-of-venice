package org.mov.parser;

/**
 * An exception which is thrown when there is a type mismatch error
 * when executing an expression. A type mismatch error is when
 * an incorrect type was supplied. For example if the expression required
 * a <code>BOOLEAN_TYPE</code> and a <code>VALUE_TYPE</code> was given
 * instead.
 * @see Expression
 */
public class TypeMismatchException extends ExpressionException {

    /**
     * Create a new type mistmatch exception.
     */
    public TypeMismatchException() {
	super("type mismatch");
    }

}
