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

    public String getToolTipText(Comparable x, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue) {
	return source.getToolTipText(x);
    }

    public Comparable getStartX() {
	return source.getGraphable().getStartX();
    }

    public Comparable getEndX() {
	return source.getGraphable().getEndX();
    }

    public Set getXRange() {
	return source.getGraphable().getXRange();
    }

    public String getYLabel(float value) {
	return source.getYLabel(value);
    }

    public String getName() {
	return source.getName();
    }

    public Float getY(Comparable x) {
	return source.getGraphable().getY(x);
    }

    public float getHighestY(Vector x) {
	return source.getGraphable().getHighestY(x);
    }

    public float getLowestY(Vector x) {
	return source.getGraphable().getLowestY(x);
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

    public String getAnnotationToolTipText(Comparable x) {
	return null;
    }
}

