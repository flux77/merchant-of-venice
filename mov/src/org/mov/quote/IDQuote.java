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

package org.mov.quote;

import org.mov.util.TradingDate;
import org.mov.util.TradingTime;

/**
 * Representation of an intra-day stock quote for a given stock, on a given
 * day at a given time.
 *
 * @author Andrew Leppard
 */
public class IDQuote implements Quote {

    // Stock symbol
    private Symbol symbol;

    // Date of quote
    private TradingDate date;

    // Time of quote
    private TradingTime time;

    // Last bid price
    private double bid;

    // Last ask price
    private double ask;

    // Last trade price
    private double last;

    /**
     * Create a new intra-day stock quote.
     *
     * @param	symbol	the stock symbol
     * @param	date	the date of this stock quote
     * @param   time    the time of this stock quote
     * @param   bid     the bid price
     * @param   ask     the ask price
     * @param   last    the price at the last trade
     */
    public IDQuote(Symbol symbol, TradingDate date, TradingTime time,
                   double bid, double ask, double last) {
        setSymbol(symbol);
        setDate(date);
        setTime(time);

        this.bid = bid;
        this.ask = ask;
        this.last = last;
    }

    /**
     * Return the stock's symbol.
     *
     * @return	the symbol
     */
    public Symbol getSymbol() {
	return symbol;
    }

    /**
     * Return the quote date.
     *
     * @return	the date
     */
    public TradingDate getDate() {
	return date;
    }

    /**
     * Return the quote time.
     *
     * @return	the time
     */
    public TradingTime getTime() {
	return time;
    }

    /**
     * Return the bid price.
     *
     * @return the bid price.
     */
    public double getBid() {
        return bid;
    }

    /**
     * Return the ask price.
     *
     * @return the ask price.
     */
    public double getAsk() {
        return ask;
    }

    /**
     * Return the last traded price.
     *
     * @return the last price.
     */
    public double getLast() {
        return last;
    }

    /**
     * Set the symbol for this quote.
     *
     * @param	symbol	the stock symbol
     */
    public void setSymbol(Symbol symbol) {	
        this.symbol = symbol;
    }

    /**
     * Set the quote date.
     *
     * @param	date	the date
     */
    public void setDate(TradingDate date) {
	this.date = date;
    }

    /**
     * Set the quote time.
     *
     * @param	time	the time
     */
    public void setTime(TradingTime time) {
	this.time = time;
    }

    public double getQuote(int quote)
        throws UnsupportedOperationException {

	switch(quote) {
	case(BID):
	    return getBid();
	case(ASK):
	    return getAsk();
	case(LAST):
	    return getLast();
	default:
            throw new UnsupportedOperationException();
	}
    }
}