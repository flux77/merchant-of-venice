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

import org.mov.parser.EvaluationException;
import org.mov.parser.Expression;
import org.mov.parser.TypeMismatchException;
import org.mov.parser.Variables;
import org.mov.prefs.PreferencesManager;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.Quote;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteFunctions;
import org.mov.quote.Symbol;

/**
 * An expression which finds the RSI over a given trading period.
 */
public class RSIExpression extends BinaryExpression {

    public RSIExpression(Expression days, Expression lag) {
        super(days, lag);
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day)
	throws EvaluationException {
	
	int days = (int)getChild(0).evaluate(variables, quoteBundle, symbol, day);

        int maximumYears = PreferencesManager.loadMaximumYears();       
        if(days <= 0)
            throw EvaluationException.rangeForRSI();
        if (days>maximumYears*365)
            throw EvaluationException.pastDate();

        int offset = (int)getChild(1).evaluate(variables, quoteBundle, symbol, day);

        if ((offset<=-maximumYears*365) || (offset>maximumYears*365))
            throw EvaluationException.pastDate();

        // To calculate an X day RSI we need X + 1 days of quotes. Put them in
        // an array so we can use the RSI function in quote functions.
        double[] values = new double[days + 1];
        int actualDays = 0;

        for(int i = 0; i <= days; i++) {
            try {
                values[actualDays] = quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, day,
                                                          i - days + offset);
                actualDays++;
            }
            catch(MissingQuoteException e) {
                // nothing to do
            }
        }

        // If we don't have enough quotes then return a neutral value
        if(actualDays <= 1)
            return 50.0D;
        else
            return QuoteFunctions.rsi(values, 1, actualDays);
    }

    public String toString() {
        Expression periodExpression = getChild(0);
        Expression lagExpression = getChild(1);

        return new String("rsi(" +
                          periodExpression.toString() + ", " +
                          lagExpression.toString() + ")");
    }

    public int checkType() throws TypeMismatchException {
	if(getChild(0).checkType() == INTEGER_TYPE &&
	   getChild(1).checkType() == INTEGER_TYPE)
	    return FLOAT_TYPE;
	else
	    throw new TypeMismatchException();
    }

    /**
     * Get the type of the expression.
     *
     * @return {@link #FLOAT_TYPE}.
     */
    public int getType() {
        return FLOAT_TYPE;
    }

    public Object clone() {
        return new RSIExpression((Expression)getChild(0).clone(),
                                 (Expression)getChild(1).clone());
    }
}

