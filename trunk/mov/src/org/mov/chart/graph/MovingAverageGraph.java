package org.mov.chart.graph;

import java.awt.*;
import java.lang.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * Simple Moving Average graph. This graph draws a single moving
 * average. When the line crosses the original graph it indicates
 * a <b>Buy</b> or <b>Sell</b> recommendation.
 */
public class MovingAverageGraph extends AbstractGraph {

    private Graphable movingAverage;
    private GraphSource source;
    private HashMap annotations;

    /**
     * Create a new simple moving average graph.
     *
     * @param	source	the source to create a moving average from
     * @param	period	the period of the moving average
     */
    public MovingAverageGraph(GraphSource source, int period) {

	super(source);

	// Create moving average graphable
	movingAverage = createMovingAverage(source.getGraphable(), period);

	// Create buy sell recommendations
	annotations = GraphTools.createAnnotations(getSource().getGraphable(),
						   movingAverage);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	// We ignore the graph colours and use our own custom colours
	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, movingAverage, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    /** 
     * Return annotations containing buy/sell recommendations based on
     * when the moving average crosses its source.
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

    // Highest Y value is in the moving average graph
    public float getHighestY(Vector x) {
	return movingAverage.getHighestY(x);
    }

    // Lowest Y value is in the moving average graph
    public float getLowestY(Vector x) {
	return movingAverage.getLowestY(x);
    }

    /**
     * Return the name of this graph.
     *
     * @return	<code>Moving Average</code>
     */
    public String getName() {
	return "Moving Average";
    }

    /**
     * Creates a new moving average based on the given data source.
     *
     * @param	source	the graph source to average
     * @param	period	the desired period of the averaged data
     * @return	the graphable containing averaged data from the source
     */
    public static Graphable createMovingAverage(Graphable source, int period) {
	Graphable movingAverage = new Graphable();

	// Date set and value array will be in sync
	float[] values = source.toArray();
	Set xRange = source.getXRange();
	Iterator iterator = xRange.iterator();

	int i = 0;	
	float average;

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();

	    average = QuoteFunctions.avg2(values,  
					  i - Math.min(period - 1, i),
					  i + 1);
	    i++;

	    movingAverage.putY(x, new Float(average));
	}

	return movingAverage;
    }
}


