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

package org.mov.prefs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.IllegalStateException;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.mov.ui.DesktopManager;

import org.mov.macro.StoredMacro;
import org.mov.portfolio.Account;
import org.mov.portfolio.CashAccount;
import org.mov.portfolio.ShareAccount;
import org.mov.portfolio.Portfolio;
import org.mov.portfolio.Transaction;
import org.mov.quote.Symbol;
import org.mov.quote.SymbolFormatException;
import org.mov.table.WatchScreen;
import org.mov.util.Money;
import org.mov.util.TradingDate;
import org.mov.util.TradingDateFormatException;

/**
 * The Preferences Manager contains a set of routines for loading and saving all
 * preferences data for the application. Consolidating these routines in a single
 * place allows us to maintain preferences namespace convention and also
 * allows us to easily change the method of storage at a later date if desired.
 *
 * @author Daniel Makovec
 */
public class PreferencesManager {
    // The base in the prefs tree where all Venice settings are stored
    private final static String base = "org.mov";

    // The user root from Venice's point of view
    private static Preferences userRoot = Preferences.userRoot().node(base);

    // This class cannot be instantiated
    private PreferencesManager() {
	// nothing to do
    }

    /** Indicates the quote source is using the inbuilt sample quotes. */
    public static int SAMPLES = 0;

    /** Indicates the quote source is accessing quotes in files. */
    public static int FILES = 1;

    /** Indicates the quote source is accessing quotes in a database */
    public static int DATABASE = 2;

    /** Web proxy preferences fields. */
    public class ProxyPreferences {

        /** Web proxy host address. */
	public String host;

        /** Web proxy port. */
	public String port;

        /** Whether we are using the web proxy. */
	public boolean isEnabled;
	
    public String user;
    public String password;
    public boolean authEnabled;

    }

    /** Database preferences fields. */
    public class DatabasePreferences {

	/** Database software (e.g. "mysql"). */
	public String software;

	/** Database host. */
	public String host;

	/** Database port. */
	public String port;

	/** Database name (e.g. "shares") */
	public String database;

	/** Database user name. */
	public String username;

	/** Database password. */
	public String password;
    }

    /** Display preferences fields. */
    public class DisplayPreferences {
	/** X location of main window. */
	public int x;

	/** Y location of main window. */
	public int y;

	/** Width of main window. */
	public int width;

	/** Height of main window. */
	public int height;
    }

    /** Web windows preferences preferences fields. */
    public class WindowPreferencePreferences {

        /** Windows preferences path. */
	public String path;
        /** Windows preferences file current selected. */
	public String XMLfile;
    }

    /** Web language preferences fields. */
    public class LanguagePreferences {

        /** Language. */
	public String locale;
    }

    /**
     * Forces the preferences data to be saved to the backend store (e.g. disk).
     */
    public static void flush() {
	try {
	    userRoot.flush();
	} catch(BackingStoreException e) {
	    // ignore
	}
    }

    /**
     * Fetches the desired user node, based at the <code>base</code> branch
     * @param node the path to the node to be fetched
     */
    public static Preferences getUserNode(String node) {
        if (node.charAt(0) == '/') node = node.substring(1);
        return userRoot.node(node);
    }

    /**
     * Get the preferences from the input XML stream
     * @param inputStream the input XML stream
     */
    public static void importPreferences(InputStream inputStream)
        throws IOException, InvalidPreferencesFormatException {
            userRoot.importPreferences(inputStream);
    }

    /**
     * Set the preferences in the output XML stream
     * @param outputStream the output XML stream
     */
    public static void exportPreferences(OutputStream outputStream)
        throws IOException, BackingStoreException {
            userRoot.exportSubtree(outputStream);
    }

    /**
     * Load the last directory used when importing quote files.
     *
     * @param  the directory type (e.g. macros, importer, etc)
     * @return the directory.
     */
    public static String loadDirectoryLocation(String dirtype) {
        Preferences prefs = getUserNode("/"+dirtype);
        String directory = prefs.get("directory", "");

        if(directory.length() != 0)
            return directory;
        else
            return null;
    }

    /**
     * Save the directory used to import quote files.
     *
     * @param dirtype the directory type (e.g. macros, importer, etc)
     * @param directory the directory.
     */
    public static void saveDirectoryLocation(String dirtype, String directory) {
        Preferences prefs = getUserNode("/"+dirtype);
        prefs.put("directory", directory);
    }

