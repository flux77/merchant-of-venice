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

package org.mov.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mov.portfolio.Portfolio;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteCache;
import org.mov.quote.Symbol;
import org.mov.quote.WeekendDateException;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.ui.ChangeFormat;
import org.mov.util.Locale;
import org.mov.util.Money;
import org.mov.util.TradingDate;

public class PortfolioModel extends AbstractTableModel {
    private QuoteBundle quoteBundle;
    private Portfolio portfolio;
    private List values;

    // Column ennumeration
    public static final int DATE_COLUMN                = 0;
    public static final int STOCKS_HELD_COLUMN         = 1;
    public static final int CASH_VALUE_COLUMN          = 2;
    public static final int SHARE_VALUE_COLUMN         = 3;
    public static final int MARKET_VALUE_COLUMN        = 4;
    public static final int RETURN_VALUE_COLUMN        = 5;
    public static final int MARKET_VALUE_CHANGE_COLUMN = 6;
    public static final int PERCENT_CHANGE_COLUMN      = 7;

    private class PortfolioValue {
        public TradingDate date;
        public Money marketValue;
        public Money cashValue;
        public Money shareValue;
        public Money returnValue;
        public String stocksHeld;
    }

    public PortfolioModel(Portfolio portfolio,
                          QuoteBundle quoteBundle) {
        super();

        // It's valid to pass in a null quote bundle, but only if the portfolio has
        // never traded any stocks.
        assert(portfolio.getSymbolsTraded().size() == 0 ||
               quoteBundle != null);

        this.portfolio = portfolio;
        this.quoteBundle = quoteBundle;

        values = calculateValues();

        List columns = new ArrayList();
        columns.add(new Column(DATE_COLUMN,
			       Locale.getString("DATE"),
			       Locale.getString("DATE_COLUMN_HEADER"),
                               TradingDate.class, Column.VISIBLE));
        columns.add(new Column(STOCKS_HELD_COLUMN,
			       Locale.getString("STOCKS_HELD"),
			       Locale.getString("STOCKS_HELD_COLUMN_HEADER"),
                               String.class, Column.VISIBLE));
        columns.add(new Column(CASH_VALUE_COLUMN,
			       Locale.getString("CASH_VALUE"),
			       Locale.getString("CASH_VALUE_COLUMN_HEADER"),
                               Money.class, Column.HIDDEN));
        columns.add(new Column(SHARE_VALUE_COLUMN,
			       Locale.getString("SHARE_VALUE"),
			       Locale.getString("SHARE_VALUE_COLUMN_HEADER"),
                               Money.class, Column.HIDDEN));
        columns.add(new Column(MARKET_VALUE_COLUMN,
			       Locale.getString("MARKET_VALUE"),
			       Locale.getString("MARKET_VALUE_COLUMN_HEADER"),
                               Money.class, Column.VISIBLE));
        columns.add(new Column(RETURN_VALUE_COLUMN,
			       Locale.getString("RETURN_VALUE"),
			       Locale.getString("RETURN_VALUE_COLUMN_HEADER"),
			       Money.class, Column.HIDDEN));
        columns.add(new Column(MARKET_VALUE_CHANGE_COLUMN,
			       Locale.getString("MARKET_VALUE_CHANGE"),
			       Locale.getString("MARKET_VALUE_CHANGE_COLUMN_HEADER"),
			       Money.class, Column.VISIBLE));
        columns.add(new Column(PERCENT_CHANGE_COLUMN,
			       Locale.getString("PERCENT_CHANGE"),
			       Locale.getString("PERCENT_CHANGE_COLUMN_HEADER"),
			       ChangeFormat.class, Column.VISIBLE));
        setColumns(columns);
    }

    public int getRowCount() {
        return values.size();
    }

