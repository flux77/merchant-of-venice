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

package org.mov.analyser.gp;

import java.util.Random;

import org.mov.analyser.OrderCache;
import org.mov.analyser.PaperTrade;
import org.mov.parser.EvaluationException;
import org.mov.parser.Expression;
import org.mov.parser.TypeMismatchException;
import org.mov.parser.Variables;
import org.mov.portfolio.Portfolio;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.QuoteBundle;
import org.mov.util.Money;
import org.mov.util.TradingDate;

public class Individual implements Comparable {
    
    private Expression buyRule = null;
    private Expression sellRule = null;
    private Portfolio portfolio = null;
    private Money value = null;

    private final static int CLONE_PERCENT                     = 10;
    private final static int SWAP_PERCENT                      = 5;
    private final static int RECOMBINE_PERCENT                 = 35;
    private final static int SWAP_AND_RECOMBINE_PERCENT        = 40;
    private final static int DOUBLE_RECOMBINE_PERCENT          = 10;

    private final static int BREED_BY_CLONING                         = 0;
    private final static int BREED_BY_SWAPPING                        = 1;
    private final static int BREED_BY_RECOMBINING                     = 2;
    private final static int BREED_BY_SWAPPING_AND_RECOMBINING        = 3;
    private final static int BREED_BY_DOUBLE_RECOMBINING              = 4;

    private final static String PORTFOLIO_NAME = "Genetic Programme Portfolio";

    public Individual(Expression buyRule, Expression sellRule) {
        this.buyRule = buyRule;
        this.sellRule = sellRule;

        checkType();                
    }

    public Individual(Mutator buyRuleMutator, Mutator sellRuleMutator) {
        // By setting the level low we create bushier trees
        buyRule = buyRuleMutator.createRandomNonTerminal(Expression.BOOLEAN_TYPE, 0);
        sellRule = sellRuleMutator.createRandomNonTerminal(Expression.BOOLEAN_TYPE, 0);

        buyRule = buyRule.simplify();
        sellRule = sellRule.simplify();

        checkType();
    }

    public Individual(Random random, Mutator buyRuleMutator, Mutator sellRuleMutator, 
                      Individual father, Individual mother) {
        int breedType = getRandomBreedType(random);

        buyRule = (Expression)father.getBuyRule().clone();

        // SWAP
        {
            if(breedType == BREED_BY_SWAPPING || 
               breedType == BREED_BY_SWAPPING_AND_RECOMBINING)
                sellRule = (Expression)mother.getSellRule().clone();
            else
                sellRule = (Expression)father.getSellRule().clone();
        }

        // RECOMBINE
        {
            // Single
            if(breedType == BREED_BY_RECOMBINING ||
               breedType == BREED_BY_DOUBLE_RECOMBINING ||
               breedType == BREED_BY_SWAPPING_AND_RECOMBINING) {
                buyRule = recombine(buyRuleMutator, buyRule, mother.getBuyRule());

                // Double
                if(breedType == BREED_BY_DOUBLE_RECOMBINING)
                    sellRule = recombine(sellRuleMutator, sellRule, mother.getSellRule());
            }
        }

        // MUTATE
        {
            if(breedType == BREED_BY_CLONING) {
                // If it is a clone, at least one of the rules must mutate otherwise
                // we've created a duplicate individual which is a waste of processing
                // power.
                int randomNumber = random.nextInt(3);

                if(randomNumber == 0 || randomNumber == 2)
                    buyRule = buyRuleMutator.mutate(buyRule, 100);
                if(randomNumber == 1 || randomNumber == 2)
                    sellRule = sellRuleMutator.mutate(sellRule, 100);
            }
            else {
                buyRule = buyRuleMutator.mutate(buyRule);
                sellRule = sellRuleMutator.mutate(sellRule);
            }
        }

        sellRule = sellRule.simplify();
        buyRule = buyRule.simplify();
        checkType();        
    }

    // equation is valid only if it fits within the given size range AND
    // the buy rule references the quote data somehow! I.e. we reject
    // all equations - no matter how successful - that are only based
    // on the day of the week, order etc. Sell rule doesn't have to be
    // based on stock price.
    public boolean isValid(int min, int max) {
        int sellRuleSize = sellRule.size();
        int buyRuleSize = buyRule.size();

        return (sellRuleSize >= min && sellRuleSize <= max &&
                buyRuleSize >= min && buyRuleSize <= max &&
                (buyRule.size(Expression.FLOAT_QUOTE_TYPE) > 0 ||
                 buyRule.size(Expression.INTEGER_QUOTE_TYPE) > 0));
    }

