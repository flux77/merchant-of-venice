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

import java.util.Enumeration;
import java.util.Random;

import org.mov.parser.Expression;
import org.mov.parser.expression.*;
import org.mov.quote.Quote;

public class Mutator {

    private final static int MUTATION_PERCENT       = 10;
    private final static int EXTRA_MUTATION_PERCENT = 10;

    private final static int INSERTION_MUTATION_PERCENT    = 10;
    private final static int DELETION_MUTATION_PERCENT     = 20;
    private final static int MODIFICATION_MUTATION_PERCENT = 40;
    private final static int SHUFFLE_MUTATION_PERCENT      = 30;

    private Random random;

    public Mutator(Random random) {
        this.random = random;
    }

    public Expression createRandom(int type) {
        return null;
    }

    public Expression createRandomTerminal(int type) {
        int randomNumber;

        switch(type) {
        case Expression.BOOLEAN_TYPE:
            randomNumber = random.nextInt(2);
            
            if(randomNumber == 0)
                return new NumberExpression(Expression.TRUE, Expression.BOOLEAN_TYPE);
            else
                return new NumberExpression(Expression.FALSE, Expression.BOOLEAN_TYPE);

        case Expression.FLOAT_TYPE:
            return new NumberExpression(random.nextFloat() * 100, Expression.FLOAT_TYPE);

        case Expression.INTEGER_TYPE:
            randomNumber = random.nextInt(3);

            if(randomNumber == 0)
                return new NumberExpression(random.nextFloat() * 100, Expression.INTEGER_TYPE);
            else if(randomNumber == 1)
                return new VariableExpression("order", Expression.INTEGER_TYPE);
            else
                return new VariableExpression("held", Expression.INTEGER_TYPE);

        case Expression.QUOTE_TYPE:
            randomNumber = random.nextInt(5);

            if(randomNumber == 0)
                return new DayOpenExpression();
            else if(randomNumber == 1)
                return new DayHighExpression();
            else if(randomNumber == 2)
                return new DayLowExpression();
            else if(randomNumber == 3)
                return new DayCloseExpression();
            else
                return new DayVolumeExpression();

        default:
            assert false;
            return null;
        }
    }

    public Expression findRandomSite(Expression expression) {
        int randomNumber = random.nextInt(expression.size());
        Expression randomSite = null;

        if(randomNumber >= 1) {
            for(Enumeration enumeration = expression.breadthFirstEnumeration();
                enumeration.hasMoreElements();) {
                
                randomSite = (Expression)enumeration.nextElement();

                // Return if this is the xth random element
                if(randomNumber-- <= 0)
                    break;
            }

            assert randomSite != null;
        }

        return randomSite;
    }

    public Expression findRandomSite(Expression expression, int type) {
        int randomNumber = random.nextInt(expression.size(type));
        Expression randomSite = null;

        if(randomNumber >= 1) {
            for(Enumeration enumeration = expression.breadthFirstEnumeration();
                enumeration.hasMoreElements();) {
                
                randomSite = (Expression)enumeration.nextElement();

                // Return if this is the xth random element of the
                // given type
                if(randomSite.getType() == type)
                    if(randomNumber-- <= 0)
                        break;
            }

            assert randomSite != null;
        }

        return randomSite;
    }

    public Expression delete(Expression root, Expression destination) {
        return insert(root, destination, 
                      createRandomTerminal(destination.getType()));
    }

    public Expression insert(Expression root, Expression destination, 
                             Expression source) {

        Expression parent = (Expression)destination.getParent();

        if(parent == null) {
            // If the destination has no parent it must be the root of the tree.
            assert root == destination;
            return source;
        }
        else {
            int childNumber = parent.getIndex(destination);
            parent.remove(childNumber);
            parent.insert(source, childNumber);
            return root;
        }
    }

    public Expression modify(Expression root, Expression destination) {
        if(destination.getType() == Expression.BOOLEAN_TYPE) 
            return modifyBoolean(root, destination);
        else if(destination.getType() == Expression.FLOAT_TYPE) 
            return modifyFloat(root, destination);
        else if(destination.getType() == Expression.INTEGER_TYPE) 
            return modifyInteger(root, destination);
        else {
            assert destination.getType() == Expression.QUOTE_TYPE;

            return modifyQuote(root, destination);
        }
    }
    
    public Expression mutate(Expression expression) {
        return mutate(expression, MUTATION_PERCENT);
    }

    public Expression mutate(Expression expression, int percent) {

        // Mutations do not always occur. Use the given percent to work
        // out whether one should occur.
        if(percent < random.nextInt(100))
            return expression;

        // Mutate
        if(INSERTION_MUTATION_PERCENT > percent) 
            expression = mutateByInsertion(expression);
        else {
            percent -= INSERTION_MUTATION_PERCENT;

            if(DELETION_MUTATION_PERCENT > percent) 
                expression = mutateByDeletion(expression);
            else {
                percent -= DELETION_MUTATION_PERCENT;

                if(MODIFICATION_MUTATION_PERCENT > percent) 
                    expression = mutateByModification(expression);
                else 
                    expression = mutateByShuffleModification(expression);
            }
        }

        // There's always the possibility of a 2nd, 3rd, etc mutation. This
        // can be useful if the gene pool is stagnant.
        return mutate(expression, EXTRA_MUTATION_PERCENT);
    }

    private Expression mutateByModification(Expression expression) {
        Expression destination = findRandomSite(expression);

        return modify(expression, destination);
    }

    private Expression mutateByInsertion(Expression expression) {
        Expression destination = findRandomSite(expression);
        Expression insertSubTree = createRandom(destination.getType());

        return insert(expression, destination, insertSubTree);
    }

    private Expression mutateByDeletion(Expression expression) {
        Expression destination = findRandomSite(expression);

        // There's no point in replacing the root node with a terminal
        // expression, and replacing one terminal expression with
        // another is better done by a mutate modification.
        // So just skip the whole deletion idea and try a random
        // mutation somewhere.
        if(destination.isRoot() || destination.getChildCount() == 0)
            return mutateByModification(expression);
        else
            return delete(expression, destination);
    }

    private Expression mutateByShuffleModification(Expression expression) {
        Expression source = findRandomSite(expression);
        Expression destination = findRandomSite(expression, source.getType());

        if(destination != null) {
            // very fucking tricky.

        }
            
        return null;
    }

    private Expression modifyBoolean(Expression root, Expression destination) {
        // and, or, equal, less than, less than equal, greater than, 
        // greater than equal, if, not, true, false.
        return root;
    }

    private Expression modifyFloat(Expression root, Expression destination) {
        // add, subtract, multiply, divide, lag, min, max, avg, percent,
        // number
        return root;
    }

    private Expression modifyInteger(Expression root, Expression destination) {
        // add, subtract, multiply, divide, lag, min, max, avg, percent,
        // number, variable

        return root;
    }

    private Expression modifyQuote(Expression root, Expression destination) {
        try {
            QuoteExpression quoteExpression = (QuoteExpression)destination; 
            int quoteKind = quoteExpression.getQuoteKind();
            
            // We don't mutate this symbol as there isn't anything of the same
            // type we can change it too
            if(quoteKind == Quote.DAY_VOLUME)
                return root;
            else {
                

            }
            
        }
        catch(ClassCastException e) {
            assert false;
        }
        
        return root;
    }



}
