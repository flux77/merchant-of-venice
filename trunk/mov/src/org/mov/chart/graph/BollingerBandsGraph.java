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
 * Bollinger Bands graph. This graph is used to show the volatility
 * of a stock. It draws two bands on the graph, they are centred around
 * the moving average of the graph. The top band is the moving average
 * plus 2 standard deviations, the lower band is the moving average
 * minus 2 standard deviations.
 */

public class BollingerBandsGraph extends AbstractGraph {

    private Graphable upperBand;
    private Graphable lowerBand;
   
    /**
     * Create a new bollinger bands graph.
     *
     * @param	source	the source to create a standard deviation from
     * @param	period	the period of the standard deviation
     */
    public BollingerBandsGraph(GraphSource source, int period) {
	
	super(source);

	// create bollinger bands
	upperBand = new Graphable();
	lowerBand = new Graphable();	

	// Date set and value array will be in sync
	float[] values = source.getGraphable().toArray();
	Iterator iterator = source.getGraphable().getXRange().iterator();

	int i = 0;	
	float average;
	float sd;

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();

	    sd = QuoteFunctions.sd(values,  
				   i - Math.min(period - 1, i),
				   i + 1);
	    average = QuoteFunctions.avg2(values,  
					  i - Math.min(period - 1, i),
					  i + 1);

	    upperBand.putY(x, new Float(average + 2 * sd));
	    lowerBand.putY(x, new Float(average - 2 * sd));

	    i++;
	}
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	// We ignore the graph colours and use our own custom colours
	g.setColor(Color.green.darker());

	GraphTools.renderLine(g, upperBand, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
	GraphTools.renderLine(g, lowerBand, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Highest Y value is in the bollinger bands graph
    public float getHighestY(Vector x) {
	return upperBand.getHighestY(x);
    }

    // Lowest Y value is in the bollinger bands graph
    public float getLowestY(Vector x) {
	return lowerBand.getLowestY(x);
    }
}


