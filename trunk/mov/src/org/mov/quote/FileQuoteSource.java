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

package org.mov.quote;

import java.io.*;
import java.util.*;

import org.mov.util.*;
import org.mov.ui.DesktopManager;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;

/**
 * Provides functionality to obtain stock quotes from files. This class
 * implements the QuoteSource interface to allow users to directly use
 * their stock quote files without creating a database. 
 *
 * Example:
 * <pre>
 *	Vector quotes = Quote.getSource().getQuotesForSymbol("CBA");
 * </pre>
 *
 * @see Quote
 */
public class FileQuoteSource implements QuoteSource
{
    // Construct a map between TradingDates and file names
    private HashMap dateToFile = new HashMap();

    // Buffer last trading date in database
    private TradingDate latestQuoteDate = null;

    // Filter to convert data into quote
    private QuoteFilter filter;

    // The number of dates in the current source
    private int num_dates = 0;
    
    // Given a name of a file containing a list of day quotes, return the
    // the day 
    private TradingDate getContainedDate(String fileName) 
	throws java.io.IOException {

	TradingDate date = null;
	FileReader fr = new FileReader(fileName);
	BufferedReader br = new BufferedReader(fr);
	Quote quote = filter.toQuote(br.readLine());
	if(quote != null)
	    date = quote.getDate();
	br.close();

	return date;
    }

    // Given a name of a file and a stock symbol, return the quote for
    // that stock in the given file
    private Quote getContainedQuote(String fileName, String symbol) {

	Quote quote = null;
	String line;
	boolean found = false;

	try {
	    FileReader fr = new FileReader(fileName);
	    BufferedReader br = new BufferedReader(fr);
	    line = br.readLine();

	    while(line != null && !found) {

		quote = filter.toQuote(line);
	    
		if(quote != null && quote.getSymbol().equals(symbol)) 
		    found = true;
		else
		    line = br.readLine();
	    }
		
	    br.close();

	} catch (java.io.IOException ioe) {
	    DesktopManager.showErrorMessage("Can't load " + fileName);
	} 

	if(found)
	    return quote;
	else
	    return null;
    }

    // Is the given stock the same as the type given?
    private boolean isType(Quote quote, int type) {
	boolean match = false;

	if(type == INDICES) {
	    if(quote.getSymbol().startsWith("x"))
		match = true;
	}
	else if(type == COMPANIES_AND_FUNDS) {
	    if(quote.getSymbol().length() == 3 &&
	       !quote.getSymbol().startsWith("x"))
		match = true;
	    
	}
	else if(type == ALL_COMMODITIES) {
	    if(!quote.getSymbol().startsWith("x"))
		match = true;
	}
	else // ALL_SYMBOLS
	    match = true;

	return match;
    }

    /**
     * Creates a new quote source using the list of files specified in the user
     * preferences. 
     *
     * @param	format	The format filter to use to parse the quotes
     * @param	fileNames	Vector of file names
     */
    public FileQuoteSource(String format, Vector fileNames) {

	// Set filter to whatever is defined in the preferences to filter
	// to our internal format
	filter = QuoteFilterList.getInstance().getFilter(format);

	// Create map between TradingDates and file names and record
	// latest trading date. This allows us to quickly locate the
	// file containg the given date.
	TradingDate date;

	// Make sure we don't pop up 1000 error messages if all the files
	// have been moved :)
	int errorCount = 0;

	// Indexing might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setTitle("Indexing files");
        p.setMaximum(fileNames.size());

        Iterator iterator = fileNames.iterator();
	String fileName;

	while(iterator.hasNext()) {
	    
	    fileName = (String)iterator.next();

	    try {
		date = getContainedDate(fileName);
	    
		if(date != null) {
		    // Buffer the latest quote date 
		    if(latestQuoteDate == null || date.after(latestQuoteDate))
			latestQuoteDate = date;
		    
		    // Associate this date with this file
		    dateToFile.put(date, fileName);
		}		    
		else {
		    if(errorCount < 5) {
			DesktopManager.
			    showErrorMessage("No quotes found in " + 
					     fileName);
			errorCount++;
		    }
		}

	    } catch (java.io.IOException ioe) {
		if(errorCount < 5) {
		    DesktopManager.showErrorMessage("Can't load " + 
						    fileName);
		    errorCount++;	    
		}
	    }
	    p.increment();
	}

	ProgressDialogManager.closeProgressDialog();
    }

    /**
     * Returns the company name associated with the given symbol. Not
     * implemented for the file quote source.
     * 
     * @param	symbol	the stock symbol.
     * @return	always an empty string.
     */
    public String getCompanyName(String symbol) {
	return new String(""); 
    }

