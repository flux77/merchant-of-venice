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

package org.mov.chart.source;

import org.mov.chart.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;

import java.util.*;

/**
 * Provides a <code>QuoteCache</code> graph source. This class
 * allows graph sources for day Open, High, Low, Close and
 * Volume (OHLCV).
 */
public class OHLCVQuoteGraphSource implements GraphSource {

    private QuoteCache cache;
    private int quote;
    private String symbol;
    private Graphable graphable;

    /**
     * Create a new graph source from the cache with the given
     * quote type.
     *
     * @param	cache	the cache containing stock quotes
     * @param	quote	the quote kind, one of: {@link Quote#DAY_OPEN}, 
     * {@link Quote#DAY_CLOSE}, {@link Quote#DAY_HIGH} or 
     * {@link Quote#DAY_LOW}
     */
    public OHLCVQuoteGraphSource(QuoteCache cache, int quote) {
	this.quote = quote;
	this.cache = cache;

	// So far only handles a single symbol - so get that symbol
	symbol = (String)cache.getSymbols()[0];

	// Build graphable so this source can be directly graphed
	graphable = new Graphable();
	Float value;
	TradingDate date;

	// List of dates is in reverse order so well need to traverse
	// it backwards to get the dates in chronological order
	ListIterator iterator = cache.dateIterator(cache.getNumberDays());

        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.setProgress(0);
        progress.setNote("Graphing dates");
	while(iterator.hasPrevious()) {
	    date = (TradingDate)iterator.previous();
	    try {
		value = new Float(cache.getQuote(symbol, quote, date));
		graphable.putY((Comparable)date, value);
                if (progress != null) progress.increment();
	    }
	    catch(EvaluationException e) {
		// ignore
	    }
	}	
    }

    public Graphable getGraphable() {
	return graphable;
    }

    public String getName() {
	// Display symbols in upper case
	return symbol.toUpperCase();
    }

    public String getToolTipText(Comparable x) {

	// In OHLCV graphs the x axis is in dates
	TradingDate date = (TradingDate)x;

	if(!cache.containsDate(date)) 
	    return null;

	try {
	
	    if(quote == Quote.DAY_VOLUME) {
		return
		    new String("<html>" +
			       symbol.toUpperCase() + 
			       ", " +
			       date.toLongString() +
			       "<p>" +
			       Math.round(cache.
					  getQuote(symbol, 
						   Quote.DAY_VOLUME, 
						   date)) +
			       "</html>");
	    }
	    else {
		return
		    new String("<html>" +
			       symbol.toUpperCase() + 
			       ", " +
			       date.toLongString() +
			       "<p>" +
			       "<font color=red>" + 
			       cache.getQuote(symbol, 
					      Quote.DAY_LOW, date) +
			       " </font>" +
			       "<font color=green>" + 
			       cache.getQuote(symbol, 
					      Quote.DAY_HIGH, date) + 
			       " </font>" +
			       cache.getQuote(symbol, 
					      Quote.DAY_OPEN, date) +
			       " " + 
			       cache.getQuote(symbol, 
					      Quote.DAY_CLOSE, date) +
			       "</html>");
	    }
	}
	catch(EvaluationException e) {
	    // Shouldn't happen
	    return null;
	}
    }

    public String getYLabel(float value) {
	if(quote == Quote.DAY_VOLUME) {
	    final float BILLION = 1000000000F;
	    final float MILLION = 1000000F;
	    String extension = "";
	    
	    if(Math.abs(value) >= BILLION) {
		value /= BILLION;
		extension = "B";
	    }
	    else if(Math.abs(value) >= MILLION) {
		value /= MILLION;
		extension = "M";
	    }
	    
	    return Integer.toString((int)value) + extension;
	}
	else {
	    return Converter.priceToString(value);
	}
    }

    public float[] getAcceptableMajorDeltas() {

	if(quote == Quote.DAY_VOLUME) {
	    float[] major = {10F,
			     100F,
			     1000F, // 1T
			     10000F,
			     100000F,
			     1000000F, // 1M
			     10000000F,
			     100000000F,
			     1000000000F, // 1B
			     10000000000F}; 
	    return major;
	}
	else {
	    float[] major = {0.001F, // 0.1c
			     0.01F, // 1c
			     0.1F, // 10c
			     1.0F, // $1
			     10.0F, // $10
			     100.0F, // $100
			     1000.0F}; // $1000
	    return major;	    
	}
    }

    public float[] getAcceptableMinorDeltas() {
	if(quote == Quote.DAY_VOLUME) {
	    float[] minor = {1F, 1.5F, 2F, 2.5F, 3F, 4F, 5F, 6F, 8F};
	    return minor;
	}
	else {
	    float[] minor = {1F, 1.1F, 1.25F, 1.3333F, 1.5F, 2F, 2.25F, 
			     2.5F, 3F, 3.3333F, 4F, 5F, 6F, 6.5F, 7F, 7.5F, 
			     8F, 9F};
	    return minor;
	}
    }
}
