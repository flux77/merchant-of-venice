package org.mov.util;

import java.util.*;

public class TradingDateComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;
	
	return(d2.compareTo(d1));
    }
    
    public boolean equals(Object o1, Object o2) {
	TradingDate d1 = (TradingDate)o1;
	TradingDate d2 = (TradingDate)o2;
	
	return(d2.equals(d1));
    }
}




