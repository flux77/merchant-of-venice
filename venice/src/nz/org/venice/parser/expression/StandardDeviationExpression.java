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
import nz.org.venice.quote.QuoteBundle;
import nz.org.venice.quote.QuoteBundleFunctionSource;
import nz.org.venice.quote.QuoteFunctions;
import nz.org.venice.quote.Symbol;

/**
 * An expression which finds the standard deviation of a quote over a given trading period.
 *
 * @author Andrew Leppard
 */
public class StandardDeviationExpression extends TernaryExpression {
   
    /**
     * Create a new standard deviation expression for the given <code>quote</code> kind,
     * for the given number of <code>days</code> starting with 
     * <code>lag</code> days away.
     *
     * @param	quote	the quote kind to analyse
     * @param	days	the number of days to analyse
     * @param	lag	the offset from the current day
     */
    public StandardDeviationExpression(Expression quote, Expression days,
                                       Expression lag) {
	super(quote, days, lag);
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day) 
	throws EvaluationException {

        // Extract arguments
	int period = (int)getChild(1).evaluate(variables, quoteBundle, symbol, day);
        if(period <= 0)
            throw EvaluationException.SD_RANGE_EXCEPTION;
        int quoteKind = ((QuoteExpression)getChild(0)).getQuoteKind();
        int offset = (int)getChild(2).evaluate(variables, quoteBundle, symbol, day);
        if (offset > 0)
           throw EvaluationException.SD_OFFSET_EXCEPTION;

        // Calculate and return the standard deviation.
        QuoteBundleFunctionSource source =
            new QuoteBundleFunctionSource(quoteBundle, symbol, quoteKind, day, offset, period);

        return QuoteFunctions.sd(source, period);
    }

    public String toString() {
	return new String("sd(" + 
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

    public Object clone() {
        return new StandardDeviationExpression((Expression)getChild(0).clone(), 
                                               (Expression)getChild(1).clone(),
                                               (Expression)getChild(2).clone());
    }
}