/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
*/

package org.mov.chart.graph;

import java.awt.*;
import java.lang.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.Locale;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * Exponentially Smoothed Moving Average graph. This graph draws a single moving
 * average. When the line crosses the original graph it indicates
 * a <b>Buy</b> or <b>Sell</b> recommendation.
 */
public class ExpMovingAverageGraph extends AbstractGraph {

    private Graphable movingAverage;
    private GraphSource source;
    private HashMap annotations;

    public static final double smoothingConstant = 0.1; //Parameterise this later

    /**
     * Create a new simple moving average graph.
     *
     * @param	source	the source to create a moving average from
     * @param	period	the period of the moving average
     */
    public ExpMovingAverageGraph(GraphSource source, int period) {

	super(source);

	// Create moving average graphable
	movingAverage = createMovingAverage(source.getGraphable(), period);

	// Create buy sell recommendations
	annotations = GraphTools.createAnnotations(getSource().getGraphable(),
						   movingAverage);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {

	// We ignore the graph colours and use our own custom colours
	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, movingAverage, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 double verticalScale,
				 double bottomLineValue)
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
    public double getHighestY(List x) {
	return movingAverage.getHighestY(x);
    }

    // Lowest Y value is in the moving average graph
    public double getLowestY(List x) {
	return movingAverage.getLowestY(x);
    }

    /**
     * Return the name of this graph.
     *
     * @return	<code>Moving Average</code>
     */
    public String getName() {
	return Locale.getString("MOVING_AVERAGE");
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
	double[] values = source.toArray();
	Set xRange = source.getXRange();
	Iterator iterator = xRange.iterator();

	int i = 0;	
	double average;

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();

	    average = QuoteFunctions.ema(values,  
                                         i - Math.min(period - 1, i),
                                         i + 1,
					 smoothingConstant);
	    i++;

	    movingAverage.putY(x, new Double(average));
	}

	return movingAverage;
    }
}


