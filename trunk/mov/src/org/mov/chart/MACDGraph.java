package org.mov.chart;

// Implements Moving Average Convergence Divergence Graph

import java.awt.*;
import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

public class MACDGraph extends Graph {

    private AveragedGraphDataSource fastSource;
    private AveragedGraphDataSource slowSource;
    private HashMap annotations;

    public MACDGraph(GraphDataSource source, int periodOne,
		     int periodTwo) {

	super(source);

	// Create averaged data sources. Fast source has the shorted period.
	// Sources are for day close
	fastSource = new AveragedGraphDataSource(source.getCache(),
						 periodOne < periodTwo? 
						 periodOne : periodTwo,
						 Token.DAY_CLOSE_TOKEN);
	slowSource = new AveragedGraphDataSource(source.getCache(), 
						 periodOne > periodTwo? 
						 periodOne : periodTwo,
						 Token.DAY_CLOSE_TOKEN);

	// Create buy sell recommendations
	annotations = GraphTools.createAnnotations(fastSource, slowSource);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	// We ignore the graph colours and use our own custom colours

	// Fast moving line
	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, fastSource, xoffset, yoffset, horizontalScale,
			      verticalScale, bottomLineValue, dates);

	// Slow moving line
	g.setColor(Color.red.darker());
	GraphTools.renderLine(g, slowSource, xoffset, yoffset, horizontalScale,
			      verticalScale, bottomLineValue, dates);
    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return null; // we never give tool tip information
    }

    // Override base class method
    public HashMap getAnnotations() {
	return annotations;
    }

    // Override base class method
    public boolean hasAnnotations() {
	return true;
    }

    // Override base class method
    public float getHighestValue(Vector dates) {
	float fastHighestValue = fastSource.getHighestValue(dates);
	float slowHighestValue = slowSource.getHighestValue(dates);

	return 
	    fastHighestValue > slowHighestValue? 
	    fastHighestValue :
	    slowHighestValue;
    }

    // Override base class method
    public float getLowestValue(Vector dates) {
	float fastLowestValue = fastSource.getLowestValue(dates);
	float slowLowestValue = slowSource.getLowestValue(dates);

	return 
	    fastLowestValue < slowLowestValue? 
	    fastLowestValue :
	    slowLowestValue;
    }

    // Override base class method
    public String getName() {
	return "MACD";
    }
}



