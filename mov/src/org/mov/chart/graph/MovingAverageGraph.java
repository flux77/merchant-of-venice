package org.mov.chart.graph;

// Implements Simple Moving Average

import java.awt.*;
import java.lang.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class MovingAverageGraph extends AbstractGraph {

    private Graphable movingAverage;
    private GraphSource source;
    private HashMap annotations;

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
	return movingAverage.getHighestY(x);
    }

    // Override base class method
    public float getLowestY(Vector x) {
	return movingAverage.getLowestY(x);
    }

    // Override base class method
    public String getName() {
	return "Moving Average";
    }

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
					  i - Math.min(period, i),
					  i + 1);
	    i++;

	    movingAverage.putY(x, new Float(average));
	}

	return movingAverage;
    }
}


