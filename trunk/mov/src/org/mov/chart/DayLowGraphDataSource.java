package org.mov.chart;

import org.mov.util.*;
import org.mov.parser.*;

public class DayLowGraphDataSource extends PriceQuoteGraphDataSource {
    
    public DayLowGraphDataSource(QuoteCache cache) {
	setCache(cache, Token.DAY_LOW_TOKEN);
    }

    public String getName() {
	return "Day Low";
    }
}
