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

package nz.org.venice.quote;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.org.venice.ui.DesktopManager;
import nz.org.venice.util.Locale;
import nz.org.venice.util.TradingDate;


/**
 * Provides functionality to manage database connections and ensures the 
 * relevant tables exist. Classes manage their own queries separately.
 * 
 * @author Mark Hummel
 * @see DatabaseQuoteSource
 * @see DatabaseAlert
 */
public class DatabaseManager 
{
    private Connection connection = null;
    private boolean checkedTables = false;

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
    public final static String SHARE_TABLE_NAME  = "shares";

    // Column names
    public final static String DATE_FIELD        = "date";
    public final static String SYMBOL_FIELD      = "symbol";
    public final static String DAY_OPEN_FIELD    = "open";
    public final static String DAY_CLOSE_FIELD   = "close";
    public final static String DAY_HIGH_FIELD    = "high";
    public final static String DAY_LOW_FIELD     = "low";
    public final static String DAY_VOLUME_FIELD  = "volume";

    // Column numbers
    public final static int DATE_COLUMN       = 1;
    public final static int SYMBOL_COLUMN     = 2;
    public final static int DAY_OPEN_COLUMN   = 3;
    public final static int DAY_CLOSE_COLUMN  = 4;
    public final static int DAY_HIGH_COLUMN   = 5;
    public final static int DAY_LOW_COLUMN    = 6;
    public final static int DAY_VOLUME_COLUMN = 7;

    // Shares indices
    private final static String DATE_INDEX_NAME   = "date_index";
    private final static String SYMBOL_INDEX_NAME = "symbol_index";

    // Info table
    public final static String LOOKUP_TABLE_NAME = "lookup";
    public final static String NAME_FIELD        = "name";

    // Exchange rate table
    public final static String EXCHANGE_TABLE_NAME = "exchange";

    // Column names
    // DATE_FIELD
    public final static String SOURCE_CURRENCY_FIELD      = "source_currency";
    public final static String DESTINATION_CURRENCY_FIELD = "destination_currency";
    public final static String EXCHANGE_RATE_FIELD        = "exchange_rate";



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
    private EODQuoteFilter filter;
    private List fileURLs;

