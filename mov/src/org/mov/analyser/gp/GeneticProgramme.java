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
import java.util.SortedSet;
import java.util.TreeSet;

import org.mov.parser.Expression;
import org.mov.parser.EvaluationException;

import org.mov.quote.QuoteBundle;

public class GeneticProgramme {

    private final int MIN_DEPTH = 3;
    private final int MAX_DEPTH = 15;

    private int breedingPopulationSize;
    private SortedSet breedingPopulation;
    private SortedSet nextBreedingPopulation;
    private Mutator mutator;
    private Random random;
    private GPQuoteBundle quoteBundle;

    private int generation;

    public GeneticProgramme(GPQuoteBundle quoteBundle, int breedingPopulationSize, int seed) {
        this.breedingPopulationSize = breedingPopulationSize;
        this.quoteBundle = quoteBundle;

        nextBreedingPopulation = new TreeSet();
        breedingPopulation = new TreeSet();
        random = new Random(seed);
        mutator = new Mutator(random);

        generation = 1;
    }

    public boolean nextIndividual() {
        Individual individual = createIndividual();

        if(individual.isValid(MIN_DEPTH, MAX_DEPTH)) {
            try {
                float newIndividualValue = individual.paperTrade(quoteBundle);
                
                // If we got here the paper trade was successful - add the
                // individual if it is fit enough to go into the current
                // breeding population
                if(nextBreedingPopulation.size() < breedingPopulationSize) 
                    nextBreedingPopulation.add(individual);
                
                else {
                    Individual weakestBreedingIndividual = 
                        (Individual)nextBreedingPopulation.last();
                    
                    if(newIndividualValue > weakestBreedingIndividual.getValue()) {
                        nextBreedingPopulation.remove(weakestBreedingIndividual);
                        nextBreedingPopulation.add(individual);
                    }
                }
                return true;
            }
            catch(EvaluationException e) {
                // If there is a problem running the equation then
                // it dies off naturally!
            }
        }

        return false;
    }

    public int nextGeneration() {
        // The new breeding population is made from the strongest
        // individuals from last generation. We also leave "nextBreedingPopulation"
        // the same - to ensure that the next population's strongest individuals
        // will be at least as good as the previous ones.
        breedingPopulation = new TreeSet(nextBreedingPopulation);

        return ++generation;
    }

    public Individual getBreedingIndividual(int index) {
        assert index < breedingPopulation.size();

        for(Iterator iterator = breedingPopulation.iterator(); iterator.hasNext();) {
            Individual individual = (Individual)iterator.next();

            if(index == 0)
                return individual;
            else
                index--;
        }

        assert false;
        return null;
    }

    private Individual createIndividual() {
        if(generation == 1) 
            // Create a new random individual
            return new Individual(mutator);
        else {
            // Otherwise breed two parent individuals
            int motherIndex = random.nextInt(breedingPopulationSize);
            int fatherIndex = random.nextInt(breedingPopulationSize);
            
            Individual mother = getBreedingIndividual(motherIndex);
            Individual father = getBreedingIndividual(fatherIndex);
            
            return new Individual(random, mutator, mother, father);
        }
    }
}
