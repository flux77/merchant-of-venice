package org.mov.chart;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class DayOpenGraphDataSource extends PriceQuoteGraphDataSource {
    
    public DayOpenGraphDataSource(QuoteCache cache) {
	setCache(cache, Token.DAY_OPEN_TOKEN);
    }

    public String getName() {
	return "Day Open";
    }
}
