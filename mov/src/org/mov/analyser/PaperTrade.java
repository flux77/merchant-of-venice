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

import org.mov.util.Locale;
import org.mov.util.Money;
import org.mov.util.TradingDate;
import org.mov.parser.EvaluationException;
import org.mov.parser.Expression;
import org.mov.parser.ExpressionFactory;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.portfolio.CashAccount;
import org.mov.portfolio.Portfolio;
import org.mov.portfolio.ShareAccount;
import org.mov.portfolio.StockHolding;
import org.mov.portfolio.Transaction;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.Quote;
import org.mov.quote.QuoteCache;
import org.mov.quote.QuoteBundle;
import org.mov.quote.Symbol;
import org.mov.quote.WeekendDateException;

/**
 * Paper trades stocks using historical quote data and buy and sell indicators.
 * Paper or back trading is a good way of testing the effectiveness of
 * indicators without risking money. This class allows the user to
 * supply historical quote data and buy and sell indicators.
 * The class will then trade using the given indicators and return the
 * final portfolio.
 *
 * <p>The final portfolio will contain a single cash and a single share account.
 *
 * @author Andrew Leppard
 */
public class PaperTrade {

    // Generic name to call all the cash accounts in all generated portfolios
    private final static String CASH_ACCOUNT_NAME = Locale.getString("CASH_ACCOUNT");

    // Generic name to call all the share accounts in all generated portfolios
    private final static String SHARE_ACCOUNT_NAME = Locale.getString("SHARE_ACCOUNT");

    // Since this process uses so many temporary variables, it makes sense
    // grouping them all together.
    private class Environment {

        // Direct access to quote cache to avoid calling getInstance() method
        public QuoteCache quoteCache;

        // Historical quote data
        public QuoteBundle quoteBundle;

        // Current portfolio
        public Portfolio portfolio;

        // Direct reference to portfolio's only cash account
        public CashAccount cashAccount;

        // Direct reference to portfolio's only share account
        public ShareAccount shareAccount;

        // Start date of paper trading
        public int startDateOffset;

        // Last date of paper trading
        public int endDateOffset;

        // The rule according to which the stock is bought
        // If none is set, open price is the common behaviour
        public String buySystemRule = "lag(open,0)";

        // The rule according to which the stock is sold
        // If none is set, open price is the common behaviour
        public String sellSystemRule = "lag(open,0)";

        /**
         * Create a new environment for paper trading.
         *
         * @param quoteBundle the historical quote data
         * @param portfolioName the name of the portfolio
         * @param startDate start date of trading
         * @param endDate last date of trading
         * @param capital initial capital for trading
         */
        public Environment(QuoteBundle quoteBundle,
                           String portfolioName,
                           TradingDate startDate,
                           TradingDate endDate,
                           Money capital,
                           String buySystemRule,
                           String sellSystemRule) {

            this.quoteBundle = quoteBundle;
            this.quoteCache = QuoteCache.getInstance();
            
            if ((buySystemRule!="") && (sellSystemRule!="")) {
                this.buySystemRule = buySystemRule;
                this.sellSystemRule = sellSystemRule;
            }

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

    // Users shouldn't instantiate this class
    private PaperTrade() {
        // nothing to do
    }

    /**
     * Attempt to sell the given stock holding. If we could not meet the trade
     * cost, the stock will not be sold.
     *
     * @param environment the paper trade environment
     * @param stockHolding the stock holding to sell
     * @param tradeCost the cost of a trade
     * @param day date of trade
     */
    private static void sell(Environment environment,
                             Variables variables,
                             StockHolding stockHolding,
                             Money tradeCost,
                             int day)
	throws EvaluationException, MissingQuoteException {

	// Make sure we have enough money for the trade
	if(environment.cashAccount.getValue().isGreaterThanEqual(tradeCost)) {

	    // Get the number of shares we own - we will sell all of them
	    int shares = stockHolding.getShares();
            Symbol symbol = stockHolding.getSymbol();

            // How much are they worth? We sell at the user defined price.
            Expression sellSystemRuleExpression = ExpressionFactory.newExpression(environment.sellSystemRule);
            double sellPrice = sellSystemRuleExpression.evaluate(variables, environment.quoteBundle, symbol, day);
            // If the wished price is lower than the maximum of the day,
            // your stocks will be sold.
            // It simulates an order of selling at fixed price (sellPrice).
            if (sellPrice<environment.quoteBundle.getQuote(symbol, Quote.DAY_HIGH, day)) {
                Money amount =
                    new Money(shares * sellPrice);
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
    }

    /**
     * Attempt to buy the given symbol.
     *
     * @param environment the paper trade environment
     * @param symbol the stock to buy
     * @param amount the amount to spend on the stock
     * @param tradeCost the cost of a trade (not including the stock price)
     * @param day date of trade
     * @return <code>true</code> if we had enough money to acquire the stock.
     */
    private static boolean buy(Environment environment,
                               Variables variables,
                               Symbol symbol,
                               Money amount,				
                               Money tradeCost,
                               int day)
	throws EvaluationException, MissingQuoteException {

        Expression buySystemRuleExpression = ExpressionFactory.newExpression(environment.buySystemRule);
        double buyPrice = buySystemRuleExpression.evaluate(variables, environment.quoteBundle, symbol, day);
        // If the wished price is greater than the minimum of the day,
        // your stocks will be bought.
        // It simulates an order of buying at fixed price (buyPrice).
        if (buyPrice>environment.quoteBundle.getQuote(symbol, Quote.DAY_LOW, day)) {
	
            // Calculate maximum number of shares we can buy with the given amount
            double sharePrice = buyPrice;
            int shares = (int)Math.floor(amount.doubleValue() / sharePrice);

            if(shares > 0) {
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
                return true;
            }
        }

        return false;
    }

    /**
     * Iterate through our stock holdings on the given date and decide
     * whether to sell any stock.
     *
     * @param environment the paper trade environment
     * @param quoteBundle the historical quote data
     * @param variables any Gondola variables set
     * @param sell the sell indicator
     * @param dateOffset date to examine
     * @param tradeCost the cost of a trade
     * @param symbols ordered list of symbols on that date
     * @param orderCache cache of ordered symbols
     */
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
                    sell(environment, variables, stockHolding, tradeCost, dateOffset + 1);
            }
            catch(MissingQuoteException e) {
                // ignore and move on
            }
            catch(EvaluationException e) {
                // Ignore and move on
            }
        }
    }

