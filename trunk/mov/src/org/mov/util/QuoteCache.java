package org.mov.util;

// TODO: Create a quote "transaction" that is a single class that can
// interface with any "DatabaseSource" so this class doent need to remember
// whether it contains a single stock or multiple.
// TODO: Make it handle auto-expand of a single stock
// TODO: Make a single global cache for the entire programme that can load/
// delete single stocks and multiple stock days etc. That way multiple
// requests by the user does not require a db call.
// TODO: Make it handle if stock "skips" a day which for some reason they
// sometimes do

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;

public class QuoteCache {

    // Load this many quote dates in any one sql query
    // 96megs holds about 200 trading days
    private final static int QUERY_PACKET_SIZE = 10;

    // Keep list of dates in cache
    private ArrayList dates = new ArrayList();

    // Cache is organised by a vector of hashmaps, each hashmap 
    // corresponds to a trading day. The hashmap's keys are stock symbols.
    private Vector cache = new Vector();

    // Keep set of stock symbols in cache
    private TreeSet symbols = new TreeSet();

    // Remember search restriction
    private int searchRestriction;

    // Remember if we loaded a single or multiple stocks
    private boolean multipleStocks;

    // Load cache with single stock of given symbol
    public QuoteCache(String symbol) {
	multipleStocks = false;

	load(Database.getInstance().getQuotesForSymbol(symbol));
    }

    // Load cache with all stocks for single day
    public QuoteCache(TradingDate date, int searchRestriction) {
	this.searchRestriction = searchRestriction;
	this.multipleStocks = true;

	load(Database.getInstance().getQuotesForDate(date, 
						     searchRestriction));
    }

    public float getQuote(String symbol, int quote, int date) 
	throws EvaluationException {

	if(containsDate(date)) {
	    
	    // First get hashmap of quotes for this date
	    HashMap quotesForDate = (HashMap)cache.elementAt(-date);

	    // Get stock for this symbol at this date
	    Stock stock = (Stock)quotesForDate.get(symbol);

	    // No stock? no value
	    if(stock != null) 
		return stock.getQuote(quote);
	    else
		return 0.0F;
	}

	// Not found
	throw new EvaluationException("date out of range");
    }

    // careful if date isnt available this will crash!
    public float getQuote(String symbol, int quote, TradingDate date) 
	throws EvaluationException {
	
	return getQuote(symbol, quote, dateToOffset(date));
    }

    // Negatve index since date at 1 is *before* date at 0 not after.
    public int dateToOffset(TradingDate date) {

	return -Collections.binarySearch(dates, date, 
					 new TradingDateComparator());
    }

    public TradingDate offsetToDate(int offset) {
	// Check bounds
	if(!containsDate(offset))
	    return null;
	else
	    return (TradingDate)dates.get(offset);
    }

    public boolean containsDate(TradingDate date) {
	return containsDate(dateToOffset(date));
    }

    // Check to see if the cache contains the date (will try to autoload the
    // date into the cache if possible).
    public boolean containsDate(int date) {
	if(containsDateNoAutoload(date))
	    return true;

	// If we are caching a single stock we will have already loaded in
	// all the quotes we can, but if its mulitple stocks we can always
	// try to load in more (providing the date is OLDER than the dates
	// in the cache)
	if(multipleStocks && date < 0) {
	    // If we dont contain the date try to load it in.
	    // Keep trying autoloads until either: 
	    // A) the cache size does not increase
	    // B) the date is now in the cache
	    // We do this because its possible that the autoload 'just misses'
	    // loading our date in.
	    
	    int oldsize;
	    
	    do {
		oldsize = dates.size();
		autoload(date);
	    }
	    while(oldsize > dates.size() && !containsDateNoAutoload(date));

	    return(containsDateNoAutoload(date));
	}
	else
	    return false;
    }

    public Iterator dateIterator() {
	return dates.iterator();
    }

    public Object[] getSymbols() {
	return symbols.toArray();
    }

    public TradingDate getStartDate() {
	// last date in list
	return (TradingDate)dates.get(dates.size() - 1); 
    }

    public TradingDate getEndDate() {
	// first date in list	
	return (TradingDate)dates.get(0); 
    }

    // Number of days in cache
    public int getNumberDays() {
	return dates.size();
    }

    // Number of symbols in cache
    public int getNumberSymbols() {
	return symbols.size();
    }

    // Checks to see if the date is in the cache - does not try to
    // autload the date if it isnt
    public boolean containsDateNoAutoload(int date) {
	if(date <=0 && date > -dates.size())
	    return true;
	else
	    return false;
    }

    // If the user tries to access a date not in cache we will automatically
    // load it in. Cache can expand to encompass older dates but the
    // newest date is FIXED (at index 0).
    //
    // NB: Should only be called if theres multiple stocks in cache

    private void autoload(int date) {

	// Convert to real index
	date = -date;

	// Loads quotes from database. Loads all quotes from the day requested
	// [day] up until all the days in the database cache. Will break down
	// load request into day chunks to reduce memory usage as java's
	// sql memory code is very inefficient. also this always future
	// progress metre stuff.
	int packets = ((date - getNumberDays()) / QUERY_PACKET_SIZE) + 1;

	// Keep expanding by query packet size
	for(int i = 0; i < packets; i++) {
	    if((date - getNumberDays()) >  QUERY_PACKET_SIZE)
		autoloadPacket(getNumberDays() + QUERY_PACKET_SIZE);
	    else
		autoloadPacket(date);
	}
    }

    // Autoloads in up to specific date 
    private void autoloadPacket(int date) {

	// Calculate end date we have to load from - which is one before
	// current start date
	TradingDate end = (TradingDate)getStartDate().clone();
	end.previous(1);

	// Calculate start date we have to load to
	TradingDate start = (TradingDate)getStartDate().clone();
	start.previous(1 + date - getNumberDays());

	// Load data from database and load it into cache
	load(Database.getInstance().getQuotesForDates(start, end,
						      searchRestriction));

    }

    // Loads a vector of quotes into the cache. Vector is a vector of
    // Stock classes.
    private void load(Vector quotes) {

	// Date we are processing
	TradingDate lastDate = null;
	int i = 0;
	HashMap map = null;
	
	// Insert each row of quotes into our cache
	while(quotes.size() > 0) {

	    // Process newest date first (quotes is ordered oldest to newest)
	    Stock stock = (Stock)quotes.remove(quotes.size() - 1);

	    // If its a new date, create a new HashMap for this date in
	    // the vector
	    if(lastDate == null || lastDate.compareTo(stock.getDate()) != 0) {
		lastDate = stock.getDate(); // new date
		dates.add(lastDate); // add date to cache
		cache.add(map = new HashMap());
	    }

	    // Periodically shrink vector we are removing elements from
	    // and force GC to keep up. This makes sure we don't require
	    // a big "spike" of memory to keep this cache in memory and
	    // the vector returned from the database
	    if(i++ % 10000 == 0) {
		quotes.trimToSize();
		Runtime.getRuntime().gc();
	    }

	    // Add symbol to our set if its not there already
	    if(!symbols.contains(stock.getSymbol())) 
		symbols.add(stock.getSymbol());

	    // Put stock in map and remove symbol and date to reduce memory
	    map.put(stock.getSymbol(), stock);
	    stock.setSymbol(null);
	    stock.setDate(null);
	}

	// Trim all vectors etc to needed size
	cache.trimToSize();
	dates.trimToSize();
	Runtime.getRuntime().gc();
    }
}


