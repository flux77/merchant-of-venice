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
    
    /**
     * Creates a new quote source using the database information specified
     * in the user preferences.
     */
    public DatabaseQuoteSource() {
	// Get driver
	DatabaseLookup db = DatabaseLookup.getInstance();
	try {
	    // The newInstance() call is a work around for some
	    // broken Java implementations
	    Class.forName(db.get("driverclass")).newInstance(); 
	    
	}
	catch (Exception E) {
	    System.err.println("Unable to load driver.");
	    E.printStackTrace();
	}

	latestQuoteDate = null;

	// connect to database
	connect();
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
	    System.out.println(E.getMessage());
	    System.exit(0);
	}
    }

    /** 
     * Returns the company name associated with the given symbol. 
     * 
     * @param	symbol	the stock symbol.
     * @return	the company name.
     */
    public String getCompanyName(String symbol) {

	String name = null;

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
		System.out.println("SQLException: " + E.getMessage());
		System.out.println("SQLState:     " + E.getSQLState());
		System.out.println("VendorError:  " + E.getErrorCode());
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

	String symbol = null;

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
		System.out.println("SQLException: " + E.getMessage());
		System.out.println("SQLState:     " + E.getSQLState());
		System.out.println("VendorError:  " + E.getErrorCode());
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
		System.out.println("SQLException: " + E.getMessage());
		System.out.println("SQLState:     " + E.getSQLState());
		System.out.println("VendorError:  " + E.getErrorCode());
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
		System.out.println("SQLException: " + E.getMessage());
		System.out.println("SQLState:     " + E.getSQLState());
		System.out.println("VendorError:  " + E.getErrorCode());
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
		System.out.println("SQLException: " + E.getMessage());
		System.out.println("SQLState:     " + E.getSQLState());
		System.out.println("VendorError:  " + E.getErrorCode());
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

	return executeQuery(selectAllString() + 
			    whereClauseString() +  
			    dateRangeString(startDate, endDate) +
			    andString() + 
			    restrictTypeString(type) +
			    orderByDateString());
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
	return executeQuery(selectAllString() +
			    whereClauseString() + 
			    specificSymbolString(symbol) +
			    orderByDateString());
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
	
	else
	    return "LENGTH("+db.get("prices.symbol")+") = 3 " + andString() + 
		"LEFT("+db.get("prices.symbol")+", 1) = 'X' ";
    }

    private String specificSymbolString(String symbol) {
	return db.get("prices.symbol")+" = '" + symbol + "' ";
    }

    private String orderByDateString() {
	return "ORDER BY "+db.get("prices.date");
    }
}


