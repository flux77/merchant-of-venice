package org.mov.util;

import java.util.*;
import java.util.regex.*;

/**
 * A replacement date for java.util.Date, java.util.Calendar & 
 * java.sql.Date.
 *
 * The main principles of this date class are speed (as fast as possible)
 * and size (as small as possible). It produces a much smaller and faster
 * date class than using the Calendar hierarchy. It also beats java.util.Date
 * by not using deprecated methods.
 */
public class TradingDate {

    private int year;
    private int month;
    private int day;

    /**
     * Create a new date from the given year, month and day.
     *
     * @param	year	a four digit year, e.g. 1996
     * @param	month	the month starting from 1
     * @param	day	the day starting from 1
     */
    public TradingDate(int year, int month, int day) {
	this.year = year;
	this.month = month;
	this.day = day;
    }
    
    /**
     * Create a new date from the given java.util.Date object.
     *
     * @param	date	the date to convert
     */
    public TradingDate(Date date) {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(date);
	this.year = gc.get(Calendar.YEAR);
	this.month = gc.get(Calendar.MONTH) + 1;
	this.day = gc.get(Calendar.DATE);
    }

    /**
     * Create a new date from the given string. We can parse the following
     * date string:
     * <p>
     * <table>
     * <tr><td><pre>YYMMDD</pre></td><td>e.g. "010203"</td></tr>
     * <tr><td><pre>YYYYMMDD</pre></td><td>e.g. "20010203"</td></tr>
     * <tr><td><pre>MM/DD/YY</pre></td><td>e.g. "03/02/01"</td></tr>
     * </table>
     *
     * @param	date	the date string to convert from
     */
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

    /**
     * Create a new date set to today.
     */
    public TradingDate() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(new Date());
	this.year = gc.get(Calendar.YEAR);
	this.month = gc.get(Calendar.MONTH) + 1;
	this.day = gc.get(Calendar.DATE);
    }

    /**
     * Return the year.
     *
     * @return four digit year
     */
    public int getYear() {
	return year;
    }

    /**
     * Return the month.
     *
     * @return the month starting with 1 for January
     */
    public int getMonth() {
	return month;
    }

    /**
     * Return the day.
     *
     * @return the day of the month starting from 1
     */
    public int getDay() {
	return day;
    }

    /**
     * Tests if this date is before the specified date.
     *
     * @param	the specified date to compare
     * @return	<pre>true</pre> if the given date is before this one
     */
    public boolean before(Object date) {
	if(compareTo(date) > 0)
	    return false;
	else 
	    return true;
    }

    /**
     * Tests if this date is after the specified date.
     *
     * @param	the specified date to compare
     * @return	<pre>true</pre> if the specified date is before this one; 
     * <pre>false</pre> otherwise.
     */
    public boolean after(Object date) {
	if(compareTo(date) > 0)
	    return true;
	else 
	    return false;
    }

    /**
     * Compares this date with the specified object.
     *
     * @param	the specified date to compare
     * @return	<pre>true</pre> if the specified date is equal; 
     * <pre>false</pre> otherwise.
     */
    public boolean equals(Object date) {
	if(compareTo(date) == 0)
	    return true;
	else
	    return false;
    }

    /**
     * Create a clone of this date
     *
     * @return	a clone of this date
     */
    public Object clone() {
	return (Object)(new TradingDate(getYear(), getMonth(), 
					getDay()));
    }

    /**
     * Create a fast hash code of this date
     *
     * @return	hash code
     */
    public int hashCode() {
	// theres enough room in an int to store all the data
	return getDay() + getMonth() * 256 + getYear() * 65536;
    }

    /**
     * Move the current date the specified number of trading days backward.
     *
     * @param	days	the number of days to move
     */
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

    /**
     * Move the current date the specified number of trading days forward.
     *
     * @param	days	the number of days to move
     */
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

    /**
     * Compare the current date to the specified object.
     *
     * @see #compareTo(TradingDate)
     */
    public int compareTo(Object date) {
	return compareTo((TradingDate)date);
    }

    /**
     * Compare the current date to the specified date.
     *
     * @param	date	the date to compare
     * @return the value <pre>0</pre> if the dates are equal;
     * <pre>1</pre> if this date is after the specified date or
     * <pre>-1</pre> if this date is before the specified date.
     */
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

    // In the given source string replace all occurences of patternText with
    // text.
    private String replace(String source, String patternText, String text) {
	Pattern pattern = Pattern.compile(patternText);
	Matcher matcher = pattern.matcher(source);
	return matcher.replaceAll(text);
    }

    /**
     * Outputs the date in a format SQL can understand - 2001-12-30.
     *
     * @return	SQL friendly date string
     */
    public String toString() {
	
    	return getYear() + "-" + getMonth() + "-" + getDay();
    }

    /**
     * Outputs the date in the format - 12/Dec.
     *
     * @return	short version of the date string
     */
    public String toShortString() {
	return getDay() + "/" + getMonth();
    }

    /**
     * Outputs date in the format - 30 Dec, 2001.
     *
     * @return	long version of the date string
     */
    public String toLongString() {
	return getDay() + " " + monthToText(getMonth()) + ", " +
	    getYear();
    }

    /**
     * Convert a month number to its 3 digit name.
     *
     * @param	month	the month number
     * @return	the 3 digit month string
     */
    public static String monthToText(int month) {
	String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			   "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	month--;

	if(month < months.length && month >= 0)
	    return months[month];
	else
	    return "Dec";
    }

    /**
     * Convert this object to a java.util.Date.
     *
     * @return	<pre>java.util.Date</pre>
     */
    public Date toDate() {
	return this.toCalendar().getTime();
    }

    /**
     * Convert this object to a java.util.Calendar.
     *
     * @return	<pre>java.util.Calendar</pre>
     */
    public Calendar toCalendar() {
	// Convert from our month of 1-12 to theirs of 0-11
	return new GregorianCalendar(getYear(), getMonth() - 1, getDay());
    }
}

