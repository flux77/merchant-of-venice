/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
*/

package org.mov.chart.graph;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

/**
 * Barchart graph. This graph draws a single vertical line between the 
 * day low and the day high values and draws horizontal bars at
 * the day open and the day close.
 */
public class BarChartGraph extends AbstractGraph {

    private Graphable dayOpen;
    private Graphable dayLow;
    private Graphable dayHigh;
    private Graphable dayClose;

    // Width in pixels of day close bar (will be scaled)
    private final static int DAY_CLOSE_BAR_WIDTH = 1;

    /**
     * Create a new barchart graph.
     *
     * @param	dayOpen	source containing the day open values
     * @param	dayLow	source containing the day low values
     * @param	dayHigh	source containing the day high values
     * @param	dayClose source containing the day close values
     */
    public BarChartGraph(GraphSource dayOpen, GraphSource dayLow, GraphSource dayHigh, GraphSource dayClose) {
	super(dayClose);

	this.dayOpen = dayOpen.getGraphable(); // a changer !!!
	this.dayLow = dayLow.getGraphable();
	this.dayHigh = dayHigh.getGraphable();
	this.dayClose = dayClose.getGraphable();       
    }

    // See Graph.java
    public void render(Graphics g, Color c, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {

	g.setColor(Color.RED);

	int xCoordinate, lowY, highY, closeY, openY;
	Double dayLowY, dayHighY, dayCloseY, dayOpenY;
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
	    dayOpenY = dayOpen.getY(x);
	    dayLowY = dayLow.getY(x);
	    dayHighY = dayHigh.getY(x);
	    dayCloseY = dayClose.getY(x);

	    // The graph is allowed to skip points
	    if(dayLowY != null && dayHighY != null &&
	       dayCloseY != null) {

		xCoordinate = (int)(xoffset + horizontalScale * i);
		
                openY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayOpenY.doubleValue(), 
						bottomLineValue, 
						verticalScale);
		lowY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayLowY.doubleValue(), 
						bottomLineValue, 
						verticalScale);
		highY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayHighY.doubleValue(), 
						bottomLineValue, 
						verticalScale);
		closeY = yoffset - 
		    GraphTools.scaleAndFitPoint(dayCloseY.doubleValue(), 
						bottomLineValue, 
						verticalScale);
		
		// Draw bar
		g.drawLine(xCoordinate, lowY, xCoordinate, highY);

		// Draw perpendicular line indicating day close
		g.drawLine(xCoordinate, closeY, 
			   (int)(xCoordinate + 0.2 * horizontalScale
				 /*DAY_CLOSE_BAR_WIDTH * horizontalScale*/ ),
			   closeY);
                
		// Draw perpendicular line indicating day open
		g.drawLine(xCoordinate, openY, 
			   (int)(xCoordinate - 0.2 * horizontalScale
				 /*DAY_CLOSE_BAR_WIDTH * horizontalScale*/),
			   openY);
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
				 double verticalScale,
				 double bottomLineValue)
    {
	Double dayLowY = dayLow.getY(x);
	Double dayHighY = dayHigh.getY(x);
	
	if(dayLowY != null && dayHighY != null) {

	    int dayLowYCoordinate = yoffset - 
		GraphTools.scaleAndFitPoint(dayLowY.doubleValue(),
					    bottomLineValue, verticalScale);

	    int dayHighYCoordinate = yoffset - 
		GraphTools.scaleAndFitPoint(dayHighY.doubleValue(),
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
    public double getHighestY(List x) {
	return dayHigh.getHighestY(x);
    }

    // Lowest value will always be in the day low source
    public double getLowestY(List x) {
        return dayLow.getLowestY(x);
    }
}