package org.mov.chart.graph;

// Implements Moving Average Convergence Divergence Graph

import java.awt.*;
import java.lang.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;

public class MACDGraph extends AbstractGraph {

    private Graphable fastMovingAverage;
    private Graphable slowMovingAverage;
    private HashMap annotations;

    public MACDGraph(GraphSource source, int periodOne,
		     int periodTwo) {

	super(source);

	// Create averaged data sources. 
	int slowPeriod = Math.max(periodOne, periodTwo);
	int fastPeriod = Math.min(periodOne, periodTwo);

	slowMovingAverage = 
	    MovingAverageGraph.createMovingAverage(source.getGraphable(), 
						   slowPeriod);
	fastMovingAverage = 
	    MovingAverageGraph.createMovingAverage(source.getGraphable(), 
						   fastPeriod);
	
	// Create buy sell recommendations
	annotations = GraphTools.createAnnotations(fastMovingAverage, 
						   slowMovingAverage);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	// We ignore the graph colours and use our own custom colours

	// Fast moving line
	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, fastMovingAverage, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, dates);

	// Slow moving line
	g.setColor(Color.red.darker());
	GraphTools.renderLine(g, slowMovingAverage, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, dates);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Override base class method
    public HashMap getAnnotations() {
	return annotations;
    }

    // Override base class method
    public boolean hasAnnotations() {
	return true;
    }

    // Override base class method
    public float getHighestY(Vector x) {
	float fastHighestY = fastMovingAverage.getHighestY(x);
	float slowHighestY = slowMovingAverage.getHighestY(x);

	return 
	    fastHighestY > slowHighestY? 
	    fastHighestY :
	    slowHighestY;
    }

    // Override base class method
    public float getLowestY(Vector x) {
	float fastLowestY = fastMovingAverage.getLowestY(x);
	float slowLowestY = slowMovingAverage.getLowestY(x);

	return 
	    fastLowestY < slowLowestY? 
	    fastLowestY :
	    slowLowestY;
    }

    // Override base class method
    public String getName() {
	return "MACD";
    }
}



