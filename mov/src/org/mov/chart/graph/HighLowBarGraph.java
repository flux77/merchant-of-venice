package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

/**
 * High low bar graph. This graph draws a single vertical line between the 
 * day low and the day high values and draws a single horizontal bars at
 * the day close.
 */
public class HighLowBarGraph extends AbstractGraph {

    private Graphable dayLow;
    private Graphable dayHigh;
    private Graphable dayClose;

    // Width in pixels of day close bar (will be scaled)
    private final static int DAY_CLOSE_BAR_WIDTH = 1;

    /**
     * Create a new high low bar graph.
     *
     * @param	dayLow	source containing the day low values
     * @param	dayHigh	source containing the day high values
     * @param	dayClose source containing the day close values
     */
    public HighLowBarGraph(GraphSource dayLow,
			   GraphSource dayHigh,
			   GraphSource dayClose) {
	super(dayClose);

	this.dayLow = dayLow.getGraphable();
	this.dayHigh = dayHigh.getGraphable();
	this.dayClose = dayClose.getGraphable();       
    }

    // See Graph.java
    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	g.setColor(colour);

	int xCoordinate, lowY, highY, closeY;
	Float dayLowY, dayHighY, dayCloseY;
	Iterator iterator = xRange.iterator();
	int i = 0;

	while(iterator.hasNext()) {

	    Comparable x = (Comparable)iterator.next();
	    
	    // Skip until our start date
	    if(x.compareTo(dayClose.getStartX()) < 0) {
		i++;
		continue;
	    }

	    // If our graph is finished exit this loop
	    if(x.compareTo(dayClose.getEndX()) > 0) 
		break;

	    // Otherwise draw bar
	    dayLowY = dayLow.getY(x);
	    dayHighY = dayHigh.getY(x);
	    dayCloseY = dayClose.getY(x);

	    // The graph is allowed to skip points
	    if(dayLowY != null && dayHighY != null &&
	       dayCloseY != null) {

		xCoordinate = (int)(xoffset + horizontalScale * i);

		lowY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayLowY.floatValue(), 
						bottomLineValue, 
						verticalScale);
		highY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayHighY.floatValue(), 
						bottomLineValue, 
						verticalScale);
		closeY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayCloseY.floatValue(), 
						bottomLineValue, 
						verticalScale);
		
		// Draw bar
		g.drawLine(xCoordinate, lowY, xCoordinate, highY);

		// Draw perpendicular line indicating day close
		g.drawLine(xCoordinate, closeY, 
			   (int)(xCoordinate + 
				 DAY_CLOSE_BAR_WIDTH * horizontalScale),
			   closeY);
	    }
	    i++;
	}
    }

    /**
     * Get the tool tip text for the given X value and y coordinate.
     *
     * @param	x	the X value
     * @param	y	the y coordinate
     * @param	yoffset	y offset from top of graph
     * @param	verticalScale	vertical scale factor
     * @param	bottomLineValue	the Y value of the lowest line in the graph
     * @return	tool tip text containing the day low, day high and day close
     *		quotes
     */
    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	Float dayLowY = dayLow.getY(x);
	Float dayHighY = dayHigh.getY(x);
	
	if(dayLowY != null && dayHighY != null) {

	    int dayLowYCoordinate = yoffset - 
		GraphTools.scaleAndFitPoint(dayLowY.floatValue(),
					    bottomLineValue, verticalScale);

	    int dayHighYCoordinate = yoffset - 
		GraphTools.scaleAndFitPoint(dayHighY.floatValue(),
					    bottomLineValue, verticalScale);
	    
	    // Its our graph if its within TOOL_TIP_BUFFER pixels of the 
	    // line from day low to day high
	    if(y >= (dayLowYCoordinate - Graph.TOOL_TIP_BUFFER) &&
	       y <= (dayHighYCoordinate + Graph.TOOL_TIP_BUFFER))
		return getSource().getToolTipText(x);
	}
	return null;
    }

    // Highest value will always be in the day high source
    public float getHighestY(Vector x) {
	return dayHigh.getHighestY(x);
    }

    // Lowest value will always be in the day low source
    public float getLowestY(Vector x) {
        return dayLow.getLowestY(x);
    }
}