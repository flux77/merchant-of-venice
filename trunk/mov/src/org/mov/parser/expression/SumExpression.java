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

/**
 * An expression which finds the sum of quotes over a given trading period.
 */
public class SumExpression extends QuoteExpression {
   
    /**
     * Create a new sum expression for the given <code>quote</code> kind,
     * for the given number of <code>days</code> starting with 
     * <code>lag</code> days away.
     *
     * @param	quote	the quote kind to sum
     * @param	days	the number of days to sum over
     * @param	lag	the offset from the current day
     */
    public SumExpression(Expression quote, Expression days,
			 Expression lag) {
	super(quote);

        assert quote != null && days != null && lag != null;

	add(quote);
	add(days);
	add(lag);
    }

    public float evaluate(Variables variables, QuoteBundle quoteBundle, String symbol, int day) 
	throws EvaluationException {
	
	int days = (int)get(1).evaluate(variables, quoteBundle, symbol, day);

        if(days == 0)
            throw new EvaluationException("Sum value over 0 days");

	int lastDay = day + (int)get(2).evaluate(variables, quoteBundle, symbol, day);

	return sum(quoteBundle, symbol, getQuoteKind(), days, lastDay);
    }

    public String toString() {
	return new String("sum(" + 
			  get(0).toString() + ", " +
			  get(1).toString() + ", " +
			  get(2).toString() + ")");
    }

    public int checkType() throws TypeMismatchException {

	// First type must be quote, second and third types must be value
	if(get(0).checkType() == QUOTE_TYPE &&
	   get(1).checkType() == INTEGER_TYPE &&
	   get(2).checkType() == INTEGER_TYPE)
	    return getType();
	else
	    throw new TypeMismatchException();
    }

    public int getNeededChildren() {
	return 3;
    }

    /** 
     * Sum the stock quotes for a given symbol in a given range. 
     *
     * @param	quoteBundle	the quote bundle to read the quotes from.
     * @param	symbol	the symbol to use.
     * @param	quote	the quote type we are interested in, e.g. DAY_OPEN.
     * @param	lastDay	fast access date offset in cache.
     * @return	sum of stock quotes.
     */
    static public float sum(QuoteBundle quoteBundle, String symbol, 
			    int quote, int days, int lastDay) {
	float sum = 0.0F;

	// Sum quotes
	for(int i = lastDay - days + 1; i <= lastDay; i++) {
            try {
                sum += quoteBundle.getQuote(symbol, quote, i);
            }
            catch(MissingQuoteException e) {
                // nothing to do
            }
        }       

	return sum;
    }

    public Object clone() {
        return new SumExpression((Expression)get(0).clone(), 
                                 (Expression)get(1).clone(),
                                 (Expression)get(2).clone());
    }
}

