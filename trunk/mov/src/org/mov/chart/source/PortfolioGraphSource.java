package org.mov.chart.source;

import org.mov.chart.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;
import org.mov.ui.ProgressDialog;
import org.mov.ui.ProgressDialogManager;

import java.util.*;

public class PortfolioGraphSource implements GraphSource {

    private QuoteCache cache;
    private int quote;
    private Graphable graphable;
    private Portfolio portfolio;

    public PortfolioGraphSource(Portfolio portfolio, QuoteCache cache,
				int quote) {
	this.portfolio = portfolio;
	this.quote = quote;
	this.cache = cache;

	// Build graphable so this source can be directly graphed
	graphable = new Graphable();

	// Construct portfolio transaction by transaction so we
	// can 'recreate' portfolio for each day to get its value	
	Vector transactions = portfolio.getTransactions();
	Iterator transactionIterator = transactions.iterator();
	
	// No transactions in portfolio? Nothing to graph
	if(!transactionIterator.hasNext())
	    return;
	Transaction transaction = (Transaction)transactionIterator.next();
	
	// Get start date from first transaction, get end date from
	// latest date in cache
	TradingDate startDate = transaction.getDate();
	TradingDate endDate = cache.getEndDate();

	// Iterate through each day between start and end date, recreating
	// portfolio value on that day
	Vector dateRange = Converter.dateRangeToTradingDateVector(startDate,
								  endDate);

	Iterator dateIterator = dateRange.iterator();	
	Portfolio temporaryPortfolio = new Portfolio("temporary");

	while(dateIterator.hasNext()) {
	    TradingDate date = (TradingDate)dateIterator.next();

	    while(transaction != null &&
		  transaction.getDate().compareTo(date) <= 0) {
		temporaryPortfolio.addTransaction(transaction);
		transaction = (Transaction)transactionIterator.next();
	    }

	    Float value = 
		new Float(temporaryPortfolio.getValue(cache, date));
	    graphable.putY((Comparable)date, value);

	    System.out.println("date: " + date + " value " + value);
	}
    }

    public Graphable getGraphable() {
	return graphable;
    }

    public String getName() {
	return portfolio.getName();
    }

    public String getToolTipText(Comparable x) {

	// In portfolio graphs the x axis is in dates
	TradingDate date = (TradingDate)x;
	
	return "nothing";

    }

    public String getYLabel(float value) {
	return Converter.priceToString(value);
    }

    public float[] getAcceptableMajorDeltas() {
	float[] major = {0.001F, // 0.1c
			 0.01F, // 1c
			 0.1F, // 10c
			 1.0F, // $1
			 10.0F, // $10
			 100.0F, // $100
			 1000.0F, // $1k
			 10000.0F, // $10k
			 100000.0F, // $100k
			 1000000.0F, // $1M
			 10000000.0F, // $10M
			 100000000.0F, // $100M
			 1000000000.0F}; // $1B

	return major;	    
    }

    public float[] getAcceptableMinorDeltas() {
	float[] minor = {1F, 1.1F, 1.25F, 1.3333F, 1.5F, 2F, 2.25F, 
			 2.5F, 3F, 3.3333F, 4F, 5F, 6F, 6.5F, 7F, 7.5F, 
			 8F, 9F};
	return minor;
    }
}
