package org.mov.quote;

import java.util.prefs.*;

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
	    Preferences p = Preferences.userRoot().node("/quote_source");
	    String quoteSource = p.get("source", "database");

	    if(quoteSource.equals("files"))
		sourceInstance = new FileQuoteSource();
	    else
		sourceInstance = new DatabaseQuoteSource();
	}

	return sourceInstance;
    }
}

