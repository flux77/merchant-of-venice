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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mov.ui.DesktopManager;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;
import org.mov.util.Locale;
import org.mov.util.TradingDate;

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
 * @author Andrew Leppard
 * @see Quote
 * @see QuoteRange
 * @see QuoteBundle
 */
public class DatabaseQuoteSource implements QuoteSource
{
    private Connection connection = null;
    private boolean checkedTables = false;

    // Buffer first and last trading date in database
    private TradingDate firstDate = null;
    private TradingDate lastDate = null;

    // Database Software

    /** MySQL Database. */
    public final static int MYSQL       = 0;

    /** PostgreSQL Database. */
    public final static int POSTGRESQL  = 1;

    /** Hypersonic SQL Database. */
    public final static int HSQLDB      = 2;

    /** Any generic SQL Database. */
    public final static int OTHER       = 3;
    // Mode

    /** Internal database. */
    public final static int INTERNAL = 0;

    /** External database. */
    public final static int EXTERNAL = 1;

    // MySQL driver info
    public final static String MYSQL_SOFTWARE = "mysql";

    // PostgreSQL driver info
    public final static String POSTGRESQL_SOFTWARE = "postgresql";

    // Hypersonic SQL driver info
    public final static String HSQLDB_SOFTWARE    = "hsql";

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

    // Database details
    private int mode;
    
    private String software;
    private String driver;
    
    // Fields for external mode

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    // Fields for internal mode
    private String fileName;

    // Fields for samples mode
    private QuoteFilter filter;
    private List fileURLs;

