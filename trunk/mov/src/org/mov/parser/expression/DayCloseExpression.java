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
 * A representation of the concept of <code>day close</code>.
 */
public class DayCloseExpression extends TerminalExpression {

    public float evaluate(Variables variables, QuoteBundle quoteBundle, String symbol, int day) {
	// shouldnt happen
        assert false;
        return 0.0F;
    }
 
    public String toString() {
	return "close";
    }

    public int checkType() throws TypeMismatchException {
	return getType();
    }

    /**
     * Get the type of the expression.
     *
     * @return {@link #QUOTE_TYPE}.
     */
    public int getType() {
        return QUOTE_TYPE;
    }

    public Object clone() {
        return new DayCloseExpression();
    }
}
