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

package org.mov.prefs;

/**
 * A representation of an equation that can be referenced by name. A stored equation
 * is saved in the Preferences data so that the user does not have to re-type the
 * equation.
 */
public class StoredEquation {
    /** Name of the stored equation. */
    public String name;

    /** The stored equation. */
    public String equation;

    /**
     * Create a new stored equation.
     *
     * @param name the name of the equation.
     * @param equation the equation to store.
     */
    public StoredEquation(String name, String equation) {
	this.name = name;
	this.equation = equation;
    }
}