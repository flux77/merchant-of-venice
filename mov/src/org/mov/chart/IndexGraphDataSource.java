package org.mov.chart;

import java.text.*;
import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

public class IndexGraphDataSource extends PriceQuoteGraphDataSource {

    public IndexGraphDataSource(QuoteCache cache) {
	// Indices are based on day close
	setCache(cache, Token.DAY_CLOSE_TOKEN);
    }

    // Override base class
    public String getToolTipText(TradingDate date) {

	// Check we have the date
	QuoteCache cache = getCache();

	if(!cache.containsDate(date)) 
	    return null;

	try {
	    return
		new String("<html>" +
			   getSymbol().toUpperCase() + 
			   ", " +
			   date.toLongString() + 
			   ", " +
			   cache.getQuote(getSymbol(), 
					  Token.DAY_CLOSE_TOKEN, date) +
			   "</html>");
	}
	catch(EvaluationException e) {
	    // shouldn't happen
	    return null;
	}
    }

    // Override base class
    public String getYLabel(float value) {
	return Integer.toString((int)value);
    }

    public String getName() {
	return new String(getSymbol() + " Index");
    }
}




