package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

public class BarGraph extends AbstractGraph {

    public BarGraph(GraphSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	g.setColor(colour);
	GraphTools.renderBar(g, getSource().getGraphable(), 
			     xoffset, yoffset, horizontalScale,
			     verticalScale, bottomLineValue, dates);

    }
}