    public int getTotalEquationSize() {
        return buyRule.size() + sellRule.size();
    }

    public Money paperTrade(GPQuoteBundle quoteBundle,
                            OrderCache orderCache,
                            TradingDate startDate,
                            TradingDate endDate,
                            Money initialCapital,
                            Money stockValue,
                            int numberStocks,
                            Money tradeCost) 
        throws EvaluationException {

        // Is there a fixed number of stocks?
        if(stockValue == null)
            portfolio = PaperTrade.paperTrade(PORTFOLIO_NAME,
                                              quoteBundle,
                                              new Variables(),
                                              orderCache,
                                              startDate,
                                              endDate,
                                              getBuyRule(),
                                              getSellRule(),
                                              initialCapital,
                                              numberStocks,
                                              tradeCost);
        // Or a fixed value?
        else {
            portfolio = PaperTrade.paperTrade(PORTFOLIO_NAME,
                                              quoteBundle,
                                              new Variables(),
                                              orderCache,
                                              startDate,
                                              endDate,
                                              getBuyRule(),
                                              getSellRule(),
                                              initialCapital,
                                              stockValue,
                                              tradeCost);
        }

        // Get final value of portfolio
        try {
            value = portfolio.getValue(quoteBundle, endDate);
        }
        catch(MissingQuoteException e) {
            // Already checked...
            assert false;
        }

        return value;
    }

    public Money getValue() {
        return value;
    }

    public Expression getBuyRule() {
        return buyRule;
    }

    public Expression getSellRule() {
        return sellRule;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public int compareTo(Object object) {
        Individual other = (Individual)object;

        return getValue().compareTo(other.getValue());
    }

    public boolean equals(Object object) {

        // TODO: equals shouldn't say the equations are equal unless they
        // ARE the same equations. Otherwise it should rate them slightly
        // differently?? I'm not sure.
        // TODO: Also the 'order' variable isn't defined if the order is
        // not set. So this variable shouldnt always be generated by
        // mutator.

        Individual other = (Individual)object;

        return getValue().equals(other.getValue());
    }

    public int hashCode() {
        // If you implement equals you should implement hashCode().
        // Since I don't need it I haven't bothered to implement a very
        // good hash.
        return getBuyRule().hashCode() + getSellRule().hashCode();
    }

    private int getRandomBreedType(Random random) {
        int percent = random.nextInt(100);

        if(CLONE_PERCENT > percent)
            return BREED_BY_CLONING;
        percent -= CLONE_PERCENT;

        if(SWAP_PERCENT > percent) 
            return BREED_BY_SWAPPING;
        percent -= SWAP_PERCENT;

        if(RECOMBINE_PERCENT > percent)
            return BREED_BY_RECOMBINING;
        percent -= RECOMBINE_PERCENT;

        if(SWAP_AND_RECOMBINE_PERCENT > percent)
            return BREED_BY_SWAPPING_AND_RECOMBINING;
        percent -= SWAP_AND_RECOMBINE_PERCENT;

        return BREED_BY_DOUBLE_RECOMBINING;
    }

    private Expression recombine(Mutator mutator, Expression destination, Expression source) {
        Expression destinationSubTree = mutator.findRandomSite(destination);
        Expression sourceSubTree = mutator.findRandomSite(source, 
                                                          destinationSubTree.getType());
            
        // It's possible that there is no match in the source for the given type. 
        if(sourceSubTree != null) {
            assert sourceSubTree.getType() == destinationSubTree.getType();
            destination = mutator.insert(destination, destinationSubTree, 
                                         (Expression)sourceSubTree.clone());
        }

        return destination;
    }

    // All created individuals should have the correct types used throughout.
    // Assert if it turns out we have made a mistake
    private void checkType() {
        try {
            // Check that both the buy and sell rules are both booleans and
            // that their subtrees have proper types.
            assert buyRule.checkType() == Expression.BOOLEAN_TYPE;
            assert sellRule.checkType() == Expression.BOOLEAN_TYPE;

        } catch(TypeMismatchException e) {
            assert false;
        }
    }
}
