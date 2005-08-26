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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mov.parser.EvaluationException;
import org.mov.parser.Expression;
import org.mov.parser.Variables;
import org.mov.quote.Quote;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.Symbol;
import org.mov.quote.WeekendDateException;
import org.mov.util.TradingDate;

/**
 * Representation of an equation column in a table. An equation column is a column
 * in quote tables that displays the results of a user equation applied to the
 * data in the table. The data type of the data displayed in the column will
 * be {@link EquationResult}.
 *
 * @author Andrew Leppard
 * @see AbstractTable
 * @see AbstractTableModel
 * @see EODQuoteModel
 * @see EquationResult
 */
public class EquationColumn extends Column implements Cloneable {

    // Text of equation
    private String equation;

    // Compiled equation
    private Expression expression;

    // A map which allows you to find the result of an equation for a given symbol
    // on a given trading date. The map is a mapping of the concatenation of
    // the symbol and the trading date string, to an EquationResult.
    private Map results;

    /**
     * Create a new equation column.
     *
     * @param number     The column number
     * @param fullName   The full name of the column which appears in menus etc.
     * @param shortName  The short name of the column which appears in the table header.
     * @param visible    Either {@link Column#HIDDEN}, {@link Column#VISIBLE} or
     *                   {@link Column#ALWAYS_HIDDEN}.
     * @param equation   Text of equation.
     * @param expression Compiled equation.
     */
    public EquationColumn(int number, 
                          String fullName, 
                          String shortName,
                          int visible,
                          String equation, 
                          Expression expression) {
        super(number, fullName, shortName, EquationResult.class, visible);
        this.equation = equation;
        this.expression = expression;
        this.results = new HashMap();
    }

    /**
     * Return the text version of the equation.
     *
     * @return Text version of the equation.
     */
    public String getEquation() {
        return equation;
    }

    /**
     * Set the text version of the equation.
     *
     * @param equation New equation text.
     */
    public void setEquation(String equation) {
        this.equation = equation;
    }

    /**
     * Get the compiled equation.
     *
     * @return Compiled equation.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Set the compiled equation.
     *
     * @param expression Compiled equation.
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Execute the equation and calculate the result for each quote. This function takes a list
     * of quotes, rather than extracting them from the quote bundle, because typically the
     * table (and therefore this column) does not display all the quotes in the quote bundle.
     * The reason is that to display a single day's quotes requires the loading of two day's
     * worth of quotes. Two days are needed to calculate the quote change values.
     *
     * @param quoteBundle Quote Bundle containing quotes
     * @param quotes      A list of {@link Quote}s which contain the symbols and dates to
     *                    evaluate. A result will be calculated for each quote in the list.
     * @throws EvaluationException If there was an error evaluating an expression, such
     *         as divide by zero.
     * @see Quote
     */
    public void calculate(EODQuoteBundle quoteBundle, List quotes) throws EvaluationException {
        results = new HashMap();

        if(expression != null) {
            for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
                Quote quote = (Quote)iterator.next();
                
                try {
                    int dateOffset = quoteBundle.dateToOffset(quote.getDate());
                    double result = expression.evaluate(new Variables(), 
                                                        quoteBundle, quote.getSymbol(), 
                                                        dateOffset);
                    results.put(quote.getSymbol().toString() + quote.getDate().toString(),
                                new EquationResult(expression.getType(), result));
                }
                catch(WeekendDateException e) {
                    // Shouldn't happen
                    assert false;
                }
            }
        }
    }
   
    /**
     * Return the result of the equation for the given symbol on the given date.
     *
     * @param symbol Query the result for this symbol.
     * @param date   Query the result for this date.
     * @return The equation result or {@link EquationResult#EMPTY} if there is
     *         currently no result for the given symbol and date.
     */
    public EquationResult getResult(Symbol symbol, TradingDate date) {
        // If we don't have that many results, just return an empty
        // result. This will show up as an empty cell in the table
        // and be sorted as so the result was 0.0.
        EquationResult equationResult = null;

        if(results != null)
            equationResult = (EquationResult)results.get(symbol.toString() + date.toString());

        if(equationResult == null)
            equationResult = EquationResult.EMPTY;

        return equationResult;
    }

    /**
     * Clone this equation column.
     *
     * @return Cloned equation column.
     */
    public Object clone() {
        return new EquationColumn(getNumber(), getFullName(), getShortName(),
                                  getVisible(), getEquation(), getExpression());
    }
}
