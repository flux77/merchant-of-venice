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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.mov.analyser.ann.ArtificialNeuralNetwork;
import org.mov.util.Locale;
import org.mov.util.Money;
import org.mov.util.TradingDate;
import org.mov.parser.EvaluationException;
import org.mov.parser.Expression;
import org.mov.parser.ExpressionFactory;
import org.mov.parser.Variable;
import org.mov.parser.Variables;
import org.mov.portfolio.Portfolio;
import org.mov.portfolio.StockHolding;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.Quote;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.Symbol;
import org.mov.ui.ProgressDialog;


/**
 * This class perform the paper trade analysis for the artificial neural network.
 * A specific class has been developed, extended from PaperTrade {@link PaperTrade},
 * because ANNs have a complete different behaviour compared to other analysis based
 * on Gondola language.
 * ANNs need to be trained, and the training session needs to know how the things would
 * be happened, if different choices have been taken day by day.
 * For further information about the techniques used, you should find out the Cross Target
 * technique. That is the technique used here to get the buy/sell signals.
 * {@link http://web.econ.unito.it/terna/ct-era/ct-era.html}
 *
 * <p>The final portfolio will contain a single cash and a single share account.
 *
 * Cross Target method to get buy and sell signal through an ANN.
 * The cross target method works in the following way:
 * we make some guesses about buy and sell signals (actions) and
 * about capital (effect of actions),
 * the guesses are done by artificial neural network (ANN);
 * then we train the ANN comparing what the ANN has guessed with the following values:
 * the buy and sell signals are compared with the buy and sell signals which would be
 * to get a capital equal to the capital guessed plus the percental increment wished;
 * the capital signal is compared with the capital got trading
 * with the guessed buy and sell signals.
 *
 * For the sake of simplicity in Merchant of Venice we've used a simplified version
 * of CT technique.
 * We do not use the capital as output of ANN, but we use only two outputs (the buy
 * and sell signals).
 * We guess the capital according to four possible states:
 * 1) We don't buy and don't sell having the stock in portfolio
 * 2) We don't buy and don't sell not having the stock in portfolio
 * 3) We buy but don't sell
 * 4) We don't buy but sell
 *
 * The core of the CT method has done in the setANNTrainingParameters method in this class.
 *
 * @author Alberto Nacher
 */
public class ANNPaperTrade extends PaperTrade {
    
    /*
     * input and output arrays used to train the ANN
    */
    private static double[][] ANNInputArray;
    private static double[][] ANNOutputDesiredArray;

    // Users shouldn't instantiate this class
    private ANNPaperTrade() {
        // nothing to do
    }


    /**
     * Iterate through our stock holdings on the given date and decide
     * whether to sell any stock.
     *
     * @param environment the paper trade environment
     * @param quoteBundle the historical quote data
     * @param variables any Gondola variables set
     * @param dateOffset date to examine
     * @param tradeCost the cost of a trade
     * @param symbols ordered list of symbols on that date
     * @param orderCache cache of ordered symbols
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     */
    private static void sellTrades(Environment environment,
                                    EODQuoteBundle quoteBundle,
                                    Variables variables,
                                    int dateOffset,
                                    Money tradeCost,
                                    List symbols,
                                    OrderCache orderCache,
                                    Expression[] inputExpressions,
                                    ArtificialNeuralNetwork artificialNeuralNetwork)
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
            variables.setValue("stockcapital",
                    getStockCapital(environment, stockHolding, symbol, dateOffset));

