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

package org.mov.ui;

import org.mov.parser.expression.NumberExpression;

/**
 * Representation of a result to be displayed in an {@link EquationColumn}.
 * This class is the type of the results displayed in that column.
 *
 * @author Andrew Leppard
 * @see AbstractTable
 * @see AbstractTableModel
 * @see EODQuoteModel
 * @see EquationColumn
 */
public class EquationResult implements Comparable {

    // Set to true if the result actually contains a result. Set to false
    // if the result is empty.
    boolean isResult;

    // Equation result type, e.g. {@link org.mov.parser.Expression#BOOLEAN_TYPE},
    // {@link org.mov.parser.Expression#FLOAT_TYPE} etc
    int type;

    // Result
    double result;

    /** Empty or missing result. */
    public final static EquationResult EMPTY = new EquationResult();

    // Create a new result
    private EquationResult() {
        result = 0.0D;
        isResult = false;
    }

    /**
     * Create a new result.
     *
     * @param type   Type of the result, e.g. {@link org.mov.parser.Expression#BOOLEAN_TYPE}
     * @param result Value of result.
     */
    public EquationResult(int type, double result) {
        isResult = true;
        this.type = type;
        this.result = result;
    }

    /**
     * Get result value.
     *
     * @return Result value.
     */
    public double getResult() {
        return result;
    }

    /**
     * Return a string representation of the result.
     *
     * @return String value.
     */
    public String toString() {
        if(isResult)
            return NumberExpression.toString(type, result);
        else
            return "";
    }
    
    /**
     * Compare this result to another result.
     *
     * @param object Result to compare with.
     * @return <code>-1</code>if the result is before this result;
     *         <code>0</code>if the results are equal;
     *         <code>1</code> if the result is after this result.
     */
    public int compareTo(Object object) {
        EquationResult result = (EquationResult)object;
        if(getResult() < result.getResult())
            return -1;
        if(getResult() > result.getResult())
            return 1;
        else
            return 0;
    }
}
