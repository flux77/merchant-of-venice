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

import java.lang.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.mov.quote.*;

/**
 * Contains a set of general conversion functions. Previously this was a
 * grab bag of functions that converted anything to anything else.
 * However thinking in more OO terms some of these functions better belonged
 * elsewhere and have been moved. This class is gradually dwindling away and
 * will be removed soon.
 */
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
     * Convert a string containing a list of symbols separated by spaces
     * or commas into a sorted set with duplicates removed.
     *
     * e.g "CBA WBC TLS" -> [CBA, TLS, WBC].
     *
     * @param	string	list of symbols
     * @return	a sorted set of each symbol in string
     */
    public static SortedSet stringToSortedSet(String string) {
        // Split the string around spaces or commas
        Pattern pattern = Pattern.compile("[, ]+");
        String[] symbols = pattern.split(string);
	TreeSet sortedSet = new TreeSet(Collator.getInstance());

        for(int i = 0; i < symbols.length; i++)
            sortedSet.add(symbols[i]);
	
	return sortedSet;
    }
}



