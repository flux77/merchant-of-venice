package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.util.*;

abstract public class Graph {
    private GraphDataSource source;

    protected GraphDataSource getSource() {
    	return source;
    }

    abstract public void render(Graphics g, Color colour, 
				int xoffset, int yoffset,
				float horizontalScale, float verticalScale,
				float bottomLineValue, Vector dates);

    abstract public String getToolTipText(TradingDate date, int y, int yoffset,
					  float verticalScale,
					  float bottomLineValue);

    public static int TOOL_TIP_BUFFER = 25;

    public Graph(GraphDataSource source) {
	this.source = source;
    }

    public Graph() {
	source = null;
    }

    public String getToolTipText(TradingDate date) {
	return source.getToolTipText(date);
    }

    public TradingDate getStartDate() {
	return source.getStartDate();
    }

    public TradingDate getEndDate() {
	return source.getEndDate();
    }

    public QuoteCache getCache() {
	return source.getCache();
    }

    public String getYLabel(float value) {
	return source.getYLabel(value);
    }

    public String getSymbol() {
	return source.getSymbol();
    }

    public String getName() {
	return source.getName();
    }

    public Float getValue(TradingDate date) {
	return source.getValue(date);
    }

    public float getHighestValue(Vector dates) {
	return source.getHighestValue(dates);
    }

    public float getLowestValue(Vector dates) {
	return source.getLowestValue(dates);
    }

    public float[] getAcceptableMajorDeltas() {
	return source.getAcceptableMajorDeltas();
    }

    public float[] getAcceptableMinorDeltas() {
	return source.getAcceptableMinorDeltas();
    }

    public HashMap getAnnotations() {
	return null;
    }

    public boolean hasAnnotations() {
	return false;
    }

    public String getAnnotationToolTipText(TradingDate date) {
	return null;
    }
}