    /**
     * Load the list of all stored equations.
     *
     * @return the list of stored equations.
     * @see StoredEquation
     */
    public static List loadStoredEquations() {
	List storedEquations = new ArrayList();
	Preferences prefs = getUserNode("/equations");

	try {
	    String[] keys = prefs.keys();
	    for(int i = 0; i < keys.length; i++)
		storedEquations.add(new StoredEquation(keys[i], prefs.get(keys[i], "")));
	}
	catch(BackingStoreException e) {
	    // ignore
	}

	return storedEquations;
    }

    /**
     * Save the list of all stored equations.
     *
     * @param storedEquations the stored equations.
     * @see StoredEquation
     */
    public static void saveStoredEquations(List storedEquations) {
	try {
	    // Remove old equations
	    Preferences prefs = getUserNode("/equations");
	    prefs.removeNode();
	    prefs = getUserNode("/equations");

	    for(Iterator iterator = storedEquations.iterator(); iterator.hasNext();) {
		StoredEquation storedEquation = (StoredEquation)iterator.next();
		prefs.put(storedEquation.name, storedEquation.equation);
	    }
	}
	catch(BackingStoreException e) {
	    // ignore
	}
    }

    /**
     * Load the list of all registered macros.
     * 
     * @return the list of registered macros
     * @see StoredMacro
     */

    public static List loadStoredMacros() {
        List stored_macros = new ArrayList();
        Preferences prefs = getUserNode("/macros/info");

	    String dirname = PreferencesManager.loadDirectoryLocation("macros");
	    if (dirname == null) return stored_macros;
	    File directory = new File(dirname);
	    if (!directory.isDirectory())
	        return null;

	    String[] list = directory.list(new FilenameFilter() {
	        public boolean accept(File dir, String filename) {
	            return (dir.getAbsolutePath().equals(PreferencesManager.loadDirectoryLocation("macros")) &&
	                    filename.indexOf(".py") == filename.length()-3);
	        }
	    });
	    
        for(int i = 0; i < list.length; i++) {
            String name = list[i].substring(0,list[i].length()-3);
            Preferences macro_node = getUserNode("/macros/info/"+list[i]);
            stored_macros.add(new StoredMacro(macro_node.get("name", name), 
                    		  list[i],
                 			  macro_node.getBoolean("on_startup",false),
                       		  macro_node.getInt("start_sequence",0),
                       		  macro_node.getBoolean("in_menu", false)));
        }
        return stored_macros;
    }

    /**
     * Save the list of all registered macros.
     *
     * @param storedEquations the registered macros.
     * @see StoredMacro
     */
    public static void saveStoredMacros(List stored_macros) {
        try {
            // Remove old macro definitions
            Preferences prefs = getUserNode("/macros_info");
            prefs.removeNode();
            prefs = getUserNode("/macros_info");
            
            for(Iterator iterator = stored_macros.iterator(); iterator.hasNext();) {
                StoredMacro stored_macro = (StoredMacro)iterator.next();
                Preferences macro_node = getUserNode("/macros/info/"+stored_macro.getFilename());
                macro_node.put("name", stored_macro.getName());
                macro_node.putBoolean("on_startup", stored_macro.isOn_startup());
                macro_node.putInt("start_sequence", stored_macro.getStart_sequence());
                macro_node.putBoolean("in_menu", stored_macro.isIn_menu());
            }
        }
        catch(BackingStoreException e) {
            // ignore
        }
    }
    
    /**
     * Load all saved user input in an Analyser Page.
     *
     * @param key a key which identifies which page settings to load.
     * @return mapping of settings.
     * @see org.mov.analyser.AnalyserPage
     */
    public static HashMap loadAnalyserPageSettings(String key) {

	HashMap settings = new HashMap();
	Preferences p = getUserNode("/analyser/" + key);
	String[] settingList = null;

	// Get all the settings that we've saved
	try {
	    settingList = p.keys();
	}
	catch(BackingStoreException e) {
	    // ignore
	}

	// Now populate settings into a hash
	for(int i = 0; i < settingList.length; i++) {
	    String value = p.get(settingList[i], "");
	    settings.put((Object)settingList[i], (Object)value);
	}

	return settings;
    }

