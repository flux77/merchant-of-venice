package org.mov.util;

/**
 * An abstract representation of the concept of Change. This class stores the
 * change of a value (in percent). It is currently used as a place holder
 * for the class <code>SortedTable</code> to allow us to differentiate it
 * from the other <code>float</code> values used in that object. This way
 * we can format it differently.
 */
public class Change {
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

}
