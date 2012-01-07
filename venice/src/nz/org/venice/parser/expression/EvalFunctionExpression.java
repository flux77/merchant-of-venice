/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package nz.org.venice.parser.expression;

import nz.org.venice.parser.Parser;
import nz.org.venice.parser.EvaluationException;
import nz.org.venice.parser.Expression;
import nz.org.venice.parser.TypeMismatchException;
import nz.org.venice.parser.AnalyserGuard;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.MissingQuoteException;
import nz.org.venice.quote.QuoteBundle;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.util.Locale;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;


/**
 * An expression which defines a user function.
 *
 * @author Mark Hummel
 */
public class EvalFunctionExpression extends UnaryExpression {

    private final String name; //The function name called
    private final int type;   //The value type which is returned.

    //Expression identifier so that AnalyserGuard can monitor the "call stack"
    //(parent hashcode uses class hashcode, so it would the same for all 
    //EvalFunctionExpressions)
    private UUID id;

    /**
     * Create a new average expression for the given <code>quote</code> kind,
     * for <code>lag</code> days away.
     *
     * @param	name	the name of the function which is called.
     * @param	type	the variable type that the function returns
     * @param  parameterList A clause expression which contains the value of the
     *         parameters passed to the function.
     */
    public EvalFunctionExpression(String name, int type, Expression parameterList) {
        super(parameterList);
	this.name = name;
	this.type = type;

	id = UUIDGenerator.getInstance().generateRandomBasedUUID();
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day)
	throws EvaluationException {
		
	AnalyserGuard.getInstance().startFunction(this, id, symbol, day);
	if (AnalyserGuard.getInstance().stackDepthLimitExceeded(this, id, symbol,day)) {
	    throw EvaluationException.STACK_DEPTH_EXCEEDED_EXCEPTION;
	}

	Variables parameters = null;

	try {
	    //Need actual copies of the variables otherwise
	    //function evaluation is not idemptotent
	    parameters = (Variables)(variables.copyVariables());

	    //setup the function parameters as new variables, 
	    //replacing variables already defined.
	    setupParameters(parameters, quoteBundle, symbol, day);
	} catch (CloneNotSupportedException e) {
	    
	}

	Expression body = getParseMetadata().getFunctionBody(name);
	double rv = body.evaluate(parameters, quoteBundle, symbol, day); 
	AnalyserGuard.getInstance().finishFunction(this, id, symbol, day);

	return rv;
    }

    public String toString() {
	Expression parameterList = getChild(0);
	
	//don't want to include the body here because of recursive functions
	return getType() + " " + getName() + "(" + parameterList.toString() + 
	    " " + ")" + "{body}";
    }

    public int checkType() throws TypeMismatchException {	
	if (checkParameters()) {
	    return getType();
	} else {
	    Expression parameterNamesList = getParseMetadata().getParameterNames(name);

	    Expression parameterValuesList = getChild(0);
	    String types = parameterValuesList.toString();
	    String expectedTypes =  parameterNamesList.toString();
	      
	    throw new TypeMismatchException(this, types, expectedTypes);
	}	
    }

    /** 
     * @return The name of the function called.
     */
    public String getName() {
	return name;
    }
    
    /** 
     * @return The variable type that the function returns.
     *
     */
    public int getType() {
	return type;
    }


    /**
     * @return a Clone of the EvalExpression object.
     */
    public Object clone() {
        return new EvalFunctionExpression(getName(), 
					  getType(),
					  (Expression)getChild(0).clone());
    }

    //Check that the types of the parameters defined match the types of the
    //values sent.
    private boolean checkParameters() {
	Expression parameterNamesList = getParseMetadata().getParameterNames(name);

	Expression parameterValuesList = getChild(0);

	assert parameterNamesList != null;

	//If no parameters are defined and sent, no checking is required.
	if (parameterValuesList.getChildCount() != parameterNamesList.getChildCount()) {
	    
	    //The only exception: 
	    //parameterValueLists are instances of ClauseExpression
	    //which must have at least one child, but functions don't need
	    //to have parameters	    
	    if (parameterValuesList.getChildCount() == 1 &&
		parameterNamesList.getChildCount() == 0) {
		return true;
	    }
	    return false;
	}

	for (int i = 0; i < parameterNamesList.getChildCount(); i++) {
	    DefineParameterExpression paramDef = (DefineParameterExpression)parameterNamesList.getChild(i);

	    Expression paramVal = parameterValuesList.getChild(i);
	    
	    if (paramDef.getType() != paramVal.getType()) {
		return false;
	    }
	}
	return true;
    }
    
    //Add/Replace variables into parameters and set their values
    private void setupParameters(Variables parameters, QuoteBundle quoteBundle, 
				 Symbol symbol, int day) throws EvaluationException {

	Expression parameterNamesList = getParseMetadata().getParameterNames(name);
	
	Expression parameterValuesList = getChild(0);
	
	//This should have been checked on parse, but extra checks to avoid
	//index out of bounds exceptions won't hurt.
	if (parameterValuesList.getChildCount() < parameterNamesList.getChildCount()) {
	    throw new EvaluationException(Locale.getString("PARAMETER_COUNT_MISMATCH"));		
	}
	       
	//No parameters sent
	if (parameterNamesList.getChildCount() == 0) {
	    return;
	}

	//Evaluate the parameter names expression so that the parameter
	//names are available to the function.
	//Can't use ClauseExpression.evaluate because it clones the 
	//variables and so they fall out of scope. 	
	for (int i = 0; i < parameterNamesList.getChildCount(); i++) {
	    DefineParameterExpression param = (DefineParameterExpression)parameterNamesList.getChild(i);	    
	    param.evaluate(parameters, quoteBundle, symbol, day);
	}	

	//Evaluate the parameter values and set the parameter values
	//with those returned.
	//Can't use ClauseExpression.evaluate because it clones the 
	//variables and so they fall out of scope.		
	for (int i = 0; i < parameterValuesList.getChildCount(); i++) {
	    DefineParameterExpression nameExpression = 
		(DefineParameterExpression)parameterNamesList.getChild(i);
	    
	    double value = parameterValuesList.getChild(i).evaluate(parameters, quoteBundle, symbol, day);	    	    
	       
	    parameters.setValue(nameExpression.getName(), value);
	}
    }
}
