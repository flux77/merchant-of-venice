package org.mov.util;

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

import org.mov.portfolio.*;

public class Database
{
    private Connection connection = null;
    private static Database instance = null;
    private static boolean launchedMysql = false;

    // Only load indices (e.g. All Ordinaries, Mining etc) - we assume
    // all three letter symbols starting with 'X' are indices. This is
    // *wrong*
    public static final int INDICES = 0;

    // Only load 3 letter symbols which are generally companies + mutual funds
    // Loads all 3 letter symbols except ones starting with 'X' (cause
    // some of them are indicies) - so skips some legit X symbols
    public static final int COMPANIES_AND_FUNDS = 1;
    
    // Load all commodoties listed on ASX (everything except indices)
    // everything except symbols starting with X. Again this is not quite
    // right.
    public static final int ALL_COMMODITIES = 2;

    // Indicates a single symbol
    public static final int SINGLE_SYMBOL = 3;

    public static Database getInstance() {
	if(instance == null) {
	    instance = new Database();
	}
	return instance;
    }

    private DatabaseLookup db;

    private Database() {
	db = new DatabaseLookup();

	// Get driver
	try {
	    // The newInstance() call is a work around for some
	    // broken Java implementations
	    Class.forName(db.driverclass).newInstance(); 
	    
	}
	catch (Exception E) {
	    System.err.println("Unable to load driver.");
	    E.printStackTrace();
	}

	// connect to database
	connect();
    }

    private void connect() {

	try {
	    connection = 
		DriverManager.
		getConnection("jdbc:"+db.drivername+"://db/shares?user="+db.user+"&password="+db.password);
	}
	catch (SQLException E) {

	    // If we got here - maybe mysql daemon hasnt started
	    if(!launchedMysql) {
		try {
		    Runtime.getRuntime().exec("mysqld");
		} catch(java.io.IOException i) {
		    // Display original database connect error
		    System.out.println(E.getMessage());
		    System.exit(0);
		}

		launchedMysql = true;
		connect(); // try to connect again
	    }
	    else { // tried the above - bomb
		System.out.println(E.getMessage());
		System.exit(0);
	    }
	}
    }

    // Get the name of a company from its symbol symbol

    // Buffer this!! should load it in and keep it in memory ie load in
    // the entire table. its not that big.

    public String getCompanyName(String symbol) {

	String name = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT "+db.info.name+" FROM "+db.info+" WHERE "+db.info.symbol+" = '"
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

    // Returns first company whose name contains given text or null if
    // not found
    public String getCompanySymbol(String partialCompanyName) {

	String symbol = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + symbol.toUpperCase() + 
		     " FROM "+db.info+" WHERE LOCATE(" +
		     "UPPER('" + partialCompanyName + "'), "+db.info.name+") != 0");

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

    // Get the earliest quote date for the given company, this is useful
    // for two things:
    // 1) Seeing if a company exists in our database
    // 2) Seeing how long a company has been listed
    public TradingDate getEarliestQuoteDate(String symbol) {
	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MIN("+db.prices.date+") FROM shares WHERE "+db.prices.symbol+" = '"
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
	    return new TradingDate(date);
	else
	    return null;
    }

    public TradingDate getLatestQuoteDate() {
	java.util.Date date = null;

	if(connection != null) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT MAX("+db.prices.date+") FROM "+db.prices);

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
	    return new TradingDate(date);
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
		    table.add(new Stock(RS.getString(db.prices.symbol).toLowerCase(),
					new TradingDate(RS.getDate(db.prices.date)),
					RS.getInt(db.prices.volume),
					RS.getFloat(db.prices.low),
					RS.getFloat(db.prices.high),
					RS.getFloat(db.prices.open),
					RS.getFloat(db.prices.close)));
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

    public Vector getQuotesForDate(TradingDate date, int type) {
	return executeQuery(selectAllString() + 
			    whereClauseString() +
			    dateString(date) +
			    andString() + 
			    restrictTypeString(type));
    }

    public Vector getQuotesForSymbol(String symbol) {
	return executeQuery(selectAllString() +
			    whereClauseString() + 
			    specificSymbolString(symbol) +
			    orderByDateString());
    }

    private String selectAllString() {
	return "SELECT * FROM "+db.prices;
    }

    private String whereClauseString() {
	return " WHERE ";
    }

    private String dateRangeString(TradingDate startDate,
				   TradingDate endDate) {
	return db.prices.date+" >= '" + startDate + "' " + andString() + 
	    db.prices.date+" <= '" + endDate + "' ";
    }

    private String dateString(TradingDate date) {
	return db.prices.date+" = '" + date + "' ";
    }

    private String andString() {
	return "AND ";
    }

    private String restrictTypeString(int type) {
	if(type == ALL_COMMODITIES)
	    return "LEFT("+db.prices.symbol+", 1) != 'X' ";
    
	else if(type == COMPANIES_AND_FUNDS)
	    return "LENGTH("+db.prices.symbol+") = 3 " + andString() + 
		"LEFT("+db.prices.symbol+",1) != 'X' ";
	
	else
	    return "LENGTH("+db.prices.symbol+") = 3 " + andString() + 
		"LEFT("+db.prices.symbol+", 1) = 'X' ";
    }

    private String specificSymbolString(String symbol) {
	return db.prices.symbol+" = '" + symbol + "' ";
    }

    private String orderByDateString() {
	return "ORDER BY "+db.prices.date;
    }
}

