package org.mov.chart;

// Implements Simple Moving Average

import java.awt.*;
import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

public class MovingAverageGraph extends Graph {

    private AveragedGraphDataSource movingAverage;
    private GraphDataSource source;
    private HashMap annotations;

    public MovingAverageGraph(GraphDataSource source, int period) {

	super(source);

	// Create averaged data sources of day close
	movingAverage = new AveragedGraphDataSource(source.getCache(), 
						    period,
						    Token.DAY_CLOSE_TOKEN);

	// Create buy sell recommendations
	annotations = GraphTools.createAnnotations(getSource(), movingAverage);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	// We ignore the graph colours and use our own custom colours

	g.setColor(Color.green.darker());
	GraphTools.renderLine(g, movingAverage, xoffset, yoffset, 
			      horizontalScale,
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
	return movingAverage.getHighestValue(dates);
    }

    // Override base class method
    public float getLowestValue(Vector dates) {
	return movingAverage.getLowestValue(dates);
    }

    // Override base class method
    public String getName() {
	return "Moving Average";
    }
}



