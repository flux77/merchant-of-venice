package org.mov.main;

import java.util.*;

import org.mov.util.*;

public class Trade {

    public final static int BUY = 0;
    public final static int SELL = 1;
    
    private TradingDate date;
    private String symbol;
    private int shares;
    private float price;
    private float tradeCode;
    private int trade;

    public Trade(TradingDate date, int trade, String symbol, int shares,
		 float price, float tradeCost) {
	this.date = date;
	this.trade = trade;
	this.symbol = symbol;
	this.shares = shares;
	this.price = price;
	this.tradeCode = tradeCost;
    }

    public TradingDate getDate() {
	return date;
    }
    
    public String getSymbol() {
	return symbol;
    }

    public int getTrade() {
	return trade;
    }

    public int getShares() {
	return shares;
    }

    public float getPrice() {
	return price;
    }

}
