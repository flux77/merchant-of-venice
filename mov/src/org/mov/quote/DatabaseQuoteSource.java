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

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

import org.mov.util.*;
import org.mov.ui.DesktopManager;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;

/**
 * Provides functionality to obtain stock quotes from a database. This class
 * implements the QuoteSource interface to allow users to obtain stock
 * quotes in the fastest possible manner.
 *
 * Example:
 * <pre>
 *      QuoteRange quoteRange = new QuoteRange("CBA");
 *      QuoteBundle quoteBundle = new QuoteBundle(quoteRange);
 *      try {
 *	    float = quoteBundle.getQuote("CBA", Quote.DAY_OPEN, 0);
 *      }
 *      catch(QuoteNotLoadedException e) {
 *          //...
 *      }
 * </pre>
 *
 * @see Quote
 * @see QuoteRange
 * @see QuoteBundle
 */
public class DatabaseQuoteSource implements QuoteSource
{
    private Connection connection = null;

    // Buffer first and last trading date in database
    private TradingDate firstDate;
    private TradingDate lastDate;

    // When we are importing, first check to make sure the database is OK
    private boolean readyForImport = false;

    // MySQL driver info
    private final static String DRIVER_NAME       = "mysql";
    private final static String DRIVER_CLASS      = "org.gjt.mm.mysql.Driver";

    // Shares table
    private final static String SHARE_TABLE_NAME  = "shares";
    private final static String SYMBOL_FIELD      = "symbol";
    private final static String DATE_FIELD        = "date";
    private final static String DAY_OPEN_FIELD    = "open";
    private final static String DAY_CLOSE_FIELD   = "close";
    private final static String DAY_HIGH_FIELD    = "high";
    private final static String DAY_LOW_FIELD     = "low";
    private final static String DAY_VOLUME_FIELD  = "volume";

    // Shares indices
    private final static String DATE_INDEX_NAME   = "date_index";
    private final static String SYMBOL_INDEX_NAME = "symbol_index";
    
    // Info table
    private final static String LOOKUP_TABLE_NAME = "lookup";
    private final static String NAME_FIELD        = "name";

    // Keep list of dates in database when importing
    private Vector allDates = null;

    /**
     * Creates a new quote source using the database information specified
     * in the user preferences.
     *
     * @param	host	the host location of the database
     * @param	port	the port of the database
     * @param	datbase	the name of the database
     * @param	username	the user login
     * @param	password	the password for the login
     */
    public DatabaseQuoteSource(String host, String port, String database,
			       String username, String password) {

	lastDate = null;

	// Get driver
	try {
	    // The newInstance() call is a work around for some
	    // broken Java implementations
	    Class.forName(DRIVER_CLASS).newInstance(); 
	 
	    // connect to database
	    connect(host, port, database, username, password);
   
	}
	catch (Exception E) {
	    DesktopManager.showErrorMessage("Unable to load MySQL driver");
	}
    }

    // Connect to the database
    private void connect(String host, String port, String database,
			 String username, String password){

	try {
	    connection = 
		DriverManager.
		getConnection("jdbc:" + DRIVER_NAME +"://"+ host +
			      ":" + port + 
			      "/"+ database +
			      "?user=" + username +
			      "&password=" + password);
	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Can't connect to database");
	}
    }

    /** 
     * Returns the company name associated with the given symbol. 
     * 
     * @param	symbol	the stock symbol.
     * @return	the company name.
     */
    public String getSymbolName(String symbol) {

	String name = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + NAME_FIELD + " FROM " + LOOKUP_TABLE_NAME + 
		     " WHERE " + SYMBOL_FIELD + " = '"
		     + symbol.toUpperCase() + "'");

		// Import SQL data into vector
		RS.next();

