package org.mov.util;

import java.util.*;

public class TradingDateComparator implements Comparator {
    
    public static int FORWARDS = 0;
    public static int BACKWARDS = 1;

    private int direction;

    public TradingDateComparator(int direction) {
	this.direction = direction;
    }

    public int compare(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;

	if(direction == BACKWARDS)
	    return d2.compareTo(d1);
	else
	    return d1.compareTo(d2);
    }
    
    public boolean equals(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;
	
	return(d2.equals(d1));
    }
}




