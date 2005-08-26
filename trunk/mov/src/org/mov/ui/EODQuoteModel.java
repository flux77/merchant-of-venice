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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mov.parser.EvaluationException;
import org.mov.quote.MissingQuoteException;
import org.mov.quote.EODQuote;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.Quote;
import org.mov.quote.Symbol;
import org.mov.util.Locale;

/**
 * Table model to display end of day quotes to the user. This model tells a table
 * how to display end of day quotes by describing the columns and how to populate
 * the table with quotes using data from a {@link EODQuoteBundle}.
 *
 * @author Andrew Leppard
 * @see AbstractTable
 * @see AbstractTableModel
 * @see Column
 * @see EquationColumn
 */
public class EODQuoteModel extends AbstractTableModel {
    private EODQuoteBundle quoteBundle;
    private EquationColumn[] equationColumns;
    private List quotes;

    /** The number of equation columns to display for tables that support them. */
    public final static int EQUATION_COLUMN_COUNT = 5;

    // Column ennumeration

    /** Symbol column number. */
    public static final int SYMBOL_COLUMN         = 0;

    /** Date column number. */
    public static final int DATE_COLUMN           = 1;

    /** Volume column number. */
    public static final int VOLUME_COLUMN         = 2;

    /** Day low column number. */
    public static final int DAY_LOW_COLUMN        = 3;

    /** Day high column number. */
    public static final int DAY_HIGH_COLUMN       = 4;

    /** Day open column number. */
    public static final int DAY_OPEN_COLUMN       = 5;

    /** Day close column number. */
    public static final int DAY_CLOSE_COLUMN      = 6;

    /** Point change column number. */
    public static final int POINT_CHANGE_COLUMN   = 7;

    /** Percent change column number. */
    public static final int PERCENT_CHANGE_COLUMN = 8;

    /** Activity column number. */
    public static final int ACTIVITY_COLUMN       = 9;

    /**
     * Create a new end of day quote model. This quote model 
     *
     * @param quoteBundle   Quote bundle
     * @param quotes        A list of {@link Quote}s which contain
     *                      the quote symbols and dates to table.
     * @param displayDate   Display the date column? Either {@link Column#HIDDEN},
     *                      {@link Column#VISIBLE} or {@link Column#ALWAYS_HIDDEN}.
     * @param displaySymbol Display the symbol column? Either {@link Column#HIDDEN},
     *                      {@link Column#VISIBLE} or {@link Column#ALWAYS_HIDDEN}.
     */ 
    public EODQuoteModel(EODQuoteBundle quoteBundle, List quotes, 
                         int displayDate, int displaySymbol) {
        super();

        this.quoteBundle = quoteBundle;
        this.quotes = quotes;

        List columns = new ArrayList();
        columns.add(new Column(SYMBOL_COLUMN, 
			       Locale.getString("SYMBOL"),
			       Locale.getString("SYMBOL_COLUMN_HEADER"),
			       Symbol.class, displaySymbol));
        columns.add(new Column(DATE_COLUMN, 
			       Locale.getString("DATE"), 
			       Locale.getString("DATE_COLUMN_HEADER"),
                               Symbol.class, displayDate));
        columns.add(new Column(VOLUME_COLUMN, 
			       Locale.getString("VOLUME"), 
			       Locale.getString("VOLUME_COLUMN_HEADER"),
                               Integer.class, Column.VISIBLE));
        columns.add(new Column(DAY_LOW_COLUMN, 
			       Locale.getString("DAY_LOW"), 
			       Locale.getString("DAY_LOW_COLUMN_HEADER"),
                               QuoteFormat.class, Column.VISIBLE));
        columns.add(new Column(DAY_HIGH_COLUMN, 
			       Locale.getString("DAY_HIGH"), 
			       Locale.getString("DAY_HIGH_COLUMN_HEADER"),
                               QuoteFormat.class, Column.VISIBLE));
        columns.add(new Column(DAY_OPEN_COLUMN, 
			       Locale.getString("DAY_OPEN"), 
			       Locale.getString("DAY_OPEN_COLUMN_HEADER"),
                               QuoteFormat.class, Column.VISIBLE));
        columns.add(new Column(DAY_CLOSE_COLUMN, 
			       Locale.getString("DAY_CLOSE"), 
			       Locale.getString("DAY_CLOSE_COLUMN_HEADER"),
                               QuoteFormat.class, Column.VISIBLE));
        columns.add(new Column(POINT_CHANGE_COLUMN, 
			       Locale.getString("POINT_CHANGE"),
			       Locale.getString("POINT_CHANGE_COLUMN_HEADER"),
                               PointChangeFormat.class, Column.HIDDEN));
        columns.add(new Column(PERCENT_CHANGE_COLUMN, 
			       Locale.getString("PERCENT_CHANGE"), 
			       Locale.getString("PERCENT_CHANGE_COLUMN_HEADER"),
			       ChangeFormat.class, Column.VISIBLE));
	columns.add(new Column(ACTIVITY_COLUMN, 
			       Locale.getString("ACTIVITY"), 
			       Locale.getString("ACTIVITY_COLUMN_HEADER"),
                               Integer.class, Column.ALWAYS_HIDDEN));
        setColumns(columns);

        equationColumns = createEquationColumns(ACTIVITY_COLUMN + 1);
    }

    /**
     * Return the list of quotes in the table.
     *
     * @return Tabled quotes.
     */
    public List getQuotes() {
        return quotes;
    }
    
    /**
     * Set the list of quotes to table.
     *
     * @param quotes New quotes to table.
     */
    public void setQuotes(List quotes) {
        this.quotes = quotes;

        // Recalculate the equations for each quote
        for(int i = 0; i < equationColumns.length; i++) {
            try {
                equationColumns[i].calculate(quoteBundle, quotes);
            }
            catch(EvaluationException e) {
                displayErrorMessage(e.getReason());
            }
        }

        fireTableDataChanged();                       
    }

