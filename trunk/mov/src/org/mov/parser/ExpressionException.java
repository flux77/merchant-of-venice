package org.mov.parser;

import java.util.*;

/**
 * An exception which is thrown when there is a problem parsing or
 * executing an expression.
 */
public class ExpressionException extends java.lang.Throwable {
    
    private String reason = null;

    /**
     * Create a new expression exception with the given error reason.
     *
     * @param	reason	the reason the exception was thrown
     */
    public ExpressionException(String reason) {
	this.reason = reason;
    }

    /**
     * Return the reason this exception was thrown.
     *
     * @return	the reason the exception was thrown
     */
    public String getReason() {
	return reason;
    }

    /**
     * Convert the exception to a string
     *
     * @return	string version of the exception
     */
    public String toString() {
	return getReason();
    }

}
