package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

public class LineGraph extends AbstractGraph {

    public LineGraph(GraphSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	g.setColor(colour);
	GraphTools.renderLine(g, getSource().getGraphable(), xoffset, yoffset, 
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
		return getSource().getToolTipText(date);
	}
	return null;
    }
}