    /**
     * Return the number of rows in the table.
     *
     * @return Number of rows in table.
     */
    public int getRowCount() {
        return quotes.size();
    }

    /**
     * Return the value at the given table cell.
     *
     * @param row    Row number.
     * @param column Column number.
     * @return Value to display in cell.
     */
    public Object getValueAt(int row, int column) {
        if(row >= getRowCount())
            return "";

        EODQuote quote = (EODQuote)quotes.get(row);
        
        switch(column) {
        case(SYMBOL_COLUMN):
            return quote.getSymbol();
            
        case(DATE_COLUMN):
            return quote.getDate();
            
        case(VOLUME_COLUMN):
            return new Integer(quote.getDayVolume());
            
        case(DAY_LOW_COLUMN):
            return new QuoteFormat(quote.getDayLow());
            
        case(DAY_HIGH_COLUMN):
            return new QuoteFormat(quote.getDayHigh());
            
        case(DAY_OPEN_COLUMN):
            return new QuoteFormat(quote.getDayOpen());
		
        case(DAY_CLOSE_COLUMN):
            return new QuoteFormat(quote.getDayClose());
            
        case(POINT_CHANGE_COLUMN):
            // Change is calculated by the percent gain between
            // yesterday's day close and today's day close. If we don't
            // have yesterday's day close available, we just use today's
            // day open.
            double finalQuote = quote.getDayClose();
            double initialQuote = quote.getDayOpen();
            
            try {
                initialQuote = 
                    quoteBundle.getQuote(quote.getSymbol(),
                                         Quote.DAY_CLOSE, 
                                         quote.getDate().previous(1));
            }
            catch(MissingQuoteException e) {
                // No big deal - we default to day open
            }
            
            return new PointChangeFormat(initialQuote, finalQuote);
            
        case(PERCENT_CHANGE_COLUMN):
            finalQuote = quote.getDayClose();
            initialQuote = quote.getDayOpen();
            
            try {
                initialQuote = quoteBundle.getQuote(quote.getSymbol(),
                                                    Quote.DAY_CLOSE, 
                                                    quote.getDate().previous(1));
            }
            catch(MissingQuoteException e) {
                // No big deal - we default to day open
            }
            
            return new ChangeFormat(initialQuote, finalQuote);
            
        case(ACTIVITY_COLUMN):
            // This column is never visible but is used to determine
            // the most active stocks - I don't actually know how to
            // calculate "the most active stocks" or whether we even
            // have enough data to do it. But this seems to be roughly
            // right.
            return new Double(quote.getDayHigh() * quote.getDayVolume());
            
        default:
            EquationColumn equationColumn = (EquationColumn)getColumn(column);
            return equationColumn.getResult(quote.getSymbol(), quote.getDate());
        }
    }

    /**
     * Return the array of equation columns.
     *
     * @return Array of equation columns.
     * @see Column
     * @see EquationColumn
     */
    public EquationColumn[] getEquationColumns() {
        return equationColumns;
    }
    
    /**
     * Set the equation columns. This function also calculates their values.
     *
     * @param quoteBundle     Quote bundle to calculate quotes.
     * @param equationColumns New equation columns
     */
    public void setEquationColumns(EODQuoteBundle quoteBundle, EquationColumn[] equationColumns) {
        Thread thread = Thread.currentThread();
        ProgressDialog progress = ProgressDialogManager.getProgressDialog();
        progress.setIndeterminate(true);
        progress.show(Locale.getString("APPLYING_EQUATIONS"));

        this.equationColumns = equationColumns;

        for(int i = 0; i < this.equationColumns.length; i++) {
            try {
                this.equationColumns[i].calculate(quoteBundle, quotes);
            }
            catch(EvaluationException e) {
                displayErrorMessage(e.getReason());
            }

            if(thread.isInterrupted())
                break;
        }

        ProgressDialogManager.closeProgressDialog(progress);        
    }

    /**
     * Return the number of columns in the table.
     *
     * @return Number of columns in table.
     */
    public int getColumnCount() {
        return super.getColumnCount() + equationColumns.length;
    }

    /**
     * Return a column.
     *
     * @param columnNumber Number of column.
     * @return Column
     */
    public Column getColumn(int columnNumber) {
        if(columnNumber < super.getColumnCount())
            return super.getColumn(columnNumber);
        else {
            columnNumber -= super.getColumnCount();
            assert columnNumber <= equationColumns.length;
            return equationColumns[columnNumber];
        }
    }

    /**
     * Display an error message to the user.
     *
     * @param message The message to display.
     */
    private void displayErrorMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showInternalMessageDialog(DesktopManager.getDesktop(),
                                                          message + ".",
                                                          Locale.getString("ERROR_EVALUATING_EQUATIONS"),
                                                          JOptionPane.ERROR_MESSAGE);
                }
            });
    }

    /**
     * Create the equation columns.
     *
     * @param columnNumber Column number.
     * @return Array of equation columns.
     */
    private EquationColumn[] createEquationColumns(int columnNumber) {
        EquationColumn[] equationColumns = new EquationColumn[EQUATION_COLUMN_COUNT];

        for(int i = 0; i < equationColumns.length; i++)
            equationColumns[i] = new EquationColumn(columnNumber++, 
						    Locale.getString("EQUATION_NUMBER", (i + 1)),
						    Locale.getString("EQUATION_COLUMN_HEADER", 
								     (i + 1)),
                                                    Column.HIDDEN,
                                                    "",
                                                    null);
        return equationColumns;
    }
}
