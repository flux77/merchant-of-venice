package org.mov.chart.source;

import java.util.*;

import org.mov.chart.*;
import org.mov.util.*;
import org.mov.quote.*;

public interface GraphSource {
    abstract public String getName();
    abstract public String getToolTipText(TradingDate date);
    abstract public String getYLabel(float value);
    abstract public float[] getAcceptableMajorDeltas();
    abstract public float[] getAcceptableMinorDeltas();
    abstract public Graphable getGraphable();
}


