package org.mov.quote;

// ye gods this is getting to be a mess - clean up
// theres two types of queries here - queries returning a table of quotes
// or queries returning a single date. 
//
// buffer table containing quotes for the latest date as this is accessed
// quite a bit.

import java.lang.*;
import java.sql.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;

import org.mov.util.*;
import org.mov.portfolio.*;
import org.mov.ui.DesktopManager;

/**
 * Provides functionality to obtain stock quotes from a database. This class
 * implements the QuoteSource interface to allow users to obtain stock
 * quotes in the fastest possible manner.
 *
 * Example:
 * <pre>
 *	Vector quotes = Quote.getSource().getQuotesForSymbol("CBA");
 * </pre>
 *
 * @see Quote
 */
public class DatabaseQuoteSource implements QuoteSource
{
    private Connection connection = null;
    private DatabaseLookup db;

    // Buffer last trading date in database
    private TradingDate latestQuoteDate;

    // When we are importing, first check to make sure the database is OK
    private boolean readyForImport = false;

    // Database internals
    private final static String TABLE_NAME =		"shares";
    private final static String SYMBOL_FIELD =		"symbol";
    private final static String DATE_FIELD =		"date";
    private final static String DAY_OPEN_FIELD =	"open";
    private final static String DAY_CLOSE_FIELD =	"close";
    private final static String DAY_HIGH_FIELD =	"high";
    private final static String DAY_LOW_FIELD =		"low";
    private final static String DAY_VOLUME_FIELD =	"volume";

    private final static String DATE_INDEX_NAME =	"date_index";
    private final static String SYMBOL_INDEX_NAME =	"symbol_index";
    
    /**
     * Creates a new quote source using the database information specified
     * in the user preferences.
     */
    public DatabaseQuoteSource() {

	latestQuoteDate = null;

	// Get driver
	DatabaseLookup db = DatabaseLookup.getInstance();
	try {
	    // The newInstance() call is a work around for some
	    // broken Java implementations
	    Class.forName(db.get("driverclass")).newInstance(); 
	 
	    // connect to database
	    connect();
   
	}
	catch (Exception E) {
	    DesktopManager.showErrorMessage("Unable to load MySQL driver");
	}
    }

    private void connect() {

	try {
	     db = DatabaseLookup.getInstance();

	    connection = 
		DriverManager.
		getConnection("jdbc:"+db.get("drivername")+"://"+db.get("host")+
			      "/"+db.get("dbname")+
			      "?user="+db.get("username")+
			      "&password="+db.get("password"));
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
    public String getCompanyName(String symbol) {

	String name = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT "+db.get("info.name")+" FROM "+db.get("info")+" WHERE "+db.get("info.symbol")+" = '"
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
    public String getCompanySymbol(String partialCompanyName) {

	String symbol = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + symbol.toUpperCase() + 
		     " FROM "+db.get("info")+" WHERE LOCATE(" +
		     "UPPER('" + partialCompanyName + "'), "+db.get("info.name")+") != 0");

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
     * @param	symbol	the symbol we are searching for.
     * @return	whether the symbol was found or not.
     */
    public boolean symbolExists(String symbol) {
	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MIN("+db.get("prices.date")+") FROM shares WHERE "+db.get("prices.symbol")+" = '"
		     + symbol.toUpperCase() + "'");

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

	if(date != null)
	    return true;
	else
	    return false;
    }

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date.
     */
    public TradingDate getLatestQuoteDate() {

	// Do we have it buffered?
	if(latestQuoteDate != null)
	    return latestQuoteDate;

	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MAX("+db.get("prices.date")+") FROM "+db.get("prices"));

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
	    latestQuoteDate = new TradingDate(date);
	    return latestQuoteDate;
	}
	else
	    return null;
    }

    private Vector executeQuery(String query) {
	Vector table = new Vector();

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery(query);

		int i = 0;

		while (RS.next()) {
		    table.add(new Stock(RS.getString(db.get("prices.symbol")).toLowerCase(),
					new TradingDate(RS.getDate(db.get("prices.date"))),
					RS.getInt(db.get("prices.volume")),
					RS.getFloat(db.get("prices.low")),
					RS.getFloat(db.get("prices.high")),
					RS.getFloat(db.get("prices.open")),
					RS.getFloat(db.get("prices.close"))));
		}
		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException E) {
		DesktopManager.showErrorMessage("Error talking to database");
	    }
	}

