package org.mov.chart;

import java.util.*;

import org.mov.util.*;

public class Graphable {

    private TradingDate start;
    private TradingDate end;
    private LinkedHashMap map;

    public Graphable() {
	map = new LinkedHashMap();
    }

    public TradingDate getEndDate() {
	return end;
    }

    public TradingDate getStartDate() {
	return start;
    }

    public Float getValue(TradingDate date) {
	return (Float)map.get(date);
    }

    public void putValue(TradingDate date, Float value) {

	// Make sure out start and end dates are consistent
	if(start == null || date.before(start))
	    start = (TradingDate)date.clone();
	if(end == null || date.after(end))
	    end = (TradingDate)date.clone();

	map.put(date, (Object)value);
    }

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

    public Set getDates() {
	return map.keySet();
    }

    public float[] toArray() {
	Collection valueCollection = map.values();
	Iterator iterator = valueCollection.iterator();

	float[] values = new float[map.size()];
	Float value;
	int i = 0;

	while(iterator.hasNext()) {
	    //	    System.out.println("object type is " +
	    //		       (Object)iterator.next());

	    value = (Float)iterator.next();	   
	    values[i++] = value.floatValue();
	}
	
	return values;
    }
}
