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
     * Return a vector of quotes for all quotes in the given quote range.
     *
     * @param	quoteRange	the range of quotes to load
     * @return	a vector of stock quotes
     * @see Quote
     * @see QuoteRange
     */
    public Vector loadQuoteRange(QuoteRange quoteRange);

    /** 
     * Return all the dates which we have quotes for (SLOW).
     *
     * @return	a vector of dates
     */
    public Vector getDates(); 

    /**
     * Is the given symbol a market index? 
     *
     * @param	symbol to test
     * @return	yes or no
     */
    public boolean isMarketIndex(String symbol);
}
