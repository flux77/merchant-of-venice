package org.mov.quote;

import java.util.Hashtable;
import java.util.prefs.*;

/** Provides a lookup mapping for our different table and field names
 */

public class DatabaseLookup {

    /**
     * Factory method for generating new Database lookup engine */

    public static DatabaseLookup getInstance() {
	return new DatabaseLookup();
    }
    
    Hashtable hash;

    /** Private constructor */

    private DatabaseLookup() {
	hash = new Hashtable();
	
	Preferences p = Preferences.userRoot().node("/quote_source/database");
	hash.put("drivername", p.get("drivername", "mysql"));
	hash.put("driverclass", p.get("driverclass", "org.gjt.mm.mysql.Driver"));
	hash.put("host", p.get("host", "db"));
	hash.put("port", p.get("port", "3306"));
	hash.put("dbname", p.get("dbname", "shares"));
	
	hash.put("username", p.get("username", ""));
	hash.put("password", p.get("password", ""));
	
	p = Preferences.userRoot().node("/quote_source/database/tables");
	hash.put("prices", p.get("prices", "prices"));
	hash.put("info", p.get("info", "info"));
	
	p = Preferences.userRoot().node("/quote_source/database/tables/prices_fields");
	hash.put("prices.date", hash.get("prices")+"."+p.get("date", "date"));
	hash.put("prices.symbol", hash.get("prices")+"."+p.get("symbol", "symbol"));
	hash.put("prices.open", hash.get("prices")+"."+p.get("open", "open"));
	hash.put("prices.high", hash.get("prices")+"."+p.get("high", "high"));
	hash.put("prices.low", hash.get("prices")+"."+p.get("low", "low"));
	hash.put("prices.close", hash.get("prices")+"."+p.get("close", "close"));
	hash.put("prices.volume", hash.get("prices")+"."+p.get("volume", "volume"));
	
	p = Preferences.userRoot().node("/quote_source/database/tables/info_fields");
	hash.put("info.name", hash.get("info")+"."+p.get("name", "name"));
	hash.put("info.symbol", hash.get("info")+"."+p.get("symbol", "symbol"));
    }

    /** Retrieve the relavent database variable */
    public String get(String key) {
	return (String)hash.get(key);
    }
}

