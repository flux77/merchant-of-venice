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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mov.util.Locale;
import org.mov.util.Money;
import org.mov.util.TradingDate;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;

public class PaperTrade {

    private final static String CASH_ACCOUNT_NAME = Locale.getString("CASH_ACCOUNT");
    private final static String SHARE_ACCOUNT_NAME = Locale.getString("SHARE_ACCOUNT");

    private class Environment {
        public QuoteCache quoteCache;
        public QuoteBundle quoteBundle;
        public Portfolio portfolio;
        public CashAccount cashAccount;
        public ShareAccount shareAccount;
        public int startDateOffset;
        public int endDateOffset;

        public Environment(QuoteBundle quoteBundle,
                           String portfolioName,
                           TradingDate startDate,
                           TradingDate endDate,
                           Money capital) {

            this.quoteBundle = quoteBundle;
            this.quoteCache = QuoteCache.getInstance();

            // First set up a new (transient) portfolio
            portfolio = new Portfolio(portfolioName, true);

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

    private static void sell(Environment environment,
                             StockHolding stockHolding,
                             Money tradeCost,
                             int day) 
	throws MissingQuoteException {

	// Make sure we have enough money for the trade
	if(environment.cashAccount.getValue().isGreaterThanEqual(tradeCost)) {

	    // Get the number of shares we own - we will sell all of them
	    int shares = stockHolding.getShares();
            Symbol symbol = stockHolding.getSymbol();

            // How much are they worth? We sell at the day open price.
            Money amount = 
                new Money(shares * 
                          environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN, day));
            TradingDate date = environment.quoteBundle.offsetToDate(day);
            Transaction sell = Transaction.newReduce(date, 
                                                     amount,
                                                     symbol,
                                                     shares,
                                                     tradeCost,
                                                     environment.cashAccount,
                                                     environment.shareAccount);
            environment.portfolio.addTransaction(sell);
	}
    }

    private static void buy(Environment environment,
                            Symbol symbol,
                            Money amount,				 
                            Money tradeCost,
                            int day) 
	throws MissingQuoteException {

	// Calculate maximum number of shares we can buy with the given amount
	double sharePrice = environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN, day);
	int shares = (int)Math.floor(amount.doubleValue() / sharePrice);
	
	// Now calculate the actual amount the shares will cost
	amount = new Money(sharePrice * shares);
	
        TradingDate date = environment.quoteBundle.offsetToDate(day);
        Transaction buy = Transaction.newAccumulate(date, 
                                                    amount,
                                                    symbol,
                                                    shares,
                                                    tradeCost,
                                                    environment.cashAccount,
                                                    environment.shareAccount);
        
        environment.portfolio.addTransaction(buy);
    }

    private static void sellTrades(Environment environment, 
                                   QuoteBundle quoteBundle,
                                   Variables variables, 
                                   Expression sell,
                                   int dateOffset,
                                   Money tradeCost,
                                   List symbols,
                                   OrderCache orderCache) 
        throws EvaluationException {

        // Iterate through our stock holdings and see if we should sell any
        List stockHoldings = new ArrayList(environment.shareAccount.getStockHoldings().values());
        
        for(Iterator iterator = stockHoldings.iterator(); iterator.hasNext();) {
            StockHolding stockHolding = (StockHolding)iterator.next();
            Symbol symbol = stockHolding.getSymbol();
            
            // If we care about the order, make sure the "order" variable is set.
            if(orderCache.isOrdered()) {
                int order = symbols.indexOf(symbol);

                // It's possible that we don't have a quote for the symbol today.
                // So skip it.
                if(order == -1)
                    continue;
                variables.setValue("order", order);
            }

            variables.setValue("held", getHoldingTime(environment, stockHolding, dateOffset));

            try {
                if(sell.evaluate(variables, quoteBundle, symbol, dateOffset) >= Expression.TRUE)
                    sell(environment, stockHolding, tradeCost, dateOffset + 1);
            }
            catch(MissingQuoteException e) {
                // ignore and move on
            }
        }
    }

