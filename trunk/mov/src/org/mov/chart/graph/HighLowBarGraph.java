package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

public class HighLowBarGraph extends AbstractGraph {

    private Graphable dayLow;
    private Graphable dayHigh;
    private Graphable dayClose;

    // Width in pixels of day close bar (will be scaled)
    private final static int DAY_CLOSE_BAR_WIDTH = 1;

    public HighLowBarGraph(GraphSource dayLow,
			   GraphSource dayHigh,
			   GraphSource dayClose) {
	super(dayClose);

	this.dayLow = dayLow.getGraphable();
	this.dayHigh = dayHigh.getGraphable();
	this.dayClose = dayClose.getGraphable();       
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
	    if(date.compareTo(dayClose.getStartDate()) < 0) {
		i++;
		continue;
	    }

	    // If our graph is finished exit this loop
	    if(date.compareTo(dayClose.getEndDate()) > 0) 
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
		return getSource().getToolTipText(date);
	}
	return null;
    }

    // Override base class method
    public float getHighestValue(Vector dates) {
	return dayHigh.getHighestValue(dates);
    }

    // Override base class method
    public float getLowestValue(Vector dates) {
        return dayLow.getLowestValue(dates);
    }
}
