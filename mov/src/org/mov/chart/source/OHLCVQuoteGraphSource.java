package org.mov.chart.source;

import org.mov.chart.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

import java.util.*;

public class OHLCVQuoteGraphSource implements GraphSource {

    private QuoteCache cache;
    private int quote;
    private String symbol;
    private Graphable graphable;

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

	while(iterator.hasPrevious()) {
	    date = (TradingDate)iterator.previous();
	    try {
		value = new Float(cache.getQuote(symbol, quote, date));
		graphable.putValue(date, value);
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
	return symbol;
    }

    public String getToolTipText(TradingDate date) {
	if(!cache.containsDate(date)) 
	    return null;

	try {
	
	    if(quote == Token.DAY_VOLUME_TOKEN) {
		return
		    new String("<html>" +
			       symbol.toUpperCase() + 
			       ", " +
			       date.toLongString() +
			       "<p>" +
			       Math.round(cache.
					  getQuote(symbol, 
						   Token.DAY_VOLUME_TOKEN, 
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
					      Token.DAY_LOW_TOKEN, date) +
			       " </font>" +
			       "<font color=green>" + 
			       cache.getQuote(symbol, 
					      Token.DAY_HIGH_TOKEN, date) + 
			       " </font>" +
			       cache.getQuote(symbol, 
					      Token.DAY_OPEN_TOKEN, date) +
			       " " + 
			       cache.getQuote(symbol, 
					      Token.DAY_CLOSE_TOKEN, date) +
			       "</html>");
	    }
	}
	catch(EvaluationException e) {
	    // Shouldn't happen
	    return null;
	}
    }

    public String getYLabel(float value) {
	if(quote == Token.DAY_VOLUME_TOKEN) {
	    final float BILLION = 1000000000F;
	    final float MILLION = 1000000F;
	    String extension = "";
	    
	    if(value >= BILLION) {
		value /= BILLION;
		extension = "B";
	    }
	    else if(value >= MILLION) {
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

	if(quote == Token.DAY_VOLUME_TOKEN) {
	    float[] major = {10F,
			     100F,
			     1000F, // 1T
			     10000F,
			     100000F,
			     1000000F, // 1M
			     10000000F,
			     100000000F,
			     1000000000F}; // 1B
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
	if(quote == Token.DAY_VOLUME_TOKEN) {
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
