package org.mov.chart.graph;

import java.awt.*;
import java.util.*;

import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.quote.*;

public interface Graph {

    public static int TOOL_TIP_BUFFER = 50;

    public void render(Graphics g, Color colour, 
		       int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates);
    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue);
    public TradingDate getStartDate();
    public TradingDate getEndDate();
    public String getYLabel(float value);
    public String getName();
    public Float getValue(TradingDate date);
    public float getHighestValue(Vector dates);
    public float getLowestValue(Vector dates);
    public float[] getAcceptableMajorDeltas();
    public float[] getAcceptableMinorDeltas();
    public HashMap getAnnotations();
    public boolean hasAnnotations();
    public String getAnnotationToolTipText(TradingDate date);
}


