package org.mov.parser;

import java.util.*;

public class ExpressionException extends java.lang.Throwable {
    
    private String reason = null;

    public ExpressionException(String reason) {
	this.reason = reason;
    }

    public String getReason() {
	return reason;
    }

}
