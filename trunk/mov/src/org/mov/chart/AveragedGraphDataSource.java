package org.mov.chart;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public class AveragedGraphDataSource extends BasicGraphDataSource {
    
    String symbol;
    TradingDate start;
    TradingDate end;
    HashMap map = new HashMap();

    public AveragedGraphDataSource(QuoteCache cache, int period,
				   int quote) {

	symbol = (String)cache.getSymbols()[0];
	start = cache.getStartDate();
	end = cache.getEndDate();

	// Calculate averages and put them in map
	float average;
	Iterator iterator = cache.dateIterator();
	TradingDate date;
	int offset = 0;

	while(iterator.hasNext()) {

	    date = (TradingDate)iterator.next();

	    // Calculate average
	    try {
		average = 
		    QuoteFunctions.avg(cache, symbol, quote,
				       cache.getNumberDays()+offset < period?
				       cache.getNumberDays()+offset : period, 
				       offset);
	    }
	    catch(EvaluationException e) {
		// shouldn't happen
		average = 0.0F;
	    }

	    offset--;

	    map.put(date, (Object)new Float(average));
	}
    }

    public TradingDate getEndDate() {
	return end;
    }

    public TradingDate getStartDate() {
	return start;
    }

    public Float getValue(TradingDate date) {
	return (Float)map.get(date);
    }
}
