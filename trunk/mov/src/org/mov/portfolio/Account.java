package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.quote.*;

public interface Account {
    public static final int CASH_ACCOUNT = 0;
    public static final int SHARE_ACCOUNT = 1;

    public String getName();
    public int getType();
    public float getValue(QuoteCache cache, TradingDate date);
    public void transaction(Transaction transaction);
}

