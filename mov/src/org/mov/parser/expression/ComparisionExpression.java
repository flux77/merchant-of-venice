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

import org.mov.parser.*;

/**
 * Abstract base class for the comparision expressions:
 * <code>>, <, ==, !=, <=, >=</code>
 */
abstract public class ComparisionExpression extends BinaryExpression {

    /**
     * Create a new comparision expression with the given left and
     * right arguments.
     */
    public ComparisionExpression(Expression left, Expression right) {
	super(left, right);
    }

    /**
     * Check the input arguments to the expression. They can only be
     * {@link #INTEGER_TYPE} or {@link #FLOAT_TYPE}. Both must be the same!
     *
     * @return	the type of the expression
     */
    public int checkType() throws TypeMismatchException {
	// left & right types must be the same and not boolean or quote
	int leftType = getLeft().checkType();
	int rightType = getRight().checkType();

	if(leftType == rightType && 
           (leftType == FLOAT_TYPE || leftType == INTEGER_TYPE))
            return getType();
	else
	    throw new TypeMismatchException();
    }

    /**
     * Get the type of the expression.
     *
     * @return {@link BOOLEAN_TYPE}.
     */
    public int getType() {
        return BOOLEAN_TYPE;
    }
}