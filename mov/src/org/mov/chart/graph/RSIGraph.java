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

import org.mov.chart.Graphable;
import org.mov.chart.GraphTools;
import org.mov.chart.source.GraphSource;
import org.mov.quote.QuoteFunctions;
import org.mov.util.Locale;

/**
 * Grpah of the RSI (Relative Strength Indicator). See {@link QuoteFunctions#rsi}
 * for more information about this indicator.
 *
 * @author Andrew Leppard
 * @see RSIGraphUI
 */
public class RSIGraph extends AbstractGraph {

    // RSI values ready to graph
    private Graphable RSI;

    /**
     * Create a new RSI graph.
     *
     * @param	source	the source to create a standard deviation from
     */
    public RSIGraph(GraphSource source) {
        super(source);
        setSettings(new HashMap());
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {

        int overSold = RSIGraphUI.getOverSold(getSettings());
        int overBought = RSIGraphUI.getOverBought(getSettings());

        g.setColor(Color.BLACK);
        GraphTools.renderHorizontalLine(g, overSold, xoffset, yoffset, horizontalScale,
                                        verticalScale, bottomLineValue, xRange);
        GraphTools.renderHorizontalLine(g, overBought, xoffset, yoffset, horizontalScale,
                                        verticalScale, bottomLineValue, xRange);


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

    /**
     * Return the name of this graph.
     *
     * @return <code>RSI</code>
     */
    public String getName() {
        return Locale.getString("RSI");
    }

    public boolean isPrimary() {
        return false;
    }

    public void setSettings(HashMap settings) {
        super.setSettings(settings);

        // Retrieve values from hashmap
        int period = RSIGraphUI.getPeriod(settings);

	// create RSI
	RSI = createRSI(getSource().getGraphable(), period);
    }

    /**
     * Return the graph's user interface.
     *
     * @param settings the initial settings
     * @return user interface
     */
    public GraphUI getUI(HashMap settings) {
        return new RSIGraphUI(settings);
    }
}

