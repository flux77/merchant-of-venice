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

import org.mov.parser.EvaluationException;
import org.mov.parser.TypeMismatchException;
import org.mov.parser.Expression;
import org.mov.quote.QuoteBundle;

public class Individual implements Comparable {
    
    private Expression buyRule;
    private Expression sellRule;
    private float value;

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

    public Individual(Expression buyRule, Expression sellRule) {
        this.buyRule = buyRule;
        this.sellRule = sellRule;

        checkType();                
    }

    public Individual(Mutator mutator) {
        buyRule = mutator.createRandom(Expression.BOOLEAN_TYPE);
        sellRule = mutator.createRandom(Expression.BOOLEAN_TYPE);

        checkType();
    }

    public Individual(Random random, Mutator mutator, Individual father, Individual mother) {
        int breedType = getRandomBreedType(random);

        buyRule = (Expression)father.getBuyRule().clone();

        // Retrieve buy/sell rules from appropriate parent (swapping)
        {
            if(breedType == BREED_BY_SWAPPING || 
               breedType == BREED_BY_SWAPPING_AND_RECOMBINING)
                sellRule = (Expression)mother.getSellRule().clone();
            else
                sellRule = (Expression)father.getSellRule().clone();
        }

        // Perform recombining if necessary
        {
            // Single
            if(breedType == BREED_BY_RECOMBINING ||
               breedType == BREED_BY_DOUBLE_RECOMBINING ||
               breedType == BREED_BY_SWAPPING_AND_RECOMBINING) {
                buyRule = recombine(mutator, buyRule, mother.getBuyRule());

                // Double
                if(breedType == BREED_BY_DOUBLE_RECOMBINING)
                    sellRule = recombine(mutator, sellRule, mother.getSellRule());
            }
        }

        // Perform mutations
        {
            if(breedType == BREED_BY_CLONING) {
                // If it is a clone, at least one of the rules must mutate otherwise
                // we've created a duplicate individual which is a waste of processing
                // power.
                buyRule = mutator.mutate(buyRule, 100);
                sellRule = mutator.mutate(sellRule, 100);
            }
            else {
                buyRule = mutator.mutate(buyRule);
                sellRule = mutator.mutate(sellRule);
            }
        }

        checkType();        
    }

    public float paperTrade(QuoteBundle quoteBundle) 
        throws EvaluationException {

        throw new EvaluationException("hiya");
    }

    public float getValue() {
        return value;
    }

    public Expression getBuyRule() {
        return buyRule;
    }

    public Expression getSellRule() {
        return sellRule;
    }

    public int compareTo(Object object) {
        Individual other = (Individual)object;

        if(getValue() < other.getValue())
            return -1;
        if(getValue() > other.getValue())
            return 1;
        else
            return 0;
    }

    public int getMaxDepth() {
        return Math.max(sellRule.getDepth(), buyRule.getDepth());
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
        if(sourceSubTree != null) 
            destination = mutator.insert(destination, destinationSubTree, 
                                         (Expression)sourceSubTree.clone());

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
