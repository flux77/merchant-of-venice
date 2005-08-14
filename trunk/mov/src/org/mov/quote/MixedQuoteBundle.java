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

import java.util.Iterator;
import java.util.List;

import org.mov.parser.EvaluationException;
import org.mov.util.TradingDate;

// TODO: Doc

// what do we do if it isn't enabled? if its disabled, i want the day close difference etc
// to be properly displayed. This won't do that... maybe we need to check the id quote bundle
// and if it's availabe use it, otherwise shift the quotes down. That way we don't have
// to make up a half-arsed quote. When the quotes come in, the bundle "shifts".

// how can I do tht without stuffing things up?
public class MixedQuoteBundle implements QuoteBundle {

    private EODQuoteBundle eodQuoteBundle;
    private IDQuoteBundle idQuoteBundle;
    private IDQuoteCache idQuoteCache;
    private boolean isIDAvailable;
    
    public MixedQuoteBundle(List symbols) {
        idQuoteCache = IDQuoteCache.getInstance();
        isIDAvailable = false;

        eodQuoteBundle = new EODQuoteBundle(new EODQuoteRange(symbols));
        idQuoteBundle = new IDQuoteBundle(symbols);
    }

    public double getQuote(Symbol symbol, int quoteType, int today, int dateOffset)
	throws EvaluationException, MissingQuoteException {

        return getQuote(symbol, quoteType, today + dateOffset);
    }

    public double getQuote(Symbol symbol, int quoteType, int dateOffset)
	throws MissingQuoteException {

        if(dateOffset > eodQuoteBundle.getLastDateOffset())
            return idQuoteBundle.getQuote(symbol, quoteType, dateOffset);
        else
            return eodQuoteBundle.getQuote(symbol, quoteType, dateOffset);
    }

    public TradingDate offsetToDate(int dateOffset) {
        if(dateOffset > eodQuoteBundle.getLastDateOffset())
            return idQuoteBundle.offsetToDate(dateOffset);
        else
            return eodQuoteBundle.offsetToDate(dateOffset);
    }

    // TODO
    // Add these to QuoteBundle.java
    // And EODQuoteCache
    // And IDQuoteCache?
    // apparently EODQuoteBundle already has getLastDateOffset(). So just rename it?

    public int getFirstOffset() {
        return eodQuoteBundle.getFirstDateOffset();
    }

    public int getLastOffset() {
        if(isIDAvailable())
            return eodQuoteBundle.getLastDateOffset() + 1;
        else
            return eodQuoteBundle.getLastDateOffset();
    }

    public int getNextOffset(int offset) {
        return ++offset;
    }

    public int getLastOffset(int offset) {
        return --offset;
    }

    // returns if there are any ID quotes available for the symbols we are interested in.
    private boolean isIDAvailable() {
        if(!isIDAvailable) {
            List symbols = eodQuoteBundle.getQuoteRange().getAllSymbols();

            // If any of the symbols are present in the intra-day quote cahce, then
            // we mark intra-day quotes as available.
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();

                try {
                    double value = idQuoteCache.getQuote(symbol, Quote.DAY_CLOSE, 0);

                    // The quote cache has the quote so ID quotes are available
                    isIDAvailable = true;
                    break;
                }
                catch(QuoteNotLoadedException e) {
                    // quote not available - try next symbol
                }
            }
        }

        return isIDAvailable;
    }
}