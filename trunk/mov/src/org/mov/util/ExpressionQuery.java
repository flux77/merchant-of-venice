package org.mov.util;

import java.awt.*;
import javax.swing.*;

import org.mov.parser.*;

public class ExpressionQuery {

    public static Expression getExpression(JDesktopPane parent, String title,
					   String prompt) {
	
	Expression expression = null;
	boolean invalidResponse;
	String expressionString = null;
	Parser parser = new Parser();

	do {
	    // False unless shown otherwise
	    invalidResponse = false;

	    // Prompt user for expression
	    expressionString = 
		JOptionPane.
		showInternalInputDialog(parent, prompt, title,
					JOptionPane.QUESTION_MESSAGE); 


	    // Parse expression checking for type errors
	    if(expressionString != null && expressionString.length() > 0)

		try {
		    expression = parser.parse(expressionString);
		}
		catch(ExpressionException e) {
		    invalidResponse = true;
		    
		    // Tell user expression is wrong and try again
		    JOptionPane.
			showInternalMessageDialog(parent, 
						  e.getReason() + ": " +
						  expressionString,
						  "Error parsing expression",
						  JOptionPane.ERROR_MESSAGE);
		}
	    
	} while(invalidResponse);

	return expression;
    }
}
