/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2004 Andrew Leppard (aleppard@picknowl.com.au)

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

package org.mov.chart;

// Module containing constants which determine shape for graphs. 

public class GraphConstants {

    private double smoothingConstant;
    private double priceReversalThreshold;

    public GraphConstants()
    {

	smoothingConstant = 0.1;
	priceReversalThreshold = 0.05;
    }
    

    public double getSmoothingConstant() {
	return smoothingConstant;
    }


    public void setSmoothingConstant(double val) {
	assert val > 0;

	smoothingConstant = val;
    }

    public double getPriceReversalThreshold() {
	return priceReversalThreshold;
    }

    public void setPriceReversalThreshold(double val) {
	assert val > 0;

	priceReversalThreshold = val;
    }

}

