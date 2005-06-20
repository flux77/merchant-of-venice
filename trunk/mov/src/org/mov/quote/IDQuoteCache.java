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

import org.mov.util.TradingTime;

/**
 * This class contains all the intra-day stock quotes currently in memory. Its purpose is to
 * cache intra-day stock quotes so that tasks can share copies of the quotes rather than
 * keep duplicate copies.
 * <p>
 * Tasks should not directly call this class, but should go through a {@link IDQuoteBundle}.
 * <p>
 * When tasks access quotes in a quote cache, either directly or via a quote bundle they
 * can access quotes in two ways. The first way is specifying the actual time they
 * are interested in, i.e. a {@link TradingTime}. The other way is specifying a fast
 * access time offset. The fast access time offset is used when lots of quotes have
 * to be queried as fast as possible.
 * <p>
 * The earliest time in the cache has an offset of 0. The next trading time has an
 * offset of 1, the next 2, etc. This is different from the {@link EODQuoteCache} which
 * numbers the latest quote at 0. You can convert to and from fast access times
 * using {@link #timeToOffset} and {@link #offsetToTime}.
 *
 * @author Andrew Leppard
 * @see IDQuote
 * @see IDQuoteBundle
 */
public class IDQuoteCache {

    // Cache is organised as a map of symbols to arrays of quotes?

    // should it be organised this way?
    //    private Map cache;

    // Singleton instance of this class
    private static IDQuoteCache instance = null;

    private class IDQuoteCacheQuote {
        public float bid;
        public float ask;
        public float last;

        public IDQuoteCacheQuote(int bid, int ask, int last) {
            this.bid = bid;
            this.ask = ask;
            this.last = last;
        }

        public double getQuote(int quoteType) {
            switch(quoteType) {
            case(Quote.BID):
                return (double)bid;
            case(Quote.ASK):
                return (double)ask;
            case(Quote.LAST):
                return (double)last;
            default:
                assert false;
                return 0.0D;
            }
        }
    }

    // Class should only be constructed once by this class
    private IDQuoteCache() {
        //        cache = new HashMap();
    }

    /**
     * Create or return the singleton instance of the quote cache.
     *
     * @return  singleton instance of this class
     */
    public static synchronized IDQuoteCache getInstance() {
	if(instance == null)
	    instance = new IDQuoteCache();

        return instance;
    }
    
    /**
     * Get a quote from the cache.
     *
     * @param symbol    the symbol to load
     * @param quoteType the quote type, one of {@link Quote#BID}, {@link Quote#ASK} or
     *                  {@link Quote#LAST}
     * @param timeOffset fast access time offset
     * @return the quote
     * @exception QuoteNotLoadedException if the quote was not in the cache
     */
    public double getQuote(Symbol symbol, int quoteType, int timeOffset)
	throws QuoteNotLoadedException {
        
        // Get the quote cache quote for the given symbol + time
        IDQuoteCacheQuote quote = getQuoteCacheQuote(symbol, timeOffset);
        
        if(quote != null)
            return quote.getQuote(quoteType);
        else
            throw QuoteNotLoadedException.getInstance();
    }

    /**
     * Convert between a time and its fast access time offset.
     *
     * @param time the time
     * @return fast access time offset
     */
    public int timeToOffset(TradingTime time) {
        return 0;
    }

    /**
     * Convert between a fast access time offset and a time.
     *
     * @param timeOffset fast access time offset
     * @return the time
     */
    public TradingTime offsetToTime(int timeOffset) {
        return null;
    }

    // Returns the quote cache object for the given tim
    private IDQuoteCacheQuote getQuoteCacheQuote(Symbol symbol, int timeOffset)    
        throws QuoteNotLoadedException {

        return null;
    }
}