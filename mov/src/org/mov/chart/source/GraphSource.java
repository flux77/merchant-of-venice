package org.mov.chart.source;

import java.util.*;

import org.mov.chart.*;
import org.mov.util.*;
import org.mov.quote.*;

/**
 * Provides an abstraction of the data being graphed, this way graphs
 * do not need to know anything about the underlying data they are graphing.
 */
public interface GraphSource {

    /**
     * Return the name of the data.
     *
     * @param	the name
     */
    public String getName();

    /**
     * Get the tool tip text for the given X value
     *
     * @param	x	the X value
     * @return	the tooltip text
     */
    public String getToolTipText(Comparable x);

    /**
     * Convert the Y value to a label to be displayed in the vertical
     * axis.
     *
     * @param	value	y value
     * @return	the label text
     */
    public String getYLabel(float value);

    /**
     * Return an array of acceptable major deltas for the vertical
     * axis.
     *
     * @return	array of floats
     * @see	org.mov.chart.graph.Graph#getAcceptableMajorDeltas
     */ 
    public float[] getAcceptableMajorDeltas();

    /**
     * Return an array of acceptable minor deltas for the vertical
     * axis.
     *
     * @return	array of floats
     * @see	org.mov.chart.graph.Graph#getAcceptableMajorDeltas
     */ 
    public float[] getAcceptableMinorDeltas();

    /**
     * Get the actual graphable data.
     *
     * @return	the graphable data
     */
    public Graphable getGraphable();
}


