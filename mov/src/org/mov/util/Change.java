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
