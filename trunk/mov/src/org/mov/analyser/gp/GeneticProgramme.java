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

import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import org.mov.analyser.OrderCache;

import org.mov.parser.Expression;
import org.mov.parser.EvaluationException;
import org.mov.quote.QuoteBundle;
import org.mov.util.Money;
import org.mov.util.TradingDate;

public class GeneticProgramme {
    // An individual with less nodes than this will be dropped
    private final int MIN_SIZE = 12;

    // An individual with more nodes than this will be dropped
    private final int MAX_SIZE = 48;

    private int breedingPopulationSize;
    private double breedingPopulationSum;
    private TreeMap breedingPopulation;
    private TreeMap nextBreedingPopulation;
    private Mutator buyRuleMutator;
    private Mutator sellRuleMutator;
    private Random random;

    private GPQuoteBundle quoteBundle;
    private OrderCache orderCache;
    private TradingDate startDate;
    private TradingDate endDate;
    private Money initialCapital;
    private Money stockValue;
    private int numberStocks;
    private Money tradeCost;

    private int generation;

    public GeneticProgramme(GPQuoteBundle quoteBundle, 
                            OrderCache orderCache,
                            TradingDate startDate,
                            TradingDate endDate,
                            Money initialCapital,
                            Money stockValue,
                            int numberStocks,
                            Money tradeCost,
                            int breedingPopulationSize, 
                            int seed) {

        this.quoteBundle = quoteBundle;
        this.orderCache = orderCache;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialCapital = initialCapital;
        this.stockValue = stockValue;
        this.numberStocks = numberStocks;
        this.tradeCost = tradeCost;
        this.breedingPopulationSize = breedingPopulationSize;

        nextBreedingPopulation = new TreeMap();
        breedingPopulation = new TreeMap();
        random = new Random(seed);

        // Create a mutator for the buy and sell rules. Buy rules shouldn't
        // use the "held" variable (buy rules won't be evaluated if held > 0).
        buyRuleMutator = new Mutator(random, false, orderCache.isOrdered());
        sellRuleMutator = new Mutator(random, true, orderCache.isOrdered());

        generation = 1;
    }

    public void nextIndividual() {
        boolean validIndividual = false;

        // Loop until we create a valid individual that paper trades OK
        while(!validIndividual) {
            Individual individual = createIndividual();

            if(individual.isValid(MIN_SIZE, MAX_SIZE)) {
                try {
                    Money value =
                        individual.paperTrade(quoteBundle,
                                              orderCache,
                                              startDate,
                                              endDate,
                                              initialCapital,
                                              stockValue,
                                              numberStocks,
                                              tradeCost);
                    
                    // If we got here the paper trade was successful. Now let the
                    // individual 'compete' to see if it gets to breed next round.
                    // If the individual is fit enough, it'll get a chance to breed.
                    competeForBreeding(individual, value);
                    validIndividual = true;
                    
                }
                catch(EvaluationException e) {
                    // If there is a problem running the equation then
                    // it dies off naturally!
                }
            }
        }
    }

    public int nextGeneration() {
        // The new breeding population is made from the strongest
        // individuals from last generation. We also leave "nextBreedingPopulation"
        // the same - to ensure that the next population's strongest individuals
        // will be at least as good as the previous ones.
        breedingPopulation = new TreeMap(nextBreedingPopulation);
        
        // Calculate sum of portfolio values of each individual. We use this
        // when choosing who gets to breed next. The bigger the value compared
        // to other individuals, the greater chance of breeding.
        breedingPopulationSum = 0.0D;

        for(Iterator iterator = breedingPopulation.keySet().iterator(); iterator.hasNext();) {
            Money value = (Money)iterator.next();
            breedingPopulationSum += value.doubleValue();
        }

        return ++generation;
    }

    public Individual getBreedingIndividual(int index) {
        assert index < breedingPopulation.size();

        for(Iterator iterator = breedingPopulation.values().iterator(); iterator.hasNext();) {
            Individual individual = (Individual)iterator.next();

            if(index == 0)
                return individual;
            else
                index--;
        }

        assert false;
        return null;
    }

    // This function is used to return a breeding individuals. We keep a sum of
    // the values of all the breeding individuals. To choose an individual to
    // breed we pick a random number between 0 and the sum of the values.
    // That random number is passed to this function. The individuals with
    // the largest values will be more likely to breed.
    private Individual getBreedingIndividual(double value) {
        assert value <= breedingPopulationSum;

        for(Iterator iterator = breedingPopulation.values().iterator(); iterator.hasNext();) {
            Individual individual = (Individual)iterator.next();

            value -= individual.getValue().doubleValue();

            if(value <= 0.0D)
                return individual;
        }

        // It's possible but unlikely we get here. If so return the best performing 
        // individual to give it a little more chance.
        return (Individual)breedingPopulation.get(breedingPopulation.lastKey());
        
    }

    public int getBreedingPopulationSize() {
        return breedingPopulation.size();
    }

    public int getNextBreedingPopulationSize() {
        return nextBreedingPopulation.size();
    }

    private Individual createIndividual() {
        if(generation == 1) 
            return new Individual(buyRuleMutator, sellRuleMutator);
        else {
            // Otherwise breed two parent individuals. We do these by calculating
            // a random value between 0 and the sum of all the individual values.
            // See getBreedingIndividual(double) for details.
            double motherValue = random.nextDouble() * breedingPopulationSum;
            double fatherValue = random.nextDouble() * breedingPopulationSum;

            Individual mother = getBreedingIndividual(motherValue);
            Individual father = getBreedingIndividual(fatherValue);

            return new Individual(random, buyRuleMutator, sellRuleMutator, mother, father);
        }
    }

    private void competeForBreeding(Individual individual, Money value) {
        // If the individual made a loss or broke even, then we are just going to get 
        // rubbish if we breed from it, so even if it is the best we've seen so far, it
        // gets ignored.
        if(value.isGreaterThan(initialCapital)) {
            // If there is another individual with exactly the same value -
            // we assume it made the same trades. Replace this individual
            // ONLY if the new individual is smaller in size. This puts a small
            // pressure for equations to be as tight as possible.
            Individual sameTradeIndividual = 
                (Individual)nextBreedingPopulation.get(value);

            if(sameTradeIndividual != null) {
                if(individual.getTotalEquationSize() < 
                   sameTradeIndividual.getTotalEquationSize())
                    nextBreedingPopulation.put(value, individual);
            }

            // Our individual is a unique butterfly. It'll get in only if the
            // breeding population isn't full yet, or it is better than an
            // existing individual.
            else {
                if(nextBreedingPopulation.size() < breedingPopulationSize) 
                    nextBreedingPopulation.put(value, individual);
            
                else {
                    Money weakestValue = (Money)nextBreedingPopulation.firstKey();

                    if(value.isGreaterThan(weakestValue)) {
                        nextBreedingPopulation.remove(weakestValue);
                        nextBreedingPopulation.put(value, individual);
                    }
                }
            }
        }
    }
}
