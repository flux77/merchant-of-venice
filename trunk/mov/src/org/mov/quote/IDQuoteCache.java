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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.mov.util.TradingDate;
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

    // Cache is organised by a list of hashmaps, each hashmap
    // corresponds to a trading day. The hashmap's keys are stock symbols.
    private List cache;

    // Keep list of times in cache
    private List times;

    // Number of quotes in cache
    private int size = 0;

    // Date of all quotes in cache
    private TradingDate date;

    // Singleton instance of this class
    private static IDQuoteCache instance = null;

    // Listeners to be notified when new intra-day quotes arrive
    private EventListenerList quoteListeners;

    private class IDQuoteCacheQuote {
        // Floats have more than enough precision to hold quotes. So we
        // store them as floats rather than doubles to reduce memory.
        public float day_low;
        public float day_high;
        public float day_open;
        public float day_close;
        public int day_volume;
        public float bid;
        public float ask;

        public IDQuoteCacheQuote(int day_volume, float day_low, float day_high,
                                 float day_open, float day_close, float bid,
                                 float ask) {
            this.day_volume = day_volume;
            this.day_low = day_low;
            this.day_high = day_high;
            this.day_open = day_open;
            this.day_close = day_close;
            this.bid = bid;
            this.ask = ask;
        }

        public double getQuote(int quote) {
            switch(quote) {
            case(Quote.DAY_OPEN):
                return (double)day_open;
            case(Quote.DAY_CLOSE):
                return (double)day_close;
            case(Quote.DAY_LOW):
                return (double)day_low;
            case(Quote.DAY_HIGH):
                return (double)day_high;
            case(Quote.DAY_VOLUME):
                return (double)day_volume;
            case(Quote.BID):
                return (double)bid;
            case(Quote.ASK):
                return (double)ask;
            default:
                assert false;
                return 0.0D;
            }
        }

        public boolean equals(int day_volume, float day_low, float day_high,
                              float day_open, float day_close, float bid,
                              float ask) {
            return (day_volume == this.day_volume &&
                    day_low == this.day_low &&
                    day_high == this.day_high &&
                    day_open == this.day_open &&
                    day_close == this.day_close &&
                    bid == this.bid &&
                    ask == this.ask);
        }
    }

    // Class should only be constructed once by this class
    private IDQuoteCache() {
        cache = new ArrayList();
        times = new ArrayList();

        //        TradingTime lastTime = QuoteSourceManager.getSource().getLastTime();

        //if(lastTime != null)
        //    addTime(lastTime);

        quoteListeners = new EventListenerList();
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
     * Returns whether this class has been instantiated yet. This is
     * used by the tuning page, which needs to know the number of
     * quotes in the cache. But it doesn't want to be the first
     * to instantiate the cache, because that would cause it to
     * access the quote source.. which might not be set up at that
     * stage.
     *
     * @return <code>true</code> if this class has been instantiated.
     */
    public static boolean isInstantiated() {
        return (instance != null);
    }

    /**
     * Get a quote from the cache.
//      *
//      * @param symbol    the symbol to load
//      * @param quoteType the quote type, one of {@link Quote#DAY_OPEN}, {@link Quote#DAY_CLOSE},
//      *                  {@link Quote#DAY_LOW}, {@link Quote#DAY_HIGH}, {@link Quote#DAY_VOLUME},
//      *                  {@link Quote#BID}, {@link Quote#ASK}.
//      * @param timeOffset fast access time offset
//      * @return the quote
//      * @exception QuoteNotLoadedException if the quote was not in the cache
//      */
//     public double getQuote(Symbol symbol, int quoteType, int timeOffset)
// 	throws QuoteNotLoadedException {

// 	// Get the quote cache quote for the given symbol + time
// 	IDQuoteCacheQuote quote = getQuoteCacheQuote(symbol, timeOffset);

// 	if(quote != null)
//             return quote.getQuote(quoteType);
//         else
// 	    throw QuoteNotLoadedException.getInstance();
//     }

//     // Returns the quote cache object for the given time
//     private IDQuoteCacheQuote getQuoteCacheQuote(Symbol symbol, int timeOffset)
//         throws QuoteNotLoadedException {

// 	// First get the hash map for the given time
// 	HashMap symbols = getQuotesForTime(timeOffset);
// 	assert symbols != null;

// 	// Second get the quote for the given symbol on the given time
// 	return  (IDQuoteCacheQuote)symbols.get(symbol);
//     }

//     // Returns a HashMap containing quotes for that time
//     private HashMap getQuotesForTime(int timeOffset)
// 	throws QuoteNotLoadedException {

// 	assert timeOffset <= 0;

// 	if(timeOffset <= -times.size())
// 	    throw QuoteNotLoadedException.getInstance();
	
// 	HashMap quotesForTime = (HashMap)cache.get(-timeOffset);

// 	if(quotesForTime == null)
// 	    throw QuoteNotLoadedException.getInstance();

// 	return quotesForTime;
//     }

//     /**
//      * Load the given quote into the cache.
//      *
//      * @param quote the quote
//      */
//     public void load(IDQuote quote) {
//         load(quote.getSymbol(),
//              quote.getTime(),
//              quote.getDayVolume(),
//              (float)quote.getDayLow(),
//              (float)quote.getDayHigh(),
//              (float)quote.getDayOpen(),
//              (float)quote.getDayClose(),
//              (float)quote.getBid(),
//              (float)quote.getAsk());
//     }

//     /**
//      * Load the given quote into the cache.
//      *
//      * @param symbol symbol of quote
//      * @param time   quote time
//      * @param day_volume day volume
//      * @param day_low day low
//      * @param day_high day high
//      * @param day_open day open
//      * @param day_close day close
//      */
//     public synchronized void load(Symbol symbol, TradingTime time, int day_volume, float day_low,
//                                   float day_high, float day_open, float day_close) {

//         // Find the fast time offset for the quote
//         int timeOffset;

//         try {
//             timeOffset = timeToOffset(time);
//         }
//         catch(WeekendTimeException e) {
//             // If the time falls on a weekend then skip it
//             return;
//         }

//         // Get hash of quotes for that time
//         HashMap quotesForTime;

//         try {
//             quotesForTime = getQuotesForTime(timeOffset);
//         }
//         catch(QuoteNotLoadedException e) {
//             // The timeToOffset() call above should have expanded
//             // the quote range so this shouldn't happen
//             assert false;

//             quotesForTime = new HashMap(0);
//         }

//         // Lots of stocks don't change between days, so check to see if
//         // this stock's quote is identical to yesterdays. If so then
//         // just use that
//         IDQuoteCacheQuote yesterdayQuote = null;
//         IDQuoteCacheQuote todayQuote = null;

//         try {
//             yesterdayQuote = getQuoteCacheQuote(symbol, timeOffset - 1);
//         }
//         catch(QuoteNotLoadedException e) {
//             // OK
//         }

//         if(yesterdayQuote != null &&
//            yesterdayQuote.equals(day_volume, day_low, day_high, day_open, day_close))
//             todayQuote = yesterdayQuote;
//         else
//             todayQuote = new IDQuoteCacheQuote(day_volume, day_low, day_high,
//                                                 day_open, day_close);

//         // Put stock in map and remove symbol and time to reduce memory
//         // (they are our indices so we already know them)
//         Object previousQuote = quotesForTime.put(symbol, todayQuote);

//         // If the quote wasn't already there then increase size counter
//         if(previousQuote == null)
//             size++;
//     }

//     /**
//      * Remove the given quote from the cache. It's OK if the quote isn't loaded.
//      *
//      * @param symbol the symbol of the quote to remove
//      * @param timeOffset the fast access time offset of the quote to remove
//      */
//     public synchronized void free(Symbol symbol, int timeOffset) {

// 	try {
// 	    HashMap quotesForTime = getQuotesForTime(timeOffset);
// 	    Object quote = quotesForTime.remove(symbol);

// 	    // If we actually deleted a quote, then reduce our quote counter.
// 	    // We have to check that we actually did remove something from
// 	    // the cache, so that our size count is correct. Its OK for the caller
//             // to try to delete a quote that's not in the cache - if it wasn't
//             // then the quote bundles would have to keep track of holidays etc...
// 	    if(quote != null) {
// 		size--;

//                 // If the hashmap is empty then resize it to the minimum size.
//                 // Otherwise we may have 1,000s of large hash maps taking up
//                 // a *LOT* of memory.
//                 if(quotesForTime.isEmpty())
//                     cache.set(-timeOffset, new HashMap());
//             }

// 	    assert size >= 0;
// 	}
// 	catch(QuoteNotLoadedException e) {
// 	    // This means we've never had any quotes on the given time that
// 	    // the caller was trying to free. This sounds like something
// 	    // wonky is going on.
// 	    assert false;
// 	}
//     }

//     /**
//      * Convert between a time and its fast access time offset.
//      *
//      * @param time the time
//      * @return fast access time offset
//      * @exception WeekendTimeException if the time is on a weekend (there are no
//      *            fast access time offsets for weekend times)
//      */
//     public int timeToOffset(TradingTime time)
// 	throws WeekendTimeException {

//         TradingTimeComparator comparator =
//             new TradingTimeComparator(TradingTimeComparator.BACKWARDS);

// 	int timeOffset = -Collections.binarySearch(times, time, comparator);

// 	// If the time isn't yet in the cache because its too old, then binary search
// 	// will return the negative size of times.
//         // If the time isn't yet in the cache because its too new, then binary search
//         // will return 1.
//         // In either case expand the cache.
// 	if(timeOffset > times.size() || timeOffset == 1) {
// 	    expandToTime(time);
// 	    timeOffset = -Collections.binarySearch(times, time, comparator);
// 	}

// 	// Only possible reason time isn't in cache now is because it falls
// 	// on a weekend or its a newer time than what is in the cache.
// 	if(timeOffset > 0)
// 	    throw new WeekendTimeException();

//         return timeOffset;
//     }

//     /**
//      * Convert between a fast access time offset and a time.
//      *
//      * @param timeOffset fast access time offset
//      * @return the time
//      */
//     public TradingTime offsetToTime(int timeOffset) {

// 	assert timeOffset <= 0;

// 	// If the time isn't in the cache then expand it
// 	while(timeOffset <= -times.size()) {
// 	    TradingTime time = getFirstTime().previous(1);
// 	    addTime(time);
// 	}

// 	return (TradingTime)times.get(-timeOffset);
//     }

//     /**
//      * Return the number of quotes in the cache.
//      *
//      * @return the cache size
//      */
//     public int size() {
// 	return size;
//     }

//     /**
//      * Get the oldest time in the cache.
//      *
//      * @return the oldest time in cache or <code>null</code> if the cache is empty.
//      */
//     public TradingTime getFirstTime() {
//         if(times.size() > 0)
//             return (TradingTime)times.get(times.size() - 1);
//         else
//             return null;
//     }

//     /**
//      * Get the newest time in the cache.
//      *
//      * @return the newest time in cache or <code>null</code> if the cache is empty.
//      */
//     public TradingTime getLastTime() {
//         if(times.size() > 0)
//             return (TradingTime)times.get(0);
//         else
//             return null;
//     }

//     /**
//      * Get the fast access offset of the oldest time in the cache.
//      *
//      * @return the fast access offset of oldest time in cache or +1 if there
//      *         are no times in the cache.
//      */
//     public int getFirstTimeOffset() {
//         return -(times.size() - 1);
//     }

//     // Add one time to cache. The time should be one trading day older than the
//     // oldest time in the cache.
//     private void addTime(TradingTime time) {
// 	// Create a map with 0 initial capacity. I.e. we create an empty one
// 	// because we might not even use it
// 	HashMap map = new HashMap(0);
// 	cache.add(map);
// 	times.add(time);	
//     }

//     // This function is used to insert a time into the cache that is newer
//     // (i.e. more recent) than any other times in the cache. It's pretty
//     // slow as it needs to shift two arrays but it's only used for import
//     // so it doesn't matter
//     private void insertTime(TradingTime time) {
// 	// Create a map with 0 initial capacity. I.e. we create an empty one
// 	// because we might not even use it
// 	HashMap map = new HashMap(0);
// 	cache.add(0, map);
// 	times.add(0, time);
//     }

//     // Expand the quote cache to encompass the given time
//     private void expandToTime(TradingTime time) {

//         assert time != null;

//         TradingTime firstTime = getFirstTime();
//         TradingTime lastTime = getLastTime();

//         // There are four cases to consider, first there are no times
//         // in the cache
//         if(firstTime == null)
//             addTime(time);

//         // Second is that the new time to add is before the first time
//         // in our cache. This is common and we can handle this quickly
//         else if(time.before(firstTime)) {
//             while(time.before(firstTime)) {
//                 firstTime = firstTime.previous(1);
//                 addTime(firstTime);
//             }
//         }

//         // The third case is that this time is newer than our newest
//         // time. This can only happen when quotes are imported into the
//         // system while Venice is running. This code is slow but we don't care.
//         else if(time.after(lastTime)) {
//             while(time.after(lastTime)) {
//                 lastTime = lastTime.next(1);
//                 insertTime(lastTime);
//             }
//         }

//         // The remaining case is the time is already in our range...
//     }

    /**
     * Add a listener to listen for new intra-day quotes.
     *
     * @param quoteListener the class to be informed about new intra-day quotes
     */
    public void addQuoteListener(QuoteListener quoteListener) {
        quoteListeners.add(QuoteListener.class, quoteListener);
    }

    /**
     * Remove a listener for new intra-day quotes.
     *
     * @param quoteListener the object to remove
     */
    public void removeQuoteListener(QuoteListener quoteListener) {
        quoteListeners.remove(QuoteListener.class, quoteListener);
    }
}


