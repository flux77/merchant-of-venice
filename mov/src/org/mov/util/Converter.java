package org.mov.util;

import java.util.*;

import org.mov.portfolio.Stock;

public class Converter {

    public static Change changeToChange(float a, float b) {
	double percent = 0.0;

	if(a != 0.0) 
	    percent = ((b - a) / a) * 100;

	percent *= 100;
	percent = Math.round(percent);
	percent /= 100;
		
	return new Change(percent);
    }

    public static String quoteToString(float quote) {
	// First round to 3 decimal places
	quote *= 1000;
	quote = Math.round(quote);
	quote /= 1000;
	return new String(""+quote);
	//	return Double.toString(quote);
    }

    public static String priceToString(float price) {

	// Convert price from cents to amount in dollars
	price /= 100;

	int dollars = (int)Math.abs(price);
	float cents = (price - dollars) * 100;

	// Dollars or cents?
	if(dollars > 0) {
	    return "$" + Integer.toString(dollars) + "." + 
		centsToString(cents);
	}
	else
	    return centsToString(cents) + "c";
    }

    private static String centsToString(float cents) {

	String string = Integer.toString((int)cents);

	if(string.length() < 2)
	    string = "0" + string;

	return string;
    }

    // Converts a start and end date to a vector of all the trading
    // dates inbetween (i.e. all days except saturdays and sundays).
    public static Vector dateRangeToTradingDateVector(TradingDate startDate,
						      TradingDate endDate) {
	Vector dates = new Vector();
	TradingDate date = (TradingDate)startDate.clone();

	while(endDate.compareTo(date) >= 0) {
	    // Add copy of date
	    dates.add((TradingDate)date.clone());
	    // Go to next trading date
	    date.next(1);
	}
	return dates;
    }

    // Converts a line of text like: "AAA,010330,250,250,250,250,0"
    // into stock quote class. 
    // TODO: Needs to deal with other formats besides MetaStock
    // TODO: Doesnt handle errors very well
    // TODO: Be nice if it auto-detected format from file
    public static Stock lineToQuote(String line) {
	Stock stock = null;

	if(line != null) {

	    String[] quoteParts = line.split(",");
	    if(quoteParts.length == 7) {

		String symbol;
		TradingDate date;
		int volume;
		float day_open;
		float day_high;
		float day_low;
		float day_close;
		int i = 0;

		symbol = quoteParts[i++];
		date = new TradingDate(quoteParts[i++]);
		day_open = Float.parseFloat(quoteParts[i++]);
		day_high = Float.parseFloat(quoteParts[i++]);
		day_low = Float.parseFloat(quoteParts[i++]);
		day_close = Float.parseFloat(quoteParts[i++]);
		volume = Integer.parseInt(quoteParts[i++]);
		stock = new Stock(symbol, date, volume, day_low, day_high,
				  day_open, day_close);
	    }	    
	}
	return stock;
    }
}