    public Object getValueAt(int row, int column) {
        if(row >= getRowCount())
            return "";

        PortfolioValue today = (PortfolioValue)values.get(row);

        switch(column) {
        case(DATE_COLUMN):
            return today.date;

        case(STOCKS_HELD_COLUMN):
            return today.stocksHeld;

        case(CASH_VALUE_COLUMN):
            return today.cashValue;

        case(SHARE_VALUE_COLUMN):
            return today.shareValue;

        case(MARKET_VALUE_COLUMN):
            return today.marketValue;

        case(RETURN_VALUE_COLUMN):
            return today.returnValue;

        case(MARKET_VALUE_CHANGE_COLUMN):
            // The values are in date order. So the previous value is at row - 1.
            if(row > 0) {
                PortfolioValue yesterday = (PortfolioValue)values.get(row - 1);
                return today.marketValue.subtract(yesterday.marketValue);
            }
            else
                return Money.ZERO;

        case(PERCENT_CHANGE_COLUMN):
            // The values are in date order. So the previous value is at row - 1.
            Money todayValue = today.marketValue;
            Money yesterdayValue = todayValue;

            if(row > 0) {
                PortfolioValue yesterday = (PortfolioValue)values.get(row - 1);
                yesterdayValue = yesterday.marketValue;
            }

            return new ChangeFormat(yesterdayValue, todayValue);

        default:
            assert false;
            return "";
        }
    }

    /**
     * This function builds an array of {@link #PortfolioValue} types which contain
     * the values for each date that we will table. We skip values on weekends, and
     * we also skip values if it requires a quote we don't have.
     *
     * @return a list of values to table, which may be empty.
     */
    private List calculateValues() {
        List values = new ArrayList();

        // Calculate the range of dates to table
        List dateRange = generateDateRange();
        Iterator portfolioIterator = portfolio.iterator();

        for (Iterator dateIterator = dateRange.iterator(); dateIterator.hasNext();) {
            // Each iteration of the portfolio iterator will advance the portfolio
            // by one date.
            TradingDate date = (TradingDate)dateIterator.next();
            Portfolio portfolio = (Portfolio)portfolioIterator.next();

            try {
                PortfolioValue value = new PortfolioValue();

                value.date = date;
                value.cashValue = portfolio.getCashValue();
                value.shareValue = portfolio.getShareValue(quoteBundle, date);
                value.marketValue = portfolio.getValue(quoteBundle, date);
                value.returnValue = portfolio.getReturnValue(quoteBundle, date);
                value.stocksHeld = getStocksHeld(portfolio);
                values.add(value);
            }

            // This exception is fine - we just won't table on that date.
            catch(MissingQuoteException e) { }
        }

        return values;
    }

    /**
     * This function examines the dates of the transactions in the portfolio, and the
     * date range of the quote bundle, and returns a list of dates which we should
     * dispaly in the table.
     *
     * @return a list of dates, which may be empty.
     */
    private List generateDateRange() {
        // This function needs to be very careful in dealing with null pointers.
        // The quote bundle might be null, both the quote bundle and the portfolio
        // might be empty and return null dates.
        List dateRange;

        // If there are no transactions in the portfolio, then there is nothing to table.
        if (portfolio.countTransactions() == 0)
            dateRange = new ArrayList();

        else {
            TradingDate firstDate = portfolio.getStartDate();
            TradingDate lastDate  = portfolio.getLastDate();

            // For the last date use the latest date of either the date of the last
            // transaction in the portfolio, or the last date in the quote bundle.
            if (quoteBundle != null &&
                quoteBundle.getLastDate() != null &&
                quoteBundle.getLastDate().after(lastDate))
                lastDate = quoteBundle.getLastDate();

            dateRange = TradingDate.dateRangeToList(firstDate, lastDate);
        }

        return dateRange;
    }

    private String getStocksHeld(Portfolio portfolio) {
        String string = new String();

        List stocksHeld = portfolio.getStocksHeld();
        Collections.sort(stocksHeld);

        for(Iterator iterator = stocksHeld.iterator(); iterator.hasNext();) {
            Symbol symbol = (Symbol)iterator.next();

            if(string.length() > 0)
                string = string.concat(", " + symbol.toString());
            else
                string = symbol.toString();
        }

        return string;
    }
}