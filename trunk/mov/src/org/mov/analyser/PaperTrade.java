/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
*/

package org.mov.analyser;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;

public class PaperTrade {

    private final static String CASH_ACCOUNT_NAME = "Cash Account";
    private final static String SHARE_ACCOUNT_NAME = "Share Account";

    private class Environment {
        public ScriptQuoteBundle quoteBundle;
        public Portfolio portfolio;
        public CashAccount cashAccount;
        public ShareAccount shareAccount;
        public int startDateOffset;
        public int endDateOffset;

        public Environment(ScriptQuoteBundle quoteBundle,
                           String portfolioName,
                           TradingDate startDate,
                           TradingDate endDate,
                           float capital) {

            this.quoteBundle = quoteBundle;

            // First set up portfolio
            portfolio = new Portfolio(portfolioName);

            // Add a cash account and a share account
            cashAccount = new CashAccount(CASH_ACCOUNT_NAME);
            shareAccount = new ShareAccount(SHARE_ACCOUNT_NAME);

            portfolio.addAccount(cashAccount);
            portfolio.addAccount(shareAccount);

            // Deposit starting capital into portfolio
            Transaction transaction = 
                Transaction.newDeposit(startDate, capital, cashAccount);

            portfolio.addTransaction(transaction);

            // Now find the fast date offsets
            try {
                startDateOffset = quoteBundle.dateToOffset(startDate);
                endDateOffset = quoteBundle.dateToOffset(endDate);
            }
            catch(WeekendDateException e) {
                assert(false);
                
                startDateOffset = endDateOffset = 0;
            }
        }
    }

    private PaperTrade() {
        // users shouldn't instantiate this class
    }

    private static boolean sell(Environment environment,
				String symbol,
				float tradeCost,
				int day) 
	throws MissingQuoteException {

	// Make sure we have enough money for the trade
	if(environment.cashAccount.getValue() >= tradeCost) {

	    // Get the number of shares we own - we will sell all of them
	    StockHolding stockHolding = environment.shareAccount.get(symbol);
	    int shares = 0;
	    if(stockHolding != null) 
		shares = stockHolding.getShares();

	    // Only sell if we have any!
	    if(shares > 0) {

		// How much are they worth? We sell at the day open price
		float amount = shares * environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN,
                                                                         day);
		
		TradingDate date = environment.quoteBundle.offsetToDate(day);
		Transaction sell = Transaction.newReduce(date, 
                                                         amount,
							 symbol, 
                                                         shares,
							 tradeCost,
							 environment.cashAccount,
							 environment.shareAccount);
		environment.portfolio.addTransaction(sell);

		return true;
	    }
	}

