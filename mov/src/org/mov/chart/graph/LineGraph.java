package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

/**
 * Simple line graph. This graph is used to draw any kind of line such
 * as day close, day open, day high, day low etc.
 */
public class LineGraph extends AbstractGraph {

    /**
     * Create a new simple line graph.
     *
     * @param	source	the source to render
     */
    public LineGraph(GraphSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	g.setColor(colour);
	GraphTools.renderLine(g, getSource().getGraphable(), xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int yCoordinate, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	Float y = getY(x);
	
	if(y != null) {
	    int yOfGraph = yoffset - 
		GraphTools.scaleAndFitPoint(y.floatValue(),
					    bottomLineValue, verticalScale);
	    // Its our graph *only* if its within 5 pixels	    
	    if(Math.abs(yCoordinate - yOfGraph) < Graph.TOOL_TIP_BUFFER) 
		return getSource().getToolTipText(x);
	}
	return null;
    }
}
