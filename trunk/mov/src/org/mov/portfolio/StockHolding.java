package org.mov.portfolio;

public class StockHolding {
    private String symbol;
    private int shares;
    
    public StockHolding(String symbol, int shares) {
	this.symbol = symbol;
	this.shares = shares;
    }

    public void accumulate(int shares) {
	this.shares += shares;
    }

    public void reduce(int shares) {
	this.shares -= shares;
    }

    public String getSymbol() {
	return symbol;
    }

    public int getShares() {
	return shares;
    }
}
