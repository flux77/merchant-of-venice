package org.mov.quote;

import org.mov.util.TradingDate;

/**
 * Provides a filter to parse the Meta Stock (volume/100) quote format. This
 * format uses 4 digit years and prices in dollars, volume is divided by 100.
 * The first column is the symbol, then the date, open, high, low, close & 
 * volume.
 * Exampe:
 * <pre>
 * XXX,19990715,1.73,1.82,1.71,1.81,36489
 * </pre>
 */
public class MetaStock2QuoteFilter implements QuoteFilter {

    /**
     * Creates an instance of the filter.
     */
    public MetaStock2QuoteFilter() {
	// nothing to do
    }
   
    /**
     * Return the name of the filter.
     *
     * @return	the name of the filter.
     */
    public String getName() {
	return "MetaStock (volume/100)";
    }
    
    /**
     * Parse the given text string and returns the stock quote or null
     * if it did not contain a valid quote.
     *
     * @param	quoteList	a single line of text containing a quote
     * @return	the stock quote
     */
    public Quote toQuote(String quoteLine) {
	Quote quote = null;

	if(quoteLine != null) {
	    String[] quoteParts = quoteLine.split(",");
	    int i = 0;
	    
	    if(quoteParts.length == 7) {
		String symbol = quoteParts[i++];
		TradingDate date = new TradingDate(quoteParts[i++],
						   TradingDate.BRITISH);
		float day_open = Float.parseFloat(quoteParts[i++]);
		float day_high = Float.parseFloat(quoteParts[i++]);
		float day_low = Float.parseFloat(quoteParts[i++]);
		float day_close = Float.parseFloat(quoteParts[i++]);

		// Convert volume from 1/100th volume to real volume
		int volume = Integer.parseInt(quoteParts[i++]) * 100;
		quote = new Quote(symbol, date, volume, day_low, day_high,
				  day_open, day_close);
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
	return new String(quote.getSymbol() + "," + 
			  quote.getDate().toString("yyyymmdd") + "," +
			  quote.getDayOpen() + "," +
			  quote.getDayHigh() + "," +
			  quote.getDayLow() + "," +
			  quote.getDayClose() + "," +
			  quote.getVolume() / 100);
    }
}