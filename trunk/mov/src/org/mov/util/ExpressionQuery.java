 package org.mov.util;

import java.awt.*;
import javax.swing.*;

import org.mov.parser.*;
import org.mov.ui.*;

/**
 * A dialog used for querying the user for an expression string.
 */
public class ExpressionQuery {

    private ExpressionQuery() {
	// Cannot instantiate this class
    }

    /**
     * Open a new <code>ExpressionQuery</code> dialog. Ask the user to
     * enter an expression string. Parse this string and check for validity,
     * if the string is not valid the user will be asked to enter a valid
     * string. 
     *
     * @param	parent	the parent desktop
     * @param	title	the title of the dialog
     * @return	the expression the user entered or <code>null</code>
     * if the user cancelled the dialog
     */
    public static String getExpression(JDesktopPane parent, String title,
				       String prompt) {
	return getExpression(parent, title, prompt, "");
    }

    /**
     * Open a new <code>ExpressionQuery</code> dialog. Ask the user to
     * enter an expression string. Parse this string and check for validity,
     * if the string is not valid the user will be asked to enter a valid
     * string. 
     *
     * @param	parent	the parent desktop
     * @param	title	the title of the dialog
     * @param	default	default expression string
     * @return	the expression the user entered or <code>null</code>
     * if the user cancelled the dialog
     */
    public static String getExpression(JDesktopPane parent, String title,
				       String prompt, String defaultExpression) {
	
	Expression expression = null;
	boolean invalidResponse;
	String expressionString = defaultExpression;
	Parser parser = new Parser();

	do {
	    // False unless shown otherwise
	    invalidResponse = false;

	    // Prompt user for expression
	    EquationDialog dlg = new EquationDialog(parent, prompt, title,
						    expressionString);
	    expressionString = dlg.showDialog();

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

	return expressionString;
    }
}
