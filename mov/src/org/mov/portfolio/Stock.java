package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

/**
 * Representation of a stock quote for a given stock on a given date.
 */
public class Stock {
    private String symbol;
    private TradingDate date;
    private int volume;
    private float day_low;
    private float day_high;
    private float day_open;
    private float day_close;

    /**
     * Create a new stock quote for the given date.
     *
     * @param	symbol	the stock symbol
     * @param	date	the date for this stock quote
     * @param	volume	the number of shares traded on this date
     * @param	day_low	the lowest quote on this date
     * @param	day_high	the highest quote on this date
     * @param	day_open	the opening quote on this date
     * @param	day_close	the closing quote on this date
     */
    public Stock(String symbol, TradingDate date,
		 int volume, float day_low, float day_high,
		 float day_open, float day_close) {

	setSymbol(symbol);
	setDate(date);

	this.volume = volume;
	this.day_low = day_low;
	this.day_high = day_high;
	this.day_open = day_open;
	this.day_close = day_close;
    }

    /**
     * Return the stock's symbol.
     *
     * @return	the symbol
     */
    public String getSymbol() {
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
     * Return the volume.
     *
     * @return	the volume
     */
    public int getVolume() {
	return volume;
    }

    /**
     * Return the day low.
     *
     * @return	the day low
     */
    public float getDayLow() {
	return day_low;
    }

    /**
     * Return the day high.
     *
     * @return	the day high
     */
    public float getDayHigh() {
	return day_high;
    }

    /**
     * Return the day open.
     *
     * @return	the day open
     */
    public float getDayOpen() {
	return day_open;
    }

    /**
     * Return the day close.
     *
     * @return	the day close
     */
    public float getDayClose() {
	return day_close;
    }

    /**
     * Set the symbol for this quote.
     *
     * @param	symbol	the stock symbol
     */
    public void setSymbol(String symbol) {	
	if(symbol != null)
	    this.symbol = symbol.toLowerCase();
	else
	    this.symbol = null;
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
     * Get a single quote.
     *
     * @param	quote	the quote type
     */
    public float getQuote(int quote) 
	throws EvaluationException {
	switch(quote) {
	case(Token.DAY_OPEN_TOKEN):
	    return getDayOpen();
	case(Token.DAY_CLOSE_TOKEN):
	    return getDayClose();
	case(Token.DAY_LOW_TOKEN):
	    return getDayLow();
	case(Token.DAY_HIGH_TOKEN):
	    return getDayHigh();
	case(Token.DAY_VOLUME_TOKEN):
	    return getVolume();
	default:
	    throw new EvaluationException("unknown quote type");
	}
    }

    /**
     * Return a string representation of the stock quote.
     *
     * @return	a string representation of the stock quote.
     */
    public String toString() {
	return new String(getSymbol() + ", " + getDate() + ", " +
			  getDayOpen() + ", " + getDayHigh() + ", " + 
			  getDayLow() + ", " + getDayClose() + ", " + 
			  getVolume());
			   
    }
}
