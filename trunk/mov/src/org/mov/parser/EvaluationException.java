package org.mov.parser;

/**
 * An exception which is thrown when there is a problem executing an
 * expression.
 */
public class EvaluationException extends ExpressionException {

    /**
     * Create a new execution exception with the given execution error reason.
     *
     * @param	reason	the reason the execution failed
     */
    public EvaluationException(String reason) {
	super(reason);
    }

}
