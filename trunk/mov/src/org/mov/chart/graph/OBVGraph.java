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
 * On Balance Volume (OBV) graph. This graph is used as a precursor for
 * stock trends. If it goes down its a possible indicator that the
 * stock will too and vice-versa. OBV is calculated by starting with
 * an arbitary constant. To calculate a day's OBV, get the previous
 * day's OBV. If the stock went up that day, add the volume. If the stock
 * went down, minus the volume. 
 */
public class OBVGraph extends AbstractGraph {

    private Graphable obv;
   
    /**
     * Create a new On Balance Volume (OBV) graph.
     *
     * @param	open	the day open price
     * @param	close	the day close price
     * @param	volume	the day volume
     * @param	start	arbitary start value
     */
    public OBVGraph(GraphSource open, GraphSource close,
		    GraphSource volume, float start) {
	
	// Use same axis as volume
	super(volume);

	// create OBV
	obv = createOBV(open.getGraphable(), close.getGraphable(),
			volume.getGraphable(), start);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	g.setColor(colour);
	GraphTools.renderLine(g, obv, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Override base class
    public float getHighestY(Vector x) {
	return obv.getHighestY(x);
    }

    // Override base class
    public float getLowestY(Vector x) {
	return obv.getLowestY(x);
    }

    /**
     * Create OBV graphable.
     *
     * @param	open	the day open price
     * @param	close	the day close price
     * @param	volume	the day volume
     * @param	start	arbitary start value
     * @return	the OBV
     */
    public static Graphable createOBV(Graphable open, Graphable close,
				      Graphable volume, float runningOBV) {

	Graphable obv = new Graphable();

	// All graphables should be in-sync
	Iterator iterator = open.getXRange().iterator();

	while(iterator.hasNext()) {
	    Comparable x = (Comparable)iterator.next();

	    Float dayOpen = open.getY(x);
	    Float dayClose = close.getY(x);
	    Float dayVolume = volume.getY(x);

	    if(dayClose.compareTo(dayOpen) > 0) 
		runningOBV += dayVolume.intValue();
	    else if(dayClose.compareTo(dayOpen) < 0)
		runningOBV -= dayVolume.intValue();

	    obv.putY(x, new Float(runningOBV));
	}

	return obv;
    }
}