	return table;
    }

    /**
     * Return a vector of quotes for all stocks in the given date range.
     * The vector will be in order of date then stock symbol.
     *
     * @param	startDate	the start of the date range (inclusive).
     * @param	endDate		the end of the date range (inclusive).
     * @param	type		the type of the search.
     * @return	a vector of stock quotes.
     * @see Stock
     */
    public Vector getQuotesForDates(TradingDate startDate, 
				    TradingDate endDate, 
				    int type) {

	// This query might take a while
	boolean owner = 
	    Progress.getInstance().open("Loading quotes " + 
					startDate.toShortString() + " to " +
					endDate.toShortString(), 1);

	Vector query =  executeQuery(selectAllString() + 
				     whereClauseString() +  
				     dateRangeString(startDate, endDate) +
				     andString() + 
				     restrictTypeString(type) +
				     orderByDateString());
	// A next right before a close is OK because we might not be the
	// owner so it might not close straight away
	Progress.getInstance().next();
	Progress.getInstance().close(owner); 

	return query;
    }

    /**
     * Return a vector of all quotes in the given date.
     * The vector will be in order of stock symbol.
     *
     * @param	date	the date to return quotes for.
     * @param	type	the type of the search.
     * @return	a vector of stock quotes.
     * @see Stock
     */
    public Vector getQuotesForDate(TradingDate date, int type) {
	return executeQuery(selectAllString() + 
			    whereClauseString() +
			    dateString(date) +
			    andString() + 
			    restrictTypeString(type));
    }

    /**
     * Return all quotes for the given symbol. They will be returned in
     * order of date.
     *
     * @param	symbol	the symbol to query.
     * @return	a vector of stock quotes.
     * @see Stock
     */
    public Vector getQuotesForSymbol(String symbol) {

	// This query might take a while
	boolean owner = 
	    Progress.getInstance().open("Loading quotes for " + symbol, 1);

	Vector query =  executeQuery(selectAllString() +
				     whereClauseString() + 
				     specificSymbolString(symbol) +
				     orderByDateString());
	
	// A next right before a close is OK because we might not be the
	// owner so it might not close straight away
	Progress.getInstance().next();
	Progress.getInstance().close(owner); 

	return query;
    }

    private String selectAllString() {
	return "SELECT * FROM "+db.get("prices");
    }

    private String whereClauseString() {
	return " WHERE ";
    }

    private String dateRangeString(TradingDate startDate,
				   TradingDate endDate) {
	return db.get("prices.date")+" >= '" + startDate + "' " + andString() + 
	    db.get("prices.date")+" <= '" + endDate + "' ";
    }

    private String dateString(TradingDate date) {
	return db.get("prices.date")+" = '" + date + "' ";
    }

    private String andString() {
	return "AND ";
    }

    private String restrictTypeString(int type) {
	if(type == ALL_COMMODITIES)
	    return "LEFT("+db.get("prices.symbol")+", 1) != 'X' ";
    
	else if(type == COMPANIES_AND_FUNDS)
	    return "LENGTH("+db.get("prices.symbol")+") = 3 " + andString() + 
		"LEFT("+db.get("prices.symbol")+",1) != 'X' ";
	
	else if(type == INDICES)
	    return "LENGTH("+db.get("prices.symbol")+") = 3 " + andString() + 
		"LEFT("+db.get("prices.symbol")+", 1) = 'X' ";
	else {
	    return "";
	}
    }

    private String specificSymbolString(String symbol) {
	return db.get("prices.symbol")+" = '" + symbol + "' ";
    }

    private String orderByDateString() {
	return "ORDER BY "+db.get("prices.date");
    }

    private boolean createTable(String databaseName) {

	boolean success = false;

	try {
	    // 1. Create the table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("CREATE TABLE " + TABLE_NAME + " (" + 
		 DATE_FIELD +		" DATE NOT NULL, "+ 
		 SYMBOL_FIELD +		" CHAR(6) NOT NULL, "+
		 DAY_OPEN_FIELD +	" FLOAT DEFAULT 0.0, "+
		 DAY_CLOSE_FIELD +	" FLOAT DEFAULT 0.0, "+
		 DAY_HIGH_FIELD +	" FLOAT DEFAULT 0.0, "+
		 DAY_LOW_FIELD +	" FLOAT DEFAULT 0.0, "+
		 DAY_VOLUME_FIELD +	" INT DEFAULT 0, "+
		 "PRIMARY KEY(" + DATE_FIELD + ", " + SYMBOL_FIELD + "))");

	    // 2. Create a couple of indices to speed things up
	    RS = statement.executeQuery
		("CREATE INDEX " + DATE_INDEX_NAME + " ON " + TABLE_NAME + 
		 " (" + DATE_FIELD + ")");
	    RS = statement.executeQuery
		("CREATE INDEX " + SYMBOL_INDEX_NAME + " ON " + TABLE_NAME + 
		 " (" + SYMBOL_FIELD + ")");

	    success = true;
	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error creating table");
	}

	return success;
	    
    }

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
			
		    if(traverseTables.equals(TABLE_NAME))
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

    public void importQuotes(String databaseName, QuoteSource source, 
			     TradingDate date) {

	if(connection == null)
	    return;

	if(!readyForImport) 
	    readyForImport = prepareForImport(databaseName);

	if(readyForImport) {
	    // Load quotes from source
	    Vector quotes = source.getQuotesForDate(date, ALL_COMMODITIES);
	    Iterator iterator = quotes.iterator();
	    Stock stock;
	    StringBuffer insertString = new StringBuffer();
	    boolean firstQuote = true;

	    // Build single query to insert stocks for a whole day into 
	    // the table
	    while(iterator.hasNext()) {
		stock = (Stock)iterator.next();

		if(firstQuote) {
		    insertString.append("INSERT INTO " + TABLE_NAME + 
					" VALUES (");
		    firstQuote = false;
		}
		else
		    insertString.append(", (");

		// Add new quote
		insertString.append("'" + stock.getDate() +	"', " +
				    "'" + stock.getSymbol() + "', " +
				    "'" + stock.getDayOpen() + "', " +
				    "'" + stock.getDayClose() + "', " +
				    "'" + stock.getDayHigh() + "', " +
				    "'" + stock.getDayLow() + "', " +
				    "'" + stock.getVolume() + "')");
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

    /** 
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public Vector getDates() {
	Vector dates = new Vector();

	try {
	    // 1. Create the table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("SELECT DISTINCT(" + DATE_FIELD + ") FROM " +
		 TABLE_NAME);

	    while(RS.next()) {
		dates.add(new TradingDate(RS.getDate(1)));
	    }

	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error talking to database");
	}

	return dates;
    }
}


