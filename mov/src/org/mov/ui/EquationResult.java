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

public class EquationResult implements Comparable {
    boolean isResult;
    int type;
    double result;

    public final static EquationResult EMPTY = new EquationResult();

    private EquationResult() {
        result = 0.0D;
        isResult = false;
    }

    public EquationResult(int type, double result) {
        isResult = true;
        this.type = type;
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public String toString() {
        if(isResult)
            return NumberExpression.toString(type, result);
        else
            return "";
    }

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
