package org.mov.chart.graph;

import java.awt.*;
import java.lang.*;
import java.util.*;

import org.mov.chart.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.prefs.*;
import org.mov.quote.*;
import org.mov.ui.*;

public class AdvanceDeclineGraph extends AbstractGraph {

    private Graphable advanceDecline;

    // Graph starts at an arbitary value
    private static final int START_VALUE = 1000;

    public AdvanceDeclineGraph() {

	super(null);
	advanceDecline = createAdvanceDecline();
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector xRange) {

	GraphTools.renderLine(g, advanceDecline, xoffset, yoffset, 
			      horizontalScale,
			      verticalScale, bottomLineValue, xRange);
    }

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	// we will give out the number that advanced and the number that
	// declined

	return null; // we never give tool tip information
    }

    /**
     * Get the first X value that this graph will draw.
     *
     * @return	X value of the first x coordinate in the default 
     *		<code>GraphSource</code>'s <code>Graphable</code>
     */
    public Comparable getStartX() {
	return advanceDecline.getStartX();
    }

    /**
     * Get the last X value that this graph will draw.
     *
     * @return	X value of the last x coordinate in the default 
     *		<code>GraphSource</code>'s <code>Graphable</code>
     */
    public Comparable getEndX() {
	return advanceDecline.getEndX();
    }

    /**
     * Get all X values that this graph will draw.
     *
     * @return	X values in the default <code>GraphSource</code>'s 
     *		<code>Graphable</code>
     */
    public Set getXRange() {
	return advanceDecline.getXRange();
    }

    /**
     * Convert the Y value to a label to be displayed in the vertical
     * axis.
     *
     * @param	value	y value
     * @return	the Y label text that the default <code>GraphSource</code>
     *		would display
     */
    public String getYLabel(float value) {
	return Integer.toString((int)value);
    }

    /**
     * Return the name of this graph.
     *
     * @return	<code>Advance/Decline</code>
     */
    public String getName() {
	return "Advance/Decline";
    }

    /**
     * Return the Y value for the given X value.
     *
     * @param	X value
     * @return	Y value of the default <code>GraphSource</code>
     */
    public Float getY(Comparable x) {
	return advanceDecline.getY(x);
    }

    /**
     * Return the highest Y value in the given X range.
     *
     * @param	xRange	range of X values
     * @return	the highest Y value of the default <code>GraphSource</code>
     */
    public float getHighestY(Vector x) {
	return advanceDecline.getHighestY(x);
    }

    /**
     * Return the loweset Y value in the given X range.
     *
     * @param	xRange	range of X values
     * @return	the lowest Y value of the default <code>GraphSource</code>
     */

    public float getLowestY(Vector x) {
	return advanceDecline.getLowestY(x);
    }

    /**
     * Return an array of acceptable major deltas for the vertical
     * axis.
     *
     * @return	an array of floats representing the minor deltas 
     *		of the default <code>GraphSource</code>
     */ 
    public float[] getAcceptableMajorDeltas() {
	float[] major = {1.0F, // 1 point
			 10.0F, // 10 points
			 100.0F, // 100 points
			 1000.0F, // 1000 points
			 10000.0F, // 10,000 points
			 100000.0F}; // 100,000 points
	return major;	    
    }

    /**
     * Return an array of acceptable minor deltas for the vertical
     * axis.
     *
     * @return	an array of floats representing the minor deltas
     *		of the default <code>GraphSource</code>
     * @see	Graph#getAcceptableMajorDeltas
     */ 
    public float[] getAcceptableMinorDeltas() {
	float[] minor = {1F, 1.1F, 1.25F, 1.3333F, 1.5F, 2F, 2.25F, 
			 2.5F, 3F, 3.3333F, 4F, 5F, 6F, 6.5F, 7F, 7.5F, 
			 8F, 9F};
	return minor;
    }

    private Graphable createAdvanceDecline() {
	Graphable advanceDecline = new Graphable();

	System.out.println("GET DATES");

	Vector dates = QuoteSourceManager.getSource().getDates();
	Iterator iterator = dates.iterator();

	System.out.println("GOT DATES");

	int cumulativeAdvanceDecline = START_VALUE;

	ProgressDialog p = ProgressDialogManager.getProgressDialog();
	p.setTitle("Calculating advance/decline");
	p.setIndeterminate(false);
	p.setMaximum(dates.size());
	p.show();

	int progress = 0;
	p.setProgress(progress);
	p.show();

	// Iterate over every date 
	while(iterator.hasNext()) {
	    TradingDate date = (TradingDate)iterator.next();

	    // Use cached version if we can
	    int advance = 
		PreferencesManager.loadCachedAdvance(date);
	    int decline =
		PreferencesManager.loadCachedDecline(date);
	    int todayAdvanceDecline = 0;

	    if(advance == PreferencesManager.UNDEFINED_INT) {
		try {
		    todayAdvanceDecline = calculateAdvanceDecline(date);

		    // Cache values
		    //		    PreferencesManager.saveCachedAdvanceDecline(date, 
		    //						todayAdvanceDecline);


		    System.out.println("SAVE date " + date + " value is " +
				       todayAdvanceDecline);
		}
		catch(EvaluationException e) {
		    // nothing to do
		}
	    }
	    else {
		//		System.out.println("LOAD date " + date + " value is " +
		//		   todayAdvanceDecline);
	    }

	    if(todayAdvanceDecline != PreferencesManager.UNDEFINED_INT) {
		cumulativeAdvanceDecline += todayAdvanceDecline;

		advanceDecline.putY((Comparable)date, 
				    new Float(cumulativeAdvanceDecline));

		System.out.println("a/d " + cumulativeAdvanceDecline);
	    }

	    p.setProgress(progress++);
	}

	return advanceDecline;
    }

    private int calculateAdvanceDecline(TradingDate date) 
	throws EvaluationException {

	int todayAdvanceDecline = 0;
	QuoteCache cache = new QuoteCache(date, 
					  QuoteSource.COMPANIES_AND_FUNDS);
	Object[] symbols = cache.getSymbols();

	// Iterate over every three letter symbol
	for(int i = 0; i < symbols.length; i++) {
	    float dayOpen = cache.getQuote((String)symbols[i], 
					   Quote.DAY_OPEN, date);
	    float dayClose = cache.getQuote((String)symbols[i], 
					    Quote.DAY_CLOSE, date);
	    if(dayClose > dayOpen)
		todayAdvanceDecline++;
	    else if(dayClose < dayOpen)
		todayAdvanceDecline--;
	}
	
	return todayAdvanceDecline;
    }
}