    private static void buyTrades(Environment environment,
                                  QuoteBundle quoteBundle,
                                  Variables variables,
                                  Expression buy,
                                  int dateOffset,
                                  Money tradeCost,
                                  List symbols,
                                  OrderCache orderCache,
                                  Money stockValue) 
        throws EvaluationException {

        variables.setValue("held", 0);

        // If we have enough money, iterate through stocks available today -
        // should we buy any of it?
        Money cash = environment.cashAccount.getValue();
        
        if(stockValue.add(tradeCost.multiply(2)).isLessThanEqual(cash)) {
            int order = 0;

            // Iterate through stocks available today - should we buy or sell any of it?
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();

                // Skip if we already own it
                if(!environment.shareAccount.isHolding(symbol)) {
                   
                    // If we care about the order, make sure the "order" variable is set
                    if(orderCache.isOrdered())
                        variables.setValue("order", order);

                    try {
                        if(buy.evaluate(variables, quoteBundle, symbol, 
                                        dateOffset) >= Expression.TRUE) {
                            buy(environment, symbol, stockValue,  tradeCost, 
                                dateOffset + 1);
                        
                            // If there is no more money left, don't even look at the
                            // other stocks
                            cash = environment.cashAccount.getValue();

                            if(stockValue.add(tradeCost.multiply(2)).isGreaterThan(cash))
                                break;
                        }
                    }
                    catch(MissingQuoteException e) {
                        // Ignore and move on
                    }
                }

                order++;
            }
        }
    }

    private static int getHoldingTime(Environment environment, StockHolding stockHolding, 
                                      int dateOffset) {
        try {              
            return (1 - (QuoteCache.getInstance().dateToOffset(stockHolding.getDate()) - 
                         dateOffset));

        }
        catch(WeekendDateException e) {
            assert false;
            return 0;
        }
    }

    public static Portfolio paperTrade(String portfolioName,
                                       QuoteBundle quoteBundle, 
                                       Variables variables,
                                       OrderCache orderCache,
                                       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       Money capital,
                                       Money stockValue,
				       Money tradeCost) 
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital);
        int dateOffset = environment.startDateOffset;

        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);

        // Now iterate through each trading date and decide whether
	// to buy/sell. The last date is used for placing the previous
	// date's buy/sell orders.
	while(dateOffset < environment.endDateOffset) {

            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);

            sellTrades(environment, quoteBundle, variables, sell, dateOffset, tradeCost, 
                       symbols, orderCache);
            buyTrades(environment, quoteBundle, variables, buy, dateOffset, tradeCost, 
                      symbols, orderCache, stockValue);

            dateOffset++;
        }

        return environment.portfolio;
    }

    public static Portfolio paperTrade(String portfolioName,
                                       QuoteBundle quoteBundle, 
                                       Variables variables,
                                       OrderCache orderCache,
                                       TradingDate startDate, 
				       TradingDate endDate,
				       Expression buy,
				       Expression sell,
				       Money capital,
                                       int numberStocks,
				       Money tradeCost) 
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital);
        int dateOffset = environment.startDateOffset;

        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);

        // Now iterate through each trading date and decide whether
	// to buy/sell. The last date is used for placing the previous
	// date's buy/sell orders.
	while(dateOffset < environment.endDateOffset) {

            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);

            sellTrades(environment, quoteBundle, variables, sell, dateOffset, tradeCost, 
                       symbols, orderCache);
            try {
                // stockValue = (portfolio / numberStocks) - (2 * tradeCost)
                Money portfolioValue = environment.portfolio.getValue(quoteBundle, dateOffset);
                Money stockValue = 
                    portfolioValue.divide(numberStocks).subtract(tradeCost.multiply(2));
                buyTrades(environment, quoteBundle, variables, buy, dateOffset, tradeCost, 
                          symbols, orderCache, stockValue);
            }
            catch(MissingQuoteException e) {
                // Ignore and move on
            }

            dateOffset++;
        }

        return environment.portfolio;
    }
}