    /**
     * Save all user input in an Analyser Page.
     *
     * @param key a key which identifies which page settings to save.
     * @param settings the settings to save.
     * @see org.mov.analyser.AnalyserPage
     */
    public static void saveAnalyserPageSettings(String key, HashMap settings) {
	Preferences p = getUserNode("/analyser/" + key);

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

	    p.put(setting, value);
	}
    }

    /**
     * Load the last preferences page visited.
     *
     * @return index of the last preferences page visited.
     */
    public static int loadLastPreferencesPage() {
	Preferences prefs = getUserNode("/prefs");
	return prefs.getInt("page", 0);
    }

    /**
     * Save last preferences page visited.
     *
     * @param page index of the last preferences page visited.
     */
    public static void saveLastPreferencesPage(int page) {
	Preferences prefs = getUserNode("/prefs");
	prefs.putInt("page", page);
    }

    /**
     * Load the cache's maximum number of quotes.
     *
     * @return the maximum number of quotes.
     */
    public static int loadMaximumCachedQuotes() {
	Preferences prefs = getUserNode("/cache");
        return prefs.getInt("maximumQuotes", 100000);
    }

    /**
     * Save the cache's maximum number of quotes.
     *
     * @param maximumCachedQuotes the maximum number of quotes.
     */
    public static void saveMaximumCachedQuotes(int maximumCachedQuotes) {
        Preferences prefs = getUserNode("/cache");
        prefs.putInt("maximumQuotes", maximumCachedQuotes);
    }
    
    /**
     * Return a list of the names of all the watch screens.
     *
     * @return the list of watch screen names.
     */
    public static String[] getWatchScreenNames() {
	Preferences p = getUserNode("/watchscreens");
	String[] names = null;

	try {
	    names = p.childrenNames();
	}
	catch(BackingStoreException e) {
	    // don't care
	}

	return names;
    }

    /**
     * Load the watch screen with the given name.
     *
     * @param name the name of the watch screen to load.
     * @return the watch screen.
     */
    public static WatchScreen loadWatchScreen(String name) {
        WatchScreen watchScreen = new WatchScreen(name);

        Preferences p = getUserNode("/watchscreens/" + name);

	try {
            // Load symbols
            String[] symbols = p.node("symbols").childrenNames();

            for(int i = 0; i < symbols.length; i++)
                try {
                    watchScreen.addSymbol(Symbol.find(symbols[i]));
                } catch(SymbolFormatException e) {
                    assert false;
                }
        }
	catch(BackingStoreException e) {
	    // don't care
	}

        return watchScreen;
    }

    /**
     * Save the watch screen.
     *
     * @param watchScreen the watch screen.
     */
    public static void saveWatchScreen(WatchScreen watchScreen) {
        Preferences p = getUserNode("/watchscreens/" + watchScreen.getName());
	p.put("name", watchScreen.getName());

        // Clear old symbols
        try {
        	p.node("symbols").removeNode();
        }
        catch(BackingStoreException e) {
        		// don't care
        }

        // Save watched symbols
        List symbols = watchScreen.getSymbols();

        for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();

            // Later on we will associate things like alerts and stops
            // for each symbol. But at the moment we only keep the list
            // of symbols
            Preferences symbolPrefs = p.node("symbols").node(symbol.toString());
            symbolPrefs.put("present", "1");
        }
    }

    /**
     * Delete the watch screen.
     *
     * @param name the watch screen name.
     */
    public static void deleteWatchScreen(String name) {
	Preferences p = getUserNode("/watchscreens/" + name);

	try {
	    p.removeNode();
	}
	catch(BackingStoreException e) {
	    // don't care
	}
    }

    /**
     * Return a  list of the names of all the portfolios.
     *
     * @return the list of portfolio names.
     */
    public static String[] getPortfolioNames() {
	Preferences p = getUserNode("/portfolio");
	String[] portfolioNames = null;

	try {
	    portfolioNames = p.childrenNames();
	}
	catch(BackingStoreException e) {
	    // don't care
	}

	return portfolioNames;
    }

    /**
     * Delete the portfolio.
     *
     * @param name the portfolio name.
     */
    public static void deletePortfolio(String name) {
	Preferences p = getUserNode("/portfolio/" + name);

	try {
	    p.removeNode();
	}
	catch(BackingStoreException e) {
	    // don't care
	}
    }

    /**
     * Load the portfolio with the given name.
     *
     * @param name the name of the portfolio to load.
     * @return the portfolio.
     */
    public static Portfolio loadPortfolio(String name) {
	Portfolio portfolio = new Portfolio(name);
	
	Preferences p = getUserNode("/portfolio/" + name);

	try {
	    // Load accounts
	    String[] accountNames = p.node("accounts").childrenNames();

	    for(int i = 0; i < accountNames.length; i++) {
		Preferences accountPrefs =
		    p.node("accounts").node(accountNames[i]);
		Account account;

		String accountType = accountPrefs.get("type", "share");
		if(accountType.equals("share")) {
		    account = new ShareAccount(accountNames[i]);
		}
		else {
		    account = new CashAccount(accountNames[i]);
		}

		portfolio.addAccount(account);
	    }

	    // Load transactions
	    List transactions = new ArrayList();

	    String[] transactionNumbers =
		p.node("transactions").childrenNames();
	
	    for(int i = 0; i < transactionNumbers.length; i++) {
		Preferences transactionPrefs =
		    p.node("transactions").node(transactionNumbers[i]);

		int type = getTransactionType(transactionPrefs.get("type", ""));

		TradingDate date = null;

                try {
                    date =
                        new TradingDate(transactionPrefs.get("date",
                                                             "01/01/2000"),
                                        TradingDate.BRITISH);
                }
                catch(TradingDateFormatException e) {
                    // Shouldnt happen unless portfolio gets corrupted
                }

		Money amount = new Money(transactionPrefs.getDouble("amount", 0.0D));
		Symbol symbol = null;
		int shares = transactionPrefs.getInt("shares", 0);
		Money tradeCost = new Money(transactionPrefs.getDouble("trade_cost", 0.0D));

                try {
                    symbol = Symbol.find(transactionPrefs.get("symbol", ""));
                }
                catch(SymbolFormatException e) {
                    // Shouldnt happen unless portfolio gets corrupted
                }

		try {
		    String cashAccountName = transactionPrefs.get("cash_account", "");
		    CashAccount cashAccount =
			(CashAccount)portfolio.findAccountByName(cashAccountName);

		    String cashAccountName2 = transactionPrefs.get("cash_account2", "");
		    CashAccount cashAccount2 =
			(CashAccount)portfolio.findAccountByName(cashAccountName2);

		    String shareAccountName = transactionPrefs.get("share_account", "");
		    ShareAccount shareAccount =
			(ShareAccount)portfolio.findAccountByName(shareAccountName);

		    // Build transaction and add it to the portfolio
		    Transaction transaction =
			new Transaction(type, date, amount, symbol, shares,
					tradeCost, cashAccount, cashAccount2,
                                        shareAccount);
						
		    transactions.add(transaction);
		}
		catch(ClassCastException e) {
		    // Shouldnt happen unless portfolio gets corrupted
		    assert false;
		}
	    }

	    portfolio.addTransactions(transactions);
	
	}
	catch(BackingStoreException e) {
	    // don't care
	}

	return portfolio;
    }

    // Venice 0.1 & 0.2 did not have i8ln support so they saved the
    // transactions by name. But this does not work if the transaction
    // names can change! But I also want 0.3 to be backward compatible
    // with 0.2. So this routine will understand both transaction name
    // and transaction number.
    private static int getTransactionType(String transactionType) {
	// Venice 0.3+ saves transactions by numbers.
	try {
	    return Integer.parseInt(transactionType);
	}
	catch(NumberFormatException e) {
	    // not a number
	}

	// Otherwise compare with all the old transaction names
	if(transactionType.equals("Accumulate"))
	    return Transaction.ACCUMULATE;
	else if(transactionType.equals("Reduce"))
	    return Transaction.REDUCE;
	else if(transactionType.equals("Deposit"))
	    return Transaction.DEPOSIT;
	else if(transactionType.equals("Fee"))
	    return Transaction.FEE;
	else if(transactionType.equals("Interest"))
	    return Transaction.INTEREST;
	else if(transactionType.equals("Withdrawal"))
	    return Transaction.WITHDRAWAL;
	else if(transactionType.equals("Dividend"))
	    return Transaction.DIVIDEND;
	else if(transactionType.equals("Dividend DRP"))
	    return Transaction.DIVIDEND_DRP;
	else
	    return Transaction.TRANSFER;
    }

    /**
     * Save the portfolio.
     *
     * @param portfolio the portfolio.
     */
    public static void savePortfolio(Portfolio portfolio) {
	Preferences p = getUserNode("/portfolio/" + portfolio.getName());
	p.put("name", portfolio.getName());

	// Clear old accounts and transactions
	try {
	    p.node("accounts").removeNode();
	    p.node("transactions").removeNode();
	}
	catch(BackingStoreException e) {
	    // don't care
	}

	// Save accounts
	List accounts = portfolio.getAccounts();
	Iterator iterator = accounts.iterator();

	while(iterator.hasNext()) {
	    Account account = (Account)iterator.next();
	    Preferences accountPrefs =
		p.node("accounts").node(account.getName());
	
	    if(account.getType() == Account.SHARE_ACCOUNT) {
		accountPrefs.put("type", "share");
	    }
	    else {
		accountPrefs.put("type", "cash");
	    }
	}

	// Save transactions
	List transactions = portfolio.getTransactions();
	iterator = transactions.iterator();
	int i = 0; // Store transactions as node 0, 1, 2 etc

	while(iterator.hasNext()) {
	    Transaction transaction = (Transaction)iterator.next();
	    Preferences transactionPrefs = p.node("transactions/" +
						  Integer.toString(i++));
	
	    transactionPrefs.put("type", Integer.toString(transaction.getType()));
	    transactionPrefs.put("date",
			     transaction.getDate().toString("dd/mm/yyyy"));
	    transactionPrefs.putDouble("amount", transaction.getAmount().doubleValue());

	    if(transaction.getSymbol() != null)
		transactionPrefs.put("symbol", transaction.getSymbol().toString());

	    transactionPrefs.putInt("shares", transaction.getShares());
	    transactionPrefs.putDouble("trade_cost",
				      transaction.getTradeCost().doubleValue());

	    CashAccount cashAccount = transaction.getCashAccount();
	    if(cashAccount != null)
		transactionPrefs.put("cash_account",
				     cashAccount.getName());

	    CashAccount cashAccount2 = transaction.getCashAccount2();
	    if(cashAccount2 != null)
		transactionPrefs.put("cash_account2",
				     cashAccount2.getName());

	    ShareAccount shareAccount = transaction.getShareAccount();
	    if(shareAccount != null)
		transactionPrefs.put("share_account",
				     shareAccount.getName());
	}
    }

    /**
     * Load proxy settings.
     *
     * @return proxy preferences.
     */
    public static ProxyPreferences loadProxySettings() {
        Preferences prefs = getUserNode("/proxy");
        PreferencesManager preferencesManager = new PreferencesManager();
        ProxyPreferences proxyPreferences = preferencesManager.new ProxyPreferences();
        proxyPreferences.host = prefs.get("host", "proxy");
        proxyPreferences.port = prefs.get("port", "8080");
        proxyPreferences.isEnabled = prefs.getBoolean("enabled", false);
        
    	proxyPreferences.user= prefs.get("user", "");
    	proxyPreferences.password = prefs.get("password", "");
    	proxyPreferences.authEnabled = prefs.getBoolean("authEnabled", false);

        return proxyPreferences;
    }

    /**
     * Save proxy settings.
     *
     * @param proxyPreferences the new proxy preferences.
     */
    public static void saveProxySettings(ProxyPreferences proxyPreferences) {
	Preferences prefs = getUserNode("/proxy");
	prefs.put("host", proxyPreferences.host);
	prefs.put("port", proxyPreferences.port);
	prefs.putBoolean("enabled", proxyPreferences.isEnabled);
	
	prefs.put("user", proxyPreferences.user);
	prefs.put("password", proxyPreferences.password);
	prefs.putBoolean("authEnabled", proxyPreferences.authEnabled);
	
    }

    /**
     * Load language settings.
     *
     * @return language preferences.
     */
    public static LanguagePreferences loadLanguageSettings() {
        Preferences prefs = getUserNode("/language");
        PreferencesManager preferencesManager = new PreferencesManager();
        LanguagePreferences languagePreferences = preferencesManager.new LanguagePreferences();
        languagePreferences.locale = prefs.get("locale", null);
        return languagePreferences;
    }

    /**
     * Save language settings.
     *
     * @param languagePreferences the new proxy preferences.
     */
    public static void saveLanguageSettings(LanguagePreferences languagePreferences) {
	Preferences prefs = getUserNode("/language");
	prefs.put("locale", languagePreferences.locale);
    }

    /**
     * Load windows preferences settings.
     *
     * @return windows preferences preferences.
     */
    public static WindowPreferencePreferences loadWindowPreferenceSettings() {
        Preferences prefs = getUserNode("/window_preference");
        PreferencesManager preferencesManager = new PreferencesManager();
        WindowPreferencePreferences windowPreferencePreferences = preferencesManager.new WindowPreferencePreferences();
        windowPreferencePreferences.path = prefs.get("path", null);
        windowPreferencePreferences.XMLfile = prefs.get("file", "");
        return windowPreferencePreferences;
    }

    /**
     * Save windows preferences settings.
     *
     * @param windowPreferencePreferences the new proxy preferences.
     */
    public static void saveWindowPreferenceSettings(WindowPreferencePreferences windowPreferencePreferences) {
	Preferences prefs = getUserNode("/window_preference");
      	
	// Replace this check with a dialog saying the 
	// path hasn't been set. 
	if (windowPreferencePreferences.path != null &&
	    windowPreferencePreferences.XMLfile != null) {
	    prefs.put("path", windowPreferencePreferences.path);
	    prefs.put("file", windowPreferencePreferences.XMLfile);
	} else {
	    DesktopManager.showWarningMessage("Window preferences file not set.");   
	}
    }

    /**
     * Get quote source setting.
     *
     * @return quote source, one of {@link #DATABASE}, {@link #FILES} or {@link #SAMPLES}.
     */
    public static int getQuoteSource() {
	Preferences prefs = getUserNode("/quote_source");
	String quoteSource = prefs.get("source", "samples");
	
	if(quoteSource.equals("samples"))
	    return SAMPLES;
	else if(quoteSource.equals("files"))
	    return FILES;
	else 
	    return DATABASE;
    }

    /**
     * Set quote source setting.
     *
     * @param quoteSource the quote source, one of {@link #DATABASE}, {@link #FILES} or 
     *                    {@link #SAMPLES}.
     */
    public static void setQuoteSource(int quoteSource) {
	assert(quoteSource == DATABASE || quoteSource == FILES ||
	       quoteSource == SAMPLES);

	Preferences prefs = getUserNode("/quote_source");
	if(quoteSource == SAMPLES)
	    prefs.put("source", "samples");
	else if(quoteSource == FILES)
	    prefs.put("source", "files");
	else
	    prefs.put("source", "database");
    }

    /**
     * Load database settings.
     *
     * @return database preferences.
     */
    public static DatabasePreferences loadDatabaseSettings() {
        Preferences prefs = getUserNode("/quote_source/database");
        PreferencesManager preferencesManager = new PreferencesManager();
        DatabasePreferences databasePreferences =
            preferencesManager.new DatabasePreferences();
        databasePreferences.software = prefs.get("software", "mysql");
        databasePreferences.host = prefs.get("host", "db");
        databasePreferences.port = prefs.get("port", "3306");
	databasePreferences.database = prefs.get("dbname", "shares");
        databasePreferences.username = prefs.get("username", "");
        databasePreferences.password = prefs.get("password", "3306");
        return databasePreferences;
    }

    /**
     * Save database settings.
     *
     * @param databasePreferences the new database preferences.
     */
    public static void saveDatabaseSettings(DatabasePreferences databasePreferences) {
	Preferences prefs = getUserNode("/quote_source/database");
	prefs.put("software", databasePreferences.software);
	prefs.put("host", databasePreferences.host);
	prefs.put("port", databasePreferences.port);
	prefs.put("dbname", databasePreferences.database);
	prefs.put("username", databasePreferences.username);
	prefs.put("password", databasePreferences.password);
    }

    /**
     * Load display settings.
     *
     * @return display preferences.
     */
    public static DisplayPreferences loadDisplaySettings() {
        Preferences prefs = getUserNode("/display");
        PreferencesManager preferencesManager = new PreferencesManager();
        DisplayPreferences displayPreferences =
            preferencesManager.new DisplayPreferences();
        displayPreferences.x = prefs.getInt("default_x", 0);
        displayPreferences.y = prefs.getInt("default_y", 0);
        displayPreferences.width = prefs.getInt("default_width", 400);
        displayPreferences.height = prefs.getInt("default_height", 400);
        return displayPreferences;
    }

    /**
     * Save display settings.
     *
     * @param displayPreferences the new display preferences.
     */
    public static void saveDisplaySettings(DisplayPreferences displayPreferences) {
	Preferences prefs = getUserNode("/display");
	prefs.putInt("default_x", displayPreferences.x);
	prefs.putInt("default_y", displayPreferences.y);
	prefs.putInt("default_width", displayPreferences.width);
	prefs.putInt("default_height", displayPreferences.height);
    }
}
