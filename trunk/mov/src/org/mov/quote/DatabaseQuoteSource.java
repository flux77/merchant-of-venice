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

    // Buffer last trading date in database
    private TradingDate latestQuoteDate;

    // When we are importing, first check to make sure the database is OK
    private boolean readyForImport = false;

    // MySQL driver info
    private final static String DRIVER_NAME =		"mysql";
    private final static String DRIVER_CLASS =		
	"org.gjt.mm.mysql.Driver";

    // Shares table
    private final static String SHARE_TABLE_NAME =     	"shares";
    private final static String SYMBOL_FIELD =		"symbol";
    private final static String DATE_FIELD =		"date";
    private final static String DAY_OPEN_FIELD =	"open";
    private final static String DAY_CLOSE_FIELD =	"close";
    private final static String DAY_HIGH_FIELD =	"high";
    private final static String DAY_LOW_FIELD =		"low";
    private final static String DAY_VOLUME_FIELD =	"volume";

    // Shares indices
    private final static String DATE_INDEX_NAME =	"date_index";
    private final static String SYMBOL_INDEX_NAME =	"symbol_index";
    
    // Info table
    private final static String INFO_TABLE_NAME =     	"info";
    private final static String NAME_FIELD =		"name";

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

	latestQuoteDate = null;

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
    public String getCompanyName(String symbol) {

	String name = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + NAME_FIELD + " FROM " + INFO_TABLE_NAME + 
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
    public String getCompanySymbol(String partialCompanyName) {

	String symbol = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + symbol.toUpperCase() + 
		     " FROM " + INFO_TABLE_NAME + " WHERE LOCATE(" +
		     "UPPER('" + partialCompanyName + "'), " +
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
     * @param	symbol	the symbol we are searching for.
     * @return	whether the symbol was found or not.
     */
    public boolean symbolExists(String symbol) {
	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MIN(" + DATE_FIELD + ") FROM " + 
		     SHARE_TABLE_NAME + " WHERE " + SYMBOL_FIELD + " = '"
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
		Stock stock;

		int i = 0;

		while (RS.next()) {
		    stock = new Stock(RS.getString(SYMBOL_FIELD).toLowerCase(),
				      new TradingDate(RS.getDate(DATE_FIELD)),
				      RS.getInt(DAY_VOLUME_FIELD),
				      RS.getFloat(DAY_LOW_FIELD),
				      RS.getFloat(DAY_HIGH_FIELD),
				      RS.getFloat(DAY_OPEN_FIELD),
				      RS.getFloat(DAY_CLOSE_FIELD));
		    table.add(stock);
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
	return "SELECT * FROM " + SHARE_TABLE_NAME;
    }

    private String whereClauseString() {
	return " WHERE ";
    }

    private String dateRangeString(TradingDate startDate,
				   TradingDate endDate) {
	return DATE_FIELD +" >= '" + startDate + "' " + andString() + 
	    DATE_FIELD +" <= '" + endDate + "' ";
    }

    private String dateString(TradingDate date) {
	return DATE_FIELD +" = '" + date + "' ";
    }

    private String andString() {
	return "AND ";
    }

    private String restrictTypeString(int type) {
	if(type == ALL_COMMODITIES)
	    return "LEFT(" + SYMBOL_FIELD + ", 1) != 'X' ";
    
	else if(type == COMPANIES_AND_FUNDS)
	    return "LENGTH(" + SYMBOL_FIELD + ") = 3 " + andString() + 
		"LEFT(" + SYMBOL_FIELD + ",1) != 'X' ";
	
	else if(type == INDICES)
	    return "LENGTH(" + SYMBOL_FIELD + ") = 3 " + andString() + 
		"LEFT(" + SYMBOL_FIELD + ", 1) = 'X' ";
	else {
	    return "";
	}
    }

    private String specificSymbolString(String symbol) {
	return SYMBOL_FIELD +" = '" + symbol + "' ";
    }

    private String orderByDateString() {
	return "ORDER BY " + DATE_FIELD;
    }

    private boolean createTable(String databaseName) {

	boolean success = false;

	try {
	    // 1. Create the table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("CREATE TABLE " + SHARE_TABLE_NAME + " (" + 
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
		("CREATE INDEX " + DATE_INDEX_NAME + " ON " + 
		 SHARE_TABLE_NAME + " (" + DATE_FIELD + ")");
	    RS = statement.executeQuery
		("CREATE INDEX " + SYMBOL_INDEX_NAME + " ON " + 
		 SHARE_TABLE_NAME + " (" + SYMBOL_FIELD + ")");

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

    public void importQuotes(String databaseName, Vector dayQuotes,
			     TradingDate date) {

	if(connection == null)
	    return;

	if(!readyForImport) 
	    readyForImport = prepareForImport(databaseName);

	if(readyForImport) {
	    Iterator iterator = dayQuotes.iterator();
	    Stock stock;
	    StringBuffer insertString = new StringBuffer();
	    boolean firstQuote = true;
	    String dateString = date.toString();

	    // Build single query to insert stocks for a whole day into 
	    // the table
	    while(iterator.hasNext()) {
		stock = (Stock)iterator.next();

		if(firstQuote) {
		    insertString.append("INSERT INTO " + SHARE_TABLE_NAME + 
					" VALUES (");
		    firstQuote = false;
		}
		else
		    insertString.append(", (");

		// Add new quote
		insertString.append("'" + dateString +		"', " +
				    "'" + stock.getSymbol() +	"', " +
				    "'" + stock.getDayOpen() +	"', " +
				    "'" + stock.getDayClose() + "', " +
				    "'" + stock.getDayHigh() +	"', " +
				    "'" + stock.getDayLow() +	"', " +
				    "'" + stock.getVolume() +	"')");
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

	if(connection == null)
	    return dates;
	
	// This might take a while
	boolean owner = 
	    Progress.getInstance().open("Retreiving dates from database", 1);

	try {
	    // 1. Create the table
	    Statement statement = connection.createStatement();
	    ResultSet RS = statement.executeQuery
		("SELECT DISTINCT(" + DATE_FIELD + ") FROM " +
		 SHARE_TABLE_NAME);

	    while(RS.next()) {
		dates.add(new TradingDate(RS.getDate(1)));
	    }

	}
	catch (SQLException E) {
	    DesktopManager.showErrorMessage("Error talking to database");
	}

	Progress.getInstance().close(owner);

	return dates;
    }
}


