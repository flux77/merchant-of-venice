package org.mov.quote;

import org.mov.portfolio.Stock;
import org.mov.util.TradingDate;

/**
 * Provides a filter to parse the Insight Trader quote format. This
 * format uses 2 digit years and prices in cents, volume is divided by 100.
 * The first column is the symbol, then the date, open, high, low, close & 
 * volume.
 * Exampe:
 * <pre>
 * XXX 07/15/99 173 182 171 181 36489
 * </pre>
 */
public class InsightTraderQuoteFilter implements QuoteFilter {

    /**
     * Creates an instance of the filter.
     */
    public InsightTraderQuoteFilter() {
	// nothing to do
    }
   
    /**
     * Return the name of the filter.
     *
     * @return	the name of the filter.
     */
    public String getName() {
	return "Insight Trader";
    }
    
    /**
     * Parse the given text string and returns the stock quote or null
     * if it did not contain a valid quote.
     *
     * @param	quoteList	a single line of text containing a quote.
     * @return	the stock quote.
     */
    public Stock filter(String quoteLine) {
	Stock stock = null;

	if(quoteLine != null) {
	    String[] quoteParts = quoteLine.split(" ");
	    int i = 0;
	    
	    if(quoteParts.length == 7) {
		String symbol = quoteParts[i++];
		TradingDate date = new TradingDate(quoteParts[i++]);

		// Convert all prices from cents to dollars
		float day_open = Float.parseFloat(quoteParts[i++]) / 100;
		float day_high = Float.parseFloat(quoteParts[i++]) / 100;
		float day_low = Float.parseFloat(quoteParts[i++]) / 100;
		float day_close = Float.parseFloat(quoteParts[i++]) / 100;

		// Convert volume from 1/100th volume to real volume
		int volume = Integer.parseInt(quoteParts[i++]) * 100;
		stock = new Stock(symbol, date, volume, day_low, day_high,
				  day_open, day_close);
	    }	    
	}
	return stock;
    }
}
