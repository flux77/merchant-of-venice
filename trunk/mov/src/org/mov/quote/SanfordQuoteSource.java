package org.mov.quote;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;
import java.util.regex.*;

import org.mov.util.*;
import org.mov.ui.DesktopManager;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;

/**
 * Provides functionality to obtain stock quotes directly from Sanford's
 * webiste. This class implements the QuoteSource interface to allow users to
 * access quotes directly from the internet without the need for local copies.
 *
 * Example:
 * <pre>
 *	Vector quotes = Quote.getSource().getQuotesForSymbol("CBA");
 * </pre>
 *
 * @see Quote
 */
public class SanfordQuoteSource implements QuoteSource {

    public final static String HOST = "www.sanford.com.au";
    public final static String PROTOCOL = "https";

    // This is used for session authentication
    private String cookie;

    // We read all quotes in Ezy Chart format from Sanford so we need to
    // convert them using this filter
    private final static String FILTER = "Ezy Chart";
    private QuoteFilter filter;

    // Login information
    private String username;
    private String password;

    // Keep hash of company symbols to name
    private HashMap symbolToName = new HashMap();

    // Are we connected?
    private boolean connected = false;

    /**
     * Creates a new quote source by downloading directly from Sanford's
     * web site. 
     *
     * @param	username	Sanford login username
     * @param	password	Sanford login password
     */
    public SanfordQuoteSource(String username, String password) {
	this.username = username;
	this.password = password;

	filter = QuoteFilterList.getInstance().getFilter(FILTER);
	login();
    }
   
    // Login in to Sanford's web site and obtain our session authentication
    // cookie
    private void login() {

	// This query might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setNote("Logging into Sanford");

	try {
	    // Login
	    URL url = new URL(PROTOCOL, HOST, "/sanford/Login.asp");
	    URLConnection connection = url.openConnection();
	    connection.setDoOutput(true);
	    
	    OutputStream ostream = connection.getOutputStream();
	    PrintWriter writer = new PrintWriter(ostream);
	    writer.print("username=" + username + "&password=" + password);
	    writer.close();

	    cookie = connection.getHeaderField("Set-Cookie");

	    connected = true;
	}
	catch(java.io.IOException io) {
	    DesktopManager.showErrorMessage("Can't connect to Sanford");
	}

	ProgressDialogManager.closeProgressDialog();
    }

    /** 
     * Returns the company name associated with the given symbol. 
     * 
     * @param	symbol	the stock symbol.
     * @return	the company name.
     */
    public String getCompanyName(String symbol) {

	if(!connected) 
	    return null;
	    
	// Have we already got it?
	String companyName;

	companyName = (String)symbolToName.get(symbol);

	// If we dont have it - try loading it from sanford
	if(companyName == null) {

	    // This query might take a while
            ProgressDialog p = ProgressDialogManager.getProgressDialog();
            p.setNote("Retrieving stock name");
	    boolean symbolFound = false;

	    try {
		// Read quotes
		URL url = new URL(PROTOCOL, HOST, 
				  "/sanford/quotesnews/Quote.asp");

		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Cookie", cookie);
		
		OutputStream os = connection.getOutputStream();
		PrintWriter writer = new PrintWriter(os);
		writer.print("Code=" + symbol + "&" + "type=Basic");
		writer.close();
		
		InputStreamReader isr =
		    new InputStreamReader(connection.getInputStream()); 
		BufferedReader reader = new BufferedReader(isr);
		String line;
		
		while ((line = reader.readLine()) != null) {
		    // Line containing compnay name will contain this string
		    if(line.indexOf("<p><font face") != -1) {
			// Extract the company name from the line (by
			// removing all html "<...>"
			Pattern pat = Pattern.compile("<[^>]*>");
			Matcher m = pat.matcher(line);
			companyName = m.replaceAll("");

			// Remove excess spaces
			pat = Pattern.compile("  ");
			m = pat.matcher(companyName);
			companyName = m.replaceAll("");

			// cache
			symbolToName.put(symbol, companyName);

			break; 
		    }
		}

		reader.close();
	    }
	    catch(java.io.IOException io) {
		DesktopManager.showErrorMessage("Error talking to Sanford");
	    }

	    ProgressDialogManager.closeProgressDialog();
	}
	
	return companyName;
    }

