package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.quote.*;

abstract public class AbstractGraph implements Graph {

    private GraphSource source;
    
    public AbstractGraph(GraphSource source) {
	this.source = source;
    }

    protected GraphSource getSource() {
    	return source;
    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue) {
	return source.getToolTipText(date);
    }

    public TradingDate getStartDate() {
	return source.getGraphable().getStartDate();
    }

    public TradingDate getEndDate() {
	return source.getGraphable().getEndDate();
    }

    public String getYLabel(float value) {
	return source.getYLabel(value);
    }

    public String getName() {
	return source.getName();
    }

    public Float getValue(TradingDate date) {
	return source.getGraphable().getValue(date);
    }

    public float getHighestValue(Vector dates) {
	return source.getGraphable().getHighestValue(dates);
    }

    public float getLowestValue(Vector dates) {
	return source.getGraphable().getLowestValue(dates);
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

