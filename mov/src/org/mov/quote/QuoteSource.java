package org.mov.quote;

import java.util.*;

import org.mov.util.*;
import org.mov.portfolio.Stock;

/**
 * Provides a generic interface in which we can query stock quotes from
 * multiple sources. The source could either be directly from files,
 * a database, a unique internal format or from the internet.
 */
public interface QuoteSource {

    /** Only load indices (All Ordinaries, Mining etc) - we assume
     *  all symbols starting with 'X' are indices. This is *wrong*. */
    public static final int INDICES = 0;

    /** 
     * Only load 3 letter symbols which are generally companies +
     * mutual funds.
     * Loads all 3 letter symbols except ones starting with 'X' (cause
     * some of them are indicies) - so skips some legit X symbols. */
    public static final int COMPANIES_AND_FUNDS = 1;
    
    /** 
     * Load all commodoties listed on the ASX (everything except indices).
     * We define indices as symbols starting with X. Again this is not quite
     * right. */
    public static final int ALL_COMMODITIES = 2;

    /** Indicates a single symbol. */
    public static final int SINGLE_SYMBOL = 3;

    /** 
     * Returns the company name associated with the given symbol. 
     * 
     * @param	symbol	the stock symbol
     * @return	the company name
     */
    public String getCompanyName(String symbol);

    /**
     * Returns the symbol associated with the given company. 
     * 
     * @param	symbol	a partial company name
     * @return	the company symbol
     */
    public String getCompanySymbol(String partialCompanyName);

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for
     * @return	whether the symbol was found or not
     */
    public boolean symbolExists(String symbol);

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date
     */
    public TradingDate getLatestQuoteDate();

    /**
     * Return a vector of quotes for all stocks in the given date range.
     * The vector will be in order of date then stock symbol.
     *
     * @param	startDate	the start of the date range (inclusive)
     * @param	endDate		the end of the date range (inclusive)
     * @param	type		the type of the search
     * @return	a vector of stock quotes
     * @see Stock
     */
    public Vector getQuotesForDates(TradingDate startDate, 
				    TradingDate endDate, 
				    int type);

    /**
     * Return a vector of all quotes in the given date.
     * The vector will be in order of stock symbol.
     *
     * @param	date	the date to return quotes for
     * @param	type	the type of the search
     * @return	a vector of stock quotes
     * @see Stock
     */
    public Vector getQuotesForDate(TradingDate date, int type);

    /**
     * Return all quotes for the given symbol. They will be returned in
     * order of date.
     *
     * @param	symbol	the symbol to query
     * @return	a vector of stock quotes
     * @see Stock
     */
    public Vector getQuotesForSymbol(String symbol);

    /** 
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public Vector getDates(); 
}
