package org.mov.util;

/* Provides a lookup mapping for our different table and field names
 */

public class DatabaseLookup {

    // Driver details - just in case we want to switch to Postgres or whatever
    public final String drivername  = "mysql";
    public final String driverclass = "org.gjt.mm.mysql.Driver";

    // Account info
    public final String user        = "";
    public final String password    = "";

    public Prices prices;
    public Info info;

    
    public DatabaseLookup() {
	prices = new Prices();
	info = new Info();
    }


    /*
     * This class represents the table that records daily stock price action.
     * Modify these variables to reflect the name of the table and respective fields
     */
    class Prices {
	public static final String table_name = "shares";
	public static final String date       = "date";
	public static final String symbol     = "symbol";
	public static final String open       = "open";
	public static final String high       = "high";
	public static final String low        = "low";
	public static final String close      = "close";
	public static final String volume     = "volume";


	public String toString() {
	    return table_name;
	}

    }

    /*
     * This class represents the table that records information about a particular stock.
     * Modify these variables to reflect the name of the table and respective fields
     */
    class Info {
	public static final String table_name = "lookup";
	public static final String symbol     = "symbol";
	public static final String name       = "name";

	public String toString() {
	    return table_name;
	}

    }
}

