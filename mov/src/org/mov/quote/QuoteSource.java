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

import org.mov.util.TradingDate;

/**
 * Provides a generic interface in which we can query stock quotes from
 * multiple sources. The source could either be directly from files,
 * a database, a unique internal format or from the internet.
 */
public interface QuoteSource {
    /**
     * Returns the company name associated with the given symbol.
     *
     * @param	symbol	the stock symbol
     * @return	the company name
     */
    public String getSymbolName(String symbol);

    /**
     * Returns the symbol associated with the given company.
     *
     * @param	symbol	a partial company name
     * @return	the company symbol
     */
    public String getSymbol(String partialSymbolName);

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for
     * @return	whether the symbol was found or not
     */
    public boolean symbolExists(String symbol);

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date
     */
    public TradingDate getLastDate();

    /**
     * Return the earliest date we have any stock quotes for.
     *
     * @return	the oldest quote date
     */
    public TradingDate getFirstDate();

    /**
     * Load the given quote range into the quote cache.
     *
     * @param	quoteRange	the range of quotes to load
     * @return  <code>TRUE</code> if the operation suceeded
     * @see Quote
     * @see QuoteCache
     */
    public boolean loadQuoteRange(QuoteRange quoteRange);

    /**
     * Returns whether the source contains any quotes for the given date.
     *
     * @param date the date
     * @return wehther the source contains the given date
     */
    public boolean containsDate(TradingDate date);

    /**
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public List getDates();

    /**
     * Is the given symbol a market index?
     *
     * @param	symbol to test
     * @return	yes or no
     */
    public boolean isMarketIndex(String symbol);

    /**
     * Return the advance/decline for the given date. This returns the number
     * of all ordinary stocks that rose (day close > day open) - the number of all
     * ordinary stocks that fell.
     *
     * @param date the date
     * @exception throws MissingQuoteException if the date wasn't in the source
     */
    public int getAdvanceDecline(TradingDate date)
        throws MissingQuoteException;
}
