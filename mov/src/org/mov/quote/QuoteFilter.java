package org.mov.quote;

import org.mov.portfolio.Stock;

/**
 * Provides an interface for converting to/from a text string containing a 
 * single quote from/to the internal stock quote object. 
 * All quotes are stored internally in MetaStock format, that is volume is 
 * the real amount and all quotes are in dollars.
 */
public interface QuoteFilter {

    /**
     * Return the name of the filter.
     *
     * @return	the name of the filter
     */
    public String getName();

    /**
     * Parse the given text string and returns the stock quote or null
     * if it did not contain a valid quote.
     *
     * @param	quoteList	a single line of text containing a quote.
     * @return	the stock quote
     */
    public Stock toQuote(String quoteLine);

    /**
     * Convert the given stock quote to a string line.
     *
     * @param	quote	a stock quote
     * @return	string version of the quote
     */
    public String toString(Stock quote);
}
