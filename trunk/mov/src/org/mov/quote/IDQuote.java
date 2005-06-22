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

import org.mov.ui.QuoteFormat;
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

    // Current volume
    private int current_volume;

    // Current low
    private double current_low;

    // Current high
    private double current_high;

    // Day open
    private double day_open;

    // Last trade price
    private double last;

    // Last bid price
    private double bid;

    // Last ask price
    private double ask;

    /**
     * Create a new intra-day stock quote.
     *
     * @param	symbol	       the stock symbol
     * @param	date	       the date of this stock quote
     * @param   time           the time of this stock quote
     * @param   current_volume the number of shares traded so far today
     * @param   current_low    the current day low
     * @param   current_high   the current day high
     * @param	day_open       the opening quote on this date
     * @param   last           the last trade price
     * @param   bid            the last bid price
     * @param   ask            the last ask price
     */
    public IDQuote(Symbol symbol, TradingDate date, TradingTime time,
                   int current_volume, double current_low, double current_high,
                   double day_open, double last, double bid, double ask) {
        setSymbol(symbol);
        setDate(date);
        setTime(time);

        this.current_volume = current_volume;
        this.current_low = current_low;
        this.current_high = current_high;
        this.day_open = day_open;
        this.last = last;
        this.bid = bid;
        this.ask = ask;
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
     * Return the current day volume.
     *
     * @return	the current day volume
     */
    public int getDayVolume() {
	return current_volume;
    }

    /**
     * Return the current day low.
     *
     * @return	the current day low
     */
    public double getDayLow() {
	return current_low;
    }

    /**
     * Return the current day high.
     *
     * @return	the current day high
     */
    public double getDayHigh() {
	return current_high;
    }

    /**
     * Return the day open.
     *
     * @return	the day open
     */
    public double getDayOpen() {
	return day_open;
    }

    /**
     * Return the last trade value.
     *
     * @return	the last trade value.
     */
    public double getDayClose() {
	return last;
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
	case(DAY_OPEN):
	    return getDayOpen();
	case(DAY_CLOSE):
	    return getDayClose();
	case(DAY_LOW):
	    return getDayLow();
	case(DAY_HIGH):
	    return getDayHigh();
	case(DAY_VOLUME):
	    return getDayVolume();
	case(BID):
	    return getBid();
	case(ASK):
	    return getAsk();
	default:
            throw new UnsupportedOperationException();
	}
    }

    /**
     * Return a string representation of the stock quote.
     *
     * @return	a string representation of the stock quote.
     */
    public String toString() {
	return new String(getSymbol() + ", " + 
                          getDate() + ", " +
                          getTime() + ", " +
			  QuoteFormat.quoteToString(getDayOpen()) + ", " + 
                          QuoteFormat.quoteToString(getDayHigh()) + ", " + 
			  QuoteFormat.quoteToString(getDayLow()) + ", " + 
                          QuoteFormat.quoteToString(getDayClose()) + ", " + 
			  getDayVolume() + ", " +
                          QuoteFormat.quoteToString(getBid()) + ", " + 
                          QuoteFormat.quoteToString(getAsk()));
    }    
}