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

public class Column {
    public int number;
    public String fullName;
    public String shortName;
    public Class type;
    public int visible;

    public final static int HIDDEN = 0;
    public final static int VISIBLE = 1;
    public final static int ALWAYS_HIDDEN = 2;

    public Column(int number, String fullName, String shortName, Class type, 
                  int visible) {
        this.number = number;
        this.fullName = fullName;
        this.shortName = shortName;
        this.type = type;
        this.visible = visible;
    }

    public int getNumber() {
        return number;
    }

    public Class getType() {
        return type;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getVisible() {
        return visible;
    }
}
    
