package org.mov.portfolio;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

public interface Account {
    public String getName();
    public float getValue(QuoteCache cache, TradingDate date) 
	throws EvaluationException;
}

