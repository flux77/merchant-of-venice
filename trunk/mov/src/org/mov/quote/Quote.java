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

import org.mov.util.TradingDate;

/**
 * Representation of either an end-of-day or an intra-day stock quote. This
 * interface allows code to manipulate both quote kinds without writing
 * specific code to handle each.
 *
 * @author Andrew Leppard
 */
public interface Quote {

    /** Represents day close quote */
    public static final int DAY_CLOSE = 0;

    /** Represents day open quote */
    public static final int DAY_OPEN = 1;

    /** Represents day low quote */
    public static final int DAY_LOW = 2;

    /** Represents day high quote */
    public static final int DAY_HIGH = 3;

    /** Represents day volume quote */
    public static final int DAY_VOLUME = 4;

    /** Represents current bid. */
    public static final int BID = 5;

    /** Represents current ask. */
    public static final int ASK = 6;

    /** Represents last trade. */
    public static final int LAST = 7;

    /**
     * Return the stock's symbol.
     *
     * @return	the symbol
     */
    public Symbol getSymbol();

    /**
     * Return the quote date.
     *
     * @return	the date
     */
    public TradingDate getDate();

    /**
     * Get a single quote.
     *
     * @param	quote	the quote type: 
     *                  {@link #DAY_OPEN},
     *                  {@link #DAY_CLOSE},
     *                  {@link #DAY_HIGH},
     *                  {@link #DAY_LOW},
     *                  {@link #DAY_VOLUME},
     *                  {@link #BID},
     *                  {@link #ASK}, or
     *                  {@link #LAST}
     * @exception UnsupportedOperationException if the quote type is not
     *            supported by the quote. For example, end of day quotes
     *            do not contain bid or ask prices.
     */
    public double getQuote(int quote)
        throws UnsupportedOperationException;
}