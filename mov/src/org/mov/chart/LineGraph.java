package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.util.*;

public class LineGraph extends Graph {

    public LineGraph(GraphDataSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	g.setColor(colour);
	GraphTools.renderLine(g, getSource(), xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, dates);
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
