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
import java.util.List;
import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.Locale;

/**
 * Moving Average Convergence Divergence graph. This graph draws two
 * simple moving averages, the fast one in green, the slow one in red.
 * When the two lines cross they indicate a <b>Buy</b> or <b>Sell</b>
 * recommendation.
 */
public class MACDGraph extends AbstractGraph {

    private Graphable fastMovingAverage;
    private Graphable slowMovingAverage;

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
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List dates) {

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
				 double verticalScale,
				 double bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Highest Y value is the highest of both the moving averages
    public double getHighestY(List x) {
	double fastHighestY = fastMovingAverage.getHighestY(x);
	double slowHighestY = slowMovingAverage.getHighestY(x);

	return
	    fastHighestY > slowHighestY?
	    fastHighestY :
	    slowHighestY;
    }

    // Lowest Y value is the lowest of both the moving averages
    public double getLowestY(List x) {
	double fastLowestY = fastMovingAverage.getLowestY(x);
	double slowLowestY = slowMovingAverage.getLowestY(x);

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
	return Locale.getString("MACD");
    }

    public boolean isPrimary() {
        return true;
    }
}



