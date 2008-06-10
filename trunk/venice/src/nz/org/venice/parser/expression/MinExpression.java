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

import nz.org.venice.parser.Expression;
import nz.org.venice.parser.EvaluationException;
import nz.org.venice.parser.TypeMismatchException;
import nz.org.venice.parser.Variables;
import nz.org.venice.quote.MissingQuoteException;
import nz.org.venice.quote.QuoteBundle;
import nz.org.venice.quote.Symbol;

/**
 * An expression which finds the minimum quote over a given trading period.
 *
 * @author Andrew Leppard
 */
public class MinExpression extends TernaryExpression {

    /**
     * Create a new minimum expression for the given <code>quote</code> kind,
     * for the given number of <code>days</code> starting with 
     * <code>lag</code> days away.
     *
     * @param	quote	the quote kind to find the minimum
     * @param	days	the number of days to search
     * @param	lag	the offset from the current day
     */
    public MinExpression(Expression quote, Expression days,
			 Expression lag) {
	super(quote, days, lag);
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day) 
	throws EvaluationException {

	int days = (int)getChild(1).evaluate(variables, quoteBundle, symbol, day);
        if(days <= 0)
            throw EvaluationException.MIN_RANGE_EXCEPTION;
        int quoteKind = ((QuoteExpression)getChild(0)).getQuoteKind();
        int offset = (int)getChild(2).evaluate(variables, quoteBundle, symbol, day);
        if (offset > 0)
           throw EvaluationException.MIN_OFFSET_EXCEPTION;
        
	return min(quoteBundle, symbol, quoteKind, days, day, offset);
    }

    public String toString() {
	return new String("min(" + 
			  getChild(0).toString() + ", " +
			  getChild(1).toString() + ", " +
			  getChild(2).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {
	// First type must be quote, second and third types must be value
	if((getChild(0).checkType() == FLOAT_QUOTE_TYPE ||
            getChild(0).checkType() == INTEGER_QUOTE_TYPE) &&
	   getChild(1).checkType() == INTEGER_TYPE &&
	   getChild(2).checkType() == INTEGER_TYPE)
	    return getType();
	else
	    throw new TypeMismatchException();
    }

    public int getType() {
        if(getChild(0).getType() == FLOAT_QUOTE_TYPE)
            return FLOAT_TYPE;
        else {
            assert getChild(0).getType() == INTEGER_QUOTE_TYPE;

           return INTEGER_TYPE;
        }
    }

    private double min(QuoteBundle quoteBundle, Symbol symbol, 
                      int quote, int days, int day, int offset)
        throws EvaluationException {

	double min = Double.MAX_VALUE;
	boolean setValue = false;
	boolean missingQuotes = false;

	for(int i = offset - days + 1; i <= offset; i++) {
            try {
                double value = quoteBundle.getQuote(symbol, quote, day, i);

                if(value < min)
                    min = value;

		setValue = true;
            }
            catch(MissingQuoteException e) {
		missingQuotes = true;
                // nothing to do
            }
	}
	
	/* 
	   Returning Double.MAX_VALUE here causes offset to overflow
	   when the parent is a LagExpression. Consequently, offset becomes
	   a positive value which triggers assertions which halt analyser 
	   processes. 
	 */	   
	if (!setValue && missingQuotes) {
	    throw EvaluationException.UNDEFINED_RESULT_EXCEPTION;
	}

	return min;
    }

    public Object clone() {
        return new MinExpression((Expression)getChild(0).clone(), 
                                 (Expression)getChild(1).clone(),
                                 (Expression)getChild(2).clone());
    }
}