    /**
     * Returns the symbol associated with the given company. 
     * 
     * @param	symbol	a partial company name.
     * @return	the company symbol.
     */
    public String getCompanySymbol(String partialCompanyName) {
	// Not supported
	return null;
    }

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for.
     * @return	whether the symbol was found or not.
     */
    public boolean symbolExists(String symbol) {
	// Try to get name of company - if we cant then we dont have it!
	return (getCompanyName(symbol) == null)? false : true;
    }

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date.
     */
    public TradingDate getLatestQuoteDate() {

	// Get and return last trading day
	TradingDate date = new TradingDate();
	date = date.previous(1);

	return date;
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

	if(!connected) 
	    return quotes;

	Vector dates = Converter.dateRangeToTradingDateVector(startDate,
							      endDate);
	Iterator iterator = dates.iterator();
	TradingDate date;
	
	// This query might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setTitle("Loading quotes " + startDate.toShortString() + " to " +
					endDate.toShortString());
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

	if(!connected) 
	    return quotes;

	// This query might take a while
	ProgressDialog p = ProgressDialogManager.getProgressDialog();
	p.setNote("Loading quotes");

	try {
	    Quote quote;

	    // Read quotes
	    URL url = new URL(PROTOCOL, HOST, 
			      "/sanford/research/HistoricalData.asp");

	    URLConnection connection = url.openConnection();
	    connection.setDoOutput(true);
	    connection.setRequestProperty("Cookie", cookie);

	    OutputStream os = connection.getOutputStream();
	    PrintWriter writer = new PrintWriter(os);
	    writer.print("HistDataDate=" + date.toString("dd/mm/yy"));
	    writer.close();

	    InputStreamReader isr =
		new InputStreamReader(connection.getInputStream()); 
	    BufferedReader reader = new BufferedReader(isr);
	    String line;

	    while ((line = reader.readLine()) != null) {
		quote = filter.toQuote(line);

		if(quote != null && isType(quote, type)) {
		    quotes.add(quote);
                    p.increment();
                }
	    }

	    reader.close();	    
	}
	catch(java.io.IOException io) {
	    DesktopManager.showErrorMessage("Error talking to Sanford");
	}

	ProgressDialogManager.closeProgressDialog();
    
	return quotes;
    }

    /**
     * Return all quotes for the given symbol. They will be returned in
     * order of date.
     *
     * @param	symbol	the symbol to query.
     * @return	a vector of stock quotes.
     * @see Quote
     */
    public Vector getQuotesForSymbol(String symbol) {

	Vector quotes = new Vector();

	if(!connected) 
	    return quotes;

	// This query might take a while
        ProgressDialog p = ProgressDialogManager.getProgressDialog();
        p.setNote("Loading quotes for " + symbol);

	try {
	    Quote quote;

	    // Read quotes
	    URL url = new URL(PROTOCOL, HOST, 
			      "/sanford/research/HistoricalData.asp");

	    URLConnection connection = url.openConnection();
	    connection.setDoOutput(true);
            connection.setRequestProperty("Cookie", cookie);

	    OutputStream os = connection.getOutputStream();
	    PrintWriter writer = new PrintWriter(os);

	    // By default we query back 2 years just so we dont d/l
	    // too much - this limitation will be removed when by default
	    // the app downloads say 2 years instead of by default the whole
	    // thing
	    TradingDate startDate = new TradingDate(); // today
	    startDate = startDate.previous(365*2); // about two years	    
	    writer.print("ASXCode=" + symbol + "&" +
			 "StartDate=" + startDate.toString("dd/mm/yy"));
	    writer.close();

	    InputStreamReader isr =
		new InputStreamReader(connection.getInputStream()); 
	    BufferedReader reader = new BufferedReader(isr);
	    String line;

	    while ((line = reader.readLine()) != null) {
		quote = filter.toQuote(line);

		if(quote != null) {
		    quotes.add(quote);
                    p.increment();
                }
	    }

	    reader.close();
	    
	}
	catch(java.io.IOException io) {
	    DesktopManager.showErrorMessage("Error talking to Sanford");
	}

	ProgressDialogManager.closeProgressDialog();
    
	return quotes;
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
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public Vector getDates() {
	// not implemented yet
	return null;
    }
}
