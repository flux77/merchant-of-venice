package org.mov.util;

// TODO: Make the next() previous() return a new TradingDate class and
// leave the current one alone. This fits in with the java philosophy better
// and works out neater i think.

// Java's three date classes (java.util.Date, java.sql.Date &
// java.util.Calendar) are a mess. This takes up less room (hopefully),
// is faster and does exactly what is needed. (Although calendar is still
// used)

import java.util.*;

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

    // Create trading date set to closest trading date to today (e.g.
    // if today is Saturday, this will be set to last Friday).
    public TradingDate() {

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

