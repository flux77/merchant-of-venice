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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.MalformedURLException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mov.util.TradingDate;
import org.mov.ui.DesktopManager;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;
import org.mov.prefs.PreferencesManager;

/**
 * Provides functionality to obtain stock quotes from the internet. The entire
 * quote sourcce interface has not been implemented so this source is only
 * suitable for importing the quotes, rather than accessing them directly.
 */
public class InternetQuoteSource implements QuoteSource
{
    // The following symbols will be replaced by the quote, date range we are after:
    private final static String SYMBOL      = "_SYM_";
    private final static String START_DAY   = "_SD_";
    private final static String START_MONTH = "_SM_";
    private final static String START_YEAR  = "_SY_";
    private final static String END_DAY     = "_ED_";
    private final static String END_MONTH   = "_EM_";
    private final static String END_YEAR    = "_EY_";

    // Each Yahoo site uses the same URL formatting. So we define it once here.
    private final static String YAHOO_FORMAT = ("?s=" + SYMBOL + "&a=" + START_MONTH + 
                                                "&b=" + START_DAY + "&c=" + START_YEAR +
                                                "&d=" + END_MONTH + "&e=" + END_DAY +
                                                "&f=" + END_YEAR + "&g=d&ignore=.csv");

    /*
    // Originally I thought you needed to go to each separate country to get
    // stocks. But it turns out you don't need to because yahoo accepts
    // prefixes, e.g. .AX for Australia. I've left the original table in because
    // it may turn out to be useful later.
    private final static String[] sources = 
    {"DAX (Yahoo)", // Frankfurt Stock Exchange
     "http://de.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "FTSE (Yahoo)", // London
     "http://uk.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "KFX (Yahoo)", // Copenhagen
     "http://dk.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "Madrid Stock Exchange (Yahoo)",
     "http://es.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "Milan Stock Exchange (Yahoo)",
     "http://it.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "NYSE/NASDAQ (Yahoo)", // US
     "http://table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "OBX (Yahoo)", // Oslo
     "http://no.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "Paris Stock Exchange (Yahoo)",        
     "http://fr.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,

     "SX (Yahoo)", // Stockholm
     "http://se.table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,
    };
    */
  
    // You can get all the stock quotes from the one URL
    private final static String[] sources = {
	"Yahoo",
	"http://table.finance.yahoo.com/table.csv" + YAHOO_FORMAT,
    };

    private final static int numberExchanges = (sources.length / 2);

    // The exchange name and pattern we are using
    private String name;
    private String URLPattern;

    private TradingDate startDate;
    private TradingDate endDate;