    /**
     * Creates a new quote source to connect to an external database.
     *
     * @param   software  the database software
     * @param   driver    the class name for the driver to connect to the database
     * @param	host	  the host location of the database
     * @param	port	  the port of the database
     * @param	database  the name of the database
     * @param	username  the user login
     * @param	password  the password for the login
     */
    public DatabaseQuoteSource(String software, String driver, String host, String port, 
			       String database, String username, String password) {

        this.mode = EXTERNAL;
        this.software = software;
        this.driver = driver;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Create a new quote source to connect to an internal HSQL 
     * database stored in the given file.
     *
     * @param fileName name of database file
     */
    public DatabaseQuoteSource(String fileName) {
        mode = INTERNAL;
        software = HSQLDB_SOFTWARE;
        this.driver = "org.hsqldb.jdbcDriver";
        this.fileName = fileName;
    }

    // Get the driver and connect to the database. Return FALSE if failed.
    private boolean checkConnection() {
        boolean success = true;

        // Connect to database if we haven't already
        if(connection == null) {
            // Connect
            success = connect();
        }

        // If we are connected, check the tables exist, if not, create them.
        if(connection != null && !checkedTables)
            success = checkedTables = (checkDatabase() && checkTables());

        return success;
    }

    // Connect to the database
    private boolean connect() {
		try {
		    // Resolve the classname
		    Class.forName(driver);
	
	        // We can operate the HSQLDB mode in one of three different wayys.
            // Construct connection string depending on mode
            String connectionURL = null;
	        
		    // Set up the conection
		    if (mode == INTERNAL && software.equals(HSQLDB_SOFTWARE)) 
		        connectionURL = new String("jdbc:hsqldb:file:/" + fileName);
		    else {
		        connectionURL = new String("jdbc:" + software +"://"+ host +
			         					   ":" + port +
			        					   "/"+ database);
			    if (username != null)
			    	connectionURL += new String("?user=" + username +
			        					   		"&password=" + password);
            }
			DriverManager.getConnection(connectionURL);
		} catch (ClassNotFoundException e) {
		    // Couldn't find the driver!
		    DesktopManager.showErrorMessage(Locale.getString("UNABLE_TO_LOAD_DATABASE_DRIVER", driver, software));
		    return false;
		}
		catch (SQLException e) {
		    DesktopManager.showErrorMessage(Locale.getString("ERROR_CONNECTING_TO_DATABASE",
		            						e.getMessage()));
	            return false;
		}
		return true;

    }

    /**
     * Returns the company name associated with the given symbol.
     *
     * @param	symbol	the stock symbol.
     * @return	the company name.
     */
    public String getSymbolName(Symbol symbol) {

	String name = null;

	if(checkConnection()) {        
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + NAME_FIELD + " FROM " + LOOKUP_TABLE_NAME +
		     " WHERE " + SYMBOL_FIELD + " = '"
		     + symbol + "'");

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
     * @param	partialCompanyName a partial company name.
     * @return	the company symbol.
     */
    public Symbol getSymbol(String partialCompanyName) {

	Symbol symbol = null;

	if(checkConnection()) {
	    try {
		Statement statement = connection.createStatement();
		
		ResultSet RS = statement.executeQuery
		    ("SELECT " + SYMBOL_FIELD + 
		     " FROM " + LOOKUP_TABLE_NAME + " WHERE LOCATE(" +
		     "UPPER('" + partialCompanyName + "'), " +
		     NAME_FIELD + ") != 0");

		// Import SQL data into vector
		RS.next();

		// Get only entry which is the name
                try {
                    symbol = Symbol.find(RS.getString(1));
                }
                catch(SymbolFormatException e) {
                    // Error in data. Ignore.
                }

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
    public boolean symbolExists(Symbol symbol) {
        boolean symbolExists = false;

	if(checkConnection()) {
	    try {
		Statement statement = connection.createStatement();
		
		// Return the first date found matching the given symbol.
		// If no dates are found - the symbol is unknown to us.
		// This should take << 1s

		String query = buildSymbolPresentQuery(symbol);

		ResultSet RS = statement.executeQuery(query);

                // Find out if it has any rows
                symbolExists = RS.next();

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException e) {
                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
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

	if(checkConnection()) {
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
	    catch (SQLException e) {
                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
	    }
	}

	if(date != null) {
	    firstDate = new TradingDate(date);
	    return firstDate;
	}
	else {
            showEmptyDatabaseError();
	    return null;
        }
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

	if(checkConnection()) {
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
	    catch (SQLException e) {
                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
            }
	}

	if(date != null) {
	    lastDate = new TradingDate(date);
	    return lastDate;
	}
	else {
            showEmptyDatabaseError();
	    return null;
        }
    }

    /**
     * Is the given symbol a market index?
     *
     * @param	symbol to test
     * @return	yes or no
     */
    public boolean isMarketIndex(Symbol symbol) {
        // HACK. It needs to keep a table which maintains a flag
        // for whether a symbol is an index or not.
	assert symbol != null;

	if(symbol.length() == 3 && symbol.charAt(0) == 'X')
	    return true;
	else
	    return false;
    }

    /**
     * Load the given quote range into the quote cache.
     *
     * @param	quoteRange	the range of quotes to load
     * @return  <code>TRUE</code> if the operation suceeded
     * @see Quote
     * @see QuoteCache
     */
    public boolean loadQuoteRange(QuoteRange quoteRange) {
        
	String queryString = buildSQLString(quoteRange);
        boolean success;
	
	// This query might take a while...
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.setNote(Locale.getString("LOADING_QUOTES"));
        progress.setIndeterminate(true);
	success = executeSQLString(progress, queryString);
        ProgressDialogManager.closeProgressDialog(progress);

        return success;
    }

    // Takes a string containing an SQL statement and then executes it. Returns
    // a vector of quotes.
    private boolean executeSQLString(ProgressDialog progress, String SQLString) {

	if(checkConnection()) {
	    try {
		Statement statement = connection.createStatement();	
                Thread monitor = cancelOnInterrupt(statement);
                Thread thread = Thread.currentThread();
		ResultSet RS = statement.executeQuery(SQLString);

                // Monitor thread is no longer needed
                monitor.interrupt();

                if(!thread.isInterrupted()) {
                    QuoteCache quoteCache = QuoteCache.getInstance();

                    while (RS.next()) {
                        quoteCache.load(Symbol.find(RS.getString(SYMBOL_FIELD)),
                                        new TradingDate(RS.getDate(DATE_FIELD)),
                                        RS.getInt(DAY_VOLUME_FIELD),
                                        RS.getFloat(DAY_LOW_FIELD),
                                        RS.getFloat(DAY_HIGH_FIELD),
                                        RS.getFloat(DAY_OPEN_FIELD),
                                        RS.getFloat(DAY_CLOSE_FIELD));
                    }
                }                    

		// Clean up after ourselves
		RS.close();
		statement.close();
                return !thread.isInterrupted();
	    }
	    catch(SQLException e) {
                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
	    }
            catch(SymbolFormatException e2) {
                DesktopManager.showErrorMessage(Locale.getString("DATABASE_BADLY_FORMATTED_SYMBOL",
								 e2.getMessage()));
            }
	}

        return false;
    }

    // This function creates a new thread that monitors the current thread
    // for the interrupt call. If the current thread is interrupted it
    // will cancel the given SQL statement. If cancelOnInterrupt() is called,
    // once the SQL statement has finisehd, you should make sure the
    // thread is terminated by calling "interrupt" on the returned thread.
    private Thread cancelOnInterrupt(final Statement statement) {
        final Thread sqlThread = Thread.currentThread();

        Thread thread = new Thread(new Runnable() {
                public void run() {
                    Thread currentThread = Thread.currentThread();

                    while(true) {

                        try {
                            Thread.sleep(1000); // 1s
                        }
                        catch(InterruptedException e) {
                            break;
                        }

                        if(currentThread.isInterrupted())
                            break;
                        
                        if(sqlThread.isInterrupted()) {
                            try {
                                statement.cancel();
                            }
                            catch(SQLException e) {
                                // It's not a big deal if we can't cancel it
                            }
                            break;
                        }
                    }
                }
            });

        thread.start();
        return thread;
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
		    List symbols = quoteRange.getAllSymbols();
	
		    if(symbols.size() == 1) {
			Symbol symbol = (Symbol)symbols.get(0);
	
			filterString =
			    filterString.concat(SYMBOL_FIELD + " = '" + symbol + "' ");
		    }
		    else {
				assert symbols.size() > 1;
		
				filterString = filterString.concat(SYMBOL_FIELD + " IN (");
				Iterator iterator = symbols.iterator();
		
				while(iterator.hasNext()) {
				    Symbol symbol = (Symbol)iterator.next();
		
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
	                                               left(SYMBOL_FIELD, 1) + " != 'X' ");
		}
		else {
		    assert quoteRange.getType() == QuoteRange.MARKET_INDICES;
	
		    filterString = filterString.concat("LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
	                                               left(SYMBOL_FIELD, 1) + " = 'X' ");
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
	
		    filterString =	filterString.concat(DATE_FIELD + " = '" + quoteRange.getFirstDate() + "' ");
		}
	
		// Otherwise check within a range of dates
		else {
		    if(filterString.length() > 0)
		        filterString = filterString.concat("AND ");
	
		    filterString = filterString.concat(DATE_FIELD + " >= '" + quoteRange.getFirstDate() + "' AND " +
					    DATE_FIELD + " <= '" + quoteRange.getLastDate() + "' ");
		}
	
		return queryString.concat(filterString);
    }
	
    // Creates database tables
    private boolean createTables() {
		boolean success = false;
	
		try {
		    // 1. Create the shares table
		    Statement statement = connection.createStatement();
		    statement.executeUpdate("CREATE TABLE " + SHARE_TABLE_NAME + " (" +
					    DATE_FIELD +	" DATE NOT NULL, " +
					    SYMBOL_FIELD +	" CHAR(" + Symbol.MAXIMUM_SYMBOL_LENGTH + 
					    ") NOT NULL, " +
					    DAY_OPEN_FIELD +	" FLOAT DEFAULT 0.0, " +
					    DAY_CLOSE_FIELD +	" FLOAT DEFAULT 0.0, " +
					    DAY_HIGH_FIELD +	" FLOAT DEFAULT 0.0, " +
					    DAY_LOW_FIELD +	" FLOAT DEFAULT 0.0, " +
					    DAY_VOLUME_FIELD +	" INT DEFAULT 0, " +
					    "PRIMARY KEY(" + DATE_FIELD + ", " + SYMBOL_FIELD + "))");
	
		    // 2. Create a couple of indices to speed things up
		    statement.executeUpdate("CREATE INDEX " + DATE_INDEX_NAME + " ON " +
					    SHARE_TABLE_NAME + " (" + DATE_FIELD + ")");
		    statement.executeUpdate("CREATE INDEX " + SYMBOL_INDEX_NAME + " ON " +
					    SHARE_TABLE_NAME + " (" + SYMBOL_FIELD + ")");
	
		    // 3. Create the lookup table
		    statement.executeUpdate("CREATE TABLE " + LOOKUP_TABLE_NAME + " (" +
		            				SYMBOL_FIELD +	" CHAR(" + Symbol.MAXIMUM_SYMBOL_LENGTH + 
		            				") NOT NULL, " +
		            				NAME_FIELD +	" VARCHAR(100), " +
	                                "PRIMARY KEY(" + SYMBOL_FIELD + "))");
	
		    success = true;
		}
		catch (SQLException e) {
            // Since hypersonic won't let us check if the table is already created,
            // we need to ignore the inevitable error about the table already being present.
            if(software != HSQLDB_SOFTWARE)
         	    DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
							     e.getMessage()));
            else
                success = true;

		}
	
		return success;	
	}
	
	private boolean checkDatabase() {
		boolean success = true;
	
	        // Skip this check for hypersonic - it doesn't support it
	        if(software != HSQLDB_SOFTWARE) {
	            try {
	                DatabaseMetaData meta = connection.getMetaData();
	                
	                // Check database exists
	                {
	                    ResultSet RS = meta.getCatalogs();
	                    String traverseDatabaseName;
	                    boolean foundDatabase = false;
	                    
	                    while(RS.next()) {
	                        traverseDatabaseName = RS.getString(1);
	                        
	                        if(traverseDatabaseName.equals(database)) {
	                            foundDatabase = true;
	                            break;
	                        }
	                    }
	                    
	                    if(!foundDatabase) {
	                        DesktopManager.showErrorMessage(Locale.getString("CANT_FIND_DATABASE",
	                                                                         database));
	                        return false;
	                    }
	                }
	            }
	            catch (SQLException e) {
	                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
	                                                                 e.getMessage()));
	                return false;
	            }
	        }
	
		// If we got here the database is available
		return success;
    }

    private boolean checkTables() {
        boolean success = false;

        try {
            boolean foundTable = false;

            // Skip this check for hypersonic - it doesn't support it
            if(software != HSQLDB_SOFTWARE) {
                DatabaseMetaData meta = connection.getMetaData();
                ResultSet RS = meta.getTables(database, null, "%", null);
                String traverseTables;
                
                while(RS.next()) {
                    traverseTables = RS.getString(3);
                    
                    if(traverseTables.equals(SHARE_TABLE_NAME)) {
                        foundTable = true;
                        break;
                    }
                }
            }
            // No table? Well have to go create it
            if(!foundTable)
                success = createTables();
            else
                success = true;
        }
        catch (SQLException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
                    						e.getMessage()));
        }

        return success;
    }

    /**
     * Import quotes into the database.
     *
     * @param quotes list of quotes to import
     * @return the number of quotes imported
     */
    public int importQuotes(List quotes) {
        int quotesImported = 0;

        if(quotes.size() > 0 && checkConnection()) {

            // Query the database to see which of these quotes is present
            List existingQuotes = findMatchingQuotes(quotes);

            // Remove duplicates
            List newQuotes = new ArrayList();
            for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
                Quote quote = (Quote)iterator.next();

                if(!containsQuote(existingQuotes, quote))
                    newQuotes.add(quote);
            }

            if(newQuotes.size() > 0) {
                if(software == HSQLDB_SOFTWARE)
                    quotesImported = importQuoteMultipleStatements(newQuotes);
                else
                    quotesImported = importQuoteSingleStatement(newQuotes);
            }
        }

        return quotesImported;
    }

