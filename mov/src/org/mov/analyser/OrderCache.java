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

package org.mov.analyser;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import org.mov.quote.QuoteBundle;

public class OrderCache {
    private QuoteBundle quoteBundle;
    private OrderComparator orderComparator;
    private HashMap dayOrders;

    public OrderCache(QuoteBundle quoteBundle, OrderComparator orderComparator) {
        this.quoteBundle = quoteBundle;
        this.orderComparator = orderComparator;

        dayOrders = new HashMap();
    }

    public List getTodaySymbols(int dateOffset) {
        Integer date = new Integer(dateOffset);
        List symbols = (List)dayOrders.get(date);

        // If we haven't cached today's symbols then find the symbols
        // in the quote bundle for today and sort them
        if(symbols == null) {
            symbols = quoteBundle.getSymbols(dateOffset);
            orderComparator.setDateOffset(dateOffset);
            Collections.sort(symbols, orderComparator);
            dayOrders.put(date, symbols);
        }
        
        return symbols;
    } 

    public boolean isOrdered() {
        return orderComparator.isOrdered();
    }
}
