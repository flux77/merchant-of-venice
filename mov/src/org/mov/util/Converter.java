package org.mov.util;

import java.util.*;

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

	return Double.toString(quote);
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

    // Finds a date x trading days before the given date
    // Deprecate - functionality in TradingDate class

    /*
    public static Date before(Date startDate, int days) {

	// Convert to Calendar
	GregorianCalendar date = new GregorianCalendar();
	date.setTime(startDate);
	
	for(int i = 0; i < days; i++) {

	    // Add 1 day or more to skip weekends as necessary
	    do {
		date.add(Calendar.DAY_OF_WEEK, 1);
	    } while(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
		    date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);

	}

	// Convert back to date and return
	return date.getTime();
    }
    */

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
}


