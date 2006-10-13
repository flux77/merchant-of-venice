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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;

/**
 * Support and Resistence "graph". This graph draws single lines across common price occurrances. 
 *
 * The support and resistence levels are determined by the most numerous troughs and peaks within the last year of data that have the greatest vertical separation and do not overlap.
 *
 *
 * NOTE: Experimental and work in progress.
 * @author Mark Hummel
 */



public class SupportAndResistenceGraph extends AbstractGraph {

    private Graphable support;
    private Graphable resistence;

    private double average;
    private double variance;

    /*
  Fudge factors: These values seemed to work well; I didn't do any sort of exhaustive experiment or optimisation to determine them.

  Length of trend: directionCount > 4
  Overlap tolerance: 20
  
    */

    private static final double delta = 0.02;
    private static final int overlapTolerance = 20;
    

    /**
     * Create a new support and Resistence graph.
     *
     * @param	source containing the data source, typically day close.
     */
    public SupportAndResistenceGraph(GraphSource source) {
	super(source);

	support = new Graphable();
	resistence = new Graphable(); 

	createSupportAndResistence(source.getGraphable(), support, resistence);

    }

    // See Graph.java
    public void render(Graphics g, Color c, int xoffset, int yoffset,
		       double horizontalScale, double verticalScale,
		       double bottomLineValue, List xRange) {


	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, support, xoffset, yoffset, 
		     horizontalScale,
		     verticalScale, bottomLineValue, xRange);

