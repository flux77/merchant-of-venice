package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.util.*;

public class HighLowBarGraph extends Graph {

    private GraphDataSource dayLow;
    private GraphDataSource dayHigh;
    private GraphDataSource dayClose;

    // Width in pixels of day close bar (will be scaled)
    private final static int DAY_CLOSE_BAR_WIDTH = 1;

    public HighLowBarGraph(GraphDataSource dayLow,
			   GraphDataSource dayHigh,
			   GraphDataSource dayClose) {
	super(dayClose);

	this.dayLow = dayLow;
	this.dayHigh = dayHigh;
	this.dayClose = dayClose;       
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	g.setColor(colour);

	int x, lowY, highY, closeY;
	Float dayLowValue, dayHighValue, dayCloseValue;
	TradingDate date;
	Iterator iterator = dates.iterator();
	int i = 0;

	while(iterator.hasNext()) {

	    date = (TradingDate)iterator.next();
	    
	    // Skip until our start date
	    if(date.compareTo(getSource().getStartDate()) < 0) {
		i++;
		continue;
	    }

	    // If our graph is finished exit this loop
	    if(date.compareTo(getSource().getEndDate()) > 0) 
		break;

	    // Otherwise draw bar
	    dayLowValue = dayLow.getValue(date);
	    dayHighValue = dayHigh.getValue(date);
	    dayCloseValue = dayClose.getValue(date);

	    // The graph is allowed to skip points
	    if(dayLowValue != null && dayHighValue != null &&
	       dayCloseValue != null) {

		x = (int)(xoffset + horizontalScale * i);

		lowY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayLowValue.floatValue(), 
						bottomLineValue, 
						verticalScale);
		highY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayHighValue.floatValue(), 
						bottomLineValue, 
						verticalScale);
		closeY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayCloseValue.floatValue(), 
						bottomLineValue, 
						verticalScale);
		
		// Draw bar
		g.drawLine(x, lowY, x, highY);

		// Draw perpendicular line indicating day close
		g.drawLine(x, closeY, 
			   (int)(x + DAY_CLOSE_BAR_WIDTH * horizontalScale),
			   closeY);
	    }
	    i++;
	}
    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	Float dayLowValue = dayLow.getValue(date);
	Float dayHighValue = dayHigh.getValue(date);
	
	if(dayLowValue != null && dayHighValue != null) {

	    int dayLowY = yoffset - 
		GraphTools.scaleAndFitPoint(dayLowValue.floatValue(),
					    bottomLineValue, verticalScale);

	    int dayHighY = yoffset - 
		GraphTools.scaleAndFitPoint(dayHighValue.floatValue(),
					    bottomLineValue, verticalScale);
	    
	    // Its our graph if its within TOOL_TIP_BUFFER pixels of the 
	    // line from day low to day high
	    if(y >= (dayLowY - Graph.TOOL_TIP_BUFFER) &&
	       y <= (dayHighY + Graph.TOOL_TIP_BUFFER))
		return getToolTipText(date);
	}
	return null;
    }

    public HashMap getAnnotations() {
	return null;
    }

    // Override base class method
    public float getHighestValue(Vector dates) {
	float dayLowHighestValue = dayLow.getHighestValue(dates);
	float dayHighHighestValue = dayHigh.getHighestValue(dates);

	return 
	    dayLowHighestValue > dayHighHighestValue? 
	    dayLowHighestValue :
	    dayHighHighestValue;	
    }

    // Override base class method
    public float getLowestValue(Vector dates) {
	float dayLowLowestValue = dayLow.getLowestValue(dates);
	float dayHighLowestValue = dayHigh.getLowestValue(dates);

	return 
	    dayLowLowestValue < dayHighLowestValue? 
	    dayLowLowestValue :
	    dayHighLowestValue;
    }
}