    /**
     * Iterate through all the stocks on the market on the given date and
     * decide whether to buy any stock.
     *
     * @param environment the paper trade environment
     * @param quoteBundle the historical quote data
     * @param variables any Gondola variables set
     * @param buy the buy indicator
     * @param dateOffset date to examine
     * @param tradeCost the cost of a trade
     * @param symbols ordered list of symbols on that date
     * @param orderCache cache of ordered symbols
     * @param stockValue amount of money to spend on stock
     */
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

                            // Did we have enough money to buy at least one share?
                            if(buy(environment, variables, symbol, stockValue,  tradeCost,
                                   dateOffset + 1)) {

                                // If there is no more money left, don't even look at the
                                // other stocks
                                cash = environment.cashAccount.getValue();

                                if(stockValue.add(tradeCost.multiply(2)).isGreaterThan(cash))
                                    break;
                            }
                        }
                    }
                    catch(MissingQuoteException e) {
                        // Ignore and move on
                    }
                    catch(EvaluationException e) {
                        // Ignore and move on
                    }
                }

                order++;
            }
        }
    }

    /**
     * Return the number of days we have held the given stock.
     *
     * @param environment the paper trade environment
     * @param stockHolding to query
     * @param dateOffset current date
     */
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

    /**
     * Perform paper trading using a fixed stock value. This method will try to keep
     * the value of each stock holding equal to <code>stockValue</code>.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param buy the buy indicator
     * @param sell the sell indicator
     * @param capital initial capital in the portfolio
     * @param stockValue the rough value of each stock holding
     * @param tradeCost the cost of a trade
     * @return the portfolio at the close of the last day's trade
     */
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
				       Money tradeCost,
                                       String buySystemRule,
                                       String sellSystemRule)
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             buySystemRule,
                                                             sellSystemRule);
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

    /**
     * Perform paper trading keeping the number of stocks in the portfolio roughly constant.
     * This method will try to keep the number of stocks in the portfolio roughly equal
     * to <code>numberStocks</code>, and will try to have all of them at roughly the same value.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param buy the buy indicator
     * @param sell the sell indicator
     * @param capital initial capital in the portfolio
     * @param numberStocks try to keep this number of stocks in the portfolio
     * @param tradeCost the cost of a trade
     * @return the portfolio at the close of the last day's trade
     */
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
				       Money tradeCost,
                                       String buySystemRule,
                                       String sellSystemRule)
        throws EvaluationException {

        // Set up environment for paper trading
        PaperTrade paperTrade = new PaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             buySystemRule,
                                                             sellSystemRule);
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
