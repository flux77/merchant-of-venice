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
import java.util.Vector;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.Locale;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * Point and Figure graph. This graph draws a series of characters (X/O) 
 * mapping the general movement. A change in column shows a reversal 
 * such that price difference met the price scale.
 * 
 */
public class PointAndFigureGraph extends AbstractGraph {

    private PFGraphable pointAndFigure;
    private GraphSource source;
    private HashMap annotations;

    private double priceScale;
    
    /**
     * Create a new point and figure graph.
     *
     * @param	source	the source to create the point and figure from
     * @param	period	the period of the graph
      */
    public PointAndFigureGraph(GraphSource source, int period, double priceScale) {

	super(source);

	this.priceScale = priceScale;
	
	// Create point and figure graphable
	pointAndFigure = createPointAndFigureGraph(source.getGraphable(), period,priceScale);

	// Create buy sell recommendations
	//annotations = GraphTools.createAnnotations(getSource().getGraphable(),
	//pointAndFigure);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {

	// We ignore the graph colours and use our own custom colours
	g.setColor(Color.green.darker());
	GraphTools.renderChar(g, pointAndFigure, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 double verticalScale,
				 double bottomLineValue)
    {

	/*
	Double yCoord = getY(x);

	int yOfGraph = yoffset -  
	    GraphTools.scaleAndFitPoint(yCoord.doubleValue(),
					bottomLineValue, verticalScale);

	*/

	// Its our graph *only* if its within 5 pixels	    
	/*
	if(Math.abs(y - yOfGraph) < Graph.TOOL_TIP_BUFFER) {
	    return getSource().getToolTipText(x);
	}	
	*/
	
	return null;
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
	return pointAndFigure.getHighestY(x);
    }

    // Lowest Y value is in the moving average graph
    public double getLowestY(List x) {
	return pointAndFigure.getLowestY(x);
    }

    /**
     * Return the name of this graph.
     *
     * @return	<code>Point and Figure</code>
     */
    public String getName() {
	return Locale.getString("POINTANDFIGURE");
    }

    /**
     * Creates a new moving average based on the given data source.
     *
     * @param	source	the graph source to average
     * @param	period	the desired period of the averaged data
     * @return	the graphable containing averaged data from the source
     */

    /* Rules of PandF: 
       1. X = Upmoves, O = DownMoves
       2. Stay in same col until price changes direction, and then move one col to the right.
       3. Plot only prices which meet the price scale. A plot is marked regardless of the direction. If a direction change occurs, the affect is to move the column one to the right.
    */

    public static PFGraphable createPointAndFigureGraph(Graphable source, int period, double priceScale) {
	PFGraphable pointAndFigure = new PFGraphable();

	// Date set and value array will be in sync
	double[] values = source.toArray();
	Vector yList = new Vector();
		    
	Set xRange = source.getXRange();
	Set column = source.getXRange(); // Associate Column with date 
	Iterator iterator = xRange.iterator();
	Iterator iterator2 = column.iterator();
	double diff, prev = 0.0;
	boolean plot; // Indicate whether this point gets plotted
	boolean upmove = true, changeDirection = false;	
	String marker;
	Comparable col;	
	
	int i = 0;	
	double average;

	col = (Comparable)iterator2.next();
	
	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();

	    diff = (i == 0)  ? values[i] : (values[i] - prev);
	    plot = (Math.abs(diff) >= priceScale) ? true : false;

	    // Now check the direction
	    if (plot) {
		if (upmove == true && diff < 0) {
		    changeDirection = true;
		} else if (upmove == false && diff > 0) {
		    changeDirection = true;
		} else {
		    changeDirection = false;		    
		}
		
		// Stay in the same column until theres a change in direction 
		if (changeDirection) {

		    // Associate a new set of points.
		    yList = new Vector();

		    upmove = (upmove) ? false: true;
		    // This places the marker on when the price changed
		    // direction
		    col = x;	
		    // This places it in the next column
		    //col = (Comparable)iterator2.next();
		    
		}
		
		marker = (upmove) ? "X" : "O";
		
		Double yTemp = new Double(values[i]);
		yList.add(yTemp);
		
		pointAndFigure.putYList(col, yList);
		pointAndFigure.putString(col, marker);
		
	    }
	    prev = values[i];	    
	    i++;
	}
	
	return pointAndFigure;
    }
}


