package org.mov.quote;

import java.util.prefs.Preferences;
import org.mov.importer.ImporterModule;
import org.mov.prefs.PreferencesManager;



/**
 * Returns the singleton reference to the quote source that the user
 * has selected in their preferences. This class will also be
 * updated when the user preferences has changed so the return quote source
 * will always be update to date.
 *
 * Example:
 * <pre>
 *	Vector quotes = Quote.getSource().getQuotesForSymbol("CBA");
 * </pre>
 * 
 * @see QuoteSource
 */
public class Quote {
    private static Quote instance = null;
    private QuoteSource sourceInstance = null;

    /**
     * Return the singleton reference to the user selected quote source.
     *
     * @return reference to a quote source.
     */
    public static QuoteSource getSource() {
	if(instance == null) {
	    instance = new Quote();
	}
	
	return instance.getSourceInstance();
    }

    /**
     * The user has changed their quote source preferences, flush singleton
     * reference and create new instance. 
     */
    public static void flush() {
	if(instance != null)
	    instance.sourceInstance = null;
    }

    private Quote() {
	// declared here so constructor is not public
    }

    // Creates and returns singleton instance of quote source
    private QuoteSource getSourceInstance() {
	if(sourceInstance == null) {
	    Preferences p = PreferencesManager.getUserNode("/quote_source");
	    String quoteSource = p.get("source", "database");

	    if(quoteSource.equals("files")) {
		sourceInstance = createFileQuoteSource();
	    }
	    else if(quoteSource.equals("database"))
		sourceInstance = createDatabaseQuoteSource();
	    else {
		sourceInstance = createInternetQuoteSource();
	    }
	}

	return sourceInstance;
    }


    /**
     * Create a file quote source directly using the user preferences.
     *
     * @return	the file quote source 
     */
    public static FileQuoteSource createFileQuoteSource() {

	// Get file format from preferences
	Preferences p = PreferencesManager.getUserNode("/quote_source/files");

	return
	    new FileQuoteSource(p.get("format", "MetaStock"),
				ImporterModule.getFileList());
    }	

    /**
     * Create an internet quote source directly using the user preferences.
     *
     * @return	the internet quote source 
     */
    public static SanfordQuoteSource createInternetQuoteSource() {
	// Get username and password from preferences
	Preferences p = PreferencesManager.getUserNode("/quote_source/internet");
	
	return new SanfordQuoteSource(p.get("username", ""),
				      p.get("password", ""));
    }
    
    /**
     * Create a database quote source directly using the user preferences.
     *
     * @return	the database quote source 
     */
    public static DatabaseQuoteSource createDatabaseQuoteSource() {

	Preferences p = PreferencesManager.getUserNode("/quote_source/database");
	String host = p.get("host", "db");
	String port = p.get("port",  "3306");
	String database = p.get("dbname", "shares");
	String username = p.get("username", "");
	String password = p.get("password", "");
	return new DatabaseQuoteSource(host, port, database, username,
				       password);
    }
}

