package org.mov.util;

import java.util.*;

/**
 * A comparator for comparing <code>TradingDate</code> objects.
 */
public class TradingDateComparator implements Comparator {

    /**
     * Sort forwards 
     */
    public static int FORWARDS = 0;

    /**
     * Sort backwards
     */
    public static int BACKWARDS = 1;

    // The direction of the sort
    private int direction;

    /**
     * Create a new <code>TradingDateComparator</code> which sorts in the
     * given direction.
     */
    public TradingDateComparator(int direction) {
	this.direction = direction;
    }

    /**
     * Compare the specified objects.
     *
     * @param	o1	the first object
     * @param	o2	the second object
     * @return	<code>0</code>if the objects are equal; <code>1</code> if
     * <code>o1</code> is greater than <code>o2</code> or <code>-1</code>
     * otherwise. If the search order is backwards this will be reversed.
     */
    public int compare(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;

	if(direction == BACKWARDS)
	    return d2.compareTo(d1);
	else
	    return d1.compareTo(d2);
    }

    /** 
     * Test the specified objects for equality.
     *
     * @param	o1	the first object
     * @param	o2	the second object
     * @return	<code>1</code> if the objects have the same date; 
     * <code>0</code> otherwise.
     */
    public boolean equals(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;
	
	return(d2.equals(d1));
    }
}




