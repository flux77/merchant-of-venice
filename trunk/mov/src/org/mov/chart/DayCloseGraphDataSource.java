package org.mov.chart;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class DayCloseGraphDataSource extends PriceQuoteGraphDataSource {
    
    public DayCloseGraphDataSource(QuoteCache cache) {
	setCache(cache, Token.DAY_CLOSE_TOKEN);
    }

    public String getName() {
	return "Day Close";
    }
}