		// Get only entry which is the name
		name = RS.getString(1);

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		// not a big deal if this fails
	    }
	}

	return name;
    }

    /**
     * Returns the symbol associated with the given company. 
     * 
     * @param	symbol	a partial company name.
     * @return	the company symbol.
     */
    public String getSymbol(String partialSymbolName) {

	String symbol = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + symbol.toUpperCase() + 
		     " FROM " + LOOKUP_TABLE_NAME + " WHERE LOCATE(" +
		     "UPPER('" + partialSymbolName + "'), " +
		     NAME_FIELD + ") != 0");

		// Import SQL data into vector
		RS.next();

		// Get only entry which is the name
		symbol = RS.getString(1);

		if(symbol != null)
		    symbol = symbol.toLowerCase();

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		// not a big deal if this fails
	    }
	}

	return symbol;
    }

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for
     * @return	whether the symbol was found or not
     */
    public boolean symbolExists(String symbol) {
        boolean symbolExists = false;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		// Return the first date found matching the given symbol.
		// If no dates are found - the symbol is unknown to us.
		// This should take << 1s
                String query = 
                    new String("SELECT " + DATE_FIELD + " FROM " + 
                               SHARE_TABLE_NAME + " WHERE " + SYMBOL_FIELD + " = '"
                               + symbol.toUpperCase() + "' " +
                               "LIMIT 1");
		ResultSet RS = statement.executeQuery(query);

                // Find out if it has any rows
                RS.last();
                symbolExists = RS.getRow() > 0;

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}

        return symbolExists;
    }

    /**
     * Return the first date in the database that has any quotes.
     *
     * @return	oldest date with quotes
     */
    public TradingDate getFirstDate() {

	// Do we have it buffered?
	if(firstDate != null)
	    return firstDate;

	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MIN(" + DATE_FIELD + ") FROM " + 
		     SHARE_TABLE_NAME);

		// Import SQL data into vector
		RS.next();

		// Get only entry which is the date
		date = RS.getDate(1);

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}

	if(date != null) {
	    firstDate = new TradingDate(date);
	    return firstDate;
	}
	else
	    return null;
    }

    /**
     * Return the last date in the database that has any quotes.
     *
     * @return	newest date with quotes
     */
    public TradingDate getLastDate() {

	// Do we have it buffered?
	if(lastDate != null)
	    return lastDate;

	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MAX(" + DATE_FIELD + ") FROM " + 
		     SHARE_TABLE_NAME);

		// Import SQL data into vector
		RS.next();

		// Get only entry which is the date
		date = RS.getDate(1);

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}

	if(date != null) {
	    lastDate = new TradingDate(date);
	    return lastDate;
	}
	else
	    return null;
    }

    /**
     * Is the given symbol a market index? 
     *
     * @param	symbol to test
     * @return	yes or no
     */
    public boolean isMarketIndex(String symbol) {
        // HACK. It needs to keep a table which maintains a flag
        // for whether a symbol is an index or not.
	assert symbol != null;

	if(symbol.length() == 3 && symbol.toUpperCase().charAt(0) == 'X')
	    return true;
	else
	    return false;
    }

    /**
     * Return a vector of quotes for all quotes in the given quote range.
     *
     * @param	quoteRange	the range of quotes to load
     * @return	a vector of stock quotes
     * @see Quote
     */    
    public Vector loadQuoteRange(QuoteRange quoteRange) {

	String queryString = buildSQLString(quoteRange);

	// This query might take a while...
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.setNote("Loading Quotes...");
        progress.setIndeterminate(true);

	Vector result = executeSQLString(progress, queryString);

        ProgressDialogManager.closeProgressDialog(progress);

        return result;
    }

    // Takes a string containing an SQL statement and then executes it. Returns
    // a vector of quotes.
    private Vector executeSQLString(ProgressDialog progress, String SQLString) {
	Vector quotes = new Vector();

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();	       
		ResultSet RS = statement.executeQuery(SQLString);

		// All this to find out how many rows in the result set
		RS.last();
		progress.setMaximum(RS.getRow());
		progress.setProgress(0);
		progress.setIndeterminate(false);
		RS.beforeFirst();

		while (RS.next()) {
		    quotes.add(new Quote(RS.getString(SYMBOL_FIELD).toLowerCase(),
					 new TradingDate(RS.getDate(DATE_FIELD)),
					 RS.getInt(DAY_VOLUME_FIELD),
					 RS.getFloat(DAY_LOW_FIELD),
					 RS.getFloat(DAY_HIGH_FIELD),
					 RS.getFloat(DAY_OPEN_FIELD),
					 RS.getFloat(DAY_CLOSE_FIELD)));

                    // Update the progress bar per row
                    progress.increment();
                }

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}

	return quotes;
    }

    // Creates an SQL statement that will return all the quotes in the given
    // quote range.
    private String buildSQLString(QuoteRange quoteRange) {

	//
	// 1. Create select line
	//

	String queryString = "SELECT * FROM " + SHARE_TABLE_NAME + " WHERE ";

	//
	// 2. Filter select by symbols we are looking for
	//

	String filterString = new String("");

	if(quoteRange.getType() == QuoteRange.GIVEN_SYMBOLS) {
	    Vector symbols = quoteRange.getAllSymbols();

	    if(symbols.size() == 1) {
		String symbol = ((String)symbols.firstElement()).toUpperCase();

		filterString = 
		    filterString.concat(SYMBOL_FIELD + " = '" + symbol + "' ");
	    }
	    else {
		assert symbols.size() > 1;

		filterString = filterString.concat(SYMBOL_FIELD + " IN (");
		Iterator iterator = symbols.iterator();

		while(iterator.hasNext()) {
		    String symbol = ((String)iterator.next()).toUpperCase();

		    filterString = filterString.concat("'" + symbol + "'");

		    if(iterator.hasNext()) 
			filterString = filterString.concat(", ");
		}

		filterString = filterString.concat(") ");
	    }
	}
	else if(quoteRange.getType() == QuoteRange.ALL_SYMBOLS) {
	    // nothing to do
	}
	else if(quoteRange.getType() == QuoteRange.ALL_ORDINARIES) {
	    filterString = filterString.concat("LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
					     "LEFT(" + SYMBOL_FIELD + ",1) != 'X' ");
	}
	else {
	    assert quoteRange.getType() == QuoteRange.MARKET_INDICES;

	    filterString = filterString.concat("LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
					     "LEFT(" + SYMBOL_FIELD + ", 1) = 'X' ");
	}

	//
	// 3. Filter select by date range
	//
	
	// No dates in quote range, mean load quotes for all dates in the database
	if(quoteRange.getFirstDate() == null) {
	    // nothing to do
	}

	// If they are the same its only one day
	else if(quoteRange.getFirstDate().equals(quoteRange.getLastDate())) {
	    if(filterString.length() > 0)
		filterString = filterString.concat("AND ");

	    filterString = 
		filterString.concat(DATE_FIELD + " = '" + quoteRange.getFirstDate() + "' ");
	}

	// Otherwise check within a range of dates
	else {
	    if(filterString.length() > 0)
		filterString = filterString.concat("AND ");

	    filterString = 
		filterString.concat(DATE_FIELD + " >= '" + quoteRange.getFirstDate() + "' AND " +
				    DATE_FIELD + " <= '" + quoteRange.getLastDate() + "' ");
	}

	return queryString.concat(filterString);
    }

    // Creates database tables
    private boolean createTable(String databaseName) {

	boolean success = false;

	try {
	    // 1. Create the shares table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("CREATE TABLE " + SHARE_TABLE_NAME + " (" + 
		 DATE_FIELD +		" DATE NOT NULL, " + 
		 SYMBOL_FIELD +		" CHAR(6) NOT NULL, " +
		 DAY_OPEN_FIELD +	" FLOAT DEFAULT 0.0, " +
		 DAY_CLOSE_FIELD +	" FLOAT DEFAULT 0.0, " +
		 DAY_HIGH_FIELD +	" FLOAT DEFAULT 0.0, " +
		 DAY_LOW_FIELD +	" FLOAT DEFAULT 0.0, " +
		 DAY_VOLUME_FIELD +	" INT DEFAULT 0, " +
		 "PRIMARY KEY(" + DATE_FIELD + ", " + SYMBOL_FIELD + "))");

	    // 2. Create a couple of indices to speed things up
	    RS = statement.executeQuery
		("CREATE INDEX " + DATE_INDEX_NAME + " ON " + 
		 SHARE_TABLE_NAME + " (" + DATE_FIELD + ")");
	    RS = statement.executeQuery
		("CREATE INDEX " + SYMBOL_INDEX_NAME + " ON " + 
		 SHARE_TABLE_NAME + " (" + SYMBOL_FIELD + ")");

	    // 3. Create the lookup table
	    RS = statement.executeQuery
		("CREATE TABLE " + LOOKUP_TABLE_NAME + " (" +
		 SYMBOL_FIELD +		" CHAR(6) NOT NULL, " +
		 NAME_FIELD +		" VARCHAR(100), " +
		 "PRIMARY KEY(" + SYMBOL_FIELD + "))");

	    success = true;
	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error creating table");
	}

	return success;	    
    }

    // Make sure database and tables exist before doing import, if
    // the database or tables do not exist then create them
    private boolean prepareForImport(String databaseName) {

	boolean success = true;

	try {
	    DatabaseMetaData meta = connection.getMetaData();

	    // 1. Check database exists
	    {
		ResultSet RS = meta.getCatalogs(); 
		String traverseDatabaseName;
		boolean foundDatabase = false;
		
		while(RS.next()) {
		    traverseDatabaseName = RS.getString(1);
		    
		    if(traverseDatabaseName.equals(databaseName)) {
			foundDatabase = true;
			break;
		    }
		}
		
		if(!foundDatabase) {
		    DesktopManager.showErrorMessage("Can't find " + 
						    databaseName +
						    " database");
		    return false;
		}
	    }

	    // 2. Check table exists - if not create it
	    {
		ResultSet RS = 
		    meta.getTables(databaseName, null, databaseName, null);
		String traverseTables;
		boolean foundTable = false;

		while(RS.next()) {
		    traverseTables = RS.getString(1);
			
		    if(traverseTables.equals(SHARE_TABLE_NAME))
			foundTable = true;
		}
		
		// No table? Well have to go create it
		if(!foundTable) 
		    success = createTable(databaseName);
	    }

	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error talking to database");
	    return false;
	}

	// If we got here its all ready for importing
	return success;
    }

    /**
     * Import quotes into the database.
     *
     * @param	databaseName	the name of the database
     * @param	dayQuotes	a vector of quotes on a given day to import
     * @param	date		the date for the day quotes
     */
    public void importQuotes(String databaseName, Vector dayQuotes,
			     TradingDate date) {

	if(connection == null)
	    return;

	if(!readyForImport) 
	    readyForImport = prepareForImport(databaseName);

	// Dont import a date thats already there
	if(databaseContainsDate(date))
	    return;
	
	if(readyForImport) {
	    Iterator iterator = dayQuotes.iterator();
	    Quote quote;
	    StringBuffer insertString = new StringBuffer();
	    boolean firstQuote = true;
	    String dateString = date.toString();

	    // Build single query to insert stocks for a whole day into 
	    // the table
	    while(iterator.hasNext()) {
		quote = (Quote)iterator.next();

		if(firstQuote) {
		    insertString.append("INSERT INTO " + SHARE_TABLE_NAME + 
					" VALUES (");
		    firstQuote = false;
		}
		else
		    insertString.append(", (");

		// Add new quote
		insertString.append("'" + dateString +		"', " +
				    "'" + quote.getSymbol().toUpperCase() + 
				    "', " +
				    "'" + quote.getDayOpen() +	"', " +
				    "'" + quote.getDayClose() + "', " +
				    "'" + quote.getDayHigh() +	"', " +
				    "'" + quote.getDayLow() +	"', " +
				    "'" + quote.getVolume() +	"')");
	    }

	    // Now insert day quote into database
	    try {
		Statement statement = connection.createStatement();
		ResultSet RS = statement.executeQuery(insertString.toString());
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}
    }

    // Return whether the database contains the given date. If it does
    // it will return true, otherwise it will add the date to its internal
    // list and return false.
    private boolean databaseContainsDate(TradingDate date) {

	// Make sure latest quote date is loaded
	getLastDate();
	
	// Already got it?
	if(lastDate != null && date.equals(lastDate))
	    return true;
	
	// If the date isnt after the above, well have to load all the
	// dates to check
	if(lastDate != null && !date.after(lastDate)) {
	    if(allDates == null) 
		allDates = getDates();
	    
	    // Don't import if the database already contains the date
	    if(containsDate(allDates, date))
		return true;
	    
	    // No database doesnt contain the date! Update our
	    // reference of what dates are in the database
	    if(allDates != null)
		allDates.add(date);
	    if(lastDate.before(date))
		lastDate = date;
	}
	
	// Dont have date
	return false;
    }

    // Return whether the given vector contains the given date
    private boolean containsDate(Vector dates, TradingDate date) {
	Iterator iterator = dates.iterator();
	TradingDate traverseDate;

	while(iterator.hasNext()) {
	    traverseDate = (TradingDate)iterator.next();

	    if(date.equals(traverseDate)) 
		return true;
	}
	
	// If we got here it wasnt found
	return false;
    }

    /** 
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public Vector getDates() {
	Vector dates = new Vector();

	if(connection == null)
	    return dates;
	
	// This might take a while
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.setNote("Getting dates...");
        progress.setIndeterminate(true);

	try {
	    // 1. Create the table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("SELECT DISTINCT(" + DATE_FIELD + ") FROM " +
		 SHARE_TABLE_NAME);

	    while(RS.next()) {
		dates.add(new TradingDate(RS.getDate(1)));
                progress.increment();
	    }

	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error talking to database");
	}

	ProgressDialogManager.closeProgressDialog(progress);

	return dates;
    }
}


