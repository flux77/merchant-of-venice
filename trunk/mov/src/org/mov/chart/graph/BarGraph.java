package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

/**
 * Horizontal bar graph. This graph is most commonly used to draw
 * the volume graph.
 */
public class BarGraph extends AbstractGraph {

    /**
     * Create a new horizontal bar graph.
     *
     * @param	source	the source to render
     */
    public BarGraph(GraphSource source) {
	super(source);
    }

    // See Graph.java
    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	g.setColor(colour);
	GraphTools.renderBar(g, getSource().getGraphable(), 
			     xoffset, yoffset, horizontalScale,
			     verticalScale, bottomLineValue, xRange);

    }
}