	g.setColor(Color.red.darker());
	GraphTools.renderLine(g, resistence, xoffset, yoffset, 
		     horizontalScale,
		     verticalScale, bottomLineValue, xRange);

    }

    /**
     * Get the tool tip text for the given X value and y coordinate.
     *
     * @param	x	the X value
     * @param	y	the y coordinate
     * @param	yoffset	y offset from top of graph
     * @param	verticalScale	vertical scale factor
     * @param	bottomLineValue	the Y value of the lowest line in the graph
     * @return	tool tip text containing the day low, day high and day close
     *		quotes
     */
    public String getToolTipText(Comparable x, int y, int yoffset,
				 double verticalScale,
				 double bottomLineValue)
    {
	return null;
    }

    public double getHighestY(List x) {
	return resistence.getHighestY(x);
    }

    public double getLowestY(List x) {
        return support.getLowestY(x);
    }

    /**
     * Return the name of this graph.
     *
     * @return <code>Bar Chart</code>
     */
    public String getName() {
        return Locale.getString("SUPPORT_AND_RESISTENCE");
    }

    public boolean isPrimary() {
        return true;
    }

    /**
     * Creates a new support and resistence based on the given data source.
     *
     * @param	source	the graph source to average     
     * @param   support the data points of a support line
     * @param   resistence the data points of a resistence line

     Currently only the points of the most numerous peaks and troughs are used       as a heuristic to determine the points of support and resistence. It's a 
     heuristic only, and still needs work.  
     */
    public Graphable[] createSupportAndResistence(Graphable source, Graphable support, Graphable resistence) {
	Graphable supportAndResistence[] = new Graphable[2];
	double[] values = source.toArray();
	double[] peaks, troughs;
	double peaksPriceData[] = new double[2];
	double troughsPriceData[] = new double[2];
	HashMap startPeaksPosn, endPeaksPosn;
	HashMap startTroughsPosn, endTroughsPosn;	
	ArrayList peaksCount = new ArrayList(values.length);
	ArrayList troughsCount = new ArrayList(values.length);
	int peaksIndex = 0;
	int troughsIndex = 0;

	int score = 0;
	boolean upmove = false;
	int directionCount = 0;

	double prev, diff;		
	int start1, end1, start2, end2;
	Set xRange = source.getXRange();
	Iterator iterator = xRange.iterator();
	int i;

	peaks = new double[values.length];
	troughs = new double[values.length];

	startPeaksPosn = new HashMap();
	startTroughsPosn = new HashMap();
	endPeaksPosn = new HashMap();
	endTroughsPosn = new HashMap();

	prev = 0.0;	
	peaksPriceData[0] = 0.0;
	peaksPriceData[1] = 0.0;
	troughsPriceData[0] = 0.0;
	troughsPriceData[1] = 0.0;
	
	//Get the initial direction
	for (i = values.length-1; i > 0; i--) {
	    
	    diff = values[i] - prev;
	    if (values[i] == values[i-1]) {
		continue;
	    }
	    if (values[0] - values[1] > 0) {
		upmove = true;		    
	    } else {
		upmove = false;
	    }		
	    break;
	}	

	//Try to make levels relevant by only considering
	//the last year's data
	int stop = values.length - 365;
	if (stop < 0) {
	    stop = 0;
	}

	// Get all the peaks and troughs

	for (i = values.length-1; i > stop; i--) {
	    diff = values[i] - prev;

	    // previous price was end of downmove, hence trough
	    if (diff > delta && !upmove && directionCount > 4) {		
		if (startTroughsPosn.get( new Double(prev)) == null) {
		    startTroughsPosn.put(new Double(prev), new Integer(i));
		}
		endTroughsPosn.put( new Double(prev), new Integer(i));
		troughs[troughsIndex++] = prev;		    
		upmove = true;
		
		directionCount = 0;
	    }
	    // previous price was end of upmove, hence peak		
	    else if (diff < -delta && upmove && directionCount > 4) {
		
		if (startPeaksPosn.get(new Double(prev)) == null) {
		    startPeaksPosn.put(new Double(prev), new Integer(i));			
		}
		endPeaksPosn.put(new Double(prev) ,new Integer(i));
		peaks[peaksIndex++] = prev;		    
		upmove = false;
		directionCount = 0;
	    } 
	    
	    else {
		directionCount++;
	    }	    
	    prev = values[i];
	}
	
	Arrays.sort(peaks, 0, peaksIndex);
	Arrays.sort(troughs, 0, troughsIndex);

	countOccurances(peaksCount, peaks, peaksIndex-1);
	countOccurances(troughsCount, troughs, troughsIndex-1);       	

	//Choose the most numerous peak and troughs 
	start1 = start2 = 0;
	end1 = end2 = Integer.MAX_VALUE;

	boolean overlap = false;
	i = 0;
	
	int scoreList[] = 
	    new int[(Math.max(peaksCount.size(),troughsCount.size()))];
	
	/* Mark levels high which are the most numerous, have the greatest
	   vertical separation and whose separation is the least.
	*/

	while (i < (Math.min(peaksCount.size(),
			     troughsCount.size()))) {

	    score = 0;
	    	
	    peaksPriceData = (double[])peaksCount.get(peaksCount.size()-i-1);
	    start1 = ((Integer)startPeaksPosn.get(new Double(peaksPriceData[0]))).intValue();
		
	    
	    end1 = ((Integer)endPeaksPosn.get(new Double(peaksPriceData[0]))).intValue();
		
	    troughsPriceData = (double[])troughsCount.get(troughsCount.size()-i-1);
	    start2 = ((Integer)startTroughsPosn.get(new Double(troughsPriceData[0]))).intValue();
	    
	    end2 = ((Integer)endTroughsPosn.get(new Double(troughsPriceData[0]))).intValue();
	    
	    
	    if (start1 - overlapTolerance > end2 ||
		end1 < start2 - overlapTolerance) {
		score += 80;
		
	    } else {
		score -= 10;
	    }	    
	    
	    score += (end1 - start1);

	    diff = Math.abs(troughsPriceData[0] - peaksPriceData[0]);
	    
	    if (diff > Math.sqrt(variance)) {
		score += 50;
	    }

	    if (diff > (2 * Math.sqrt(variance))) {
		score += 60;
	    }

	    score += (int)diff;
	    scoreList[i] = score;
	    
	    i++;
	}


	Arrays.sort(scoreList);	
	i = scoreList.length - 1;
	
	if (i > peaksCount.size()-1) {
	    i = peaksCount.size() - 1;
	}
	if (i > troughsCount.size()-1) {
	    i = troughsCount.size() - 1;
	}
	
	troughsPriceData = (double[])troughsCount.get(i);
	peaksPriceData = (double[])peaksCount.get(i);
	
	double supportPrice = troughsPriceData[0];
	double resistPrice = peaksPriceData[0];
	
	i = 0;
	while (iterator.hasNext()) {   	    
	    Comparable x = (Comparable)iterator.next();
	    
	    if (values[i] == resistPrice ||
		values[i] == supportPrice) {
		resistence.putY(x, new Double(resistPrice));
		support.putY(x, new Double(supportPrice));		
	    }
	    i++;
	}
	
	supportAndResistence[0] = support;
	supportAndResistence[1] = resistence;

	return supportAndResistence;
    }
    
    // Creates a two element list (price, occurranceCount)  
    // in list from the array values[]
    private void countOccurances(ArrayList list, double values[], int length) {
	int i, priceIndex = 0;
	double prev;
	double priceData[] = new double[2];

	average = 0.0;
	variance = 0.0;
	
	prev = 0.0;	
	priceData[0] = 0.0;
	priceData[1] = 0.0;

	for (i = 0; i < length-3; i++) {	    
	    average += values[i];
	    
	    
	    if (prev != values[i]) {
		if (i > 0) {
		    double[] tmp = new double[2];
		    tmp[0] = priceData[0];
		    tmp[1] = priceData[1];

		    list.add(priceIndex++, (double[])tmp);
		}
		
		priceData[0] = values[i];
		priceData[1] = 0;

	    } else {
		priceData[1]++;
	    }	   
	    prev = values[i];
	}

	variance = 0.0;
	average /= length-3;
	for (i = 0; i< length-3; i++) {
	    variance += (values[i] - average)*(values[i] - average);
	}

	variance /= length-3;

	// Last price data may have been same as prev, so it won't
	// have been added.       
	if (length > 0 && values[i-1] == prev) {
	    list.add(priceIndex, priceData);
	}
	
	/* Add the average line to the list and make sure its on top */
	Collections.sort(list, new Comparator() {
		public int compare(Object a, Object b) {
		    double[] v1, v2;
		    v1 = (double[])a;
		    v2 = (double[])b;
		    
		    if (v1[1] < v2[1]) {
			return -1; 
		    } 
		    if (v1[1] > v2[1]) {
			return 1;
		    }
		    return 0;
		}
	    });	
    }
}
