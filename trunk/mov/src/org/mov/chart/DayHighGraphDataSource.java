package org.mov.chart;

import org.mov.util.*;
import org.mov.parser.*;

public class DayHighGraphDataSource extends PriceQuoteGraphDataSource {
    
    public DayHighGraphDataSource(QuoteCache cache) {
	setCache(cache, Token.DAY_HIGH_TOKEN);
    }

    public String getName() {
	return "Day High";
    }
}
