package org.mov.chart;

import java.util.*;

import org.mov.util.*;

abstract public class GraphDataSource extends BasicGraphDataSource {
    abstract public String getName();
    abstract QuoteCache getCache();
    abstract String getSymbol();
    abstract public String getToolTipText(TradingDate date);
    abstract public String getYLabel(float value);
    abstract public float[] getAcceptableMajorDeltas();
    abstract public float[] getAcceptableMinorDeltas();
}


