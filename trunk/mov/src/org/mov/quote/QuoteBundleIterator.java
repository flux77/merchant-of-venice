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

import java.util.*;

import org.mov.util.*;

/**
 * Iterator for traversing Quotes in a QuoteBundle. This iterator allows the user to
 * traverse each quote in any quote bundle. The quotes will be pulled out in date then
 * symbol order. To get an interator for a QuoteBundle, use the {@link QuoteBundle#iterator}
 * method.
 *
 * @see Quote
 * @see QuoteBundle
 */
public class QuoteBundleIterator implements Iterator {

    private Iterator symbolsIterator;
    private QuoteBundle quoteBundle;

    private TradingDate nextDate;
    private String nextSymbol;

    private boolean isMore;

    /**
     * Create a new iterator over the given quote bundle.
     */
    public QuoteBundleIterator(QuoteBundle quoteBundle) {
        this.quoteBundle = quoteBundle;

        nextDate = quoteBundle.getFirstDate();

        Vector symbols = quoteBundle.getSymbols(nextDate);
        symbolsIterator = symbols.iterator();

        // go to next quote
        isMore = true;
        findNext();
    }

    // Locate the next quote in the bundle and set the flag isMore to
    // indicate whether they are anymore quotes.
    private void findNext() {

        // Make sure we haven't already decided there isn't more. There might
        // not be, but we assume there are.
        assert isMore;

        // Is there anymore symbols for this date?
        if(symbolsIterator.hasNext())
            nextSymbol = (String)symbolsIterator.next();

        // No, try the next date.. and the next date...
        else {
            nextDate = nextDate.next(1);

            while(nextDate.compareTo(quoteBundle.getLastDate()) <= 0) {
                Vector symbols = quoteBundle.getSymbols(nextDate);

                // Are there symbols in the cache?
                if(symbols.size() > 0) {
                    symbolsIterator = symbols.iterator();
                    return;
                }
                nextDate = nextDate.next(1);
            }
            isMore = false;
        }
    }

    /**
     * Return the next Quote in the QuoteBundle.
     *
     * @return quote the next quote
     */
    public Object next() {
        if(hasNext()) {
            int dateOffset;

            try {
                dateOffset = QuoteCache.getInstance().dateToOffset(nextDate);
            }
            catch(WeekendDateException e) {
                // hasNext() should have sorted this out
                assert false;
                return null;
            }

            try {
                Float volume =
                    new Float(quoteBundle.getQuote(nextSymbol, Quote.DAY_VOLUME, dateOffset));

                Quote quote =
                    new Quote(nextSymbol,
                              nextDate,
                              volume.intValue(),
                              quoteBundle.getQuote(nextSymbol, Quote.DAY_LOW, dateOffset),
                              quoteBundle.getQuote(nextSymbol, Quote.DAY_HIGH, dateOffset),
                              quoteBundle.getQuote(nextSymbol, Quote.DAY_OPEN, dateOffset),
                              quoteBundle.getQuote(nextSymbol, Quote.DAY_CLOSE, dateOffset));
                findNext();

                return (Object)quote;
            }
            catch(MissingQuoteException e) {
                // hasNext() should have sorted this out
                assert false;
                return null;
            }
        }
        else
            throw new NoSuchElementException();
    }

    /**
     * Return whether the QuoteBundle has anymore Quotes.
     *
     * @return whether there are anymore quotes
     */
    public boolean hasNext() {
        return isMore;
    }

    /**
     * Removing Quotes from the QuoteBundle is not supported.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}