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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.MalformedURLException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mov.ui.DesktopManager;
import org.mov.prefs.PreferencesManager;
import org.mov.util.Locale;
import org.mov.util.Report;
import org.mov.util.TradingDate;

/**
 * Import quotes from Yahoo into Venice.
 *
 * @author Andrew Leppard
 * @see FileImportExport, ImportQuoteModule
 */
public class InternetImport {

    // The following symbols will be replaced by the quote, date range we are after:
    private final static String SYMBOL      = "_SYM_";
    private final static String START_DAY   = "_SD_";
    private final static String START_MONTH = "_SM_";
    private final static String START_YEAR  = "_SY_";
    private final static String END_DAY     = "_ED_";
    private final static String END_MONTH   = "_EM_";
    private final static String END_YEAR    = "_EY_";

    // Each Yahoo site uses the same URL formatting. So we define it once here.
    private final static String YAHOO_PATTERN = ("?s=" + SYMBOL + "&a=" + START_MONTH + 
                                                "&b=" + START_DAY + "&c=" + START_YEAR +
                                                "&d=" + END_MONTH + "&e=" + END_DAY +
                                                "&f=" + END_YEAR + "&g=d&ignore=.csv");
    
    private final static String YAHOO_URL_PATTERN =
        ("http://ichart.finance.yahoo.com/table.csv" + YAHOO_PATTERN);
         
    // This class is not instantiated.
    private InternetImport() {
        assert false;
    }

    /**
     * Import quotes from Yahoo into Venice.
     *
     * @param report report to log warnings and errors
     * @param symbol symbol to import
     * @param startDate start of date range to import
     * @param endDate end of date range to import
     * @return list of quotes
     * @exception IOException if there was an error retrieving the quotes
     */
    public static List importSymbol(Report report, Symbol symbol,
                                    TradingDate startDate, TradingDate endDate)
        throws IOException {

        List quotes = new ArrayList();
        String URLString = constructURL(symbol, startDate, endDate);
        QuoteFilter filter = new YahooQuoteFilter(symbol);

	PreferencesManager.ProxyPreferences proxyPreferences =
            PreferencesManager.loadProxySettings();

        try {
	    URL url = new URL(URLString);

            InputStreamReader input = new InputStreamReader(url.openStream());
            BufferedReader bufferedInput = new BufferedReader(input);

            // Skip first line as it doesn't contain a quote
            String line = bufferedInput.readLine();
  
            while(line != null) {
                line = bufferedInput.readLine();

                if(line != null) {
                    try {
                        Quote quote = filter.toQuote(line);     
                        quotes.add(quote);
                        verify(report, quote);
                    }                    
                    catch(QuoteFormatException e) {
                        report.addError(Locale.getString("YAHOO") + ":" + 
                                        symbol + ":" +
                                        Locale.getString("ERROR") + ": " +
                                        e.getMessage());
                    }
                }
            }

            bufferedInput.close();
        }

	catch(BindException e) {
	    DesktopManager.showErrorMessage(Locale.getString("UNABLE_TO_CONNECT_ERROR",
                                                             e.getMessage()));
            throw new IOException();
	}

	catch(ConnectException e) {
	    DesktopManager.showErrorMessage(Locale.getString("UNABLE_TO_CONNECT_ERROR",
                                                             e.getMessage()));
            throw new IOException();
	}

	catch(UnknownHostException e) {
	    DesktopManager.showErrorMessage(Locale.getString("UNKNOWN_HOST_ERROR",
                                                             e.getMessage()));
            throw new IOException();
	}

	catch(NoRouteToHostException e) {
	    DesktopManager.showErrorMessage(Locale.getString("DESTINATION_UNREACHABLE_ERROR",
                                                             e.getMessage()));
            throw new IOException();
	}

	catch(MalformedURLException e) {
	    DesktopManager.showErrorMessage(Locale.getString("INVALID_PROXY_ERROR",
                                                             proxyPreferences.host,
                                                             proxyPreferences.port));
            throw new IOException();
	}
        
        catch(FileNotFoundException e) {
            // Don't abort the import if there are no quotes for a given symbol
            report.addError(Locale.getString("YAHOO") + ":" + 
                            symbol + ":" +
                            Locale.getString("ERROR") + ": " +
                            Locale.getString("NO_QUOTES_FOUND"));
        }

        catch(IOException e) {
            DesktopManager.showErrorMessage(Locale.getString("ERROR_DOWNLOADING_QUOTES"));
            throw new IOException();
        } 
        
        return quotes;        
    }

    /**
     * Construct the URL necessary to retrieve all the quotes for the given symbol between
     * the given dates from Yahoo.
     *
     * @param symbol the symbol to retrieve
     * @param start the start date to retrieve
     * @param end the end date to retrieve
     * @return URL string
     */
    private static String constructURL(Symbol symbol, TradingDate start, TradingDate end) {
        String URLString = YAHOO_URL_PATTERN;
        URLString = replace(URLString, SYMBOL, symbol.toString());
        URLString = replace(URLString, START_DAY, Integer.toString(start.getDay()));
        URLString = replace(URLString, START_MONTH, Integer.toString(start.getMonth() - 1));
        URLString = replace(URLString, START_YEAR, Integer.toString(start.getYear()));
        URLString = replace(URLString, END_DAY, Integer.toString(end.getDay()));
        URLString = replace(URLString, END_MONTH, Integer.toString(end.getMonth() - 1));
        URLString = replace(URLString, END_YEAR, Integer.toString(end.getYear()));
        return URLString;
    }

    /**
     * Perform a find replace on a string.
     *
     * @param string the source string
     * @param oldSubString the text which to replace
     * @param newSubString the text to replace with
     * @return the new string
     */
    private static String replace(String string, String oldSubString, String newSubString) {
        Pattern pattern = Pattern.compile(oldSubString);
        Matcher matcher = pattern.matcher(string);
        return matcher.replaceAll(newSubString);
    }

    /**
     * Verify the quote is valid. Log any problems to the report and try to clean
     * it up the best we can.
     *
     * @param report the report
     * @param quote the quote
     */
    private static void verify(Report report, Quote quote) {
        try {
            quote.verify();
        }
        catch(QuoteFormatException e) {
            List messages = e.getMessages();

            for(Iterator iterator = messages.iterator(); iterator.hasNext();) {
                String message = (String)iterator.next();

                report.addWarning(Locale.getString("YAHOO") + ":" + 
                                  quote.getSymbol() + ":" + 
                                  quote.getDate() + ":" +
                                  Locale.getString("WARNING") + ": " +
                                  message);
            }
        }
    }
}