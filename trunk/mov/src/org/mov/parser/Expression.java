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

package org.mov.parser;

import javax.swing.tree.*;

import org.mov.util.*;
import org.mov.quote.*;

/** 
 * Representation of a composite executable parse tree. Any expression
 * in the <i>Gondola</i> language is parsed into a composite structure
 * built upon this class. This class therefore represents an executable
 * expression. 
 * <p>
 * Any single object of this type could represent a <b>terminal expression</b>
 * that is a number such as "<code>5</code>",
 * a <b>unary expression</b> such as "<code>not(X)</code>",
 * a <b>binary expression</b> such as "<code>X and Y</code>" or a 
 * <b>ternary expression</b>. The arguments labelled above as <code>X</code>
 * and <code>Y</code> would be represented by separate <code>Expression</code>
 * classes.
 * Those classes would however be contained by this class.
 */
public abstract class Expression extends DefaultMutableTreeNode implements Cloneable {

    /** A boolean type that can contain either <code>1</code> or 
	<code>0</code> */
    public static final int BOOLEAN_TYPE = 0;

    /** A float type that can contain any number. */
    public static final int FLOAT_TYPE = 1;

    /** An integer type that can contain any integer number. */
    public static final int INTEGER_TYPE = 2;

    /** Represents a stock quote <b>type</b>: open, close, low, high */
    public static final int QUOTE_TYPE = 3;

    /** Threshold level where a number is registered as <code>TRUE</code> */
    public final static float TRUE_LEVEL = 0.1F;

    /** <code>Value of TRUE</code> */
    public final static float TRUE = 1.0F;

    /** <code>Value of FALSE</code> */
    public final static float FALSE = 0.0F;
    
    /**
     * Create a new expression.
     */
    public Expression() {
	// nothing to do
    }

    /**
     * Evaluates the given expression and returns the result.
     *
     * @param   variables       variable storage area for expression
     * @param	quoteBundle	the quote bundle containing quote data to use
     * @param	symbol	the current symbol
     * @param	day	current date in cache fast access format
     * @return	the result of the expression
     * @throws	EvaluationException if the expression tries to access
     *		dates outside of the cache
     */
    abstract public float evaluate(Variables variables, QuoteBundle quoteBundle, 
                                   String symbol, int day)
	throws EvaluationException;

    /**
     * Convert the given expression to a string.
     * 
     * @return	the string representation of the expression
     */
    abstract public String toString();

    /**
     * Perform type checking on the expression.
     *
     * @return	the return type of the expression
     * @throws	TypeMismatchException if the expression has incorrect types
     */
    abstract public int checkType() throws TypeMismatchException;

    /**
     * Get the type of the expression.
     *
     * @return one of {@link BOOLEAN_TYPE}, {@link FLOAT_TYPE},
     *         {@link INTEGER_TYPE} or {@link QUOTE_TYPE}.
     */
    abstract public int getType();

    /**
     * Return the number of children (arguments) that this expression
     * needs.
     *
     * @return	the required number of arguments
     */
    abstract public int getNeededChildren();

    /** 
     * Count the number of nodes in the tree.
     *
     * @return number of nodes or 1 if this is a terminal node
     */
    public int size() {
        int count = 1;

        for(int i = 0; i < getChildCount(); i++) {
            Expression childExpression = (Expression)getChildAt(i);

            count += childExpression.size();
        }

        return count;
    }

    /**
     * Count the number of nodes in the tree with the given type.
     *
     * @return number of nodes in the tree with the given type.
     */
    public int size(int type) {
        int count = 0;

        assert(type == BOOLEAN_TYPE || type == FLOAT_TYPE || type == INTEGER_TYPE ||
               type == QUOTE_TYPE);

        if(getType() == type)
            count = 1;

        for(int i = 0; i < getChildCount(); i++) {
            Expression childExpression = (Expression)getChildAt(i);

            count += childExpression.size(type);
        }

        return count;
    }
}


