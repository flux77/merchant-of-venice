package org.mov.portfolio;

/**
 * Representation of a single stock holding in a share account. 
 * @see ShareAccount
 */
public class StockHolding {

    // Stock held
    private String symbol;

    // Number of shares of stock
    private int shares;
    
    /**
     * Create a new stock holding
     *
     * @param	symbol	the stock to own
     * @param	shares	the number of shares of that stock
     */
    public StockHolding(String symbol, int shares) {
	this.symbol = symbol;
	this.shares = shares;
    }

    /**
     * Increase ownership of stock.
     *
     * @param	shares	number of new shares to accumulate
     */
    public void accumulate(int shares) {
	this.shares += shares;
    }

    /**
     * Decrease ownership of stock.
     *
     * @param	shares	number of shares to reduce
     */
    public void reduce(int shares) {
	this.shares -= shares;
    }

    /**
     * Get symbol of stock held.
     *
     * @return	symbol
     */
    public String getSymbol() {
	return symbol;
    }

    /**
     * Get number of shares held.
     *
     * @return	number of shares
     */
    public int getShares() {
	return shares;
    }
}
