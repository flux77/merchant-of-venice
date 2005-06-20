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

package org.mov.quote;

import java.util.List;

import org.mov.parser.EvaluationException;
import org.mov.util.TradingDate;

/**
 * When a task requires intra-day stock quotes, it should create an instance of this class
 * which represents all the task's required quotes. The task can then access quotes
 * from this class, which in turn reads its stock quotes from the global intra-day quote
 * cache - {@link IDQuoteCache}.
 * <p>
 * Example:
 * <pre>
 *      IDQuoteBundle quoteBundle = new IDQuoteBundle("CBA");
 *      try {
 *	    double = quoteBundle.getQuote("CBA", Quote.ASK, 0);
 *      }
 *      catch(QuoteNotLoadedException e) {
 *          //...
 *      }
 * </pre>
 *
 * @author Andrew Leppard
 * @see IDQuote
 * @see IDQuoteCache
 * @see EODQuoteBundle
 * @see Symbol
 */
public class IDQuoteBundle implements QuoteBundle {
    
    private TradingDate date;
    private List symbols;
    private IDQuoteCache quoteCache;

    /**
     * Create a new intra-day quote bundle containing all today's quotes for
     * the given symbols.
     *
     * @param symbols the quote symbols
     */
    public IDQuoteBundle(List symbols) {
        this.symbols = symbols;
    }

    public double getQuote(Symbol symbol, int quoteType, int now, int offset)
	throws EvaluationException, MissingQuoteException {

        return 0.0D;
    }

    public double getQuote(Symbol symbol, int quoteType, int offset)
	throws MissingQuoteException {
        
        return 0.0D;
    }

    public TradingDate offsetToDate(int offset) {
        // The entire quote bundle is on the same date
        return date;
    }
}