    /**
     * Creates a new database connection.
     *
     * @param   software  the database software
     * @param   driver    the class name for the driver to connect to the database
     * @param	host	  the host location of the database
     * @param	port	  the port of the database
     * @param	database  the name of the database
     * @param	username  the user login
     * @param	password  the password for the login
     */
    public DatabaseManager(String software, String driver, String host, String port, 
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
    public DatabaseManager(String fileName) {
        mode = INTERNAL;
        software = HSQLDB_SOFTWARE;
        this.driver = "org.hsqldb.jdbcDriver";
        this.fileName = fileName;
    }

    /**
     * Return True if a connection to the database was established
     *        and the database exists with the right tables. 
     */

    // Get the driver and connect to the database. Return FALSE if failed.

    protected boolean getConnection() {
        boolean success = true;
	
	success = connect();

        // If we are connected, check the tables exist, if not, create them.
        if(connection != null && !checkedTables) {
	    success = checkedTables = checkDatabase() && createTables();
	}
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

            connection = DriverManager.getConnection(connectionURL);
	    
        } 
        catch (ClassNotFoundException e) {	    
            // Couldn't find the driver!
            DesktopManager.showErrorMessage(Locale.getString("UNABLE_TO_LOAD_DATABASE_DRIVER", 
                                                             driver, software));
            return false;
        }
        catch (SQLException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_CONNECTING_TO_DATABASE",
                                                             e.getMessage()));
            return false;
        }

	
        return true;
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
    private String buildSQLString(EODQuoteRange quoteRange) {
        //
        // 1. Create select line
        //
	
        String queryString = "SELECT * FROM " + SHARE_TABLE_NAME + " WHERE ";
	
        //
        // 2. Filter select by symbols we are looking for
        //
	
        String filterString = new String("");
	
        if(quoteRange.getType() == EODQuoteRange.GIVEN_SYMBOLS) {
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
        else if(quoteRange.getType() == EODQuoteRange.ALL_SYMBOLS) {
            // nothing to do
        }
        else if(quoteRange.getType() == EODQuoteRange.ALL_ORDINARIES) {
            filterString = filterString.concat("LENGTH(" + SYMBOL_FIELD + ") = 3 AND " +
                                               left(SYMBOL_FIELD, 1) + " != 'X' ");
        }
        else {
            assert quoteRange.getType() == EODQuoteRange.MARKET_INDICES;
            
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
            
            filterString = filterString.concat(DATE_FIELD + " = '" +
                                               toSQLDateString(quoteRange.getFirstDate()) + "' ");
        }
	
        // Otherwise check within a range of dates
        else {
            if(filterString.length() > 0)
                filterString = filterString.concat("AND ");
            
            filterString = filterString.concat(DATE_FIELD + " >= '" +
                                               toSQLDateString(quoteRange.getFirstDate()) +
                                               "' AND " +
                                               DATE_FIELD + " <= '" +
                                               toSQLDateString(quoteRange.getLastDate()) +
                                               "' ");
        }
	
        return queryString.concat(filterString);
    }
    
    /**
     * Create the share table.
     *
     * @return <code>true</code> iff this function was successful.
     */
    private boolean createShareTable() {
        boolean success = false;
	
        try {
            // Create the shares table.
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE " + getTableType() + " TABLE " + SHARE_TABLE_NAME + " (" +
                                    DATE_FIELD +	" DATE NOT NULL, " +
                                    SYMBOL_FIELD +	" CHAR(" + Symbol.MAXIMUM_SYMBOL_LENGTH + 
                                    ") NOT NULL, " +
                                    DAY_OPEN_FIELD +	" FLOAT DEFAULT 0.0, " +
                                    DAY_CLOSE_FIELD +	" FLOAT DEFAULT 0.0, " +
                                    DAY_HIGH_FIELD +	" FLOAT DEFAULT 0.0, " +
                                    DAY_LOW_FIELD +	" FLOAT DEFAULT 0.0, " +
                                    DAY_VOLUME_FIELD +	" BIGINT DEFAULT 0, "  +
                                    "PRIMARY KEY(" + DATE_FIELD + ", " + SYMBOL_FIELD + "))");
            
            // Create a couple of indices to speed things up.
            statement.executeUpdate("CREATE INDEX " + DATE_INDEX_NAME + " ON " +
                                    SHARE_TABLE_NAME + " (" + DATE_FIELD + ")");
            statement.executeUpdate("CREATE INDEX " + SYMBOL_INDEX_NAME + " ON " +
                                    SHARE_TABLE_NAME + " (" + SYMBOL_FIELD + ")");
            
            // Create the lookup table.
            //statement.executeUpdate("CREATE " + getTableType() + " TABLE " + LOOKUP_TABLE_NAME + " (" +
            //                        SYMBOL_FIELD +	" CHAR(" + Symbol.MAXIMUM_SYMBOL_LENGTH + 
            //                         ") NOT NULL, " +
            //                        NAME_FIELD +	" VARCHAR(100), " +
            //                        "PRIMARY KEY(" + SYMBOL_FIELD + "))");
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
    
    /**
     * Create the exchange table.
     *
     * @return <code>true</code> iff this function was successful.
     */
    private boolean createExchangeTable() {
        boolean success = false;
	
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE " + getTableType() + " TABLE " +
                                    EXCHANGE_TABLE_NAME         + " (" +
                                    DATE_FIELD                  + " DATE NOT NULL, " +

                                    // ISO 4217 currency code is 3 characters.
                                    SOURCE_CURRENCY_FIELD       + " CHAR(3) NOT NULL, " +
                                    DESTINATION_CURRENCY_FIELD  + " CHAR(3) NOT NULL, " +
                                    EXCHANGE_RATE_FIELD         + " FLOAT DEFAULT 1.0, " +
                                    "PRIMARY KEY(" + DATE_FIELD + ", " +
                                    SOURCE_CURRENCY_FIELD + ", " +
                                    DESTINATION_CURRENCY_FIELD + "))");
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

    //Return true if the tables were created successfully
    //or if they already exist.    
    private boolean createTables() {
        boolean success = true;
        
        try {
            boolean foundShareTable = false;
            boolean foundExchangeTable = false;

            // Skip this check for hypersonic - it doesn't support it
            if(software != HSQLDB_SOFTWARE) {
                DatabaseMetaData meta = connection.getMetaData();
                ResultSet RS = meta.getTables(database, null, "%", null);
                String traverseTables;
                
                while(RS.next()) {
                    traverseTables = RS.getString(3);
                    
                    if(traverseTables.equals(SHARE_TABLE_NAME))
                        foundShareTable = true;

                    if(traverseTables.equals(EXCHANGE_TABLE_NAME))
                        foundExchangeTable = true;
                }
            }

            // No table? Let's try and create them.
            if(!foundShareTable)
                success = createShareTable();
            if(!foundExchangeTable && success)
                success = createExchangeTable();
        }
        catch (SQLException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
                                                             e.getMessage()));
            success = false;
        }

        return success;
    }



    /**
     * Shutdown the database. Only used for the internal database.
     */
    public void shutdown() {
        // We only need to shutdown the internal HYSQLDB database
        if(software == HSQLDB_SOFTWARE && mode == INTERNAL && getConnection()) {
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
            EODQuote quote = (EODQuote)iterator.next();

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
                          DATE_FIELD + " >= '" + toSQLDateString(startDate) + "' AND " +
                          DATE_FIELD + " <= '" + toSQLDateString(endDate) + "' ");

        // 2. All quotes are on the same date.
        else if(sameDate)
            buffer.append(DATE_FIELD + " = '" + toSQLDateString(date) + "'");

        // 3. The quotes contain a mixture of symbols and dates. Bite the bullet
        // and do a slow SQL query which checks each one individually.
        else {
            for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
                EODQuote quote = (EODQuote)iterator.next();
                buffer.append("(" + SYMBOL_FIELD + " = '" + quote.getSymbol() + "' AND " +
                              DATE_FIELD + " = '" + toSQLDateString(quote.getDate()) + "')");
                if(iterator.hasNext())
                    buffer.append(" OR ");
            }
        }

        return buffer.toString();
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
    public  String left(String field, int length) {
        if(software.equals(MYSQL_SOFTWARE))
            return new String("LEFT(" + field + ", " + length + ")");
        else {
            // This is probably more portable than the above
            return new String("SUBSTR(" + field + ", 1, " + length + ")");
        }        
    }

    /**
     * Return SQL modify that comes after <code>CREATE</code> and before <code>TABLE</code>.
     * Currently this is only used for HSQLDB.
     *
     * @return the SQL modify for <code>CREATE</code> calls.
     */
    private String getTableType() {
        // We need to supply the table type "CACHED" when creating a HSQLDB
        // table. This tells the database to store the table on disk and cache
        // part of it in memory. If we do not specify this, it will load and
        // work with the entire table in memory.
        if(software.equals(HSQLDB_SOFTWARE))
            return new String("CACHED");
        else
            return "";
    }


    public Statement createStatement() {
	assert connection != null;

	Statement rv = null;

	try {
	    rv = connection.createStatement();
	} catch (SQLException e) {
	    DesktopManager.showErrorMessage(Locale.getString("ERROR_TALKING_TO_DATABASE",
							     e.getMessage()));	
	} finally {
	    return rv;
	}
    }

    /**
     * Return a date string that can be used as part of an SQL query.
     * E.g. 2000-12-03.
     *
     * @param date Date.
     * @return Date string ready for SQL query.
     */
    public String toSQLDateString(TradingDate date) {
    	return date.getYear() + "-" + date.getMonth() + "-" + date.getDay();
    }

        /**
     * Return the SQL clause for detecting whether the given symbol appears
     * in the table.
     *
     * @param symbol the symbol
     * @return the SQL clause
     */
    protected String buildSymbolPresentQuery(Symbol symbol) {
        if(software == HSQLDB_SOFTWARE)
            return new String("SELECT TOP 1 " + DatabaseManager.SYMBOL_FIELD + " FROM " +
                              DatabaseManager.SHARE_TABLE_NAME + " WHERE " + DatabaseManager.SYMBOL_FIELD + " = '"
                              + symbol + "' ");
        else
            return new String("SELECT " + DatabaseManager.SYMBOL_FIELD + " FROM " +
                              DatabaseManager.SHARE_TABLE_NAME + " WHERE " + DatabaseManager.SYMBOL_FIELD + " = '"
                              + symbol + "' LIMIT 1");
    }

        /**
     * Return the SQL clause for detecting whether the given date appears
     * in the table.
     *
     * @param data the date
     * @return the SQL clause
     */
    protected String buildDatePresentQuery(TradingDate date) {
        if(software == HSQLDB_SOFTWARE)
            return new String("SELECT TOP 1 " + DatabaseManager.DATE_FIELD + " FROM " +
                              DatabaseManager.SHARE_TABLE_NAME + " WHERE " + DatabaseManager.DATE_FIELD + " = '"
                              + toSQLDateString(date) + "' ");
        else
            return new String("SELECT " + DatabaseManager.DATE_FIELD + " FROM " +
                              DatabaseManager.SHARE_TABLE_NAME + " WHERE " + DatabaseManager.DATE_FIELD + " = '"
                              + toSQLDateString(date) + "' LIMIT 1");
    }


    public boolean multipleStatementSupported() {
	return (software == HSQLDB_SOFTWARE) ? false : true;
    }
   
}