            try {
                // Generate the input array of doubles according to the input expressions
                double[] inputDoubles = getANNInput(inputExpressions,
                        variables, quoteBundle, symbol, dateOffset);
                
                // calculate the price wanted by user trade value expression
                // to sell the stock (tradeValueWanted).
                // If trade value expression is 'open', then
                // set tradeValueWanted = 0 and sell at open price.
                double tradeValueWanted = 0;
                if(!environment.tradeValueSell.equals("open")) {
                    Expression tradeValueSellExpression =
                            ExpressionFactory.newExpression(environment.tradeValueSell);
                    tradeValueWanted =
                            tradeValueSellExpression.evaluate(variables,
                            environment.quoteBundle, symbol, dateOffset);
                }

                // If you want to buy the stock, do not sell it.
                boolean[] buySell = artificialNeuralNetwork.run(inputDoubles);
                if(!(buySell[artificialNeuralNetwork.OUTPUT_BUY])) {
                    if(buySell[artificialNeuralNetwork.OUTPUT_SELL]) {
                        
                        // Did we have enough money to buy at least one share?
                        // Will the stock reach the price wanted (tradeValueWanted)?
                        sell(environment, variables, stockHolding,
                                tradeCost, tradeValueWanted, dateOffset + 1);
                    }
                }
            }
            catch(MissingQuoteException e) {
                // ignore and move on
            }
            //catch(EvaluationException e) {
                // Ignore and move on
            //}
        }
    }

    /**
     * Iterate through all the stocks on the market on the given date and
     * decide whether to buy any stock.
     *
     * @param environment the paper trade environment
     * @param quoteBundle the historical quote data
     * @param variables any Gondola variables set
     * @param dateOffset date to examine
     * @param tradeCost the cost of a trade
     * @param symbols ordered list of symbols on that date
     * @param orderCache cache of ordered symbols
     * @param stockValue amount of money to spend on stock
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     */
    private static void buyTrades(Environment environment,
                                    EODQuoteBundle quoteBundle,
                                    Variables variables,
                                    int dateOffset,
                                    Money tradeCost,
                                    List symbols,
                                    OrderCache orderCache,
                                    Money stockValue,
                                    Expression[] inputExpressions,
                                    ArtificialNeuralNetwork artificialNeuralNetwork)
        throws EvaluationException {

        variables.setValue("held", 0);
        variables.setValue("stockcapital", 0);

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

                    // calculate the price wanted by user trade value expression
                    // to buy the stock (tradeValueWanted).
                    // If trade value expression is 'open', then
                    // set tradeValueWanted = 0 and buy at open price.
                    double tradeValueWanted = 0;
                    if(!environment.tradeValueBuy.equals("open")) {
                        Expression tradeValueBuyExpression =
                                ExpressionFactory.newExpression(environment.tradeValueBuy);
                        tradeValueWanted =
                                tradeValueBuyExpression.evaluate(variables,
                                environment.quoteBundle, symbol, dateOffset);
                    }
                            
                    try {
                        // Generate the input array of doubles according to the input expressions
                        double[] inputDoubles = getANNInput(inputExpressions,
                                variables, quoteBundle, symbol, dateOffset);
                
                        boolean[] buy = artificialNeuralNetwork.run(inputDoubles);
                        if(buy[artificialNeuralNetwork.OUTPUT_BUY]) {

                            // Did we have enough money to buy at least one share?
                            // Will the stock reach the price wanted (tradeValueWanted)?
                            if(buy(environment, variables, symbol, stockValue,  tradeCost,
                                   tradeValueWanted, dateOffset + 1)) {
                                
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
                    //catch(EvaluationException e) {
                        // Ignore and move on
                    //}
                }

                order++;
            }
        }
    }

    /**
     * Return the input for the ANN.
     *
     * @param inputExpressions expressions in input for the ANN
     * @param variables any Gondola variables set
     * @param quoteBundle historical quote data
     * @param symbol current stock symbol to be processed
     * @param dateOffset current date
     *
     * @return the input for the ANN
     */
    private static double[] getANNInput(Expression[] inputExpressions, Variables variables,
            EODQuoteBundle quoteBundle, Symbol symbol, int dateOffset)
        throws EvaluationException {
        
        // Generate the input array of doubles according to the input expressions
        double[] inputDoubles = new double[inputExpressions.length];
        for (int ii=0; ii<inputDoubles.length; ii++) {
            inputDoubles[ii] = inputExpressions[ii].evaluate(variables,
                    quoteBundle, symbol, dateOffset);
        }
        
        return inputDoubles;
    }

    /**
     * Perform paper trading using a fixed stock value.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param capital initial capital in the portfolio
     * @param stockValue the rough value of each stock holding
     * @param tradeCost the cost of a trade
     * @param tradeValueBuy the value at which we want to buy
     * @param tradeValueSell the value at which we want to sell
     * @param progress the progress bar shown while ANN is running
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     *
     * @return the portfolio at the close of the last day's trade
     */
    public static Portfolio paperTrade(String portfolioName,
                EODQuoteBundle quoteBundle,
                Variables variables,
                OrderCache orderCache,
                TradingDate startDate,
                TradingDate endDate,
                Money capital,
                Money stockValue,
                Money tradeCost,
                String tradeValueBuy,
                String tradeValueSell,
                ProgressDialog progress,
                Expression[] inputExpressions,
                ArtificialNeuralNetwork artificialNeuralNetwork)
        throws EvaluationException {

        // Set up environment for paper trading
        ANNPaperTrade paperTrade = new ANNPaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             tradeValueBuy,
                                                             tradeValueSell);
        int dateOffset = environment.startDateOffset;

        // Paper Trading variables
        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("daysfromstart"))
            variables.add("daysfromstart", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("transactions"))
            variables.add("transactions", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("capital"))
            variables.add("capital", Expression.FLOAT_TYPE, Variable.CONSTANT);
        if(!variables.contains("stockcapital"))
            variables.add("stockcapital", Expression.FLOAT_TYPE, Variable.CONSTANT);

        // daysfromstart
        int daysRest = (int)(-1) * dateOffset;

        int timeLength = Math.abs(dateOffset) - Math.abs(environment.endDateOffset);
        progress.setMaximum(timeLength);
                
        // Now iterate through each trading date and decide whether
        // to buy/sell. The last date is used for placing the previous
        // date's buy/sell orders.
        while(dateOffset < environment.endDateOffset) {
            
            // Running the ANN means we might need to load in
            // more quotes so the note may have changed...
            progress.setNote(Locale.getString("RUNNING"));
            progress.increment();
            
            // Set the value of days elapsed from the begin of the Paper Trade process
            variables.setValue("daysfromstart", daysRest + dateOffset);

            // Set the value of the number of transactions done until now
            variables.setValue("transactions", environment.portfolio.countTransactions());

            // Set the value of actual capital
            variables.setValue("capital", getCapital(environment.portfolio,
                    environment.quoteBundle, dateOffset));

            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);
            
            sellTrades(environment, quoteBundle, variables, dateOffset, tradeCost,
                    symbols, orderCache,
                    inputExpressions, artificialNeuralNetwork);
            buyTrades(environment, quoteBundle, variables, dateOffset, tradeCost,
                    symbols, orderCache, stockValue,
                    inputExpressions, artificialNeuralNetwork);
            
            dateOffset++;
        }

        // Set the tip for the next day
        setTip(environment, quoteBundle, variables, dateOffset, tradeCost,
                orderCache.getTodaySymbols(dateOffset), orderCache,
                inputExpressions, artificialNeuralNetwork);
        
        return environment.portfolio;
    }

    /**
     * Perform training using a fixed stock value.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param capital initial capital in the portfolio
     * @param stockValue the rough value of each stock holding
     * @param tradeCost the cost of a trade
     * @param tradeValueBuy the value at which we want to buy
     * @param tradeValueSell the value at which we want to sell
     * @param progress the progress bar shown while ANN is running
     * @param ANNTrainingPage the pointer to the training page
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     */
    public static void paperTraining(String portfolioName,
                EODQuoteBundle quoteBundle,
                Variables variables,
                OrderCache orderCache,
                TradingDate startDate,
                TradingDate endDate,
                Money capital,
                Money stockValue,
                Money tradeCost,
                String tradeValueBuy,
                String tradeValueSell,
                ProgressDialog progress,
                ANNTrainingPage ANNTrainingPage,
                Expression[] inputExpressions,
                ArtificialNeuralNetwork artificialNeuralNetwork)
        throws EvaluationException {

        // Total cycles to train ANN
        int totCycles = ANNTrainingPage.getTotCycles();
        progress.setMaximum(totCycles);
        
        // Set up environment for paper trading
        ANNPaperTrade paperTrade = new ANNPaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             tradeValueBuy,
                                                             tradeValueSell);
        int dateOffset = environment.startDateOffset;

        // ANN training arrays
        int dateOffsetToGetANNArrayLength = dateOffset;
        int ANNArrayLength = 0;
        int ANNArrayPointer = 0;
        while(dateOffsetToGetANNArrayLength < environment.endDateOffset) {
            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffsetToGetANNArrayLength);
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();
                ANNArrayLength++;
            }
            dateOffsetToGetANNArrayLength++;
        }
        ANNInputArray =
                new double[ANNArrayLength][inputExpressions.length];
        ANNOutputDesiredArray =
                new double[ANNArrayLength][artificialNeuralNetwork.OUTPUT_NEURONS];

        // Paper Trading variables
        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("daysfromstart"))
            variables.add("daysfromstart", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("transactions"))
            variables.add("transactions", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("capital"))
            variables.add("capital", Expression.FLOAT_TYPE, Variable.CONSTANT);
        if(!variables.contains("stockcapital"))
            variables.add("stockcapital", Expression.FLOAT_TYPE, Variable.CONSTANT);

        // daysfromstart
        int daysRest = (int)(-1) * dateOffset;

        // Now iterate through each trading date and decide whether
        // to buy/sell. The last date is used for placing the previous
        // date's buy/sell orders.
        while(dateOffset < environment.endDateOffset) {

            // Set the value of days elapsed from the begin of the Paper Trade process
            variables.setValue("daysfromstart", daysRest + dateOffset);

            // Set the value of the number of transactions done until now
            variables.setValue("transactions", environment.portfolio.countTransactions());

            // Set the value of actual capital
            variables.setValue("capital", getCapital(environment.portfolio,
                    environment.quoteBundle, dateOffset));

            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);

            // Calculate the input for ANN training
            // Iterate through stocks available today - should we buy or sell any of it?
            Hashtable hashtable = new Hashtable();
            
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();
                /* 
                 * Set ANNInputArray and ANNOutputDesiredArray arrays
                 * row by row.
                 * Each call to setANNTrainingParameters method makes
                 * an assignment to a single row.
                 */
                boolean stockHeld = false;
                stockHeld =
                        (hashtable.get(symbol.get()) == null) ?
                            false :
                            (new Boolean((Boolean)hashtable.get(symbol.get()))).booleanValue();
                boolean newStockValue =
                        setANNTrainingParameters(ANNInputArray[ANNArrayPointer],
                        ANNOutputDesiredArray[ANNArrayPointer],
                        stockHeld,
                        environment, quoteBundle, variables, dateOffset, tradeCost,
                        symbol, orderCache, stockValue,
                        inputExpressions, artificialNeuralNetwork,
                        ANNTrainingPage.getEarningPercentage());
                hashtable.put(symbol.get(),
                        (Boolean)new Boolean(newStockValue));
                ANNArrayPointer++;
            }

            dateOffset++;
        }

        // Train the ANN
        artificialNeuralNetwork.runTraining(ANNInputArray, ANNOutputDesiredArray,
                ANNTrainingPage.getLearningRate(), 
                ANNTrainingPage.getMomentum(), ANNTrainingPage.getPreLearning(), 
                ANNTrainingPage.getTotCycles(), ANNArrayPointer);
    }

    /**
     * Perform training using a fix number of stocks.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param capital initial capital in the portfolio
     * @param numberStocks try to keep this number of stocks in the portfolio
     * @param tradeCost the cost of a trade
     * @param tradeValueBuy the value at which we want to buy
     * @param tradeValueSell the value at which we want to sell
     * @param progress the progress bar shown while ANN is running
     * @param ANNTrainingPage the pointer to the training page
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     */
    public static void paperTraining(String portfolioName,
                EODQuoteBundle quoteBundle,
                Variables variables,
                OrderCache orderCache,
                TradingDate startDate,
                TradingDate endDate,
                Money capital,
                int numberStocks,
                Money tradeCost,
                String tradeValueBuy,
                String tradeValueSell,
                ProgressDialog progress,
                ANNTrainingPage ANNTrainingPage,
                Expression[] inputExpressions,
                ArtificialNeuralNetwork artificialNeuralNetwork)
        throws EvaluationException {

        // Total cycles to train ANN
        int totCycles = ANNTrainingPage.getTotCycles();
        progress.setMaximum(totCycles);
                    
        // Set up environment for paper trading
        ANNPaperTrade paperTrade = new ANNPaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             tradeValueBuy,
                                                             tradeValueSell);
        int dateOffset = environment.startDateOffset;

        // ANN training arrays
        int dateOffsetToGetANNArrayLength = dateOffset;
        int ANNArrayLength = 0;
        int ANNArrayPointer = 0;
        while(dateOffsetToGetANNArrayLength < environment.endDateOffset) {
            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffsetToGetANNArrayLength);
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();
                ANNArrayLength++;
            }
            dateOffsetToGetANNArrayLength++;
        }
        ANNInputArray = new double[ANNArrayLength][inputExpressions.length];
        ANNOutputDesiredArray = new double[ANNArrayLength][artificialNeuralNetwork.OUTPUT_NEURONS];

        // Paper Trading variables
        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("daysfromstart"))
            variables.add("daysfromstart", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("transactions"))
            variables.add("transactions", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("capital"))
            variables.add("capital", Expression.FLOAT_TYPE, Variable.CONSTANT);
        if(!variables.contains("stockcapital"))
            variables.add("stockcapital", Expression.FLOAT_TYPE, Variable.CONSTANT);

        // daysfromstart
        int daysRest = (int)(-1) * dateOffset;

        // Now iterate through each trading date and decide whether
        // to buy/sell. The last date is used for placing the previous
        // date's buy/sell orders.
        while(dateOffset < environment.endDateOffset) {

            // Set the value of days elapsed from the begin of the Paper Trade process
            variables.setValue("daysfromstart", daysRest + dateOffset);

            // Set the value of the number of transactions done until now
            variables.setValue("transactions", environment.portfolio.countTransactions());

            // Set the value of actual capital
            variables.setValue("capital", getCapital(environment.portfolio,
                    environment.quoteBundle, dateOffset));

            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);

            // Calculate the input for ANN training
            Money stockValue = new Money(0);
            try {
                // stockValue = (portfolio / numberStocks) - (2 * tradeCost)
                Money portfolioValue = environment.portfolio.getValue(quoteBundle, dateOffset);
                stockValue =
                    portfolioValue.divide(numberStocks).subtract(tradeCost.multiply(2));
            }
            catch(MissingQuoteException e) {
                // Ignore and move on
            }
            // Iterate through stocks available today - should we buy or sell any of it?
            Hashtable hashtable = new Hashtable();
            
            for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
                Symbol symbol = (Symbol)iterator.next();
                /* 
                 * Set ANNInputArray and ANNOutputDesiredArray arrays
                 * row by row.
                 * Each call to setANNTrainingParameters method makes an assignment to
                 * a single row.
                 */
                boolean stockHeld = false;
                stockHeld =
                        (hashtable.get(symbol.get()) == null) ?
                            false :
                            (new Boolean((Boolean)hashtable.get(symbol.get()))).booleanValue();
                boolean newStockValue =
                        setANNTrainingParameters(ANNInputArray[ANNArrayPointer],
                        ANNOutputDesiredArray[ANNArrayPointer],
                        stockHeld,
                        environment, quoteBundle, variables, dateOffset, tradeCost,
                        symbol, orderCache, stockValue,
                        inputExpressions, artificialNeuralNetwork,
                        ANNTrainingPage.getEarningPercentage());
                hashtable.put(symbol.get(),
                        (Boolean)new Boolean(newStockValue));
                ANNArrayPointer++;
            }

            dateOffset++;
        }

        // Train the ANN
        artificialNeuralNetwork.runTraining(ANNInputArray, ANNOutputDesiredArray,
                ANNTrainingPage.getLearningRate(), 
                ANNTrainingPage.getMomentum(), ANNTrainingPage.getPreLearning(), 
                ANNTrainingPage.getTotCycles(), ANNArrayPointer);
    }

    /**
     * Perform paper trading using a fix number of stocks.
     *
     * @param portfolioName name to call portfolio
     * @param quoteBundle historical quote data
     * @param variables any Gondola variables set
     * @param orderCache cache of ordered symbols
     * @param startDate start date of trading
     * @param endDate last date of trading
     * @param capital initial capital in the portfolio
     * @param numberStocks try to keep this number of stocks in the portfolio
     * @param tradeCost the cost of a trade
     * @param tradeValueBuy the value at which we want to buy
     * @param tradeValueSell the value at which we want to sell
     * @param progress the progress bar shown while ANN is running
     * @param inputExpressions the input expressions of ANN
     * @param artificialNeuralNetwork the ANN object
     *
     * @return the portfolio at the close of the last day's trade
     */
    public static Portfolio paperTrade(String portfolioName,
                EODQuoteBundle quoteBundle,
                Variables variables,
                OrderCache orderCache,
                TradingDate startDate,
                TradingDate endDate,
                Money capital,
                int numberStocks,
                Money tradeCost,
                String tradeValueBuy,
                String tradeValueSell,
                ProgressDialog progress,
                Expression[] inputExpressions,
                ArtificialNeuralNetwork artificialNeuralNetwork)
        throws EvaluationException {

        // Set up environment for paper trading
        ANNPaperTrade paperTrade = new ANNPaperTrade();
        Environment environment = paperTrade.new Environment(quoteBundle,
                                                             portfolioName,
                                                             startDate,
                                                             endDate,
                                                             capital,
                                                             tradeValueBuy,
                                                             tradeValueSell);
        int dateOffset = environment.startDateOffset;

        // Paper Trading variables
        if(orderCache.isOrdered() && !variables.contains("order"))
            variables.add("order", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("held"))
            variables.add("held", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("daysfromstart"))
            variables.add("daysfromstart", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("transactions"))
            variables.add("transactions", Expression.INTEGER_TYPE, Variable.CONSTANT);
        if(!variables.contains("capital"))
            variables.add("capital", Expression.FLOAT_TYPE, Variable.CONSTANT);
        if(!variables.contains("stockcapital"))
            variables.add("stockcapital", Expression.FLOAT_TYPE, Variable.CONSTANT);

        // daysfromstart
        int daysRest = (int)(-1) * dateOffset;
        
        int timeLength = Math.abs(dateOffset) - Math.abs(environment.endDateOffset);
        progress.setMaximum(timeLength);
                
        // Now iterate through each trading date and decide whether
        // to buy/sell. The last date is used for placing the previous
        // date's buy/sell orders.
        while(dateOffset < environment.endDateOffset) {
            
            // Running the ANN means we might need to load in
            // more quotes so the note may have changed...
            progress.setNote(Locale.getString("RUNNING"));
            progress.increment();

            // Set the value of days elapsed from the begin of the Paper Trade process
            variables.setValue("daysfromstart", daysRest + dateOffset);
            
            // Set the value of the number of transactions done until now
            variables.setValue("transactions", environment.portfolio.countTransactions());
            
            // Set the value of actual capital
            variables.setValue("capital", getCapital(environment.portfolio,
                    environment.quoteBundle, dateOffset));
            
            // Get all the (ordered) symbols that we can trade for today and
            // that we have quotes for.
            List symbols = orderCache.getTodaySymbols(dateOffset);

            sellTrades(environment, quoteBundle, variables, dateOffset, tradeCost,
                        symbols, orderCache,
                        inputExpressions, artificialNeuralNetwork);
            try {
                // stockValue = (portfolio / numberStocks) - (2 * tradeCost)
                Money portfolioValue = environment.portfolio.getValue(quoteBundle, dateOffset);
                Money stockValue =
                    portfolioValue.divide(numberStocks).subtract(tradeCost.multiply(2));
                buyTrades(environment, quoteBundle, variables, dateOffset, tradeCost,
                        symbols, orderCache, stockValue,
                        inputExpressions, artificialNeuralNetwork);
            }
            catch(MissingQuoteException e) {
                // Ignore and move on
            }

            dateOffset++;
        }

        // Set the tip for the next day
        setTip(environment, quoteBundle, variables, dateOffset, tradeCost,
                orderCache.getTodaySymbols(dateOffset), orderCache,
                inputExpressions, artificialNeuralNetwork);
        
        return environment.portfolio;
    }

    /**
     * Set the information for the tip of the next day.
     */
    private static void setTip(Environment environment,
                            EODQuoteBundle quoteBundle,
                            Variables variables,
                            int dateOffset,
                            Money tradeCost,
                            List symbols,
                            OrderCache orderCache,
                            Expression[] inputExpressions,
                            ArtificialNeuralNetwork artificialNeuralNetwork) {
                                      
        symbolStock = new String[symbols.size()];
        buyRule = new boolean[symbols.size()];
        sellRule = new boolean[symbols.size()];
        buyValue = new double[symbols.size()];
        sellValue = new double[symbols.size()];
        
        setSellTip(environment, quoteBundle, variables, dateOffset,
                    tradeCost, symbols, orderCache,
                    inputExpressions, artificialNeuralNetwork);
        
        setBuyTip(environment, quoteBundle, variables, dateOffset,
                    tradeCost, symbols, orderCache,
                    inputExpressions, artificialNeuralNetwork);
        
    }

    private static void setSellTip(Environment environment,
                                    EODQuoteBundle quoteBundle,
                                    Variables variables,
                                    int dateOffset,
                                    Money tradeCost,
                                    List symbols,
                                    OrderCache orderCache,
                                    Expression[] inputExpressions,
                                    ArtificialNeuralNetwork artificialNeuralNetwork) {
        
        // Count the sell tip for the next day
                                      
        int order = 0;

        int index = 0;
        
        // Iterate through stocks available today - should we sell any of it?
        for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();

            // If we care about the order, make sure the "order" variable is set.
            if(orderCache.isOrdered()) {
                order = symbols.indexOf(symbol);

                // It's possible that we don't have a quote for the symbol today.
                // So skip it.
                if(order == -1)
                    continue;
                variables.setValue("order", order);
            }

            // Check if the stock is hold, so that held variable is set.
            List stockHoldings = new ArrayList(environment.shareAccount.getStockHoldings().values());
            for(Iterator iteratorHolding = stockHoldings.iterator(); iteratorHolding.hasNext();) {
                StockHolding stockHolding = (StockHolding)iteratorHolding.next();
                Symbol symbolHolding = stockHolding.getSymbol();
                if (symbolHolding.toString().equals(symbol.toString())) {
                    variables.setValue("held",
                            getHoldingTime(environment, stockHolding, dateOffset));
                    variables.setValue("stockcapital",
                            getStockCapital(environment, stockHolding, symbol, dateOffset));
                    break;
                } else {
                    variables.setValue("held", 0);
                    variables.setValue("stockcapital", 0.0D);
                }
            }

            try {
                // Generate the input array of doubles according to the input expressions
                double[] inputDoubles = getANNInput(inputExpressions,
                        variables, quoteBundle, symbol, dateOffset);
                
                // Get if the stock must be sold
                boolean[] sell = artificialNeuralNetwork.run(inputDoubles);
                sellRule[index] = sell[artificialNeuralNetwork.OUTPUT_SELL];
                        
                // calculate the price wanted by user trade value expression
                // to sell the stock (tradeValueWanted).
                // If trade value expression is 'open', then
                // set the price to zero (sell at open price).
                sellValue[index] = 0;
                if(!environment.tradeValueSell.equals("open")) {
                    Expression tradeValueSellExpression =
                            ExpressionFactory.newExpression(environment.tradeValueSell);
                    sellValue[index] =
                            tradeValueSellExpression.evaluate(variables,
                            environment.quoteBundle, symbol, dateOffset);
                }
            }
            catch(EvaluationException e) {
                // do nothing
            }
            finally {
                index++;
            }
        }
    }
    
    private static void setBuyTip(Environment environment,
                                EODQuoteBundle quoteBundle,
                                Variables variables,
                                int dateOffset,
                                Money tradeCost,
                                List symbols,
                                OrderCache orderCache,
                                Expression[] inputExpressions,
                                ArtificialNeuralNetwork artificialNeuralNetwork) {
                                      
        // Count the buy tip for the next day
        variables.setValue("held", 0);
        
        variables.setValue("stockcapital", 0.0D);

        int order = 0;
        
        int index = 0;
        
        // Iterate through stocks available today
        for(Iterator iterator = symbols.iterator(); iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();

            symbolStock[index] = new String(symbol.get());
            
            // If we care about the order, make sure the "order" variable is set
            if(orderCache.isOrdered())
                variables.setValue("order", order);

            try {
                // Generate the input array of doubles according to the input expressions
                double[] inputDoubles = getANNInput(inputExpressions,
                        variables, quoteBundle, symbol, dateOffset);
                
                // Get if the stock must be bought
                boolean[] buy = artificialNeuralNetwork.run(inputDoubles);
                buyRule[index] = buy[artificialNeuralNetwork.OUTPUT_BUY];
                
                // If you own the stock and both sell and buy rule fire,
                // you wouldn't sell it, neither would you buy it.
                // So it is necessary set the buyRule and sellRule to false.
                //if(environment.shareAccount.isHolding(symbol) && sellRule[index] &&
                //  buyRule[index]) {
                //    sellRule[index] = false;
                //    buyRule[index] = false;
                //}

                // calculate the price wanted by user trade value expression
                // to buy the stock (tradeValueWanted).
                // If trade value expression is 'open', then
                // set this price to zero (buy at open price).
                buyValue[index] = 0;
                if(!environment.tradeValueBuy.equals("open")) {
                    Expression tradeValueBuyExpression =
                            ExpressionFactory.newExpression(environment.tradeValueBuy);
                    buyValue[index] =
                            tradeValueBuyExpression.evaluate(variables,
                            environment.quoteBundle, symbol, dateOffset);
                }
            }
            catch(EvaluationException e) {
                // do nothing
            }
            finally {
                index++;
            }

            order++;
        }
    }
    
    /*
     * This is the core method which manages the Cross Target technique.
     * All the CT method is described at the beginning of this class.
     */
    private static boolean setANNTrainingParameters(double[] ANNInputArrayRow,
            double[] ANNOutputDesiredArrayRow,
            boolean stockHeld,
            Environment environment,
            EODQuoteBundle quoteBundle,
            Variables variables,
            int dateOffset,
            Money tradeCost,
            Symbol symbol,
            OrderCache orderCache,
            Money stockValue,
            Expression[] inputExpressions,
            ArtificialNeuralNetwork artificialNeuralNetwork,
            double earningPercentage) {
        
        // The return value that says if a stock is held at the end of this trading day
        boolean retValue = false;
        
        /* Input parameters for the ANN */
        double[] inputDoubles = new double[inputExpressions.length];
        try {
            inputDoubles = getANNInput(inputExpressions,
                    variables, quoteBundle, symbol, dateOffset);
            for (int ii=0; ii<inputDoubles.length; ii++) {
                ANNInputArrayRow[ii] = inputDoubles[ii];
            }
        }
        catch(EvaluationException e) {
            // Ignore and move on
        }
        
        
        /* Output parameters for the ANN */
        
        /* 
         * Inizialize the hypothetic capitals according to all possible buy/sell signal.
         * The only signal that has not been taken into consideration is the buy and sell,
         * because if we want to buy, it is a nonsense selling it at the same time.
         */
        double capitalOld = 0.0D;
        double capitalNotBuyNotSell = 0.0D;
        double capitalNotBuyYesSell = 0.0D;
        double capitalYesBuyNotSell = 0.0D;
        
        /* Calculate the capitals */
        // Capital got from previous day trade
        capitalOld = getCapital(environment.portfolio, quoteBundle, dateOffset);
        
        /*
         * Maximum number of stocks in portfolio
         */
        int maxStocks = (int)Math.floor(capitalOld / stockValue.doubleValue());
        
        // NOT BUY AND NOT SELL
        double capitalDoNothing = 0;
        
        try {
            double openPrice = environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN,
                            dateOffset + 1);
            double closePrice = environment.quoteBundle.getQuote(symbol, Quote.DAY_CLOSE,
                            dateOffset + 1);
            int shares =
                    (int)Math.floor(stockValue.doubleValue() / Math.max(openPrice, closePrice));
            if (stockHeld) {
                // not buy and not sell having the stock in portfolio (stockHeld == true)
                capitalDoNothing = shares * (closePrice - openPrice);
            } else {
                // not buy and not sell not having the stock in portfolio (stockHeld == false)
                capitalDoNothing = 0;
            }
        }
        catch(MissingQuoteException e) {
            // Ignore and move on
        }
                
        // Capital got after this day trade, as if we have neither bought neither sold
        capitalNotBuyNotSell = capitalOld + capitalDoNothing;
        
        // BUY AND NOT SELL
        double capitalPortfolioBuy = 0;
        try {
            // calculate the price wanted by user trade value expression
            // to buy the stock (tradeValueWanted).
            // If trade value expression is 'open', then
            // set tradeValueWanted at open price.
            double tradeValueWanted = 0;
            if(!environment.tradeValueBuy.equals("open")) {
                Expression tradeValueBuyExpression = ExpressionFactory.newExpression(
                        environment.tradeValueBuy);
                tradeValueWanted = tradeValueBuyExpression.evaluate(variables,
                        environment.quoteBundle, symbol, dateOffset);
            } else {
                tradeValueWanted = environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN,
                        dateOffset + 1);
            }
            // If the wished price is greater than the minimum of the day,
            // your stocks will be bought.
            // It simulates an order of buying at fixed price (tradeValueWanted).
            if (tradeValueWanted>=environment.quoteBundle.getQuote(symbol, Quote.DAY_LOW,
                    dateOffset + 1)) {
                // Calculate maximum number of shares we can buy with the given amount
                double sharePrice = tradeValueWanted;
                int shares = (int)Math.floor(stockValue.doubleValue() / sharePrice);

                if(shares > 0) {
                    // Now calculate the change of capital.
                    // Buying we have to add what we earn from the close of dateOffset + 1
                    // and the buy time (@ tradeValueWanted price).
                    // We have also to subtract the tradeCost.
                    capitalPortfolioBuy =
                            shares * (
                            quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, dateOffset + 1)
                            - tradeValueWanted) -
                            tradeCost.doubleValue();
                }
            }
        }
        catch(MissingQuoteException e) {
            // Ignore and move on
        }
        catch(EvaluationException e) {
            // Ignore and move on
        }
        
        // Capital got after this day trade, as if we have bought but not sold
        capitalYesBuyNotSell = capitalOld + capitalPortfolioBuy;
        
        // NOT BUY AND SELL
        double capitalPortfolioSell = 0;
        try {
            // calculate the price wanted by user trade value expression
            // to buy the stock (tradeValueWanted).
            // If trade value expression is 'open', then
            // set tradeValueWanted at open price.
            double tradeValueWanted = 0;
            if(!environment.tradeValueSell.equals("open")) {
                Expression tradeValueSellExpression = ExpressionFactory.newExpression(
                        environment.tradeValueSell);
                tradeValueWanted = tradeValueSellExpression.evaluate(variables,
                        environment.quoteBundle, symbol, dateOffset);
            } else {
                tradeValueWanted = environment.quoteBundle.getQuote(symbol, Quote.DAY_OPEN,
                        dateOffset + 1);
            }
            // If the wished price is lower than the maximum of the day,
            // your stocks will be sold.
            // It simulates an order of selling at fixed price (tradeValueWanted).
            if (tradeValueWanted<=environment.quoteBundle.getQuote(symbol, Quote.DAY_HIGH,
                    dateOffset + 1)) {
                int shares = (int)Math.floor(stockValue.doubleValue() / tradeValueWanted);

                // Now calculate the change of capital.
                // Selling we have to add what we earn from the close of dateOffset
                // and the sell time (@ tradeValueWanted price).
                // We have also to subtract the tradeCost.
                capitalPortfolioSell =
                        shares * (
                        tradeValueWanted -
                        quoteBundle.getQuote(symbol, Quote.DAY_CLOSE, dateOffset)) -
                        tradeCost.doubleValue();
            }
        }
        catch(MissingQuoteException e) {
            // Ignore and move on
        }
        catch(EvaluationException e) {
            // Ignore and move on
        }
        
        // Capital got after this day trade, as if we haven't bought but sold
        capitalNotBuyYesSell = capitalOld + capitalPortfolioSell;
        
        
        /* 
         * Calculate the desired output according to Cross Target method.
         * We choose the best performance according to Cross Target parameters
         * among capital values calculated above.
         * The best is used to train the ANN.
         * The best is the nearest value to the earningPercentage,
         * recalculated according to the number of maximum stocks
         */
        // Generate the input array of doubles according to the input expressions
        double earningPercentageAll = earningPercentage / maxStocks;
        double maxEarn = Math.min(
                Math.abs((100.0D * (capitalNotBuyNotSell - capitalOld) / capitalOld) -
                earningPercentageAll),
                Math.abs((100.0D * (capitalNotBuyYesSell - capitalOld) / capitalOld) -
                earningPercentageAll));
        maxEarn = Math.min(maxEarn,
                Math.abs((100.0D * (capitalYesBuyNotSell - capitalOld) / capitalOld) -
                earningPercentageAll));
        
        
        // Set the desired outputs
        if (maxEarn == Math.abs((100.0D * (capitalNotBuyNotSell - capitalOld) / capitalOld) -
                earningPercentageAll)) {
            // NOT BUY AND NOT SELL
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_BUY] =
                    artificialNeuralNetwork.LOW_BOOL;
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_SELL] =
                    artificialNeuralNetwork.LOW_BOOL;
            // We countinue to have/not have the stock according to the previous day trade
            retValue = stockHeld;
        } else if (maxEarn == Math.abs((100.0D * (capitalNotBuyYesSell - capitalOld) / capitalOld) -
                earningPercentageAll)) {
            // NOT BUY AND SELL
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_BUY] =
                    artificialNeuralNetwork.LOW_BOOL;
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_SELL] =
                    artificialNeuralNetwork.HIGH_BOOL;
            // We sell, so we don't have the stock anymore.
            retValue = false;
        } else {
            // BUY AND NOT SELL
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_BUY] =
                    artificialNeuralNetwork.HIGH_BOOL;
            ANNOutputDesiredArrayRow[artificialNeuralNetwork.OUTPUT_SELL] =
                    artificialNeuralNetwork.LOW_BOOL;
            // We buy, so we have the stock
            retValue = true;
        }
        
        // return if stock has held this day trade
        return retValue;
    }
}
