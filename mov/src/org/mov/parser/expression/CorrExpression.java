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

package org.mov.parser.expression;

import org.mov.parser.Expression;
import org.mov.parser.EvaluationException;
import org.mov.parser.TypeMismatchException;
import org.mov.parser.Variables;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteFunctions;
import org.mov.quote.Symbol;
import org.mov.quote.SymbolFormatException;
import org.mov.util.Locale;

/**
 * An expression which finds the correleation between two stock quotes.
 */
public class CorrExpression extends QuaternaryExpression {
   
    /**
     * Create a new correlation expression between the current stock quote, and
     * the given quote. Find the correlation of the given quote kind. For
     * the given number of days, at the given offset.
     *
     * @param symbol    the symbol of the correlated stock
     * @param quote     the quote kind to correlate
     * @param days      the number of days to correlate over
     * @param lag       the offset from the current day
     */
    public CorrExpression(Expression symbol, Expression quote, Expression days,
                          Expression lag) {
	super(symbol, quote, days, lag);
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day) 
	throws EvaluationException {

        // Get and check arguments
        String correlatedSymbolString = ((StringExpression)getChild(0)).getText();
        int quoteKind = ((QuoteExpression)getChild(1)).getQuoteKind();
	int days = (int)getChild(2).evaluate(variables, quoteBundle, symbol, day);

        if(days <= 1)
            throw new EvaluationException(Locale.getString("CORR_RANGE_ERROR"));

        int offset = (int)getChild(3).evaluate(variables, quoteBundle, symbol, day);
        Symbol correlatedSymbol;

        // Convert string into symbol
        try {
            correlatedSymbol = Symbol.find(correlatedSymbolString);
        }
        catch(SymbolFormatException e) {
            throw new EvaluationException(e.getReason());
        }

        // Store the data into an array
        double[] values = new double[days];
        double[] correlatedValues = new double[days];

        int actualDays = 0;

        for(int i = 0; i < days; i++) {
            try {
                values[actualDays] = quoteBundle.getQuote(symbol, quoteKind, day, i - days + offset);
                correlatedValues[actualDays] = quoteBundle.getQuote(correlatedSymbol, quoteKind, 
                                                                    day, i - days + offset);
                actualDays++;
            }
            catch(MissingQuoteException e) {
                // nothing to do
            }
        }

        // If there is not enough data, assume there is no correlation.
        if(actualDays <= 1)
            return 0.0D;
        else
            return QuoteFunctions.corr(values, correlatedValues, 0, actualDays);
    }

    public String toString() {
	return new String("corr(" + 
			  getChild(0).toString() + ", " +
			  getChild(1).toString() + ", " +
			  getChild(2).toString() + ", " +
			  getChild(3).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {
	// First type must be string, then quote, then two integers.
	if(getChild(0).checkType() == STRING_TYPE &&
           (getChild(1).checkType() == FLOAT_QUOTE_TYPE ||
            getChild(1).checkType() == INTEGER_QUOTE_TYPE) &&
	   getChild(2).checkType() == INTEGER_TYPE &&
	   getChild(3).checkType() == INTEGER_TYPE)
	    return getType();
	else
	    throw new TypeMismatchException();
    }

    public int getType() {
        return FLOAT_TYPE;
    }

    public Object clone() {
        return new CorrExpression((Expression)getChild(0).clone(), 
                                  (Expression)getChild(1).clone(),
                                  (Expression)getChild(2).clone(),
                                  (Expression)getChild(3).clone());
    }
}
