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

/**
 * An abstract representation of the concept of Change. This class stores the
 * change of a value (in percent). It is currently used as a place holder
 * for the class <code>SortedTable</code> to allow us to differentiate it
 * from the other <code>float</code> values used in that object. This way
 * we can format it differently.
 */
public class Change implements Comparable {
    double change;

    /**
     * Create a new <code>Change</code> object.
     *
     * @param	change	the change in percent
     */
    public Change(double change) {
	this.change = change;
    }
    
    /**
     * Get the change percent.
     *
     * @return	the change in percent
     */
    public double getChange() {
	return change;
    }

    /**
     * Compare two change objects.
     *
     * @param	change	change object to compare to
     * @return	the value <code>0</code> if the change objects are equal;
     * <code>1</code> if this change object is after the specified change
     * object or
     * <code>-1</code> if this change object is before the specified change
     * object
     */
    public int compareTo(Object object) {

	Change change = (Change)object;

	if(getChange() < change.getChange())
	    return -1;
	if(getChange() > change.getChange())
	    return 1;

	return 0;
    }	    
}
