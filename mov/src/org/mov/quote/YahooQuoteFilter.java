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
import org.mov.util.TradingDateFormatException;

/**
 * Provides a filter to parse the Yahoo stock quote format. This
 * format uses a date with the month name, prices are in dollars.
 * The first column is the date, then open, high, low, close &
 * volume. The final field is the adjusted close.
 *
 * Yahoo also provides a different format for intraday quotes and
 * also in some cases provides a stock symbol. We currently do
 * not support these.
 *
 * This filter is not hooked up to the QuoteFilterList because we
 * cannot currently ask the user to enter the missing symbol.
 *
 * Example:
 * <pre>
 * 16-Oct-03,38.75,39.15,38.22,38.70,307300,38.70
 * </pre>
 */

public class YahooQuoteFilter implements QuoteFilter {

    private Symbol symbol;

    /**
     * Creates an instance of the filter.
     */
    public YahooQuoteFilter(Symbol symbol) {
        this.symbol = symbol;
    }

    /**
     * Return the name of the filter.
     *
     * @return	the name of the filter.
     */
    public String getName() {
	return "Yahoo";
    }

    /**
     * Parse the given text string and returns the stock quote or null
     * if it did not contain a valid quote.
     *
     * @param	quoteLine	a single line of text containing a quote
     * @return	the stock quote
     */
    public Quote toQuote(String quoteLine) {
	Quote quote = null;

	if(quoteLine != null) {
	    String[] quoteParts = quoteLine.split(",");
	    int i = 0;
	    
	    if(quoteParts.length == 7) {
		TradingDate date = null;

                try {
                    date = new TradingDate(quoteParts[i++],
                                           TradingDate.US);
                }
                catch(TradingDateFormatException e) {
                    return null;
                    // Return null - couldn't parse quote
                }

                try {
                    float day_open = Float.parseFloat(quoteParts[i++]);
                    float day_high = Float.parseFloat(quoteParts[i++]);
                    float day_low = Float.parseFloat(quoteParts[i++]);
                    float day_close = Float.parseFloat(quoteParts[i++]);
                    int day_volume = Integer.parseInt(quoteParts[i++]);
                    // the remaining one is adjusted day close

                    quote = new Quote(symbol, date, day_volume, day_low, day_high,
                                      day_open, day_close);
                } 
                catch(NumberFormatException e) {
                    // Return null - couldn't parse quote
                }
            }
        }
        return quote;
    }

    /**
     * Convert the given stock quote to a string line.
     *
     * @param	quote	a stock quote
     * @return	string version of the quote
     */
    public String toString(Quote quote) {
        throw new UnsupportedOperationException();
    }
}
