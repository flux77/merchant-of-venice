package org.mov.quote;

import org.mov.portfolio.Stock;
import org.mov.util.TradingDate;

/**
 * Provides a filter to parse the Meta Stock quote format. This
 * format uses 4 digit years and prices in dollars. The first
 * column is the symbol, then the date, open, high, low, close & volume.
 * Exampe:
 * <pre>
 * XXX,19990715,1.73,1.82,1.71,1.81,3648921
 * </pre>
 */
public class MetaStockQuoteFilter implements QuoteFilter {

    /**
     * Creates an instance of the filter.
     */
    public MetaStockQuoteFilter() {
	// nothing to do
    }
   
    /**
     * Return the name of the filter.
     *
     * @return	the name of the filter.
     */
    public String getName() {
	return "MetaStock";
    }
    
    /**
     * Parse the given text string and returns the stock quote or null
     * if it did not contain a valid quote.
     *
     * @param	quoteList	a single line of text containing a quote
     * @return	the stock quote
     */
    public Stock toQuote(String quoteLine) {
	Stock stock = null;

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
		int volume = Integer.parseInt(quoteParts[i++]);
		stock = new Stock(symbol, date, volume, day_low, day_high,
				  day_open, day_close);
	    }	    
	}
	return stock;
    }

    /**
     * Convert the given stock quote to a string line.
     *
     * @param	quote	a stock quote
     * @return	string version of the quote
     */
    public String toString(Stock quote) {
	return new String(quote.getSymbol() + "," + 
			  quote.getDate().toString("yyyymmdd") + "," +
			  quote.getDayOpen() + "," +
			  quote.getDayHigh() + "," +
			  quote.getDayLow() + "," +
			  quote.getDayClose() + "," +
			  quote.getVolume());
    }
}
