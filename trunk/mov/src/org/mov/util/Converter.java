package org.mov.util;

import java.io.File;
import java.lang.*;
import java.util.*;

import org.mov.quote.*;

/**
 * Contains a set of general conversion functions. These functiosn include
 * things like converting two digit years to four digits years, integers
 * to fixed point strings etc.
 */
public class Converter {

    /**
     * Convert a file array to a file name array.
     *
     * @param	files	an array of files
     * @return	a vector of strings
     */
    public static Vector toFileNameVector(File[] files) {
	Vector fileNames = new Vector();
	
	for(int i = 0; i < files.length; i++) {
	    fileNames.add(files[i].getPath());
	}

	return fileNames;
    }

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

    /**
     * Convert a change in two floats to the Change object which can
     * be displayed by SortedTable.
     *
     * @param	a	the start value
     * @param	b	the end value
     * @return	the Change object
     */
    public static Change changeToChange(float a, float b) {
	double percent = 0.0;

	if(a != 0.0) 
	    percent = ((b - a) / a) * 100;

	percent *= 100;
	percent = Math.round(percent);
	percent /= 100;
		
	return new Change(percent);
    }

    /**
     * Convert from a quote (in dollars) to string. 
     *
     * @param	quote	the quote
     * @return	the quote string
     */
    public static String quoteToString(float quote) {
	// First round to 3 decimal places
	quote *= 1000;
	quote = Math.round(quote);
	quote /= 1000;

	return new String(""+quote);
    }

    /**
     * Convert from a price (in dollars) to string. This will add the
     * appropriate "$" and "c" symbols as needed.
     *
     * @param	price	the price
     * @return	the price string
     */
    public static String priceToString(float price) {
	int dollars = (int)Math.abs(price);
	float cents = (Math.abs(price) - dollars) * 100;
	String sign;

	if(price < 0)
	    sign = "-";
	else
	    sign ="";

	// Dollars or cents?
	if(dollars > 0) {
	    return sign + "$" + Integer.toString(dollars) + "." + 
		Converter.toFixedString((int)cents, 2);
	}
	else
	    return 
		sign + 
		Converter.toFixedString((int)cents, 2) + "c";
    }

    /** 
     * Convert a start and end date to a vector of all the trading
     * dates inbetween which do not fall on weekends.
     *
     * @param	startDate	the start date of the range
     * @param	endDate		the end date of the range
     * @return	a vector of all the trading dates inbetween
     */
    public static Vector dateRangeToTradingDateVector(TradingDate startDate,
						      TradingDate endDate) {
	Vector dates = new Vector();

	TradingDate date = (TradingDate)startDate.clone();

	while(!date.after(endDate)) {

	    System.out.println("loop date " + date);

	    dates.add(date);
	    date = date.next(1);
	}

	return dates;
    }
}



