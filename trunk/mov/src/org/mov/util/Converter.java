package org.mov.util;

import java.util.*;

import org.mov.portfolio.Stock;

public class Converter {

    /**
     * Convert a number to a fixed length string of the given number of
     * digits. E.g. converting 3 to a fixed 4 digit string yields "0003".
     *
     * @param	number	the number to convert into a string
     * @param	digits	the fixed number of digits to output
     * @return	the string
     */
    public static String toFixedString(int number, int digits) {
	String string = Integer.toString(number);
	String zero = new String("0");
	
	// Keep adding zeros at the front until its as big as digits
	while(string.length() < digits) {
	    string = zero.concat(string);
	}
	
	return string;
    }

    /**
     * Converts a two digit year to four digit year. The year 0 to 30
     * are transformed to 2000 to 2030 respecitvely; the years 31 to 99 to 
     * 1931 and 1999 respectively.
     * 
     * @param	year	a two digit year
     * @return	a four digit year
     */
    public static int twoToFourDigitYear(int year) {
	// Convert year from 2 digit to 4 digit
	if(year > 30)
	    year += 1900;
	else
	    year += 2000;

	return year;
    }

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
    }

    public static String priceToString(float price) {
	int dollars = (int)Math.abs(price);
	float cents = (price - dollars) * 100;

	// Dollars or cents?
	if(dollars > 0) {
	    return "$" + Integer.toString(dollars) + "." + 
		Converter.toFixedString((int)cents, 2);
	}
	else
	    return Converter.toFixedString((int)cents, 2) + "c";
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
}



