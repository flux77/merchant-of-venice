package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.*;

public class GraphTools {

    public static void renderLine(Graphics g, Graphable source, 
				  int xoffset, int yoffset,
				  float horizontalScale, float verticalScale,
				  float bottomLineValue, Vector dates) {
	
	int x, y;
	int lastX = -1 , lastY = -1;
	Float value;
	TradingDate date;
	Iterator iterator = dates.iterator();
	int i = 0;

	while(iterator.hasNext()) {

	    date = (TradingDate)iterator.next();

	    // Skip until our start date
	    if(date.compareTo(source.getStartDate()) < 0) {
		i++;
		continue;
	    }

	    // If our graph is finished exit this loop
	    if(date.compareTo(source.getEndDate()) > 0) 
		break;

	    // Otherwise draw point
	    value = source.getValue(date);

	    // The graph is allowed to skip points
	    if(value != null) {
		x = (int)(xoffset + horizontalScale * i);
		y = yoffset - scaleAndFitPoint(value.floatValue(), 
					       bottomLineValue, verticalScale);
		
		if(lastX != -1)
		    g.drawLine(x, y, lastX, lastY);
		else
		    g.drawLine(x, y, x, y);

		lastX = x;
		lastY = y ;
	    }
	    i++;
	}
    }

    public static void renderBar(Graphics g, Graphable source, 
				 int xoffset, int yoffset,
				 float horizontalScale, float verticalScale,
				 float bottomLineValue, Vector dates) {
	int x2, y1, y2;
	int x1 = -1;
	Float value;
	float floatValue;
	TradingDate date;
	Iterator iterator = dates.iterator();
	int i = 0;

	y2 = yoffset - scaleAndFitPoint(0, bottomLineValue, verticalScale);

	while(iterator.hasNext()) {

	    date = (TradingDate)iterator.next();

	    // Skip until our start date
	    if(date.compareTo(source.getStartDate()) < 0) {
		i++;
		continue;
	    }

	    // If our graph is finished exit this loop
	    if(date.compareTo(source.getEndDate()) > 0) 
		break;

	    // Otherwise draw point
	    value = source.getValue(date);

	    // The graph is allowed to skip points
	    if(value == null) 
		floatValue = 0;
	    else
		floatValue = value.floatValue();

	    x2 = (int)(xoffset + horizontalScale * i);
	    y1 = yoffset - scaleAndFitPoint(floatValue, 
					    bottomLineValue, verticalScale);
	    
	    if(x1 != -1) 
		g.fillRect(x1, y1, Math.abs(x2-x1) + 1, Math.abs(y2-y1));
	    
	    x1 = x2 + 1;

	    i++;
	}
    }

    // Given the float y value of a point, the verticale offset and the
    // vertical scale, return the y coordinate where the point should be.
    public static int scaleAndFitPoint(float point, 
				       float offset, float scale) {
	return (int)((point - offset) * scale);
    }

    // Returns a hash map of annotations with buy/sell recommendations
    // based on when graph 1 cuts graph 2:
    //
    // If the graph1 crosses from below to above graph2 its a signal to buy.
    // If the graph1 crosses from above to below graph2 its a signal to sell.
    //
    // This is true of Moving Average & MACD
    //
    // Assumption both graph1 & graph2 have the same date range

    public static HashMap createAnnotations(Graphable graph1,
					    Graphable graph2) {
	HashMap annotations = new HashMap();

	TradingDate today = 
	    (TradingDate)graph1.getStartDate().clone();

	Float todayGraph1Value, todayGraph2Value;
	Float yesterdayGraph1Value, yesterdayGraph2Value;

	do {
	    yesterdayGraph1Value = graph1.getValue(today);
	    yesterdayGraph2Value = graph2.getValue(today);
	    today.next(1);
	} while((yesterdayGraph1Value == null || 
		 yesterdayGraph2Value == null) &&
		today.compareTo(graph1.getEndDate()) <= 0);

	while(today.compareTo(graph1.getEndDate()) <= 0) {

	    todayGraph1Value = graph1.getValue(today);
	    todayGraph2Value = graph2.getValue(today);

	    if(todayGraph1Value != null && todayGraph2Value != null) {

		// Buy
		if(todayGraph1Value.compareTo(todayGraph2Value) > 0 &&
		   yesterdayGraph1Value.compareTo(yesterdayGraph2Value) <= 0)
		    annotations.put((TradingDate)today.clone(), "Buy");
		
		// Sell
		else if(todayGraph1Value.compareTo(todayGraph2Value) < 0 &&
		   yesterdayGraph1Value.compareTo(yesterdayGraph2Value) >= 0)
		    annotations.put((TradingDate)today.clone(), "Sell");

		yesterdayGraph1Value = todayGraph1Value;
		yesterdayGraph2Value = todayGraph2Value;

	    }
	    today.next(1); 
	}

	return annotations;

    }
}

