package org.mov.chart.graph;

import java.awt.*;
import java.lang.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;

/**
 * Moving Average Convergence Divergence graph. This graph draws two
 * simple moving averages, the fast one in green, the slow one in red.
 * When the two lines cross they indicate a <b>Buy</b> or <b>Sell</b>
 * recommendation.
 */
public class MACDGraph extends AbstractGraph {

    private Graphable fastMovingAverage;
    private Graphable slowMovingAverage;
    private HashMap annotations;

    /**
     * Create a new MACD graph.
     *
     * @param	source	the source to create two moving averages from
     * @param	periodOne	period of one of the moving averages
     * @param	periodTwo	period of the other moving average
     */
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

    /** 
     * Return annotations containing buy/sell recommendations based on
     * when the moving averages cross each other.
     *
     * @return	annotations
     */
    public HashMap getAnnotations() {
	return annotations;
    }

    /**
     * Return that we support annotations.
     *
     * @return	<code>true</code>
     */
    public boolean hasAnnotations() {
	return true;
    }

    // Highest Y value is the highest of both the moving averages
    public float getHighestY(Vector x) {
	float fastHighestY = fastMovingAverage.getHighestY(x);
	float slowHighestY = slowMovingAverage.getHighestY(x);

	return 
	    fastHighestY > slowHighestY? 
	    fastHighestY :
	    slowHighestY;
    }

    // Lowest Y value is the lowest of both the moving averages
    public float getLowestY(Vector x) {
	float fastLowestY = fastMovingAverage.getLowestY(x);
	float slowLowestY = slowMovingAverage.getLowestY(x);

	return 
	    fastLowestY < slowLowestY? 
	    fastLowestY :
	    slowLowestY;
    }

    /**
     * Return the name of this graph.
     *
     * @return	<code>MACD</code>
     */
    public String getName() {
	return "MACD";
    }
}



