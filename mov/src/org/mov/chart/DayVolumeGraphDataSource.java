package org.mov.chart;

import java.text.*;
import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class DayVolumeGraphDataSource extends QuoteGraphDataSource {
    
    public DayVolumeGraphDataSource(QuoteCache cache) {
	setCache(cache, Token.DAY_VOLUME_TOKEN);
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
			   Math.round(cache.getQuote(symbol, 
						     Token.DAY_VOLUME_TOKEN, 
						     date)) +
			   "</html>");
	}
	catch(EvaluationException e) {
	    // Shouldn't happen
	    return null;
	}
    }

    public String getYLabel(float value) {	
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

    public String getName() {
	return "Day Volume";
    }

    public float[] getAcceptableMajorDeltas() {
	// The Y axis of the graph has axis lines across the graph 
	// denoting sensible values. Legal values are made up by
	// multiplying the major by the minor values.
	//
	// This sytem avoids silly axis lines at values like "$1.63".
	//
	// For volume we will only want big amounts like 100, 200, 500
	// 1000, 10000 etc.

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

    public float[] getAcceptableMinorDeltas() {
	float[] minor = {1F, 1.5F, 2F, 2.5F, 3F, 4F, 5F, 6F, 8F};

	return minor;
    }
}
