package org.mov.util;

// TODO: Make the next() previous() return a new TradingDate class and
// leave the current one alone. This fits in with the java philosophy better
// and works out neater i think.

// Java's three date classes (java.util.Date, java.sql.Date &
// java.util.Calendar) are a mess. This takes up less room (hopefully),
// is faster and does exactly what is needed. (Although calendar is still
// used)

import java.util.*;
import java.util.regex.*;

public class TradingDate {

    private int year;
    private int month;
    private int day;

    // Stored as month 1-12, day 1-31
    public TradingDate(int year, int month, int day) {
	this.year = year;
	this.month = month;
	this.day = day;
    }
    
    public TradingDate(Date date) {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(date);
	this.year = gc.get(Calendar.YEAR);
	this.month = gc.get(Calendar.MONTH) + 1;
	this.day = gc.get(Calendar.DATE);
    }

    // Create a trading date from a string. Can be in one of the following
    // formats:
    // YYMMDD, e.g. "010203"
    // YYYYMMDD, e.g. "20010203"
    // MM/DD/YY, e.g. "03/02/01"
    public TradingDate(String date) {

	// Handle DD/MM/YY
	if(date.lastIndexOf('/') != -1) {
		month = Integer.parseInt(date.substring(0, 2));
		day = Integer.parseInt(date.substring(3, 5));
		year = Integer.parseInt(date.substring(6, 8));
		
		year = Converter.twoToFourDigitYear(year);
	}

	// Handle YYMMDD and YYYYMMDD
	else {

	    if(date.length() == 6) {
		year = Integer.parseInt(date.substring(0, 2));
		month = Integer.parseInt(date.substring(2, 4));
		day = Integer.parseInt(date.substring(4, 6));

		year = Converter.twoToFourDigitYear(year);
	    }
	    else if(date.length() == 8) {
		year = Integer.parseInt(date.substring(0, 4));
		month = Integer.parseInt(date.substring(4, 6));
		day = Integer.parseInt(date.substring(6, 8));
	    }
	}
    }

    // Construct trading date set to today
    public TradingDate() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(new Date());
	this.year = gc.get(Calendar.YEAR);
	this.month = gc.get(Calendar.MONTH) + 1;
	this.day = gc.get(Calendar.DATE);
    }

    public int getYear() {
	return year;
    }

    public int getMonth() {
	return month;
    }

    public int getDay() {
	return day;
    }

    public boolean before(Object date) {
	if(compareTo(date) > 0)
	    return false;
	else 
	    return true;
    }

    public boolean after(Object date) {
	if(compareTo(date) > 0)
	    return true;
	else 
	    return false;
    }

    public boolean equals(Object date) {
	if(compareTo(date) == 0)
	    return true;
	else
	    return false;
    }

    public Object clone() {
	return (Object)(new TradingDate(getYear(), getMonth(), 
					getDay()));
    }

    public int hashCode() {
	// theres enough room in an int to store all the data
	return getDay() + getMonth() * 256 + getYear() * 65536;
    }

    public void previous(int days) {

	Calendar date = this.toCalendar();

	for(int i = 0; i < days; i++) {

	    // Take 1 day or more to skip weekends as necessary
	    do {
		date.add(Calendar.DAY_OF_WEEK, -1);
	    } while(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
		    date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
	}

	// Convert back
	year = date.get(Calendar.YEAR);
	month = date.get(Calendar.MONTH) + 1;
	day = date.get(Calendar.DAY_OF_MONTH);
    }

    public void next(int days) {

	Calendar cal = this.toCalendar();

	for(int i = 0; i < days; i++) {

	    // Add 1 day or more to skip weekends as necessary
	    do {
		cal.add(Calendar.DAY_OF_WEEK, 1);
	    } while(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
		    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
	}

	// Convert back
	year = cal.get(Calendar.YEAR);
	month = cal.get(Calendar.MONTH) + 1;
	day = cal.get(Calendar.DAY_OF_MONTH);
    }

    public int compareTo(Object date) {
	return compareTo((TradingDate)date);
    }

    public int compareTo(TradingDate date) {
	if(getYear() < date.getYear())
	    return -1;
	if(getYear() > date.getYear())
	    return 1;

	if(getMonth() < date.getMonth())
	    return -1;
	if(getMonth() > date.getMonth())
	    return 1;

	if(getDay() < date.getDay())
	    return -1;
	if(getDay() > date.getDay())
	    return 1;

	return 0;
    }

    /**
     * Convert date to string in specified format. Will convert the date
     * to a string matching the given format.
     * The following substitutions will be made:
     * <p>
     * <table>
     * <tr><td><pre>d?</pre></td><td>Replaced with one or two digit day</td></tr>
     * <tr><td><pre>dd</pre></td><td>Replaced with two digit day</td></tr>
     * <tr><td><pre>m?</pre></td><td>Replaced with one or two digit month</td></tr>
     * <tr><td><pre>mm</pre></td><td>Replaced with two digit month</td></tr>
     * <tr><td><pre>MMM</pre></td><td>Replaced with 3 letter month name</td></tr>
     * <tr><td><pre>yy</pre></td><td>Replaced with two digit year</td></tr>
     * <tr><td><pre>yyyy</pre></td><td>Replaced with four digit year</td></tr>
     * </table>
     * <p>
     * E.g.:
     * <pre>text = date.toString("d?-m?-yyyy");</pre>
     *
     * @param	format	the format of the string
     * @return	the text string 
     */
    public String toString(String format) {

	format = replace(format, "d\\?", Integer.toString(getDay())); 
	format = replace(format, "dd", Converter.toFixedString(getDay(), 2)); 
	format = replace(format, "m\\?", Integer.toString(getMonth())); 
	format = replace(format, "mm", 
			 Converter.toFixedString(getMonth(), 2)); 

	format = replace(format, "MMM", monthToText(getMonth()));
	format = replace(format, "yyyy", 
			 Converter.toFixedString(getYear(), 4));	
	
	format = replace(format, "yy", 
			 Integer.toString(getYear()).substring(2));
	return format;
    }

    /**
     * Convert the date to a string. Defaults to the format string "d?/MMM/yy",
     * e.g. 3/Sep/01.
     *
     * @return	the text string
     */
    //    public String toString() {
    //	return toString("d?/MMM/yy");
    //}

    private String replace(String source, String patternText, String text) {
	Pattern pattern = Pattern.compile(patternText);
	Matcher matcher = pattern.matcher(source);
	return matcher.replaceAll(text);
    }

    // Outputs date in a format SQL can understand "year-month-day"
    public String toString() {
	
    	return getYear() + "-" + getMonth() + "-" + getDay();
    }

    // Outputs date in format "day/month"
    public String toShortString() {
	return getDay() + "/" + getMonth();
    }

    // Outputs date in format "monthname day, year"
    public String toLongString() {
	return getDay() + " " + monthToText(getMonth()) + ", " +
	    getYear();
    }

    public static String monthToText(int month) {
	String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			   "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	month--;

	if(month < months.length && month >= 0)
	    return months[month];
	else
	    return "Dec";
    }

    public Date toDate() {
	return this.toCalendar().getTime();
    }

    public Calendar toCalendar() {
	// Convert from our month of 1-12 to theirs of 0-11
	return new GregorianCalendar(getYear(), getMonth() - 1, getDay());
    }
}

