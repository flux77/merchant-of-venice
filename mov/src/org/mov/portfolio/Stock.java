package org.mov.portfolio;

import java.util.*;

import org.mov.util.*;
import org.mov.parser.*;

public class Stock {
    private String symbol;
    private TradingDate date;
    private int volume;
    private float day_low;
    private float day_high;
    private float day_open;
    private float day_close;

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

    public String getSymbol() {
	return symbol;
    }

    public TradingDate getDate() {
	return date;
    }

    public int getVolume() {
	return volume;
    }

    public float getDayLow() {
	return day_low;
    }

    public float getDayHigh() {
	return day_high;
    }

    public float getDayOpen() {
	return day_open;
    }

    public float getDayClose() {
	return day_close;
    }

    public void setSymbol(String symbol) {	
	if(symbol != null)
	    this.symbol = symbol.toLowerCase();
	else
	    this.symbol = null;
    }

    public void setDate(TradingDate date) {
	this.date = date;
    }

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

    public String toString() {
	return new String(getSymbol() + ", " + getDate() + ", " +
			  getDayOpen() + ", " + getDayHigh() + ", " + 
			  getDayLow() + ", " + getDayClose() + ", " + 
			  getVolume());
			   
    }
}
