package org.mov.chart;

import java.awt.*;
import java.util.*;

import org.mov.util.*;

public class BarGraph extends Graph {

    public BarGraph(GraphDataSource source) {
	super(source);
    }

    public void render(Graphics g, Color colour, int xoffset, int yoffset,
		       float horizontalScale, float verticalScale,
		       float bottomLineValue, Vector dates) {

	g.setColor(colour);
	GraphTools.renderBar(g, getSource(), xoffset, yoffset, horizontalScale,
			     verticalScale, bottomLineValue, dates);

    }

    public String getToolTipText(TradingDate date, int y, int yoffset,
				 float verticalScale,
				 float bottomLineValue)
    {
	return getToolTipText(date);
    }

    public HashMap getAnnotations() {
	return null;
    }
}