    /**
     * Create a new quote source from the given exchange between the given dates.
     *
     * @param exchange the exchange.
     * @param startDate the start date.
     * @param endDate the end date.
     */
    public InternetQuoteSource(int exchange, TradingDate startDate, TradingDate endDate) {
        assert exchange < numberExchanges;

        name = sources[exchange * 2];
        URLPattern  = sources[exchange * 2 + 1];

        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Return a list of all the stock exchanges we support.
     *
     * @return array of stock exchanges
     */
    public static Object[] getExchanges() {
        Object[] exchanges = new Object[numberExchanges];

        for(int i = 0; i < numberExchanges; i++)
            exchanges[i] = sources[i * 2];
        
        return exchanges;
    }

    /**
     * Returns the company name associated with the given symbol.
     *
     * @param	symbol	the stock symbol
     * @return	the company name
     */
    public String getSymbolName(Symbol symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the symbol associated with the given company.
     *
     * @param	partialCompanyName	a partial company name
     * @return	the company symbol
     */
    public Symbol getSymbol(String partialCompanyName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether we have any quotes for the given symbol.
     *
     * @param	symbol	the symbol we are searching for
     * @return	whether the symbol was found or not
     */
    public boolean symbolExists(Symbol symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the latest date we have any stock quotes for.
     *
     * @return	the most recent quote date
     */
    public TradingDate getLastDate() {
        return endDate;
    }

    /**
     * Return the earliest date we have any stock quotes for.
     *
     * @return	the oldest quote date
     */
    public TradingDate getFirstDate() {
        return startDate;
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

        // Yahoo Finance only allows me to retrieve a list of given symbols, and
        // then only for a list of dates.
        if(quoteRange.getType() != QuoteRange.GIVEN_SYMBOLS ||
           quoteRange.getFirstDate() == null ||
           quoteRange.getLastDate() == null)
            throw new UnsupportedOperationException();

        // We load the quotes symbol-by-symbol. This allows us to load multiple
        // quotes with a single HTTP operation.
        else {
            List symbols = quoteRange.getAllSymbols();
            boolean success = true;

            // This needs to be before the progress dialog otherwise
            // we might end up (during an import) trying to open 3
            // progress dialogs within one thread which is illegal.
            QuoteCache quoteCache = QuoteCache.getInstance();

            // This query might take a while
            Thread thread = Thread.currentThread();
            ProgressDialog progress = ProgressDialogManager.getProgressDialog();
            progress.setNote("Loading Quotes...");

            if(symbols.size() > 1) {
                progress.setMaximum(symbols.size());
                progress.setProgress(0);
                progress.setIndeterminate(false);
            }
            else
                progress.setIndeterminate(true);

            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();

                if(!loadSymbol(quoteCache, symbol, 
                               quoteRange.getFirstDate(), 
                               quoteRange.getLastDate()) ||
                   thread.isInterrupted()) {
                    success = false;
                    break;
                }
                    
                progress.increment();
            }

            ProgressDialogManager.closeProgressDialog(progress);
            return success;
        }
    }
    
    /**
     * Returns whether the source contains any quotes for the given date.
     *
     * @param date the date
     * @return wehther the source contains the given date
     */
    public boolean containsDate(TradingDate date) {
        return (date.compareTo(startDate) >= 0 &&
                date.compareTo(endDate) <= 0);
    }

    /**
     * Return all the dates which we have quotes for.
     *
     * @return	a vector of dates
     */
    public List getDates() {
        return TradingDate.dateRangeToList(startDate, endDate);
    }

    /**
     * Is the given symbol a market index?
     *
     * @param	symbol to test
     * @return	yes or no
     */
    public boolean isMarketIndex(Symbol symbol) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    // Load all the quotes of the given symbol, between the given dates, and then
    // store them into the quote cache. Returns true if there were no problems,
    // false otherwise.
    private boolean loadSymbol(QuoteCache quoteCache, Symbol symbol, 
                               TradingDate startDate, TradingDate endDate) {
        boolean success = true;
        String URLString = constructURL(symbol, startDate, endDate);
	PreferencesManager.ProxyPreferences proxyPreferences = 
	    PreferencesManager.loadProxySettings();

        try {
	    URL url;

	    if(proxyPreferences.isEnabled)
		url = new URL("http", 
			      proxyPreferences.host, 
			      Integer.parseInt(proxyPreferences.port),
			      URLString);
	    else
		url = new URL(URLString);

            InputStreamReader input = new InputStreamReader(url.openStream());
            BufferedReader bufferedInput = new BufferedReader(input);
            String line;
                
            while((line = bufferedInput.readLine()) != null) {
                QuoteFilter quoteFilter = new YahooQuoteFilter(symbol);
                Quote quote = quoteFilter.toQuote(line);
                    
                if(quote != null)
                    quoteCache.load(quote);
            }
                
            bufferedInput.close();
        }

	catch(BindException e) {
	    DesktopManager.showErrorMessage("Unable to connect: " + e.getMessage() + ".");
	    success = false;
	}

	catch(ConnectException e) {
	    DesktopManager.showErrorMessage("Unable to connect: " + e.getMessage() + ".");
	    success = false;
	}

	catch(UnknownHostException e) {
	    DesktopManager.showErrorMessage("Unknown host: " + e.getMessage() + ".");
	    success = false;
	}

	catch(NoRouteToHostException e) {
	    DesktopManager.showErrorMessage("Destination unreachable: " + e.getMessage() + ".");
	    success = false;
	}

	catch(MalformedURLException e) {
	    DesktopManager.showErrorMessage("Invalid proxy address: " + proxyPreferences.host + 
					    ":" + proxyPreferences.port + ".");
	    success = false;
	}

        catch(IOException e) {
            DesktopManager.showErrorMessage("Error downloading quotes.");
            success = false;
        }
        
        return success;
    }

    private String constructURL(Symbol symbol, TradingDate start, TradingDate end) {
        String URLString = URLPattern;
        URLString = replace(URLString, SYMBOL, symbol.toString());
        URLString = replace(URLString, START_DAY, Integer.toString(start.getDay()));
        URLString = replace(URLString, START_MONTH, Integer.toString(start.getMonth() - 1));
        URLString = replace(URLString, START_YEAR, Integer.toString(start.getYear()));
        URLString = replace(URLString, END_DAY, Integer.toString(end.getDay()));
        URLString = replace(URLString, END_MONTH, Integer.toString(end.getMonth() - 1));
        URLString = replace(URLString, END_YEAR, Integer.toString(end.getYear()));
        return URLString;
    }

    private String replace(String string, String oldSubString, String newSubString) {
        Pattern pattern = Pattern.compile(oldSubString);
        Matcher matcher = pattern.matcher(string);
        return matcher.replaceAll(newSubString);
    }
}