    /**
     * Searches the list of quotes for the given quote. A match only
     * requires the symbol and date fields to match.
     *
     * @param quotes the list of quotes to search
     * @param quote the quote to search for
     * @return <code>true</code> if the quote is in the list, <code>false</code> otherwise
     */
    private boolean containsQuote(List quotes, Quote quote) {
        for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
            Quote containedQuote = (Quote)iterator.next();

            if(containedQuote.getSymbol().equals(quote.getSymbol()) &&
               containedQuote.getDate().equals(quote.getDate()))
                return true;
        }

        return false;
    }

    /**
     * Import quotes into the database using a separate insert statement for
     * each row. Use this function when the database does not support
     * multi-row inserts.
     *
     * @param	quotes list of quotes to import
     * @return the number of quotes imported
     */
    private int importQuoteMultipleStatements(List quotes) {
        int quotesImported = 0;

        // Iterate through the quotes and import them one-by-one.
        Iterator iterator = quotes.iterator();
        
        try {
            while(iterator.hasNext()) {
                Quote quote = (Quote)iterator.next();
                
                String insertQuery = new String("INSERT INTO " + SHARE_TABLE_NAME +
                                                " VALUES (" +
                                                "'" + quote.getDate()      + "', " +
                                                "'" + quote.getSymbol()    + "', " +
                                                "'" + quote.getDayOpen()   + "', " +
                                                "'" + quote.getDayClose()  + "', " +
                                                "'" + quote.getDayHigh()   + "', " +
                                                "'" + quote.getDayLow()    + "', " +
                                                "'" + quote.getDayVolume() + 
                                                "')");
                
                // Now insert the quote into database
                Statement statement = connection.createStatement();
                statement.executeUpdate(insertQuery);
                quotesImported++;
            }
        }
        catch (SQLException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
                                                             e.getMessage()));
        }

        return quotesImported;
    }

    /**
     * Import quotes into the database using a single insert statement for
     * all rows. Use this function when the database supports
     * multi-row inserts.
     *
     * @param	quotes list of quotes to import
     * @return the number of quotes imported
     */
    private int importQuoteSingleStatement(List quotes) {
        int quotesImported = 0;
        StringBuffer insertString = new StringBuffer();
        boolean firstQuote = true;
        
        // Build single query to insert stocks for a whole day into
        for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
            Quote quote = (Quote)iterator.next();
            
            if(firstQuote) {
                insertString.append("INSERT INTO " + SHARE_TABLE_NAME +
                                    " VALUES (");
                firstQuote = false;
            }
            else
                insertString.append(", (");
            
            // Add new quote
            insertString.append("'" + quote.getDate()      + "', " +
                                "'" + quote.getSymbol()    + "', " +
                                "'" + quote.getDayOpen()   + "', " +
                                "'" + quote.getDayClose()  + "', " +
                                "'" + quote.getDayHigh()   + "', " +
                                "'" + quote.getDayLow()    + "', " +
                                "'" + quote.getDayVolume() + "')");
        }
        
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(insertString.toString());
            quotesImported = quotes.size();
        }
        catch (SQLException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
                                                             e.getMessage()));
        }

        return quotesImported;
    }

    /**
     * Returns whether the source contains any quotes for the given date.
     *
     * @param date the date
     * @return wehther the source contains the given date
     */
    public boolean containsDate(TradingDate date) {
        boolean containsDate = false;

	if(checkConnection()) {
	    try {
		Statement statement = connection.createStatement();
		
		// Return the first date found matching the given date.
		// If no dates are found - the date is not in the source.
		// This should take << 1s.
                String query = buildDatePresentQuery(date);
		ResultSet RS = statement.executeQuery(query);

                // Find out if it has any rows
                containsDate = RS.next();

		// Clean up after ourselves
		RS.close();
		statement.close();
	    }
	    catch (SQLException e) {
		DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
	    }
	}

        return containsDate;
    }

    /**
     * Return all the dates which we have quotes for. REALLY SLOW.
     *
     * @return	a list of dates
     */    
    public List getDates() {
	List dates = new ArrayList();

	if(checkConnection()) {

            // This might take a while
            ProgressDialog progress = ProgressDialogManager.getProgressDialog();
            progress.setIndeterminate(true);
            progress.show(Locale.getString("GETTING_DATES"));
            progress.setNote(Locale.getString("GETTING_DATES"));
            
            try {
                // Get dates
                Statement statement = connection.createStatement();
                ResultSet RS = statement.executeQuery
                    ("SELECT DISTINCT(" + DATE_FIELD + ") FROM " +
                     SHARE_TABLE_NAME);
                
                while(RS.next()) {
                    dates.add(new TradingDate(RS.getDate(1)));
                    progress.increment();
                }
                
            }
            catch (SQLException e) {
		DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e.getMessage()));
            }
            
            ProgressDialogManager.closeProgressDialog(progress);
        }

	return dates;
    }

    /**
     * Return the advance/decline for the given date. This returns the number
     * of all ordinary stocks that rose (day close > day open) - the number of all
     * ordinary stocks that fell.
     *
     * @param date the date
     * @exception throws MissingQuoteException if the date wasn't in the source
     */
    public int getAdvanceDecline(TradingDate date)
        throws MissingQuoteException {
       
	if(!checkConnection())
            return 0;

        try {
            //
            // First get number of stocks where close > open
            //

            Statement statement = connection.createStatement();

            String query =
                new String("SELECT COUNT(*) FROM " + SHARE_TABLE_NAME +
                           " WHERE " + DATE_FIELD + " = '" + date + "' AND " +
                           DAY_CLOSE_FIELD + " > " + DAY_OPEN_FIELD + " AND " +
                           "LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
                           left(SYMBOL_FIELD ,1)  + " != 'X' ");

            ResultSet RS = statement.executeQuery(query);
            boolean isDatePresent = RS.next();
            int advanceDecline = 0;

            if(isDatePresent) {
                advanceDecline = RS.getInt(1);

                // Clean up after ourselves
                RS.close();
                statement.close();
            }
            else {
                // Clean up after ourselves
                RS.close();
                statement.close();

                throw MissingQuoteException.getInstance();
            }

            //
            // Now get number of stocks where close < open
            //

            statement = connection.createStatement();

            query =
                new String("SELECT COUNT(*) FROM " + SHARE_TABLE_NAME +
                           " WHERE " + DATE_FIELD + " = '" + date + "' AND " +
                           DAY_CLOSE_FIELD + " < " + DAY_OPEN_FIELD + " AND " +
                           "LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
                           left(SYMBOL_FIELD, 1) + " != 'X' ");

            RS = statement.executeQuery(query);
            isDatePresent = RS.next();

            if(isDatePresent) {
                advanceDecline -= RS.getInt(1);

                // Clean up after ourselves
                RS.close();
                statement.close();
            }
            else {
                // Clean up after ourselves
                RS.close();
                statement.close();

                // Shouldn't happen!
                assert false;
            }

            return advanceDecline;
        }
        catch (SQLException e) {
	    DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
							     e.getMessage()));
            return 0;
        }
    }

    /**
     * Shutdown the database. Only used for the internal database.
     */
    public void shutdown() {
        // We only need to shutdown the internal HYSQLDB database
        if(software == HSQLDB_SOFTWARE && mode == INTERNAL && checkConnection()) {
            try {
                Statement statement = connection.createStatement();
                ResultSet RS = statement.executeQuery("SHUTDOWN");
                RS.close();
                statement.close();
            }
            catch(SQLException e) {
                DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
                                                                 e.getMessage()));
            }
        }
    }

    /**
     * The database is very slow at taking an arbitrary list of symbol and date pairs
     * and finding whether they exist in the database. This is unfortuante because
     * we need this functionality so we don't try to import quotes that are already
     * in the database. If we try to import a quote that is already present, we
     * get a constraint violation error. We can't just ignore this error because
     * we can't tell errors apart and we don't want to ignore all import errors.
     * <p>
     * This function examines the list of quotes and optimises the query for returning
     * matching quotes. This basically works by seeing if all the quotes are on
     * the same date or have the same symbol.
     * <p>
     * CAUTION: This function will return all matches, but it may return some false ones too.
     * The SQL query returned will only return the symbol and date fields.
     * Don't call this function if the quote list is empty.
     *
     * @param quotes the quote list.
     * @return SQL query statement
     */
    private String buildMatchingQuoteQuery(List quotes) {
        boolean sameSymbol = true;
        boolean sameDate = true;
        Symbol symbol = null;
        TradingDate date = null;
        TradingDate startDate = null;
        TradingDate endDate = null;

        // This function should only be called if there are any quotes to match
        assert quotes.size() > 0;

        StringBuffer buffer = new StringBuffer();
        buffer.append("SELECT " + SYMBOL_FIELD + "," + DATE_FIELD + " FROM " +
                      SHARE_TABLE_NAME + " WHERE ");
        
        // Check if all the quotes have the same symbol or fall on the same date.
        for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
            Quote quote = (Quote)iterator.next();

            if(symbol == null || date == null) {
                symbol = quote.getSymbol();
                startDate = endDate = date = quote.getDate();
            }
            else {
                if(!symbol.equals(quote.getSymbol()))
                    sameSymbol = false;
                if(!date.equals(quote.getDate()))
                    sameDate = false;

                // Keep a track of the date range in case we do a symbol query, as if
                // they are importing a single symbol, we don't want to pull in every date
                // to check!
                if(quote.getDate().before(startDate))
                    startDate = quote.getDate();
                if(quote.getDate().after(endDate))
                    endDate = quote.getDate();
            }
        }

        // 1. All quotes have the same symbol.
        if(sameSymbol)
            buffer.append(SYMBOL_FIELD + " = '" + symbol.toString() + "' AND " +
                          DATE_FIELD + " >= '" + startDate + "' AND " +
                          DATE_FIELD + " <= '" + endDate + "' ");

        // 2. All quotes are on the same date.
        else if(sameDate)
            buffer.append(DATE_FIELD + " = '" + date.toString() + "'");

        // 3. The quotes contain a mixture of symbols and dates. Bite the bullet
        // and do a slow SQL query which checks each one individually.
        else {
            for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
                Quote quote = (Quote)iterator.next();
                buffer.append("(" + SYMBOL_FIELD + " = '" + quote.getSymbol() + "' AND " +
                              DATE_FIELD + " = '" + quote.getDate() + "')");
                if(iterator.hasNext())
                    buffer.append(" OR ");
            }
        }

        return buffer.toString();
    }

    /**
     * Return a list of all the quotes in the database that match the input list.
     * This function is used during import to find out which quotes are already
     * in the database.
     * <p>
     * CAUTION: This function will return all matches, but it may return some false ones too.
     * The SQL query returned will only return the symbol and date fields.
     *
     * @param quotes quotes to query
     * @return matching quotes
     */
    private List findMatchingQuotes(List quotes) {
        List matchingQuotes = new ArrayList();
        
	if(checkConnection() && quotes.size() > 0) {
            // Since this is part of import, don't bother with progress dialog
            try {
                // Construct query from list
                Statement statement = connection.createStatement();
                String query = buildMatchingQuoteQuery(quotes);
                ResultSet RS = statement.executeQuery(query);

                // Retrieve matching quotes
                while(RS.next()) {
                    try {
                        matchingQuotes.add(new Quote(Symbol.find(RS.getString(SYMBOL_FIELD)),
                                                     new TradingDate(RS.getDate(DATE_FIELD)),
                                                     0, 0.0, 0.0, 0.0, 0.0));
                    }
                    catch(SymbolFormatException e) {
                        // This can't happen because we are only matching already known
                        // valid symbols.
                        assert false;
                    }
                }

                // Clean up after ourselves
                RS.close();
                statement.close();
            }
            catch(SQLException e2) {
		DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
								 e2.getMessage()));
            }
        }

        return matchingQuotes;
    }

    /**
     * This function shows an error message if there are no quotes in the
     * database. We generally only care about this when trying to get the
     * the current date or the lowest or highest. This method will also
     * interrupt the current thread. This way calling code only needs to
     * check for cancellation, rather than each individual fault.
     */
    private void showEmptyDatabaseError() {
        DesktopManager.showErrorMessage(Locale.getString("NO_QUOTES_FOUND"));
    }

    /**
     * Return the SQL clause for returning the left most characters in
     * a string. This function is needed because there seems no portable
     * way of doing this.
     *
     * @param field the field to extract
     * @param length the number of left most characters to extract
     * @return the SQL clause for performing <code>LEFT(string, letters)</code>
     */
    private String left(String field, int length) {
        if(software.equals(MYSQL_SOFTWARE))
            return new String("LEFT(" + field + ", " + length + ")");
        else {
            // This is probably more portable than the above
            return new String("SUBSTR(" + field + ", 1, " + length + ")");
        }        
    }

    /**
     * Return the SQL clause for detecting whether the given date appears
     * in the table.
     *
     * @param data the date
     * @return the SQL clause
     */
    private String buildDatePresentQuery(TradingDate date) {
        if(software == HSQLDB_SOFTWARE)
            return new String("SELECT TOP 1 " + DATE_FIELD + " FROM " +
                              SHARE_TABLE_NAME + " WHERE " + DATE_FIELD + " = '"
                              + date + "' ");
        else
            return new String("SELECT " + DATE_FIELD + " FROM " +
                              SHARE_TABLE_NAME + " WHERE " + DATE_FIELD + " = '"
                              + date + "' LIMIT 1");
    }

    /**
     * Return the SQL clause for detecting whether the given symbol appears
     * in the table.
     *
     * @param symbol the symbol
     * @return the SQL clause
     */
    private String buildSymbolPresentQuery(Symbol symbol) {
        if(software == HSQLDB_SOFTWARE)
            return new String("SELECT TOP 1 " + SYMBOL_FIELD + " FROM " +
                              SHARE_TABLE_NAME + " WHERE " + SYMBOL_FIELD + " = '"
                              + symbol + "' ");
        else
            return new String("SELECT " + SYMBOL_FIELD + " FROM " +
                              SHARE_TABLE_NAME + " WHERE " + SYMBOL_FIELD + " = '"
                              + symbol + "' LIMIT 1");
    }
}


