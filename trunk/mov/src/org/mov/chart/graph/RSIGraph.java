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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * RSI graph.
 */
public class RSIGraph extends AbstractGraph {

    private Graphable RSI;

    /**
     * Create a new standard deviation graph.
     *
     * @param	source	the source to create a standard deviation from
     * @param	period	the period of the standard deviation
     */
    public RSIGraph(GraphSource source, int period) {
	
	super(source);

	// create RSI
	RSI = createRSI(source.getGraphable(), period);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {

	g.setColor(colour);
	GraphTools.renderLine(g, RSI, xoffset, yoffset,
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 double verticalScale,
				 double bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    public double getHighestY(List x) {
	return 100.0D;
    }

    public double getLowestY(List x) {
	return 0.0D;
    }

    // Override vertical axis
    public double[] getAcceptableMajorDeltas() {
	double[] major = {0.1D,
			 0.5D,
			 1D,
			 10D,
			 100D};
	return major;
    }

    // Override vertical axis
    public double[] getAcceptableMinorDeltas() {
	double[] minor = {1D,
			 2D,
			 3D,
			 4D,
			 5D,
			 6D,
			 7D,
			 8D,
			 9D};
	return minor;
    }

    // Override vertical axis
    public String getYLabel(double value) {
	return Double.toString(value);
    }

    /**
     * Creates a new RSI based on the given data source.
     *
     * @param	source	the input graph source
     * @param	period	the desired period of the RSI
     * @return	the RSI graphable
     */
    public static Graphable createRSI(Graphable source, int period) {

	Graphable RSI = new Graphable();

	// Date set and value array will be in sync
	double[] values = source.toArray();
	Iterator iterator = source.getXRange().iterator();

	int i = 0;	

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();
	    double rsi = QuoteFunctions.rsi(values,
                                            i - Math.min(period - 1, i),
                                            i + 1);
	    i++;

	    RSI.putY(x, new Double(rsi));
	}

	return RSI;
    }
}


