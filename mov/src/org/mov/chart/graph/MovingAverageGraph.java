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
		       float bottomLineValue, Vector dates) {

	// We ignore the graph colours and use our own custom colours
	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, movingAverage, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, dates);
    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
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
    public float getHighestValue(Vector dates) {
	return movingAverage.getHighestValue(dates);
    }

    // Override base class method
    public float getLowestValue(Vector dates) {
	return movingAverage.getLowestValue(dates);
    }

    // Override base class method
    public String getName() {
	return "Moving Average";
    }

    public static Graphable createMovingAverage(Graphable source, int period) {
	Graphable movingAverage = new Graphable();

	// Date set and value array will be in sync
	float[] values = source.toArray();
	Set dates = source.getDates();
	Iterator iterator = dates.iterator();

	int i = 0;	
	float average;

	while(iterator.hasNext()) {
	    TradingDate date = (TradingDate)iterator.next();

	    average = QuoteFunctions.avg2(values,  
					  i - Math.min(period, i),
					  i + 1);
	    i++;

	    movingAverage.putValue(date, new Float(average));
	}

	return movingAverage;
    }
}


