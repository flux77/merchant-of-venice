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

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mov.chart.Graphable;
import org.mov.chart.GraphTools;
import org.mov.chart.source.GraphSource;
import org.mov.util.Locale;
import org.mov.quote.QuoteFunctions;

/**
 * Exponentially Smoothed Moving Average graph. This graph draws a single moving
 * average. When the line crosses the original graph it indicates
 * a <b>Buy</b> or <b>Sell</b> recommendation.
 *
 * @author Mark Hummel
 */
public class ExpMovingAverageGraph extends AbstractGraph {

    private Graphable movingAverage;
    private double smoothingConstant;

    /**
     * Create a new simple moving average graph.
     *
     * @param	source	the source to create a moving average from
     * @param	period	the period of the moving average
     */
    public ExpMovingAverageGraph(GraphSource source, int period, double smoothingConstant) {

	super(source);	

	this.smoothingConstant = smoothingConstant;

	// Create moving average graphable
	movingAverage = createMovingAverage(source.getGraphable(), period, smoothingConstant);
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
     * @return	<code>Exponentially Weighted Moving Average</code>
     */
    public String getName() {
	return Locale.getString("EXP_MOVING_AVERAGE");
    }

    /**
     * Creates a new moving average based on the given data source.
     *
     * @param	source	the graph source to average
     * @param	period	the desired period of the averaged data
     * @return	the graphable containing averaged data from the source
     */
    public static Graphable createMovingAverage(Graphable source, int period,
                                                double smoothingConstant) {
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

    public boolean isPrimary() {
        return true;
    }
}


