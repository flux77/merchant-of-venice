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

package org.mov.parser;

/**
 * A representation of a variable in the <i>Gondola</i> language.
 */
public class Variable {

    // Name of variable
    private String name;

    // Value of variable
    private double value;

    // Type of variable
    private int type;

    /**
     * Create a new variable.
     *
     * @param name the name of the variable.
     * @param type the type of the variable, one of {@link Expression#BOOLEAN_TYPE},
     *        {@link Expression#FLOAT_TYPE} or {@link Expression#INTEGER_TYPE}.
     * @param value the initial value of the variable.
     */
    public Variable(String name, int type, double value) {
        assert(type == Expression.BOOLEAN_TYPE || type == Expression.FLOAT_TYPE ||
               type == Expression.INTEGER_TYPE);

        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * Return the name of the variable.
     *
     * @return the name of the variable.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the type of the variable.
     *
     * @return the type of the variable.
     */

    public int getType() {
        return type;
    }

    /**
     * Return the value of the variable.
     *
     * @return the value of the variable.
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the value of the variable.
     *
     * @param value the new value.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Set the value of the variable.
     *
     * @param value the new value.
     */
    public void setValue(int value) {
        this.value = (double)value;
    }
}
