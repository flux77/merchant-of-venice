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

package org.mov.parser.expression;

import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * An expression which multiplies two sub-expressions.
 */
public class MultiplyExpression extends ArithmeticExpression {

    public MultiplyExpression(Expression left, Expression right) {
	super(left, right);
    }

    public double evaluate(Variables variables, QuoteBundle quoteBundle, Symbol symbol, int day) 
	throws EvaluationException {

	return getLeft().evaluate(variables, quoteBundle, symbol, day) *
	    getRight().evaluate(variables, quoteBundle, symbol, day);
    }

    public Expression simplify() {
        // First perform arithmetic simplifications
        Expression simplified = super.simplify();

        if(simplified == this) {
            NumberExpression left = (getLeft() instanceof NumberExpression? 
                                     (NumberExpression)getLeft() : null);
            NumberExpression right = (getRight() instanceof NumberExpression? 
                                      (NumberExpression)getRight() : null);

            // 0*a -> 0.
            if(left != null && left.equals(0.0F))
                return new NumberExpression(0.0F, getType());

            // a*0 -> 0.
            else if(right != null && right.equals(0.0F))
                return new NumberExpression(0.0F, getType());

            // 1*a -> a.
            else if(left != null && left.equals(1.0F))
                return getRight();

            // a*1 -> a.
            else if(right != null && right.equals(1.0F))
                return getLeft();
        }
        return simplified;
    }

    public boolean equals(Object object) {

        // Are they both multiply expressions?
        if(object instanceof MultiplyExpression) {
            MultiplyExpression expression = (MultiplyExpression)object;

            // (x*y) == (x*y)
            if(getLeft().equals(expression.getLeft()) &&
               getRight().equals(expression.getRight()))
                return true;

            // (x*y) == (y*x)
            if(getLeft().equals(expression.getRight()) &&
               getRight().equals(expression.getLeft()))
                return true;
        }
    
        return false;
    }

    public String toString() {
	return super.toString("*");
    }

    public Object clone() {
        return new MultiplyExpression((Expression)getLeft().clone(), 
                                      (Expression)getRight().clone());
    }
}