    /**
     * Returns the symbol associated with the given company. Not
     * implemented for the file quote source.
     * 
     * @param	symbol	a partial company name.
     * @return	always an empty string.
     */
    public String getCompanySymbol(String partialCompanyName) {
	return new String(""); 
    }

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for.
     * @return	whether the symbol was found or not.
     */
    public boolean symbolExists(String symbol) {
	// Iterate through all files until we find one containing the
	// symbol name we are looking for

	Set dates = dateToFile.keySet();
	Iterator iterator = dates.iterator();
	TradingDate date;
	Quote quote;

	// All checks are done in lower case no matter what case the
	// file format is in
	symbol = symbol.toLowerCase();
	
	while(iterator.hasNext()) {
	    date = (TradingDate)iterator.next();
	    quote = getContainedQuote((String)dateToFile.get(date),
				      symbol);
	    if(quote != null)
		return true; // found!
	}

	return false; 
    }

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date.
     */
    public TradingDate getLatestQuoteDate() {
	return latestQuoteDate;
    }

    /**
     * Return a vector of quotes for all stocks in the given date range.
     * The vector will be in order of date then stock symbol.
     *
     * @param	startDate	the start of the date range (inclusive).
     * @param	endDate		the end of the date range (inclusive).
     * @param	type		the type of the search.
     * @return	a vector of stock quotes.
     * @see Quote
     */
    public Vector getQuotesForDates(TradingDate startDate, 
				    TradingDate endDate, 
				    int type) {

	Vector quotes = new Vector();
	Vector dates = Converter.dateRangeToTradingDateVector(startDate,
							      endDate);
	Iterator iterator = dates.iterator();
	TradingDate date;

	// This query might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setTitle("Loading quotes " + startDate.toShortString() +" to "+ endDate.toShortString());
        p.setMaximum(dates.size());

	while(iterator.hasNext()) {
	    date = (TradingDate)iterator.next();

	    quotes.addAll((Collection)getQuotesForDate(date, type));

	    p.increment();
	}

	ProgressDialogManager.closeProgressDialog();

	return quotes;
    }

    /**
     * Return all quotes for the given symbols between the given dates. 
     * They will be returned in order of date.
     *
     * @param	symbols	the symbols to query.
     * @param	startDate	the first trading date to query for
     * @param	endDate		the last trading date to query for
     * @return	a vector of stock quotes.
     * @see Quote
     */
    public Vector getQuotesForSymbolsAndDates(Vector symbols, 
					      TradingDate startDate,
					      TradingDate endDate) {
	// not implemented yet
	return new Vector();
    }

    /**
     * Returns the filename that contains quotes for the given date.
     *
     * @param	date	the given date
     * @return	the file containing quotes for this date
     */
    public String getFileForDate(TradingDate date) {
	return (String)dateToFile.get(date);
    }

    /**
     * Return a vector of all quotes in the given date.
     * The vector will be in order of stock symbol.
     *
     * @param	date	the date to return quotes for.
     * @param	type	the type of the search.
     * @return	a vector of stock quotes.
     * @see Quote
     */
    public Vector getQuotesForDate(TradingDate date, int type) {
	Vector quotes = new Vector();
	String fileName = getFileForDate(date);
	String line;	
	Quote quote;

	try {
	    FileReader fr = new FileReader(fileName);
	    BufferedReader br = new BufferedReader(fr);
	    line = br.readLine();

	    while(line != null) {
		quote = filter.toQuote(line);

		if(quote != null && isType(quote, type)) 
		    quotes.add((Object)quote);

		line = br.readLine();
	    }

	    br.close();

	} catch (java.io.IOException ioe) {
	    DesktopManager.showErrorMessage("Can't load " + fileName);
	} 
	return quotes;
    }

    /**
     * Return all quotes for the given symbol. They will be returned in
     * order of date.
     *
     * @param	symbol	the symbol to query.
     * @param   progress the progress dialog to display progress with
     * @return	a vector of stock quotes.
     * @see Quote
     */
    public Vector getQuotesForSymbol(String symbol) {
	// Get list of dates available and sort them
	TreeSet dates = new 
	    TreeSet(new TradingDateComparator(TradingDateComparator.FORWARDS));
	dates.addAll(dateToFile.keySet());	

	Iterator iterator = dates.iterator();
	Vector quotes = new Vector();
	TradingDate date = null;
	Quote quote = null;

	// This query might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setMaximum(dates.size());
        p.setTitle("Retrieving dates");

	// All checks are done in lower case no matter what case the
	// file format is in
	symbol = symbol.toLowerCase();
	
	while(iterator.hasNext()) {
	    date = (TradingDate)iterator.next();

	    quote = getContainedQuote(getFileForDate(date),
				      symbol);
	    if(quote != null) 
		quotes.add(quote);

	    p.increment();
	}

	ProgressDialogManager.closeProgressDialog();

	return quotes;
    }

    /**
     * Return all the dates which we have quotes for.
     */
    public Vector getDates() {
	return new Vector(dateToFile.keySet());
    }
}
