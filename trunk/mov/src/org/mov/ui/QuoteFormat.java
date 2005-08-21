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

import java.text.NumberFormat;

public class QuoteFormat implements Comparable {

    private double quote;

    private static NumberFormat format = null;

    public QuoteFormat(double quote) {
        this.quote = quote;
    }

    /**
     * Convert from a quote (in dollars) to string. 
     *
     * @param	quote	the quote
     * @return	the quote string
     */
    public static String quoteToString(double quote) {
        return getNumberFormat().format(quote);
    }

    public String toString() {
        return quoteToString(getQuote());
    }

    public double getQuote() {
        return quote;
    }

    public int compareTo(Object object) {
        QuoteFormat format = (QuoteFormat)object;

        if(getQuote() < format.getQuote())
            return -1;
        if(getQuote() > format.getQuote())
            return 1;
        else
            return 0;
    }

    private static NumberFormat getNumberFormat() {
        // Synchronisation cannot cause issues here. So this code
        // isn't synchronised.
        if(format == null) {
            format = NumberFormat.getInstance();
            format.setMinimumIntegerDigits(1);
            format.setMinimumFractionDigits(5);
            format.setMaximumFractionDigits(5);
        }

        return format;
    }
    
}
