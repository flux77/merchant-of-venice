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
 * Momentum graph. This graph is used to show the direction that the
 * stock price is moving. The momentum for a given day is calculated
 * as the day close of today minus the day close of <i>period</i>
 * days ago.
 */
public class MomentumGraph extends AbstractGraph {

    private Graphable momentum;
   
    /**
     * Create a new momentum graph.
     *
     * @param	source	the source to create a momentum graph from
     * @param	period	the period of the momentum
     */
    public MomentumGraph(GraphSource source, int period) {
	
	super(source);

	// create momentum
	momentum = createMomentum(source.getGraphable(),
				  period);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	g.setColor(colour);
	GraphTools.renderBar(g, momentum, xoffset, yoffset, 
			     horizontalScale,
			     verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Centre momentum graph
    public float getHighestY(Vector x) {
	return Math.max(Math.abs(momentum.getHighestY(x)),
			Math.abs(momentum.getLowestY(x)));
    }

    // Centre momentum graph
    public float getLowestY(Vector x) {
	return -getHighestY(x);
    }

    // Override vertical axis
    public float[] getAcceptableMajorDeltas() {
	float[] major = {0.01F,
			 0.1F,
			 1F,
			 10F,
			 100F,
			 1000F};
	return major;
    }

    // Override vertical axis
    public float[] getAcceptableMinorDeltas() {
	float[] minor = {1F, 
			 2F,
			 3F,
			 4F,
			 5F,
			 6F,
			 7F,
			 8F,
			 9F};
	return minor;
    }

    // Override vertical axis
    public String getYLabel(float value) {
	return Float.toString(value);
    }

    /**
     * Create a momentum graphable.
     *
     * @param	source	the input graph source 
     * @param	period	the desired period of the momentum graph
     * @return	the graphable containing averaged data from the source
     */
    public static Graphable createMomentum(Graphable source, 
					   int period) {
	Graphable momentum = new Graphable();

	// Date set and value array will be in sync
	float[] values = source.toArray();
	Iterator iterator = source.getXRange().iterator();

	int i = 0;	

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();
	    float todaysMomentum = 
		values[i] - values[Math.max(1 + i - period, 0)];

	    momentum.putY(x, new Float(todaysMomentum));

	    i++;
	}

	return momentum;
    }
}


