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

package org.mov.util;

import java.text.NumberFormat;

/**
 * A replacement time for java.util.Calendar.
 *
 * The main principles of this time class are speed (as fast as possible)
 * and size (as small as possible). It produces a much smaller and faster
 * time class than using the Calendar hierarchy.
 *
 * @author Andrew Leppard
 */
public class TradingTime implements Cloneable, Comparable {

    private int hour;
    private int minute;
    private int second;

    // Format to display minutes and seconds
    private static NumberFormat format = null;

    /**
     * Create a new time from the given hour, minute and second.
     *
     * @param hour   the hour from 0 to 23.
     * @param minute the minute from 0 to 59.
     * @param second the second from 0 to 62 (leap seconds).
     */
    public TradingTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /*
     * Create a new time from the given string. We can parse the following
     * time strings:
     * <p>
     * <table>
     * <tr><td><pre>H:MMAP</pre></td><td>e.g. "4:02pm"</td></tr>
     * <tr><td><pre>HH:MMAP</pre></td><td>e.g. "04:02pm"</td></tr>
     * </table>
     *
     * @param time the time string to parse
     * @exception TradingTimeException if the time couldn't be parsed
     */
    public TradingTime(String time)
        throws TradingTimeFormatException {

        hour = 0;
        minute = 0;
        second = 0;

        time = time.toUpperCase();

        try {
            int colonIndex = time.indexOf(':');

            // H:MMAP or HH:MMAP (AP = "AM" or "PM")
            if(colonIndex >= 0 && (time.length() == 6 || time.length() == 7)) {
                int i = 0;

                // HOUR
                assert colonIndex != -1;

                hour = Integer.parseInt(time.substring(i, colonIndex));
                if(hour == 12)
                    hour = 0;

                i = colonIndex + 1;

                // MINUTE
                int dayIndex = time.indexOf('A');
                if(dayIndex == -1)
                    dayIndex = time.indexOf('P');
                if(dayIndex == -1)
                    throw new TradingTimeFormatException(time);

                minute = Integer.parseInt(time.substring(i, dayIndex));

                // AM or PM
                i = dayIndex;
                if(time.substring(i).equals("AM"));
                else if(time.substring(i).equals("PM"))
                    hour += 12;
                else
                    throw new TradingTimeFormatException(time);
            }

            // We don't recognise the format
            else
                throw new TradingTimeFormatException(time);
        }

        // If we can't parse, throw an exception
        catch(NumberFormatException e) {
            throw new TradingTimeFormatException(time);
        }
        catch(StringIndexOutOfBoundsException e) {
            throw new TradingTimeFormatException(time);
        }

        // Simple range checking
        if(hour > 23 || minute > 59 || second > 62)
            throw new TradingTimeFormatException(time);
    }

    /*
     * Create a clone of this time.
     *
     * @return	a clone of this time.
     */
    public Object clone() {
	return (Object)(new TradingTime(getHour(), getMinute(), getSecond()));
    }

    /**
     * Compare the current time to the specified object.
     *
     * @see #compareTo(TradingTime)
     */
    public int compareTo(Object time) {
	return compareTo((TradingTime)time);
    }

    /**
     * Compare the current time to the specified time.
     *
     * @param	time	the time to compare
     * @return the value <code>0</code> if the times are equal;
     * <code>1</code> if this time is after the specified time or
     * <code>-1</code> if this time is before the specified time.
     */
    public int compareTo(TradingTime time) {
	if(getHour() < time.getHour())
	    return -1;
	if(getHour() > time.getHour())
	    return 1;

	if(getMinute() < time.getMinute())
	    return -1;
	if(getMinute() > time.getMinute())
	    return 1;

	if(getSecond() < time.getSecond())
	    return -1;
	if(getSecond() > time.getSecond())
	    return 1;

	return 0;
    }

    /**
     * Return the hour.
     *
     * @return the hour staring from 0.
     */
    public int getHour() {
        return hour;
    }

    /**
     * Return the minute.
     *
     * @return the minute staring from 0.
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Return the second.
     *
     * @return the second staring from 0.
     */
    public int getSecond() {
        return second;
    }

    /**
     * Returns a string version of the time.
     *
     * @return string version
     */
    public String toString() {
        return (getHour() + ":" +
                getNumberFormat().format(getMinute()) + ":" +
                getNumberFormat().format(getSecond()));
    }

    /**
     * Return the format suitable for displaying the minute and second
     * fields.
     *
     * @return the number format.
     */
    private static NumberFormat getNumberFormat() {
        // Synchronisation cannot cause issues here. So this code
        // isn't synchronised.
        if(format == null) {
            format = NumberFormat.getInstance();
            format.setMinimumIntegerDigits(2);
            format.setMinimumFractionDigits(0);
            format.setMaximumFractionDigits(0);
        }

        return format;
    }
}