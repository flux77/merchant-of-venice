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
import org.mov.quote.QuoteBundle;
import org.mov.quote.Symbol;
import org.mov.quote.WeekendDateException;
import org.mov.util.TradingDate;

public class EquationColumn extends Column implements Cloneable {
    private String equation;
    private Expression expression;

    // Contains a mapping between Symbol and TradingDate concatenated with
    // result for that date.
    private Map results;

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

    public EquationColumn(int number, 
                          String fullName, 
                          String shortName,
                          int visible,
                          String equation, 
                          Expression expression,
                          Map results) {
        super(number, fullName, shortName, EquationResult.class, visible);
        this.equation = equation;
        this.expression = expression;
        this.results = results;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void recalculate(QuoteBundle quoteBundle, List quotes) throws EvaluationException {
        results = new HashMap();
        calculate(quoteBundle, quotes);
    }

    public void calculate(QuoteBundle quoteBundle, List quotes) throws EvaluationException {
        assert results != null;

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

    public Object clone() {
        return new EquationColumn(getNumber(), getFullName(), getShortName(),
                                  getVisible(), getEquation(), getExpression());
    }
}
