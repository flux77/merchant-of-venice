package org.mov.quote;

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

import org.liquid.misc.*;
import org.mov.util.*;
import org.mov.portfolio.*;
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
    private final static String LOOKUP_TABLE_NAME =    	"lookup";
    private final static String NAME_FIELD =		"name";

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
    public String getCompanySymbol(String partialCompanyName) {

	String symbol = new String("");

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + symbol.toUpperCase() + 
		     " FROM " + LOOKUP_TABLE_NAME + " WHERE LOCATE(" +
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
     * @param	symbol	the symbol we are searching for
     * @return	whether the symbol was found or not
     */
    public boolean symbolExists(String symbol) {
	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		// Return the first date found matching the given symbol.
		// If no dates are found - the symbol is unknown to us.
		// This should take << 1s
		ResultSet RS = statement.executeQuery
		    ("SELECT " + DATE_FIELD + " FROM " + 
		     SHARE_TABLE_NAME + " WHERE " + SYMBOL_FIELD + " = '"
		     + symbol.toUpperCase() + "' " +
		     "LIMIT 1");

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

    // Execute the given query and return the results as a vector of
    // stock quotes
    private Vector executeQuery(String query) {
	Vector table = new Vector();
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
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
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        try {
            progress.setNote("Connecting to source");
            progress.setIndeterminate(true);
            Statement statement = connection.createStatement();	       
            ResultSet RS = statement.executeQuery("SELECT COUNT(*) FROM " + SHARE_TABLE_NAME +
			 " WHERE " + DATE_FIELD +" >= '" + startDate + "' " +
			 " AND " + DATE_FIELD +" <= '" + endDate + "' " + 
			 restrictTypeString(type));
            
            int rows = 0;
            if (RS.next()) {
                // Update the progress bar
                progress.setMaximum(RS.getInt(1));
                progress.setProgress(0);
                progress.setIndeterminate(false);
            }
        } catch (Exception e) {}

        progress.setNote("Loading quotes for " + startDate.toShortString() + " to " +
                                                 endDate.toShortString());

        Vector query = 
	    executeQuery("SELECT * FROM " + SHARE_TABLE_NAME +
			 " WHERE " + DATE_FIELD +" >= '" + startDate + "' " +
			 " AND " + DATE_FIELD +" <= '" + endDate + "' " + 
			 restrictTypeString(type) +
			 " ORDER BY " + DATE_FIELD);

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
        ProgressDialogManager.getProgressDialog().setNote("Retrieving stocks present on date "+date);
	return executeQuery("SELECT * FROM " + SHARE_TABLE_NAME + " " +
			    "WHERE " + DATE_FIELD + " = '" + date + "'");
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
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();

        try {
            progress.setNote("Connecting to source");
            progress.setIndeterminate(true);
            Statement statement = connection.createStatement();	       
            ResultSet RS = statement.executeQuery("SELECT COUNT(*) FROM " + SHARE_TABLE_NAME + " " +
                                                  "WHERE " + SYMBOL_FIELD +" = '" + symbol + "' ");
        
            int rows = 0;
            if (RS.next()) {
                // Update the progress bar
                progress.setMaximum(RS.getInt(1));
                progress.setNote("Retrieving prices");
                progress.setProgress(0);
                progress.setIndeterminate(false);
            }
        } catch (Exception e) {}

        
        Vector query = 
	    executeQuery("SELECT * FROM " + SHARE_TABLE_NAME + " " +
			 "WHERE " + SYMBOL_FIELD +" = '" + symbol + "' " +
			 "ORDER BY " + DATE_FIELD);
	
	return query;
    }

    // Generate SQL construct the restrict query for types of quotes
    // e.g. indices, commodoties, all quotes etc
    private String restrictTypeString(int type) {
	if(type == ALL_COMMODITIES)
	    return " AND LEFT(" + SYMBOL_FIELD + ", 1) != 'X' ";
    
	else if(type == COMPANIES_AND_FUNDS)
	    return " AND LENGTH(" + SYMBOL_FIELD + ") = 3 AND " + 
		"LEFT(" + SYMBOL_FIELD + ",1) != 'X' ";
	
	else if(type == INDICES)
	    return " AND LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
		"LEFT(" + SYMBOL_FIELD + ", 1) = 'X' ";
	else {
	    return "";
	}
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

    // Return whether the database contains the given date. If it does
    // it will return true, otherwise it will add the date to its internal
    // list and return false.
    private boolean databaseContainsDate(TradingDate date) {

	// Make sure latest quote date is loaded
	getLatestQuoteDate();
	
	// Already got it?
	if(latestQuoteDate != null && date.equals(latestQuoteDate))
	    return true;
	
	// If the date isnt after the above, well have to load all the
	// dates to check
	if(latestQuoteDate != null && !date.after(latestQuoteDate)) {
	    if(allDates == null) 
		allDates = getDates();
	    
	    // Don't import if the database already contains the date
	    if(containsDate(allDates, date))
		return true;
	    
	    // No database doesnt contain the date! Update our
	    // reference of what dates are in the database
	    if(allDates != null)
		allDates.add(date);
	    if(latestQuoteDate.before(date))
		latestQuoteDate = date;
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
        progress.setTitle("Getting dates...");
        progress.setNote("");
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

	ProgressDialogManager.closeProgressDialog();

	return dates;
    }
}