	return false;
    }

    private static boolean buy(Environment environment,
			       String symbol,
			       float amount,				 
			       float tradeCost,
			       int day) 
	throws MissingQuoteException {


	// Calculate maximum number of shares we can buy with
	// the given amount
	float sharePrice = environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN, day);
	int shares = 
	    (new Double(Math.floor(amount / sharePrice))).intValue();
	
	// Now calculate the actual amount the shares will cost
	amount = sharePrice * shares;
	
	// Make sure we have enough money for the trade
	if(environment.cashAccount.getValue() >= (tradeCost + amount)) {

	    TradingDate date = environment.quoteBundle.offsetToDate(day);
	    Transaction buy = Transaction.newAccumulate(date, 
                                                        amount,
							symbol, 
                                                        shares,
							tradeCost,
							environment.cashAccount,
							environment.shareAccount);

	    environment.portfolio.addTransaction(buy);

	    return true;
	}
	
	return false;
    }

    private static int getHoldingTime(StockHolding stockHolding, int dateOffset) {
        int holdingTime = 0;

        try {              
            holdingTime = -(QuoteCache.getInstance().dateToOffset(stockHolding.getDate()) - 
                            dateOffset);
        }
        catch(WeekendDateException e) {
            assert false;
        }

        return holdingTime;
    }

    public static Portfolio paperTrade(String portfolioName, 
				       ScriptQuoteBundle quoteBundle, 
                                       Variables variables,
                                       String symbol,
				       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       float capital,
				       float tradeCost) 
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital);
        int dateOffset = environment.startDateOffset;

	// This is set when we own the stock and we set the date we acquired the
        // stock
	boolean ownStock = false;
        int dateAcquiredOffset = 0;

        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, 0.0F);
        else
            variables.setValue("held", 0.0F);

	// Now iterate through each trading date and decide whether
	// to buy/sell. The last date is used for placing the previous
	// date's buy/sell orders.
	while(dateOffset < environment.endDateOffset) {

	    try {		
		// Sell?
		if(ownStock) {
                    variables.setValue("held", -(dateAcquiredOffset - dateOffset));

		    if(sell.evaluate(variables, 
                                     quoteBundle, 
                                     symbol, 
                                     dateOffset) >= Expression.TRUE) {
			if(sell(environment, symbol, tradeCost, dateOffset + 1))
			    ownStock = false;
		    }

                    variables.setValue("held", 0.0F);
		}
		
		// Buy?
		else {
		    if(buy.evaluate(variables, quoteBundle, symbol, 
                                    dateOffset) >= Expression.TRUE) {
			// Spend all our money except for enough to do
			// a buy and a later sell trade. 
			float amount = environment.cashAccount.getValue() - 2 * tradeCost;

			if(buy(environment, symbol, amount, tradeCost, dateOffset + 1)) {
                            dateAcquiredOffset = dateOffset;
			    ownStock = true;
                        }
		    }
		}
	    }
	    catch(MissingQuoteException e) {
                // Ignore and move on
	    }

	    // Go to the next trading date
	    dateOffset++;
	}

	return environment.portfolio;
    }

    public static Portfolio paperTrade(String portfolioName,
                                       ScriptQuoteBundle quoteBundle, 
                                       Variables variables,
                                       OrderComparator orderComparator,
                                       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       float capital,
                                       float valuePerStock,
				       float tradeCost) 
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital);
        int dateOffset = environment.startDateOffset;

        if(orderComparator != null && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, 0.0F);
        else
            variables.setValue("held", 0.0F);

        // Now iterate through each trading date and decide whether
	// to buy/sell. The last date is used for placing the previous
	// date's buy/sell orders.
	while(dateOffset < environment.endDateOffset) {

            List symbols = quoteBundle.getSymbols(dateOffset);

            // If we've been asked to trade in a specific order, then order them
            if(orderComparator != null) 
                Collections.sort(symbols, orderComparator);

            // Iterate through stocks available today - should we buy or sell any of it?
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {

                String symbol = (String)iterator.next();
                StockHolding stockHolding = environment.shareAccount.get(symbol);

                // If we care about the order, make sure the "order" variable is set
                if(orderComparator != null)
                    variables.setValue("order", symbols.indexOf(symbol));

                try {
                    // Sell?
                    if(stockHolding != null) {                  
                        variables.setValue("held", getHoldingTime(stockHolding, dateOffset));

                        if(sell.evaluate(variables, quoteBundle, symbol, 
                                         dateOffset) >= Expression.TRUE)
                            sell(environment, symbol, tradeCost, dateOffset + 1);

                        variables.setValue("held", 0.0F);
                    }

                    // Buy? But only if we have enough money
                    else if((valuePerStock + 2 * tradeCost) < 
                            environment.cashAccount.getValue()) {

                        if(buy.evaluate(variables, quoteBundle, symbol, 
                                        dateOffset) >= Expression.TRUE)
                            buy(environment, symbol, valuePerStock,  tradeCost, 
                                dateOffset + 1);
                    }
                }
                catch(MissingQuoteException e) {
                    // Ignore and move on
                }
            }
            dateOffset++;
        }

        return environment.portfolio;
    }
}
