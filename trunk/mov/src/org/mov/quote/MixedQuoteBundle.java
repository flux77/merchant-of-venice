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
public class MixedQuoteBundle implements QuoteBundle {

    // Contains the end of day quotes
    private EODQuoteBundle eodQuoteBundle;

    // Contains the intra-day quotes
    private IDQuoteBundle idQuoteBundle;
    
    /**
     * Create a new mixed quote bundle that contains the end of day quotes from
     * between the two dates given and the current day's intra-day quotes.
     * The quote bundle should be given at least two days of end of day quotes to
     * properly calculate quote change, in case the intra-day quotes are not
     * available.
     *
     * @param symbols the symbols to load
     * @param firstDate the first end of day quotes to load
     * @param lastDate the last end of day quotes to load
     */
    public MixedQuoteBundle(List symbols, TradingDate firstDate, TradingDate lastDate) {
        eodQuoteBundle = new EODQuoteBundle(new EODQuoteRange(symbols, firstDate, lastDate));
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
            return idQuoteBundle.offsetToDate(0);
        else
            return eodQuoteBundle.offsetToDate(dateOffset);
    }

    // TODO
    // Add these to QuoteBundle.java
    // And EODQuoteCache
    // And IDQuoteCache?
    // apparently EODQuoteBundle already has getLastDateOffset(). So just rename it?

    // gets a getOffset(Quote quote) method?? but what about ID quotes?

    public int getFirstOffset() {
        return eodQuoteBundle.getFirstDateOffset();
    }

    public int getLastOffset() {
        if(useIDQuotes())
            return eodQuoteBundle.getLastDateOffset() + 1;
        else
            return eodQuoteBundle.getLastDateOffset();
    }

    /**
     * Return whether we should display intra-day quotes or end of day quotes.
     * This decision will be made solely on the basis of whether the intra-day quote
     * sync is running. If we are not automatically downloading intra-day quotes,
     * then display end of day quotes.
     *
     * @return <code>true</code> if we should display intra-day quotes.
     */
    private boolean useIDQuotes() {
        return IDQuoteSync.getInstance().isRunning();
    }

    /**
     * Retrieve the fast access offset from the given quote.
     *
     * @param quote quote
     * @return fast access offset
     * @exception WeekendDateException if the date falls on a weekend.
     */
    public int getOffset(Quote quote) throws WeekendDateException {
        if(quote.getDate().after(eodQuoteBundle.getLastDate()))
            return eodQuoteBundle.getLastDateOffset() + 1;
        else
            return eodQuoteBundle.getOffset(quote);
    }
}
