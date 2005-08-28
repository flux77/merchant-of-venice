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

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mov.parser.EvaluationException;
import org.mov.quote.QuoteBundle;
import org.mov.util.Locale;

/**
 * Helper for constructing quote table models. This abstract table model allows
 * you to pass a list of columns for describing a table. The model append
 * a list of equation columns that let the user apply equations to quotes in the
 * table. The model will then care care of returning information to the table
 * about each column and recomputing the equation columns as necessary.
 *
 * @author Andrew Leppard
 * @see Column
 * @see EquationColumn
 */
public abstract class AbstractQuoteModel extends AbstractTableModel {

    /** The number of equation columns to display for tables that support them. */
    public final static int EQUATION_COLUMN_COUNT = 5;

    // Quote bundle
    private QuoteBundle quoteBundle;

    // List of quotes to be displayed in table
    private List quotes;

    // Array of equation columns
    private EquationColumn[] equationColumns;
    
    /**
     * Create a new quote table model with no columns.
     *
     * @param quoteBundle         Quote bundle
     * @param quotes              A list of {@link Quote}s which contain
     *                            the quote symbols and dates to table.
     * @param firstEquationColumn The column number of the first equation
     *                            column.
     */
    public AbstractQuoteModel(QuoteBundle quoteBundle,
                              List quotes,
                              int firstEquationColumn) {
        super();
        this.quoteBundle = quoteBundle;
        this.quotes = quotes;

        equationColumns = createEquationColumns(firstEquationColumn);
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
     * @param equationColumns New equation columns
     */
    public void setEquationColumns(EquationColumn[] equationColumns) {
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
