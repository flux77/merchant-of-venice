package org.mov.chart;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.mov.util.*;

public class HorizontalAxis {
    
    // Axis is in years
    public final static int YEARS = 0;
    
    // Axis is in quarters
    public final static int QUARTERS = 1;
    
    // Axis is in months
    public final static int MONTHS = 2;

    // Months in a quarter
    private final static int MONTHS_PER_QUARTER = 3;
    
    // Axis can be either major or minor
    public final static int MAJOR = 0;
    public final static int MINOR = 1;

    // Factors determing height of grid lines
    static final int STATIC_GRID_HEIGHT = 5;
    static final int VARIABLE_GRID_HEIGHT_SCALE = 50;
    static final float MAJOR_MINOR_GRID_PROPORTION = 1.5F;

    private Vector dates;
    private Vector points;
    private int period;
    private int type;

    public HorizontalAxis(Vector dates, int period, int type) {

	Iterator iterator = dates.iterator();
        TradingDate thisDate = null;
	int lastValue = -1;
	int thisValue;
	int i = 0;

	this.period = period;
	this.type = type;

	this.dates = new Vector();
	this.points = new Vector();

	while(iterator.hasNext()) {
	    thisDate = (TradingDate)iterator.next();
	    thisValue = value(thisDate);

	    // Add the point if:
	    // 1. Its the first point on the graph
	    // 2. Its the last point on the graph
	    // 3. Its a change in axis (e.g Jan->Feb)

	    if(lastValue == -1 || !iterator.hasNext() || 
	       thisValue != lastValue)
		add(thisDate, new Integer(i));

	    i++;

	    lastValue = thisValue;
	}
    }

    public void drawLabels(Graphics g, float scale, int x, int y) {
	Integer value;
	int lastValue = 0;
	TradingDate date, lastDate;
	int midX, startX;
	int availableWidth;
	Iterator dateIterator = dates.iterator();
	Iterator pointIterator = points.iterator();
	String string;

	lastValue = ((Integer)pointIterator.next()).intValue();
	lastDate = (TradingDate)dateIterator.next();

	while(pointIterator.hasNext()) {

	    value = (Integer)pointIterator.next();
	    date = (TradingDate)dateIterator.next();
	    string = stringValue(lastDate);

	    availableWidth = (int)((value.intValue() - lastValue) * scale);
	    
	    if(availableWidth >
	       1.5 * g.getFontMetrics().stringWidth(string)) {
		midX =
		    (int)(((value.intValue() + lastValue) / 2) * scale) + x;
		startX =
		    midX - g.getFontMetrics().stringWidth(string) / 2;

		g.drawString(string, startX, y);
	    }

	    lastValue = value.intValue();
	    lastDate = date;
	}
    }

    public static float calculateScale(int width, int dataPoints) {
	float horizontalScale = 1.0F;
	
	if(dataPoints < width) {
	    horizontalScale = (float)width / dataPoints;
	}
	return horizontalScale;
    }

    public void drawGrid(Graphics g, int y,
			 float horizontalScale,
			 int heightOfGraph) {
	
	Iterator iterator = points.iterator();
	Integer axisPoint;
	int lineSize;
	int i = 0;

	// We dont draw the whole grid lines just stumps, the major
	lineSize = (int)(STATIC_GRID_HEIGHT + 
			 heightOfGraph / VARIABLE_GRID_HEIGHT_SCALE);

	// Major axis is indicated by taller stumps
	if(getType() == MAJOR)
	    lineSize *= MAJOR_MINOR_GRID_PROPORTION;

	while(iterator.hasNext()) {
	    axisPoint = (Integer)iterator.next();

	    // dont draw the first or last axis points as they are the
	    // start and end of the graph indicators
	    if(i++ > 0 && iterator.hasNext())
		g.drawLine((int)(axisPoint.intValue() * horizontalScale), y,
			   (int)(axisPoint.intValue() * horizontalScale), 
			   y - lineSize);
	}
    }
    
    private void add(TradingDate date, Integer point) {
	dates.add(date);
	points.add(point);
    }
    
    private int getPeriod() {
	return period;
    }

    private int getType() {
	return type;
    }

    private String stringValue(TradingDate date) {
	switch(period) {
	case(YEARS):
	    return Integer.toString(value(date));
	case(QUARTERS):
	    return "Q" + 
		Integer.toString(value(date));
	case(MONTHS):
	    return TradingDate.monthToText(value(date));
	default:
	    return "???";
	}
    }
    
    private int value(TradingDate date) {
	
	switch(period) {
	case(YEARS):
	    return date.getYear();
	case(QUARTERS):
	    return ((date.getMonth()-1) / MONTHS_PER_QUARTER + 1);
	case(MONTHS):
	    return date.getMonth();
	default:
	    return 0;
	}
    }
    
    public int getWidth() {
	// Dont use the width between the first two points as that
	// may be shorter than the average
	if(points.size() > 2)
	    return ((Integer)points.elementAt(2)).intValue() - 
		((Integer)points.elementAt(1)).intValue();
	
	// unless weve only two points
	else if (points.size() > 1)
	    return ((Integer)points.elementAt(1)).intValue() - 
		((Integer)points.elementAt(0)).intValue();
	
	// else no points
	return 0;
    }
}
