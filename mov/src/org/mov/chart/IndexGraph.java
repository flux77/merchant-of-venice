package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.util.*;

public class IndexGraph extends Graph {

    public IndexGraph(GraphDataSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {
	
	int x, y;
	int lastX = -1 , lastY = -1;
	Float value;
	TradingDate date;
	Iterator iterator = dates.iterator();
	int i = 0;

	Color darker = colour.darker();
	Color brighter = colour.brighter();

	int y2 = yoffset - 
	    GraphTools.scaleAndFitPoint(bottomLineValue, 
					bottomLineValue, verticalScale);

	while(iterator.hasNext()) {

	    date = (TradingDate)iterator.next();

	    // Skip until our start date
	    if(date.compareTo(getStartDate()) < 0) {
		i++;
		continue;
	    }	    

	    // If our graph is finished exit this loop
	    if(date.compareTo(getEndDate()) > 0) 
		break;

	    // Otherwise draw point
	    value = getValue(date);

	    // The graph is allowed to skip points
	    if(value != null) {
		x = (int)(xoffset + horizontalScale * i);
		y = yoffset - 
		    GraphTools.scaleAndFitPoint(value.floatValue(), 
						bottomLineValue, 
						verticalScale);
	
		if(lastX != -1) {

		    // draw filled in graph 
		    g.setColor(colour);
		    Polygon polygon = new Polygon();

		    polygon.addPoint(lastX, lastY);
		    polygon.addPoint(x+1, y);		
		    polygon.addPoint(x+1, y2);
		    polygon.addPoint(lastX, y2);
		
		    g.fillPolygon(polygon);

		    // overlay light/dark line on the top of the filled
		    // colour
		    if(y <= lastY) // graph is increasing or levelling
			g.setColor(brighter);
		    else
			g.setColor(darker);

		    // 2 lines thick
		    g.drawLine(lastX, lastY, x, y);

		    if(y+1 < y2 && lastY+1 < y2)
			g.drawLine(lastX, lastY+1, x, y+1);
		}

		lastX = x;
		lastY = y ;
	    }
	    i++;
	}
    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	Float value = getValue(date);
	
	if(value != null) {
	    int yOfGraph = yoffset - 
		GraphTools.scaleAndFitPoint(value.floatValue(),
					    bottomLineValue, verticalScale);
	    // Its our graph *only* if its within 5 pixels	    
	    if(Math.abs(y - yOfGraph) < Graph.TOOL_TIP_BUFFER) 
		return getToolTipText(date);
	}
	return null;
    }

    public HashMap getAnnotations() {
	return null;
    }
}
