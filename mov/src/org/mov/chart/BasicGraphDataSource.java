package org.mov.chart;

import java.util.*;

import org.mov.util.*;

// Class contains enough information to plot a graph but without the
// x,y axis lines etc
abstract public class BasicGraphDataSource {

    abstract public TradingDate getEndDate();
    abstract public TradingDate getStartDate();
    abstract public Float getValue(TradingDate date);

    // Given a range of dates and an implemented getValue() method
    // we can calculate the highest and lowest values
    public float getHighestValue(Vector dates) {
	Iterator iterator = dates.iterator();
	Float value = null;
	Float highest = new Float(Float.MIN_VALUE);
	
	while(iterator.hasNext()) {
	    value = getValue((TradingDate)iterator.next());

	    if(highest == null && value != null)
		highest = value;
	    else if(value != null && value.compareTo(highest) > 0)
		highest = value;
	}
	
	return highest.floatValue();
    }

    public float getLowestValue(Vector dates) {
	Iterator iterator = dates.iterator();
	Float value = null;
	Float lowest = new Float(Float.MAX_VALUE);
	
	while(iterator.hasNext()) {

	    value = getValue((TradingDate)iterator.next());
	    
	    if(lowest == null && value != null)
		lowest = value;
	    else if(value != null && value.compareTo(lowest) < 0)
		lowest = value;
	}

	return lowest.floatValue();
    }
}

