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
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class controls the periodic downloading, or synchronisation, of new
 * intra-day quotes.
 *
 * @author Andrew Leppard
 */
public class IDQuoteSync {
    
    /**
     * This class contains the function that is periodically calld to download
     * new intra-day quotes.
     */
    private class QuoteSync extends TimerTask {

        // List of symbols to download
        private List symbols;

        // Quote cache to store quotes
        private IDQuoteCache quoteCache;

        /**
         * Create a new object to periodically download intra-day quotes.
         */
        public QuoteSync(List symbols) {
            assert symbols.size() > 0;
            this.symbols = symbols;
            quoteCache = IDQuoteCache.getInstance();
        }

        /**
         * Download the current intra-day quotes.
         */
        public void run() {

            // TODO: Fix this up
            try {
                List quotes = YahooIDQuoteImport.importSymbols(symbols);
                quoteCache.load(quotes);
                
                System.out.println("---------");
                for(Iterator iterator = quotes.iterator(); iterator.hasNext();) 
                    System.out.println(iterator.next());
            }
            catch(ImportExportException e) {
                // deal with this
                assert false;
            }
        }
    }
 
    /** The default time period inbetween quote downloads. */
    final static int DEFAULT_PERIOD = 60;

    // Number of milliseconds in one second.
    private final static int MILLISECONDS_IN_SECOND = 1000;

    // Singleton instance of this class
    private static IDQuoteSync instance = null;

    // List of symbols to import
    private List symbols;

    // Status of whether the quote sync is enabled
    private boolean isEnabled;

    // Period, in seconds, between quote sync
    private int period;

    // Timer which schedules quote syncs
    private Timer timer;

    /**
     * Create a new intra-day quote synchoronisation object.
     */
    private IDQuoteSync() {
        symbols = new ArrayList();
        isEnabled = false;
        period = DEFAULT_PERIOD;
        timer = null;
    }

    /**
     * Create or return the singleton instance of the intra-day quote synchronisation object.
     *
     * @return  singleton instance of this class
     */
    public static synchronized IDQuoteSync getInstance() {
	if(instance == null)
	    instance = new IDQuoteSync();

        return instance;
    }

    /**
     * Enable automatic downloading of intra-day quotes.
     */
    public void enable() {
        isEnabled = true;
        startTimer();
    }
    
    /**
     * Disable automatic downloading of intra-day quotes.
     */
    public void disable() {
        isEnabled = false;
        stopTimer();
    }
    
    /**
     * Return the list of symbols of the intra-day quotes to download.
     *
     * @return symbols list
     */
    public List getSymbols(List symbols) {
        return symbols;
    }

    /**
     * Add the a list of symbols to the list of symbols for the intra-day quotes
     * to download.
     *
     * @param symbols new symbols to download
     */
    public void addSymbols(List symbols) {

        // Add the new unique symbols
        for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();
            if(!this.symbols.contains(symbol))
                this.symbols.add(symbol);
        }

        // Restart sync task so that it has the updated symbol list
        restartTimer();
    }

    /**
     * Set the time period inbetween quote downloads
     *
     * @param period the period in seconds.
     */
    public void setPeriod(int period) {
        if(period != this.period) {
            assert period != 0;
            this.period = period;
            restartTimer();
        }
    }

    /**
     * Start the timer that triggers the quote download.
     */
    private void startTimer() {
        // Don't start up timer if we have no symbols to download
        if (isEnabled && timer == null && symbols.size() > 0) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new QuoteSync(symbols), 0, period * MILLISECONDS_IN_SECOND);
        }
    }

    /**
     * Stop the timer that triggers the quote download.
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Restart the timer that triggers the quote download.
     */
    private void restartTimer() {
        stopTimer();
        startTimer();
    }

}

