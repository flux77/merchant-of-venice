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

import java.io.File;
import java.lang.*;
import java.text.*;
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
	    dates.add(date);
	    date = date.next(1);
	}

	return dates;
    }

    /** Convert space separated list into vector of compay symbols
     * e.g "CBA WBC TLS" -> [CBA, TLS, WBC].
     *
     * @param	string	list of symbols
     * @return	a sorted set of each symbol in string
     */
    public static SortedSet stringToSortedSet(String string) {
	int space;
	Vector vector = new Vector();
	boolean endOfString = false;

	if(string.length() > 0) {
	    while(!endOfString) {
		space = string.indexOf(" ");
		
		if(space == -1) {
		    vector.add(string);
		    endOfString = true;
		}
		else {
		    vector.add(new String(string.substring(0, space)));
		    string = string.substring(space+1);
		}
	    }
	}
	
	// Sort vector
	TreeSet sortedSet = new TreeSet(Collator.getInstance());
	sortedSet.addAll(vector);
	
	return sortedSet;
    }
}



