package org.mov.chart;

import org.mov.util.*;
import org.mov.parser.*;

abstract public class PriceQuoteGraphDataSource extends QuoteGraphDataSource {

    public float[] getAcceptableMajorDeltas() {
	// The Y axis of the graph has axis lines across the graph 
	// denoting sensible values. Legal values are made up by
	// multiplying the major by the minor values.
	//
	// This sytem avoids silly axis lines at values like "$1.63".

	float[] major = {1.0F, // 1c
			 10.0F, // 10c
			 100.0F, // $1
			 1000.0F, // $10
			 10000.0F, // $100
			 100000.0F}; // $1000
	return major;
    }

    public float[] getAcceptableMinorDeltas() {
	float[] minor = {1F, 1.1F, 1.25F, 1.3333F, 1.5F, 2F, 2.25F, 
			 2.5F, 3F, 3.3333F, 4F, 5F, 6F, 6.5F, 7F, 7.5F, 
			 8F, 9F};
	return minor;
    }

    public String getToolTipText(TradingDate date) {
	// Check we have the date
	QuoteCache cache = getCache();

	if(!cache.containsDate(date)) 
	    return null;

	String symbol = getSymbol();
	
	try {
	    return
		new String("<html>" +
			   symbol.toUpperCase() + 
			   ", " +
			   date.toLongString() +
			   "<p>" +
			   "<font color=red>" + 
			   cache.getQuote(symbol, Token.DAY_LOW_TOKEN, date) +
			   " </font>" +
			   "<font color=green>" + 
			   cache.getQuote(symbol, 
					  Token.DAY_HIGH_TOKEN, date) + 
			   " </font>" +
			   cache.getQuote(symbol, Token.DAY_OPEN_TOKEN, date) +
			   " " + 
			   cache.getQuote(symbol, 
					  Token.DAY_CLOSE_TOKEN, date) +
			   "</html>");
	}
	catch(EvaluationException e) {
	    // Shouldn't happen
	    return null;
	}
    }

    public String getYLabel(float value) {	
	return Converter.priceToString(value);
    }
}



